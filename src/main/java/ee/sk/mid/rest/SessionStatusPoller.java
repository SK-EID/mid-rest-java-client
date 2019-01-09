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
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.SessionStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class SessionStatusPoller {

    public static final String SIGNATURE_SESSION_PATH = "/mid-api/signature/session/{sessionId}";
    public static final String AUTHENTICATION_SESSION_PATH = "/mid-api/authentication/session/{sessionId}";

    private static final Logger logger = LoggerFactory.getLogger(SessionStatusPoller.class);

    private MobileIdConnector connector;
    private int pollingSleepTimeoutSeconds = 1;


    public SessionStatusPoller(MobileIdConnector connector) {
        this.connector = connector;
    }

    public SessionStatus fetchFinalSignatureSessionStatus(String sessionId) throws TechnicalErrorException {
        return fetchFinalSessionStatus(sessionId, SIGNATURE_SESSION_PATH);
    }

    public SessionStatus fetchFinalAuthenticationSessionStatus(String sessionId) throws TechnicalErrorException {
        return fetchFinalSessionStatus(sessionId, AUTHENTICATION_SESSION_PATH);
    }

    public SessionStatus fetchFinalSessionStatus(String sessionId, String path) throws TechnicalErrorException {
        logger.debug("Starting to poll session status for session " + sessionId);
        try {
            SessionStatus sessionStatus = pollForFinalSessionStatus(sessionId, path);
            validateResult(sessionStatus);
            return sessionStatus;
        } catch (InterruptedException e) {
            logger.error("Failed to poll session status: " + e.getMessage());
            throw new TechnicalErrorException("Failed to poll session status: " + e.getMessage(), e);
        }
    }

    private SessionStatus pollForFinalSessionStatus(String sessionId, String path) throws InterruptedException {
        SessionStatus sessionStatus = null;
        while (sessionStatus == null || equalsIgnoreCase("RUNNING", sessionStatus.getState())) {
            sessionStatus = pollSessionStatus(sessionId, path);
            if (equalsIgnoreCase("COMPLETE", sessionStatus.getState())) {
                break;
            }
            logger.debug("Sleeping for " + pollingSleepTimeoutSeconds + " seconds");
            TimeUnit.SECONDS.sleep(pollingSleepTimeoutSeconds);
        }
        logger.debug("Got session final session status response");
        return sessionStatus;
    }

    private SessionStatus pollSessionStatus(String sessionId, String path) {
        logger.debug("Polling session status");
        SessionStatusRequest request = createSessionStatusRequest(sessionId);
        return connector.getSessionStatus(request, path);
    }

    private SessionStatusRequest createSessionStatusRequest(String sessionId) {
        return new SessionStatusRequest(sessionId);
    }

    private void validateResult(SessionStatus sessionStatus) throws TechnicalErrorException {
        String result = sessionStatus.getResult();
        if (result == null) {
            logger.error("Result is missing in the session status response");
            throw new TechnicalErrorException("Result is missing in the session status response");
        }
        validateResult(result);
    }

    private void validateResult(String result) throws MobileIdException {
        if (equalsIgnoreCase(result, "TIMEOUT")) {
            logger.error("Session timeout");
            throw new SessionTimeoutException();
        } else if (equalsIgnoreCase(result, "ERROR")) {
            logger.error("Error getting response from cert-store/MSSP");
            throw new ResponseRetrievingException();
        } else if (equalsIgnoreCase(result, "NOT_MID_CLIENT")) {
            logger.error("Given user has no active certificates and is not M-ID client");
            throw new NotMIDClientException();
        } else if (equalsIgnoreCase(result, "EXPIRED_TRANSACTION")) {
            logger.error("MSSP transaction timed out");
            throw new ExpiredException();
        } else if (equalsIgnoreCase(result, "USER_CANCELLED")) {
            logger.error("User cancelled the operation");
            throw new UserCancellationException();
        } else if (equalsIgnoreCase(result, "MID_NOT_READY")) {
            logger.error("Mobile-ID not ready");
            throw new MIDNotReadyException();
        } else if (equalsIgnoreCase(result, "PHONE_ABSENT")) {
            logger.error("Sim not available");
            throw new SimNotAvailableException();
        } else if (equalsIgnoreCase(result, "DELIVERY_ERROR")) {
            logger.error("SMS sending error");
            throw new DeliveryException();
        } else if (equalsIgnoreCase(result, "SIM_ERROR")) {
            logger.error("Invalid response from card");
            throw new InvalidCardResponseException();
        } else if (equalsIgnoreCase(result, "SIGNATURE_HASH_MISMATCH")) {
            logger.error("Hash does not match with certificate type");
            throw new SignatureHashMismatchException();
        } else if (!equalsIgnoreCase(result, "OK")) {
            logger.error("Session status end result is '" + result + "'");
            throw new TechnicalErrorException("Session status end result is '" + result + "'");
        }
    }

    public void setPollingSleepTimeSeconds(int pollingSleepTimeSeconds) {
        logger.debug("Polling sleep time is " + pollingSleepTimeSeconds + " second(s)");
        pollingSleepTimeoutSeconds = pollingSleepTimeSeconds;
    }
}
