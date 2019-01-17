package ee.sk.mid;

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
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.SessionStatusPoller;
import ee.sk.mid.rest.dao.SessionSignature;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MobileIdClient {

    private static final Logger logger = LoggerFactory.getLogger(MobileIdClient.class);

    private String relyingPartyUUID;
    private String relyingPartyName;
    private String hostUrl;
    private ClientConfig networkConnectionConfig;
    private int pollingSleepTimeoutSeconds;
    private MobileIdConnector connector;
    private SessionStatusPoller sessionStatusPoller;

    private MobileIdClient(MobileIdClientBuilder builder) {
        this.relyingPartyUUID = builder.relyingPartyUUID;
        this.relyingPartyName = builder.relyingPartyName;
        this.hostUrl = builder.hostUrl;
        this.networkConnectionConfig = builder.networkConnectionConfig;
        this.pollingSleepTimeoutSeconds = builder.pollingSleepTimeoutSeconds;
        this.connector = builder.connector;

        this.createSessionStatusPoller();
    }

    public MobileIdConnector getMobileIdConnector() {
        if (null == connector) {
            this.connector = new MobileIdRestConnector(hostUrl, networkConnectionConfig);
        }
        return connector;
    }

    public SessionStatusPoller getSessionStatusPoller() {
        return sessionStatusPoller;
    }


    public String getRelyingPartyUUID() {
        return relyingPartyUUID;
    }

    public String getRelyingPartyName() {
        return relyingPartyName;
    }


    private SessionStatusPoller createSessionStatusPoller() {
        SessionStatusPoller sessionStatusPoller = new SessionStatusPoller(this.getMobileIdConnector());
        sessionStatusPoller.setPollingSleepTimeSeconds(pollingSleepTimeoutSeconds);
        this.sessionStatusPoller = sessionStatusPoller;
        return sessionStatusPoller;
    }

    public X509Certificate createMobileIdCertificate(CertificateChoiceResponse certificateChoiceResponse) {
        validateCertificateResult(certificateChoiceResponse.getResult());
        validateCertificateResponse(certificateChoiceResponse);
        return CertificateParser.parseX509Certificate(certificateChoiceResponse.getCert());
    }

    public MobileIdSignature createMobileIdSignature(SessionStatus sessionStatus) {
        validateResponse(sessionStatus);
        SessionSignature sessionSignature = sessionStatus.getSignature();

        return MobileIdSignature.newBuilder()
                .withValueInBase64(sessionSignature.getValue())
                .withAlgorithmName(sessionSignature.getAlgorithm())
                .build();
    }

    public MobileIdAuthentication createMobileIdAuthentication(SessionStatus sessionStatus, String hashInBase64, HashType hashType) {
        validateResponse(sessionStatus);
        SessionSignature sessionSignature = sessionStatus.getSignature();
        X509Certificate certificate = CertificateParser.parseX509Certificate(sessionStatus.getCert());

        return MobileIdAuthentication.newBuilder()
            .withResult(sessionStatus.getResult())
            .withSignatureValueInBase64(sessionSignature.getValue())
            .withAlgorithmName(sessionSignature.getAlgorithm())
            .withCertificate(certificate)
            .withSignedHashInBase64(hashInBase64)
            .withHashType(hashType)
            .build();
    }

    private void validateCertificateResult(String result) throws MobileIdException {
        if (equalsIgnoreCase(result, "NOT_FOUND")) {
            logger.error("No certificate for the user was found");
            throw new CertificateNotPresentException("No certificate for the user was found");
        } else if (equalsIgnoreCase(result, "NOT_ACTIVE")) {
            logger.error("Inactive certificate found");
            throw new ExpiredException("Inactive certificate found");
        } else if (!equalsIgnoreCase(result, "OK")) {
            logger.error("Session status end result is '" + result + "'");
            throw new TechnicalErrorException("Session status end result is '" + result + "'");
        }
    }

    private void validateCertificateResponse(CertificateChoiceResponse certificateChoiceResponse) throws TechnicalErrorException {
        if (certificateChoiceResponse.getCert() == null || isBlank(certificateChoiceResponse.getCert())) {
            logger.error("Certificate was not present in the session status response");
            throw new TechnicalErrorException("Certificate was not present in the session status response");
        }
    }

    private void validateResponse(SessionStatus sessionStatus) throws TechnicalErrorException {
        if (sessionStatus.getSignature() == null || isBlank(sessionStatus.getSignature().getValue())) {
            logger.error("Signature was not present in the response");
            throw new TechnicalErrorException("Signature was not present in the response");
        }
    }

    public static MobileIdClientBuilder newBuilder() {
        return new MobileIdClientBuilder();
    }

    public static class MobileIdClientBuilder {
        private String relyingPartyUUID;
        private String relyingPartyName;
        private String hostUrl;
        private ClientConfig networkConnectionConfig;
        private int pollingSleepTimeoutSeconds = 1;
        private MobileIdConnector connector;

        private MobileIdClientBuilder() {}

        public MobileIdClientBuilder withRelyingPartyUUID(String relyingPartyUUID) {
            this.relyingPartyUUID = relyingPartyUUID;
            return this;
        }

        public MobileIdClientBuilder withRelyingPartyName(String relyingPartyName) {
            this.relyingPartyName = relyingPartyName;
            return this;
        }

        public MobileIdClientBuilder withHostUrl(String hostUrl) {
            this.hostUrl = hostUrl;
            return this;
        }

        public MobileIdClientBuilder withNetworkConnectionConfig(ClientConfig networkConnectionConfig) {
            this.networkConnectionConfig = networkConnectionConfig;
            return this;
        }

        public MobileIdClientBuilder withPollingSleepTimeoutSeconds(int pollingSleepTimeoutSeconds) {
            this.pollingSleepTimeoutSeconds = pollingSleepTimeoutSeconds;
            return this;
        }

        public MobileIdClientBuilder withMobileIdConnector(MobileIdConnector mobileIdConnector) {
            this.connector = mobileIdConnector;
            return this;
        }

        public MobileIdClient build() {
            return new MobileIdClient(this);
        }
    }
}
