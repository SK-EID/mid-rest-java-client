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

public class MobileIdSsIT {

    private MidClient client;

    public static final LocalDate LIVE_SERVER_CERT_EXPIRATION_DATE = LocalDate.of(2023, 3, 12);
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

    public static final LocalDate DEMO_SERVER_CERT_EXPIRATION_DATE = LocalDate.of(2027, 2, 6);
    public static final String DEMO_SERVER_CERT = "-----BEGIN CERTIFICATE-----\n"
            + "MIIGujCCBaKgAwIBAgIQAvzLET/HKStz0GM6x/Dx9zANBgkqhkiG9w0BAQsFADBZ\n"
            + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTMwMQYDVQQDEypE\n"
            + "aWdpQ2VydCBHbG9iYWwgRzIgVExTIFJTQSBTSEEyNTYgMjAyMCBDQTEwHhcNMjYw\n"
            + "MTA2MDAwMDAwWhcNMjcwMjA2MjM1OTU5WjBVMQswCQYDVQQGEwJFRTEQMA4GA1UE\n"
            + "BxMHVGFsbGlubjEbMBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQQD\n"
            + "Ew50c3AuZGVtby5zay5lZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n"
            + "AL2uXO+8VCXz7P9c1E6SzbssRqMcTq3CFWgM2jTiJmN0271Y208GiPB2P6A/jOQu\n"
            + "/pbky7Y494OpCbGKgH82Kiox/NILRyKQZoEqWIKSFr9BoCb5i45ZZfBIdC7EtwvV\n"
            + "RtlILDFCetBOztc+XOBh8ZO8GBgrhZ0Osa55HHmdLQAetcfX9HvYe8XoH4doc6za\n"
            + "YZ7ocP4VFvyKoKpj32uVSNborgkOE04HS20/IHjYl4QQ/tbjHymZW1ENA6n0URxw\n"
            + "aHBev4GnF6BgoeNg1xbMf3l+Zan4jUT1xywr8Y3tCJd8TPWVA8s1+gY1PE+Wj3tC\n"
            + "MrhmGoTJBNrtJdLq5MmrPsECAwEAAaOCA4AwggN8MB8GA1UdIwQYMBaAFHSFgMBm\n"
            + "x9833s+9KTeqAx2+7c0XMB0GA1UdDgQWBBTaA9oJontGg5jKsb2uklqZzonBgTAZ\n"
            + "BgNVHREEEjAQgg50c3AuZGVtby5zay5lZTA+BgNVHSAENzA1MDMGBmeBDAECAjAp\n"
            + "MCcGCCsGAQUFBwIBFhtodHRwOi8vd3d3LmRpZ2ljZXJ0LmNvbS9DUFMwDgYDVR0P\n"
            + "AQH/BAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMBMIGfBgNVHR8EgZcwgZQwSKBG\n"
            + "oESGQmh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEdsb2JhbEcyVExT\n"
            + "UlNBU0hBMjU2MjAyMENBMS0xLmNybDBIoEagRIZCaHR0cDovL2NybDQuZGlnaWNl\n"
            + "cnQuY29tL0RpZ2lDZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3Js\n"
            + "MIGHBggrBgEFBQcBAQR7MHkwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2lj\n"
            + "ZXJ0LmNvbTBRBggrBgEFBQcwAoZFaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29t\n"
            + "L0RpZ2lDZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3J0MAwGA1Ud\n"
            + "EwEB/wQCMAAwggF+BgorBgEEAdZ5AgQCBIIBbgSCAWoBaAB2AExj3JjlnB2riPYe\n"
            + "ij3ero+rRKM3e1+blMP7oZz8wb4mAAABm5OS2tQAAAQDAEcwRQIgRX1rOx5VdRnn\n"
            + "xRaRhIAaMH6MT8Oz3a//HQaKFLd29+8CIQC65BZeeJ7ciGmNjuEdvqH6xWM3j0Rx\n"
            + "+UwR32DQkzfY1AB2AByfaCzp+vBFaVD4G5aKh93bMhDYTObIsuOCUkrEz1mfAAAB\n"
            + "m5OS2uwAAAQDAEcwRQIgF8Wy26uMM+m385qW7AL8OmkdjN7h8F1AM+IWaEy+EQEC\n"
            + "IQCOaJ9LwiI6vI+wC0SGm/8OQwQ/ZMgTCXwYpc0QKe17lwB2AGBMmq96f3dfAdQG\n"
            + "/JINyJnrCxx9+MlSG/r6F3c7l4vJAAABm5OS26UAAAQDAEcwRQIhAN1H0YZN65bC\n"
            + "WArSxO3VtDSJ1ZgEr/6BoCbLYeB3DsxFAiAROHFeImcLvLeRNxhP4fXRKlrOLaqg\n"
            + "tHlK9xhlVjX0cjANBgkqhkiG9w0BAQsFAAOCAQEAIWJFR5AmbX48i1AYltc0Misk\n"
            + "g7NPwa7wbjm0yzELKZlHGaEZ+K9EzVQSVsCSIUwizrStD3NBfJ5nVABpErpBervu\n"
            + "w0cfP+xyie/rHUPt/KnCvrUHCj+FJQYLJ0Vx0VPoE279qPpVETy03mtyUExLxABR\n"
            + "ujN6+MHtH5rTIwcaWWaFvcUBBvP27il5dgr0/qBQlZO+JvbkQBUC2uNdCwAwv3As\n"
            + "YjA5paaVAF6xAt5TtGeBR4KC+xfTbtL/FO09jSq7ivG7B8Dcz6ZJa+hmArOiFVp0\n"
            + "MM6cni+f0eGON0L6r+XQF+jyUsghrb1XKb9T6t9TIeY4G6h+ubNU1af8KmZQMA==\n"
            + "-----END CERTIFICATE-----\n";

    @Test(expected = MidSslException.class)
    public void makeRequestToGoogleApi_useDefaultSSLContext_sslHandshakeFailsAndThrowsException() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        InputStream is = MobileIdSsIT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
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

        InputStream is = MobileIdSsIT.class.getResourceAsStream("/production_server_trusted_ssl_certs.jks");
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

        InputStream is = MobileIdSsIT.class.getResourceAsStream("/production_server_trusted_ssl_certs.p12");
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

        InputStream is = MobileIdSsIT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
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
        InputStream is = MobileIdSsIT.class.getResourceAsStream("/wrong_ssl_cert.jks");
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
        InputStream is = MobileIdSsIT.class.getResourceAsStream("/wrong_ssl_cert.jks");
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

        InputStream is = MobileIdSsIT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
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

    @Test(expected = MidSslException.class)
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
