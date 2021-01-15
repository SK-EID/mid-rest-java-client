package ee.sk.mid.integration;

import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidClient;
import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSslException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import org.junit.Test;

public class MobileIdSSL_IT {

    private MidClient client;

    public static final LocalDate LIVE_SERVER_CERT_EXPIRATION_DATE = LocalDate.of(2021, 3, 25);
    public static final String LIVE_SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGezCCBWOgAwIBAgIQBs+E+B8gYnf1I31IIanXXjANBgkqhkiG9w0BAQsFADBN\n" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5E\n" +
            "aWdpQ2VydCBTSEEyIFNlY3VyZSBTZXJ2ZXIgQ0EwHhcNMTkwMzIxMDAwMDAwWhcN\n" +
            "MjEwMzI1MTIwMDAwWjBQMQswCQYDVQQGEwJFRTEQMA4GA1UEBxMHVGFsbGlubjEb\n" +
            "MBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMRIwEAYDVQQDEwltaWQuc2suZWUw\n" +
            "ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDE0RI6DQ7wN5hKhlhCSN7Z\n" +
            "x68hIfGG54XktQLbnvSeJSHZqqSJTCYSkMPQ1cSTMolviHdOWl7qUzX7OCoseV+g\n" +
            "okvgig83amfPR25Qdt3vzvCLT0gj4GojKIYtSSRqU9lsXliib0lNypdBoPvUKicT\n" +
            "1WWHz8pnUv7ZK/iu9190hjGaUxbqmJWyFSjh8Olowr1I2mGCWf7ymAX5Lqnk5Gxi\n" +
            "J9r79e5JTPx0dOaIgC+Fo3ZrH1xSdpXb3ycSMWwMsYoLN1D4J8fIOBk4GDB1UwBJ\n" +
            "QMu3F90sXjbaJrwgHeHP6LNxKY3BYOe3uVy+zXiNcmIirr6x4oS0lL90QFSGq/R1\n" +
            "AgMBAAGjggNSMIIDTjAfBgNVHSMEGDAWgBQPgGEcgjFh1S8o541GOLQs4cbZ4jAd\n" +
            "BgNVHQ4EFgQU2+x3/zzTZeraNrpJb/B6SL1r4d4wFAYDVR0RBA0wC4IJbWlkLnNr\n" +
            "LmVlMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH\n" +
            "AwIwawYDVR0fBGQwYjAvoC2gK4YpaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL3Nz\n" +
            "Y2Etc2hhMi1nNi5jcmwwL6AtoCuGKWh0dHA6Ly9jcmw0LmRpZ2ljZXJ0LmNvbS9z\n" +
            "c2NhLXNoYTItZzYuY3JsMEwGA1UdIARFMEMwNwYJYIZIAYb9bAEBMCowKAYIKwYB\n" +
            "BQUHAgEWHGh0dHBzOi8vd3d3LmRpZ2ljZXJ0LmNvbS9DUFMwCAYGZ4EMAQICMHwG\n" +
            "CCsGAQUFBwEBBHAwbjAkBggrBgEFBQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQu\n" +
            "Y29tMEYGCCsGAQUFBzAChjpodHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20vRGln\n" +
            "aUNlcnRTSEEyU2VjdXJlU2VydmVyQ0EuY3J0MAwGA1UdEwEB/wQCMAAwggF+Bgor\n" +
            "BgEEAdZ5AgQCBIIBbgSCAWoBaAB2AO5Lvbd1zmC64UJpH6vhnmajD35fsHLYgwDE\n" +
            "e4l6qP3LAAABaaDXZ0QAAAQDAEcwRQIgN7q4F8UJyQOT8OsG8h96BZHRdMUk4Aly\n" +
            "G7tztptFBW8CIQDF7tr5je9pxFzlczVwdq6LzlI9cnSnloCdgJ0E2/P5sQB2AId1\n" +
            "v+dZfPiMQ5lfvfNu/1aNR1Y2/0q1YMG06v9eoIMPAAABaaDXaIIAAAQDAEcwRQIg\n" +
            "RSfaNfCLY/0tvCIw+oVusNddo4lSa++xCIqMvjnkZ6YCIQCv+UoMOs9kCd5yZbay\n" +
            "jXCbVuiNrWvDijYGGF2lfPWpDwB2AESUZS6w7s6vxEAH2Kj+KMDa5oK+2MsxtT/T\n" +
            "M5a1toGoAAABaaDXZwEAAAQDAEcwRQIgL3CaRptYqf/5EPebOO/QzWn9xJh2fbeu\n" +
            "BQaYCYNtECwCIQCBnj61xJxy361r1qAI5Y7EZIUWt8Z/9vxztACxf/mPMDANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAPqjpkav+c7bZSMFRwTB3+t68UD0zG7JFRWblxqi4QcG8\n" +
            "bTDoXfrZTp8nC0FQa56SbQVrFlkP6306+O9Itc09049J3qBZ3YDXNy4aetsL8LMa\n" +
            "VqF8mZadv2BQz6mCw56XLgKJVhKRA6QVHRgsocx9Ujp9NZsdP7JxhFIHXUAu6CHk\n" +
            "SYZoUeXL3/mwbr/ul6JvF5cQ8uyxVz7uw5narW9+I8hlzbAXLzL126MyAbQ+v45E\n" +
            "2goHz9848QEGlu6AtlCvcmp8VqO+BH6e4e4a+ihUaXy1ykCgCw4Nq+3VVARdVv6+\n" +
            "s/OHdPfZDLVzkZJA4Vl/GqmJpFAUF+FtG/oFT5gmRw==\n" +
            "-----END CERTIFICATE-----\n";

    public static final LocalDate DEMO_SERVER_CERT_EXPIRATION_DATE = LocalDate.of(2022, 2, 10);
    public static final String DEMO_SERVER_CERT = "-----BEGIN CERTIFICATE-----\n"
         + "MIIGMDCCBRigAwIBAgIQA5xbiUE+ydsAl33/XsbLUDANBgkqhkiG9w0BAQsFADBP\n"
         + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMSkwJwYDVQQDEyBE\n"
         + "aWdpQ2VydCBUTFMgUlNBIFNIQTI1NiAyMDIwIENBMTAeFw0yMTAxMDgwMDAwMDBa\n"
         + "Fw0yMjAyMDgyMzU5NTlaMFUxCzAJBgNVBAYTAkVFMRAwDgYDVQQHEwdUYWxsaW5u\n"
         + "MRswGQYDVQQKExJTSyBJRCBTb2x1dGlvbnMgQVMxFzAVBgNVBAMTDnRzcC5kZW1v\n"
         + "LnNrLmVlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmmcxbeqoO15G\n"
         + "WCeT8gXsvjKTEs17+JRiZTwLmoWtCx1BN+KkozLfibt0BV4qddGg4q4piEIDWnxH\n"
         + "z7Mpcm+/tXoF2RaGcgOzJsruSMKKdklmGJN75QhxYMqm9qNABx7GT42XDb3xB7rD\n"
         + "9u6VMISVHkYZzowtloKJ98V7qyWKWfV1MOk5d/OrcYr02AmpsDGxtAwvnzsIPXwe\n"
         + "WkSgw9RQh6xG7KWbXVfmxwI8he8yIXiaMMQq6mtMwU/2N5vKP8UuHIl9HKbukcI7\n"
         + "qVaFDOppdiSiLrAbE7mYEY0tehrwtUJw3w4xd47oQnFNRVGg3zLYHfLGti5ZJ+lG\n"
         + "MsCcr++pkwIDAQABo4IDADCCAvwwHwYDVR0jBBgwFoAUt2ui6qiqhIx56rTaD5iy\n"
         + "xZV2ufQwHQYDVR0OBBYEFJegSAv63zxIV7bAsOfejPx4gkIvMBkGA1UdEQQSMBCC\n"
         + "DnRzcC5kZW1vLnNrLmVlMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEF\n"
         + "BQcDAQYIKwYBBQUHAwIwgYsGA1UdHwSBgzCBgDA+oDygOoY4aHR0cDovL2NybDMu\n"
         + "ZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VExTUlNBU0hBMjU2MjAyMENBMS5jcmwwPqA8\n"
         + "oDqGOGh0dHA6Ly9jcmw0LmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFRMU1JTQVNIQTI1\n"
         + "NjIwMjBDQTEuY3JsMEsGA1UdIAREMEIwNgYJYIZIAYb9bAEBMCkwJwYIKwYBBQUH\n"
         + "AgEWG2h0dHA6Ly93d3cuZGlnaWNlcnQuY29tL0NQUzAIBgZngQwBAgIwfQYIKwYB\n"
         + "BQUHAQEEcTBvMCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdpY2VydC5jb20w\n"
         + "RwYIKwYBBQUHMAKGO2h0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2Vy\n"
         + "dFRMU1JTQVNIQTI1NjIwMjBDQTEuY3J0MAwGA1UdEwEB/wQCMAAwggEGBgorBgEE\n"
         + "AdZ5AgQCBIH3BIH0APIAdwApeb7wnjk5IfBWc59jpXflvld9nGAK+PlNXSZcJV3H\n"
         + "hAAAAXbinLNjAAAEAwBIMEYCIQCLRptzJcdFwx4Ejy23m9/WUi8KYZpDMLGTJfKW\n"
         + "w+WvawIhAMe/36Lvka4G26F5fgbYhAbhHAc3g9xqZ7x2o7gvV0QYAHcAIkVFB1lV\n"
         + "JFaWP6Ev8fdthuAjJmOtwEt/XcaDXG7iDwIAAAF24pyzswAABAMASDBGAiEAxnq3\n"
         + "JvwCEZ8yDXCPi+H/pcO8nbrYGBnKVVRniDMSICYCIQC707vnF8cGAy+2v/vmA8uP\n"
         + "PpKJRKjuzWg+/TPprKgYdDANBgkqhkiG9w0BAQsFAAOCAQEAmidHLPU5hnQIsmZX\n"
         + "Hh+rOWizoFe1KywyMYwX3vHrsBMk2nKiNje9PIlYjq0frfyhyxGvCGF9AN6l1Hyf\n"
         + "7HnunjUpJ4HGSmNaeABXeYhtzpCzjxXCQ5bo2Agdv2fOm9LVODU+0U2Wq6bFSydF\n"
         + "rbjo9MARb1++Pu6ojwnxV2Fc0/nqwWDXIPt9IKYO1TPikCsqhjb7QEotG+rEHa8a\n"
         + "KjZ5LzPttDy6AuKq/jtHg8z5mxiAMT0cAkT6xQCtXlSNfe/VSYPZl5rrThhmwq4+\n"
         + "jDGx9SiZGDgSEqTKHXDjtnXEzdpZGWVYxcZnq63yiogeCpzwN6x2eIUpUCeNFMIs\n"
         + "L7cAvQ==\n"
         + "-----END CERTIFICATE-----\n";

    @Test(expected = MidSslException.class)
    public void makeRequestToGoogleApi_useDefaultSSLContext_sslHandshakeFailsAndThrowsException() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://www.google.com")
             .withTrustStore(trustStore)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidSslException.class)
    public void makeRequestToWrongApi_shouldThrowException() {

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://sid.demo.sk.ee/smart-id-rp/v1/")
             .withTrustedCertificates(DEMO_SERVER_CERT)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void makeRequestToLiveEnvApi_useDefaultSslContextRPdoesntExist_sslHandshakeSucceedsButThrowsUnauthorizedException() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        assumeTrue("new certificate of mid.sk.ee server needs to imported into file production_server_trusted_ssl_certs.jks", LIVE_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/production_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withTrustStore(trustStore)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void makeRequestToLiveEnvApi_usePKCS12TrustStoreRpDoesNotExist_sslHandshakeSucceedsButThrowsUnauthorizedException() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        assumeTrue("new certificate of mid.sk.ee server needs to imported into file production_server_trusted_ssl_certs.p12", LIVE_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/production_server_trusted_ssl_certs.p12");
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(is, "changeit".toCharArray());
        is.close();

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withTrustStore(trustStore)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test
    public void makeRequestToApi_loadSslContextFromKeyStore_sslHandshakeSucceedsAndAuthenticationInitialized() throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustStore(trustStore)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidInternalErrorException.class)
    public void makeRequestToApi_loadSslContextFromKeyStore_emptyKeystore_sslHandshakeFailsAndThrowsException() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustStore(trustStore)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidSslException.class)
    public void makeRequestToApi_loadSslContextFromKeyStore_wrongSslCert_sslHandshakeFailsAndThrowsException() throws Exception {
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/wrong_ssl_cert.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustStore(trustStore)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidSslException.class)
    public void makeRequestToApi_buildWithSSLContext_wrongSslCert_sslHandshakeFailsAndThrowsException() throws Exception {
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/wrong_ssl_cert.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustSslContext(sslContext)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidInternalErrorException.class)
    public void makeRequestToApi_buildWithSSLContext_emptyKeyStore_sslHandshakeFailsAndThrowsException() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustSslContext(sslContext)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test
    public void makeRequestToApi_buildWithSSLContext_sslHandshakeSucceedsAndAuthenticationInitiated() throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustSslContext(sslContext)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test
    public void makeRequestToApi_withDemoEnvCertificates_sslHandshakeSucceedsAndAuthenticationInitiated() {
        assumeTrue("DEMO_SERVER_CERT needs to be updated with the new certificate of tsp.demo.sk.ee", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        String demoServerCertOld = "-----BEGIN CERTIFICATE-----\nMIIGCTCCBPGgAwIBAgIQBA7WIBNf/nQokV7tEm7VzjANBgkqhkiG9w0BAQsFADBNMQswCQYDVQQG\nEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5EaWdpQ2VydCBTSEEyIFNlY3Vy\nZSBTZXJ2ZXIgQ0EwHhcNMTkwMTAyMDAwMDAwWhcNMjAwMTA3MTIwMDAwWjBVMQswCQYDVQQGEwJF\nRTEQMA4GA1UEBxMHVGFsbGlubjEbMBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQQD\nEw50c3AuZGVtby5zay5lZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMDgbX2OZxh8\nnvJN1GRsBkxr6Xwms6YrDj5uP5uKNyfeZFCoaJMRgNdc/KUWaAVHdXD1xtizm7hINAUAZ/QyZ5vJ\nK7laAgoGTiIHQ/t1t3XlEvwVzZ6sqFOj+CcwGiVr7FORBiebmGzkoJQY8AaKaotZ7pLdMbj7wB7O\nQif9E7WR9N67sC79XMerZMLCQbGvwS59Xl1dHwIio9GiSFHOOLU9LTHSOs4eVty9h1GgvQ2S/nbO\ns/BtBXIcy8Kv+13fX81B27mSLwhevmtWoSiZEnH9Hm9nlB4R/EFyWmClTLc9qhBDQsJurQWwathP\n5mbUFbSEvMVQ3eMgr0ZrJw/pLEMCAwEAAaOCAtswggLXMB8GA1UdIwQYMBaAFA+AYRyCMWHVLyjn\njUY4tCzhxtniMB0GA1UdDgQWBBT3wIWp06oUffH4mObRQMxAWyM1KDAZBgNVHREEEjAQgg50c3Au\nZGVtby5zay5lZTAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC\nMGsGA1UdHwRkMGIwL6AtoCuGKWh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9zc2NhLXNoYTItZzYu\nY3JsMC+gLaArhilodHRwOi8vY3JsNC5kaWdpY2VydC5jb20vc3NjYS1zaGEyLWc2LmNybDBMBgNV\nHSAERTBDMDcGCWCGSAGG/WwBATAqMCgGCCsGAQUFBwIBFhxodHRwczovL3d3dy5kaWdpY2VydC5j\nb20vQ1BTMAgGBmeBDAECAjB8BggrBgEFBQcBAQRwMG4wJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3Nw\nLmRpZ2ljZXJ0LmNvbTBGBggrBgEFBQcwAoY6aHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0Rp\nZ2lDZXJ0U0hBMlNlY3VyZVNlcnZlckNBLmNydDAMBgNVHRMBAf8EAjAAMIIBAgYKKwYBBAHWeQIE\nAgSB8wSB8ADuAHUA7ku9t3XOYLrhQmkfq+GeZqMPfl+wctiDAMR7iXqo/csAAAFoDyD7JQAABAMA\nRjBEAiAjX3nrh7tKevmTNdOu7cEM6mqb6XTp5szZGGv5g0TqiQIgZ1YcBmcZeXFfyq4itn0Tz/q3\nk+3df5vA5ktf3FRJWkcAdQCHdb/nWXz4jEOZX73zbv9WjUdWNv9KtWDBtOr/XqCDDwAAAWgPIPv1\nAAAEAwBGMEQCIFqNMmHmvvIPsX65ivT6wo7pq4r9urrQfpugWuJy8/5UAiB8B75UeAnWM6vGWgpa\nIKMj3VVmGQJwCreQkQzUe0+TKjANBgkqhkiG9w0BAQsFAAOCAQEAnJAV7nga3CMcD3iw1XnvQRWS\n0JLmB1WYgsouInDHSmfuxnIQck/VD4DMybefIc2gnilcLMUgahK+svudJNxFEpxT3MD8o7GgkRKs\nxMHnfB4ptxl5SfxfhxSOfKG8iN8QWU4R01uykn2WVZ8Ixx5KO2wkYiy2JNEH9JfS9VKVaH/J0FFw\nZqvRVWMI9Zd9rd4XLM5mpIn9TWubdCUdDtPTqekXa96Ufs7zjuWuv94opLSVP6O2lyZWYNmqlpu4\nP4oyiaWF3UBeqQGxLGP9pwGG/M2icoSseF59JlZx9OodAvDal1Ax2ySEjNZxzL2VMFxA5suHYY3k\nATRVfMxGqdXMAg==\n-----END CERTIFICATE-----";

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustedCertificates(demoServerCertOld, DEMO_SERVER_CERT)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidSslException.class)
    public void makeRequestToLiveApi_withDemoEnvCertificates_sslHandshakeFails() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withTrustedCertificates(DEMO_SERVER_CERT)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void makeRequestToLiveApi_withLiveEnvCertificates_sslHandshakeSucceedsButMidUnauthorizedExceptionThrown() {
        assumeTrue("LIVE_SERVER_CERT needs to be updated with the new certificate of mid.sk.ee server", LIVE_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withTrustedCertificates(LIVE_SERVER_CERT)
             .build();

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest midAuthRequest = MidAuthenticationRequest.newBuilder()
             .withPhoneNumber(VALID_PHONE)
             .withNationalIdentityNumber(VALID_NAT_IDENTITY)
             .withHashToSign(authenticationHash)
             .withLanguage(MidLanguage.EST)
             .withDisplayText("Log into internet banking system")
             .withDisplayTextFormat(MidDisplayTextFormat.GSM7)
             .build();

        client.getMobileIdConnector().authenticate(midAuthRequest);
    }

}
