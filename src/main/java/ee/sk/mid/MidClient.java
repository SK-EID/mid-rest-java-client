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
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.MidSessionStatusPoller;
import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MidClient {

    private static final Logger logger = LoggerFactory.getLogger( MidClient.class);

    private String relyingPartyUUID;
    private String relyingPartyName;
    private String hostUrl;
    private Configuration networkConnectionConfig;
    private Client configuredClient;
    private MidConnector connector;
    private MidSessionStatusPoller sessionStatusPoller;
    private SSLContext sslContext;
    private static final String SSL_CERT_VALID_FROM_2019_03_21_TO_2021_03_25 = "-----BEGIN CERTIFICATE-----\nMIIGezCCBWOgAwIBAgIQBs+E+B8gYnf1I31IIanXXjANBgkqhkiG9w0BAQsFADBN\nMQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5E\naWdpQ2VydCBTSEEyIFNlY3VyZSBTZXJ2ZXIgQ0EwHhcNMTkwMzIxMDAwMDAwWhcN\nMjEwMzI1MTIwMDAwWjBQMQswCQYDVQQGEwJFRTEQMA4GA1UEBxMHVGFsbGlubjEb\nMBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMRIwEAYDVQQDEwltaWQuc2suZWUw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDE0RI6DQ7wN5hKhlhCSN7Z\nx68hIfGG54XktQLbnvSeJSHZqqSJTCYSkMPQ1cSTMolviHdOWl7qUzX7OCoseV+g\nokvgig83amfPR25Qdt3vzvCLT0gj4GojKIYtSSRqU9lsXliib0lNypdBoPvUKicT\n1WWHz8pnUv7ZK/iu9190hjGaUxbqmJWyFSjh8Olowr1I2mGCWf7ymAX5Lqnk5Gxi\nJ9r79e5JTPx0dOaIgC+Fo3ZrH1xSdpXb3ycSMWwMsYoLN1D4J8fIOBk4GDB1UwBJ\nQMu3F90sXjbaJrwgHeHP6LNxKY3BYOe3uVy+zXiNcmIirr6x4oS0lL90QFSGq/R1\nAgMBAAGjggNSMIIDTjAfBgNVHSMEGDAWgBQPgGEcgjFh1S8o541GOLQs4cbZ4jAd\nBgNVHQ4EFgQU2+x3/zzTZeraNrpJb/B6SL1r4d4wFAYDVR0RBA0wC4IJbWlkLnNr\nLmVlMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH\nAwIwawYDVR0fBGQwYjAvoC2gK4YpaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL3Nz\nY2Etc2hhMi1nNi5jcmwwL6AtoCuGKWh0dHA6Ly9jcmw0LmRpZ2ljZXJ0LmNvbS9z\nc2NhLXNoYTItZzYuY3JsMEwGA1UdIARFMEMwNwYJYIZIAYb9bAEBMCowKAYIKwYB\nBQUHAgEWHGh0dHBzOi8vd3d3LmRpZ2ljZXJ0LmNvbS9DUFMwCAYGZ4EMAQICMHwG\nCCsGAQUFBwEBBHAwbjAkBggrBgEFBQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQu\nY29tMEYGCCsGAQUFBzAChjpodHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20vRGln\naUNlcnRTSEEyU2VjdXJlU2VydmVyQ0EuY3J0MAwGA1UdEwEB/wQCMAAwggF+Bgor\nBgEEAdZ5AgQCBIIBbgSCAWoBaAB2AO5Lvbd1zmC64UJpH6vhnmajD35fsHLYgwDE\ne4l6qP3LAAABaaDXZ0QAAAQDAEcwRQIgN7q4F8UJyQOT8OsG8h96BZHRdMUk4Aly\nG7tztptFBW8CIQDF7tr5je9pxFzlczVwdq6LzlI9cnSnloCdgJ0E2/P5sQB2AId1\nv+dZfPiMQ5lfvfNu/1aNR1Y2/0q1YMG06v9eoIMPAAABaaDXaIIAAAQDAEcwRQIg\nRSfaNfCLY/0tvCIw+oVusNddo4lSa++xCIqMvjnkZ6YCIQCv+UoMOs9kCd5yZbay\njXCbVuiNrWvDijYGGF2lfPWpDwB2AESUZS6w7s6vxEAH2Kj+KMDa5oK+2MsxtT/T\nM5a1toGoAAABaaDXZwEAAAQDAEcwRQIgL3CaRptYqf/5EPebOO/QzWn9xJh2fbeu\nBQaYCYNtECwCIQCBnj61xJxy361r1qAI5Y7EZIUWt8Z/9vxztACxf/mPMDANBgkq\nhkiG9w0BAQsFAAOCAQEAPqjpkav+c7bZSMFRwTB3+t68UD0zG7JFRWblxqi4QcG8\nbTDoXfrZTp8nC0FQa56SbQVrFlkP6306+O9Itc09049J3qBZ3YDXNy4aetsL8LMa\nVqF8mZadv2BQz6mCw56XLgKJVhKRA6QVHRgsocx9Ujp9NZsdP7JxhFIHXUAu6CHk\nSYZoUeXL3/mwbr/ul6JvF5cQ8uyxVz7uw5narW9+I8hlzbAXLzL126MyAbQ+v45E\n2goHz9848QEGlu6AtlCvcmp8VqO+BH6e4e4a+ihUaXy1ykCgCw4Nq+3VVARdVv6+\ns/OHdPfZDLVzkZJA4Vl/GqmJpFAUF+FtG/oFT5gmRw==\n-----END CERTIFICATE-----\n";
    private static final String DEMO_HOST_SSL_CERTIFICATE_VALID_FROM_2019_01_02_TO_2020_01_07 = "-----BEGIN CERTIFICATE-----\nMIIGCTCCBPGgAwIBAgIQBA7WIBNf/nQokV7tEm7VzjANBgkqhkiG9w0BAQsFADBNMQswCQYDVQQG\nEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5EaWdpQ2VydCBTSEEyIFNlY3Vy\nZSBTZXJ2ZXIgQ0EwHhcNMTkwMTAyMDAwMDAwWhcNMjAwMTA3MTIwMDAwWjBVMQswCQYDVQQGEwJF\nRTEQMA4GA1UEBxMHVGFsbGlubjEbMBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQQD\nEw50c3AuZGVtby5zay5lZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMDgbX2OZxh8\nnvJN1GRsBkxr6Xwms6YrDj5uP5uKNyfeZFCoaJMRgNdc/KUWaAVHdXD1xtizm7hINAUAZ/QyZ5vJ\nK7laAgoGTiIHQ/t1t3XlEvwVzZ6sqFOj+CcwGiVr7FORBiebmGzkoJQY8AaKaotZ7pLdMbj7wB7O\nQif9E7WR9N67sC79XMerZMLCQbGvwS59Xl1dHwIio9GiSFHOOLU9LTHSOs4eVty9h1GgvQ2S/nbO\ns/BtBXIcy8Kv+13fX81B27mSLwhevmtWoSiZEnH9Hm9nlB4R/EFyWmClTLc9qhBDQsJurQWwathP\n5mbUFbSEvMVQ3eMgr0ZrJw/pLEMCAwEAAaOCAtswggLXMB8GA1UdIwQYMBaAFA+AYRyCMWHVLyjn\njUY4tCzhxtniMB0GA1UdDgQWBBT3wIWp06oUffH4mObRQMxAWyM1KDAZBgNVHREEEjAQgg50c3Au\nZGVtby5zay5lZTAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC\nMGsGA1UdHwRkMGIwL6AtoCuGKWh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9zc2NhLXNoYTItZzYu\nY3JsMC+gLaArhilodHRwOi8vY3JsNC5kaWdpY2VydC5jb20vc3NjYS1zaGEyLWc2LmNybDBMBgNV\nHSAERTBDMDcGCWCGSAGG/WwBATAqMCgGCCsGAQUFBwIBFhxodHRwczovL3d3dy5kaWdpY2VydC5j\nb20vQ1BTMAgGBmeBDAECAjB8BggrBgEFBQcBAQRwMG4wJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3Nw\nLmRpZ2ljZXJ0LmNvbTBGBggrBgEFBQcwAoY6aHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0Rp\nZ2lDZXJ0U0hBMlNlY3VyZVNlcnZlckNBLmNydDAMBgNVHRMBAf8EAjAAMIIBAgYKKwYBBAHWeQIE\nAgSB8wSB8ADuAHUA7ku9t3XOYLrhQmkfq+GeZqMPfl+wctiDAMR7iXqo/csAAAFoDyD7JQAABAMA\nRjBEAiAjX3nrh7tKevmTNdOu7cEM6mqb6XTp5szZGGv5g0TqiQIgZ1YcBmcZeXFfyq4itn0Tz/q3\nk+3df5vA5ktf3FRJWkcAdQCHdb/nWXz4jEOZX73zbv9WjUdWNv9KtWDBtOr/XqCDDwAAAWgPIPv1\nAAAEAwBGMEQCIFqNMmHmvvIPsX65ivT6wo7pq4r9urrQfpugWuJy8/5UAiB8B75UeAnWM6vGWgpa\nIKMj3VVmGQJwCreQkQzUe0+TKjANBgkqhkiG9w0BAQsFAAOCAQEAnJAV7nga3CMcD3iw1XnvQRWS\n0JLmB1WYgsouInDHSmfuxnIQck/VD4DMybefIc2gnilcLMUgahK+svudJNxFEpxT3MD8o7GgkRKs\nxMHnfB4ptxl5SfxfhxSOfKG8iN8QWU4R01uykn2WVZ8Ixx5KO2wkYiy2JNEH9JfS9VKVaH/J0FFw\nZqvRVWMI9Zd9rd4XLM5mpIn9TWubdCUdDtPTqekXa96Ufs7zjuWuv94opLSVP6O2lyZWYNmqlpu4\nP4oyiaWF3UBeqQGxLGP9pwGG/M2icoSseF59JlZx9OodAvDal1Ax2ySEjNZxzL2VMFxA5suHYY3k\nATRVfMxGqdXMAg==\n-----END CERTIFICATE-----";
    private final List<String> sslCertificates;

    private MidClient(MobileIdClientBuilder builder) {
        this.relyingPartyUUID = builder.relyingPartyUUID;
        this.relyingPartyName = builder.relyingPartyName;
        this.hostUrl = builder.hostUrl;
        this.networkConnectionConfig = builder.networkConnectionConfig;
        this.configuredClient = builder.configuredClient;
        this.connector = builder.connector;
        this.sslCertificates=builder.sslCertificates;
        this.sslContext = builder.sslContext == null ? createSslContext() : builder.sslContext;

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
                .withSslContext(sslContext)
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

    public SSLContext createSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, "".toCharArray());
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            int i = 0;
            for (String sslCertificate : this.sslCertificates) {
                Certificate certificate = factory.generateCertificate(new ByteArrayInputStream(sslCertificate.getBytes(UTF_8)));
                keyStore.setCertificateEntry("mid_api_ssl_cert" + (++i), certificate);
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new MidSslException(e.getMessage());
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
        private SSLContext sslContext;
        private List<String> sslCertificates = new ArrayList<>(Arrays.asList(SSL_CERT_VALID_FROM_2019_03_21_TO_2021_03_25, DEMO_HOST_SSL_CERTIFICATE_VALID_FROM_2019_01_02_TO_2020_01_07));

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

        public MobileIdClientBuilder withSslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public MobileIdClientBuilder withDemoEnvCertificates() {
            this.sslCertificates = new ArrayList<>(Arrays.asList(DEMO_HOST_SSL_CERTIFICATE_VALID_FROM_2019_01_02_TO_2020_01_07));
            return this;
        }

        public MobileIdClientBuilder withLiveEnvCertificates() {
            this.sslCertificates = new ArrayList<>(Arrays.asList(SSL_CERT_VALID_FROM_2019_03_21_TO_2021_03_25));
            return this;
        }

        public MobileIdClientBuilder withSslCertificates(String ...sslCertificate) {
            this.sslCertificates = new ArrayList<>(Arrays.asList(sslCertificate));
            return this;
        }

        public MobileIdClientBuilder withSslKeyStore(KeyStore keyStore) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
                trustManagerFactory.init(keyStore);
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                this.sslContext = sslContext;
            }
            catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                throw new MidSslException(e.getMessage());
            }
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
