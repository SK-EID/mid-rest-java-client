package ee.sk.mid.rest;

/*-
 * #%L
 * Mobile ID sample Java client
 * %%
 * Copyright (C) 2018 - 2019 SK ID Solutions AS
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import ee.sk.mid.exception.*;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class MidSessionStatusPoller {

    public static final String SIGNATURE_SESSION_PATH = "/signature/session/{sessionId}";
    public static final String AUTHENTICATION_SESSION_PATH = "/authentication/session/{sessionId}";

    public static final int DEFAULT_POLLING_SLEEP_TIMEOUT_SECONDS = 3;

    private static final Logger logger = LoggerFactory.getLogger( MidSessionStatusPoller.class);

    private MidConnector connector;
    private int pollingSleepTimeoutSeconds;
    private int longPollingTimeoutSeconds;


    public MidSessionStatusPoller(SessionStatusPollerBuilder builder) {
        this.connector = builder.connector;
        this.pollingSleepTimeoutSeconds = builder.pollingSleepTimeoutSeconds;
        this.longPollingTimeoutSeconds = builder.longPollingTimeoutSeconds;

        if (longPollingTimeoutSeconds == 0 && this.pollingSleepTimeoutSeconds == 0) {
            logger.info("Both longPollingTimeoutSeconds and pollingSleepTimeoutSeconds are set to 0. Setting pollingSleepTimeoutSeconds=2.");
            this.pollingSleepTimeoutSeconds = DEFAULT_POLLING_SLEEP_TIMEOUT_SECONDS;
        }

    }

    public MidSessionStatus fetchFinalSignatureSessionStatus(String sessionId) {
        return fetchFinalSessionStatus(sessionId, SIGNATURE_SESSION_PATH);
    }

    public MidSessionStatus fetchFinalAuthenticationSessionStatus(String sessionId) {
        return fetchFinalSessionStatus(sessionId, AUTHENTICATION_SESSION_PATH);
    }

    public MidSessionStatus fetchFinalSessionStatus(String sessionId, String path) {
        logger.debug("Starting to poll session status for session " + sessionId);
        try {
            MidSessionStatus sessionStatus = pollForFinalSessionStatus(sessionId, path);
            validateResult(sessionStatus);
            return sessionStatus;
        } catch (InterruptedException e) {
            logger.error("Failed to poll session status: " + e.getMessage());
            throw new MidInternalErrorException("Failed to poll session status: " + e.getMessage(), e);
        }
    }

    private MidSessionStatus pollForFinalSessionStatus(String sessionId, String path) throws InterruptedException {
        MidSessionStatus sessionStatus = null;
        while (sessionStatus == null || equalsIgnoreCase("RUNNING", sessionStatus.getState())) {

            logger.debug("Polling session status");
            MidSessionStatusRequest request = new MidSessionStatusRequest(sessionId, this.longPollingTimeoutSeconds);
            try {
                sessionStatus = connector.getSessionStatus(request, path);

                if ("COMPLETE".equalsIgnoreCase(sessionStatus.getState())) {
                    break;
                }

                logger.debug("Sleeping for " + pollingSleepTimeoutSeconds + " seconds");
                TimeUnit.SECONDS.sleep(pollingSleepTimeoutSeconds);

            }
            catch (ProcessingException exception) {
                Throwable cause = exception.getCause();
                if (isTimeout(cause)) {
                    logger.warn("Session status request for MID-API timed out. Retrying.", cause);
                }
                else {
                    throw exception;
                }
            }
        }
        logger.debug("Got session final session status response");
        return sessionStatus;
    }

    static boolean isTimeout(Throwable cause) {
        return null != cause && cause.getClass().isAssignableFrom(SocketTimeoutException.class);
    }

    private void validateResult(MidSessionStatus sessionStatus) {
        String result = sessionStatus.getResult();
        if (result == null) {
            logger.error("Result is missing in the session status response");
            throw new MidInternalErrorException("Result is missing in the session status response");
        }
        validateResult(result);
    }

    private void validateResult(String result) throws MidException {
        switch (result) {
            case "OK":
                return;
            case "TIMEOUT":
            case "EXPIRED_TRANSACTION":
                throw new MidSessionTimeoutException();
            case "NOT_MID_CLIENT":
                throw new MidNotMidClientException();
            case "USER_CANCELLED":
                throw new MidUserCancellationException();
            case "SIGNATURE_HASH_MISMATCH":
                throw new MidInvalidUserConfigurationException();
            case "PHONE_ABSENT":
                throw new MidPhoneNotAvailableException();
            case "SIM_ERROR":
            case "DELIVERY_ERROR":
                logger.error("Error with SIM or communicating with it");
                throw new MidDeliveryException();
            default:
                throw new MidInternalErrorException("MID returned error code '" + result + "'");
        }

    }

    public static SessionStatusPollerBuilder newBuilder() {
        return new SessionStatusPollerBuilder();
    }

    public static class SessionStatusPollerBuilder {
        private MidConnector connector;
        private int pollingSleepTimeoutSeconds = 0;
        private int longPollingTimeoutSeconds = 0;

        public SessionStatusPollerBuilder withConnector(MidConnector connector) {
            this.connector = connector;
            return this;
        }
        public SessionStatusPollerBuilder withPollingSleepTimeoutSeconds(int pollingSleepTimeoutSeconds) {
            this.pollingSleepTimeoutSeconds = pollingSleepTimeoutSeconds;
            return this;
        }
        public SessionStatusPollerBuilder withLongPollingTimeoutSeconds(int longPollingTimeoutSeconds) {
            this.longPollingTimeoutSeconds = longPollingTimeoutSeconds;
            return this;
        }

        public MidSessionStatusPoller build() {
            return new MidSessionStatusPoller(this);
        }

    }
}
