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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.security.cert.X509Certificate;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.MidSessionStatusPoller;
import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidClient {

    private static final Logger logger = LoggerFactory.getLogger( MidClient.class);

    private String relyingPartyUUID;
    private String relyingPartyName;
    private String hostUrl;
    private Configuration networkConnectionConfig;
    private Client configuredClient;
    private MidConnector connector;
    private MidSessionStatusPoller sessionStatusPoller;

    private MidClient(MobileIdClientBuilder builder) {
        this.relyingPartyUUID = builder.relyingPartyUUID;
        this.relyingPartyName = builder.relyingPartyName;
        this.hostUrl = builder.hostUrl;
        this.networkConnectionConfig = builder.networkConnectionConfig;
        this.configuredClient = builder.configuredClient;
        this.connector = builder.connector;

        this.sessionStatusPoller = MidSessionStatusPoller.newBuilder()
            .withConnector(this.getMobileIdConnector())
            .withPollingSleepTimeoutSeconds(builder.pollingSleepTimeoutSeconds)
            .withLongPollingTimeoutSeconds(builder.longPollingTimeoutSeconds)
            .build();
    }

    public MidConnector getMobileIdConnector() {
        if (null == connector) {
            this.connector = MidRestConnector.newBuilder()
                .withEndpointUrl(hostUrl)
                .withConfiguredClient(configuredClient)
                .withClientConfig(networkConnectionConfig)
                .withRelyingPartyUUID(relyingPartyUUID)
                .withRelyingPartyName(relyingPartyName)
                .build();
        }
        return connector;
    }

    public MidSessionStatusPoller getSessionStatusPoller() {
        return sessionStatusPoller;
    }

    public String getRelyingPartyUUID() {
        return relyingPartyUUID;
    }

    public String getRelyingPartyName() {
        return relyingPartyName;
    }


    public X509Certificate createMobileIdCertificate(MidCertificateChoiceResponse certificateChoiceResponse) {
        validateCertificateResult(certificateChoiceResponse.getResult());
        validateCertificateResponse(certificateChoiceResponse);
        return MidCertificateParser.parseX509Certificate(certificateChoiceResponse.getCert());
    }

    public MidSignature createMobileIdSignature(MidSessionStatus sessionStatus) {
        validateResponse(sessionStatus);
        MidSessionSignature sessionSignature = sessionStatus.getSignature();

        return MidSignature.newBuilder()
                .withValueInBase64(sessionSignature.getValue())
                .withAlgorithmName(sessionSignature.getAlgorithm())
                .build();
    }

    public MidAuthentication createMobileIdAuthentication(MidSessionStatus sessionStatus, MidHashToSign hashSigned) {
        validateResponse(sessionStatus);
        MidSessionSignature sessionSignature = sessionStatus.getSignature();
        X509Certificate certificate = MidCertificateParser.parseX509Certificate(sessionStatus.getCert());

        return MidAuthentication.newBuilder()
            .withResult(sessionStatus.getResult())
            .withSignatureValueInBase64(sessionSignature.getValue())
            .withAlgorithmName(sessionSignature.getAlgorithm())
            .withCertificate(certificate)
            .withSignedHashInBase64(hashSigned.getHashInBase64())
            .withHashType(hashSigned.getHashType())
            .build();
    }

    private void validateCertificateResult(String result) throws MidException {
        if ( "NOT_FOUND".equalsIgnoreCase(result) || "NOT_ACTIVE".equalsIgnoreCase(result)) {
            throw new MidNotMidClientException();
        }
        else if (!"OK".equalsIgnoreCase(result)) {
            logger.error("Session status end result is '" + result + "'");
            throw new MidInternalErrorException("Session status end result is '" + result + "'");
        }
    }

    private void validateCertificateResponse(MidCertificateChoiceResponse certificateChoiceResponse) {
        if (certificateChoiceResponse.getCert() == null || isBlank(certificateChoiceResponse.getCert())) {
            logger.error("Certificate was not present in the session status response");
            throw new MidInternalErrorException("Certificate was not present in the session status response");
        }
    }

    private void validateResponse(MidSessionStatus sessionStatus) {
        if (sessionStatus.getSignature() == null || isBlank(sessionStatus.getSignature().getValue())) {
            logger.error("Signature was not present in the response");
            throw new MidInternalErrorException("Signature was not present in the response");
        }
    }

    public static MobileIdClientBuilder newBuilder() {
        return new MobileIdClientBuilder();
    }

    public static class MobileIdClientBuilder {
        private String relyingPartyUUID;
        private String relyingPartyName;
        private String hostUrl;
        private Configuration networkConnectionConfig;
        private Client configuredClient;
        private int pollingSleepTimeoutSeconds;
        private int longPollingTimeoutSeconds;
        private MidConnector connector;

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

        public MobileIdClientBuilder withNetworkConnectionConfig(Configuration networkConnectionConfig) {
            this.networkConnectionConfig = networkConnectionConfig;
            return this;
        }

        public MobileIdClientBuilder withConfiguredClient(Client configuredClient) {
            this.configuredClient = configuredClient;
            return this;
        }

        public MobileIdClientBuilder withPollingSleepTimeoutSeconds(int pollingSleepTimeoutSeconds) {
            this.pollingSleepTimeoutSeconds = pollingSleepTimeoutSeconds;
            return this;
        }

        public MobileIdClientBuilder withLongPollingTimeoutSeconds(int longPollingTimeoutSeconds) {
            this.longPollingTimeoutSeconds = longPollingTimeoutSeconds;
            return this;
        }

        public MobileIdClientBuilder withMobileIdConnector(MidConnector mobileIdConnector) {
            this.connector = mobileIdConnector;
            return this;
        }

        public MidClient build() {
            validateFileds();
            return new MidClient(this);
        }

        private void validateFileds() {
            if (this.pollingSleepTimeoutSeconds < 0) {
                throw new MidMissingOrInvalidParameterException("pollingSleepTimeoutSeconds must be non-negative number");
            }
            if (this.longPollingTimeoutSeconds < 0) {
                throw new MidMissingOrInvalidParameterException("longPollingTimeoutSeconds must be non-negative number");
            }
        }
    }
}
