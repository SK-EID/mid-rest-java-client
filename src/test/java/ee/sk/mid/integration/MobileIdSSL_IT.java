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
import javax.ws.rs.ProcessingException;

import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidClient;
import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.exception.MidInternalErrorException;
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

    public static final LocalDate DEMO_SERVER_CERT_EXPIRATION_DATE = LocalDate.of(2021, 1, 15);
    public static final String DEMO_SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGCTCCBPGgAwIBAgIQB5CCfJUfCEruWfwaDQQ8ojANBgkqhkiG9w0BAQsFADBN\n" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5E\n" +
            "aWdpQ2VydCBTSEEyIFNlY3VyZSBTZXJ2ZXIgQ0EwHhcNMjAwMTA0MDAwMDAwWhcN\n" +
            "MjEwMTE1MTIwMDAwWjBVMQswCQYDVQQGEwJFRTEQMA4GA1UEBxMHVGFsbGlubjEb\n" +
            "MBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQQDEw50c3AuZGVtby5z\n" +
            "ay5lZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMEXbF6n3XKvkLKy\n" +
            "EcnYoPBTvHaqjWIXnguu/aLiEC17ZuELrf4YkwXcQ6mTK6t1H21p7bluWDuhGzy1\n" +
            "pcf4zSPD7SYBYFDQJZUHEC54TPDRkZkm8vVYrtQ3s/I7VcDF54Gp2jy5QrZ/KKtx\n" +
            "qT1L3J7VNjNcjHp1qg5nGoNMfMHajaZITVmXUV7MdcVwgXunjK3I4R48TxkfevEO\n" +
            "QkeJMW4Nj+tuqd/aj3iPxBRC5N9QnwsUFh+GlTvWO7JdN4RgUvrpzYCXITdcR9fb\n" +
            "+GN62LwUioNar+ixzbx5x4+aKeiZch57mQnnccuAlaJZ50/XB38Pil5aJvSb1cqN\n" +
            "zhESRGsCAwEAAaOCAtswggLXMB8GA1UdIwQYMBaAFA+AYRyCMWHVLyjnjUY4tCzh\n" +
            "xtniMB0GA1UdDgQWBBT6vCJtkTvBJNfLz37w57NiN/s+ATAZBgNVHREEEjAQgg50\n" +
            "c3AuZGVtby5zay5lZTAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUH\n" +
            "AwEGCCsGAQUFBwMCMGsGA1UdHwRkMGIwL6AtoCuGKWh0dHA6Ly9jcmwzLmRpZ2lj\n" +
            "ZXJ0LmNvbS9zc2NhLXNoYTItZzYuY3JsMC+gLaArhilodHRwOi8vY3JsNC5kaWdp\n" +
            "Y2VydC5jb20vc3NjYS1zaGEyLWc2LmNybDBMBgNVHSAERTBDMDcGCWCGSAGG/WwB\n" +
            "ATAqMCgGCCsGAQUFBwIBFhxodHRwczovL3d3dy5kaWdpY2VydC5jb20vQ1BTMAgG\n" +
            "BmeBDAECAjB8BggrBgEFBQcBAQRwMG4wJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3Nw\n" +
            "LmRpZ2ljZXJ0LmNvbTBGBggrBgEFBQcwAoY6aHR0cDovL2NhY2VydHMuZGlnaWNl\n" +
            "cnQuY29tL0RpZ2lDZXJ0U0hBMlNlY3VyZVNlcnZlckNBLmNydDAMBgNVHRMBAf8E\n" +
            "AjAAMIIBAgYKKwYBBAHWeQIEAgSB8wSB8ADuAHUAu9nfvB+KcbWTlCOXqpJ7RzhX\n" +
            "lQqrUugakJZkNo4e0YUAAAFvcP7EQwAABAMARjBEAiA2fFBYg7BrD8fvSMUPdSIk\n" +
            "CcASBgvqn6ySm//nyYT7jgIgDl3+FTpQJyLXTqzurjna9AnbNZkGiaoxEdCL6iBW\n" +
            "s2YAdQBElGUusO7Or8RAB9io/ijA2uaCvtjLMbU/0zOWtbaBqAAAAW9w/sPdAAAE\n" +
            "AwBGMEQCIBXEX2dbwSUQJfo6pP0Uf/YLVC200QfIdO+1oESfxLIMAiAWGLVR4MTe\n" +
            "H2+iOK+Hndbo9LkDdMibWTyIIByLCyHKhzANBgkqhkiG9w0BAQsFAAOCAQEAWOWm\n" +
            "sTfs3TSDd04c3GvB0b+x5xu34SYG8OCYfpTbdU5X8+7mk3+XR9yqBZpN/WSBBk0f\n" +
            "Vx+ukpON3z1v/TOMMHSOykxxw3yQsNB+NZ/a6d7ns4OBsRY4/TLu1DI1Ey7jkE0m\n" +
            "erqbzCAgx3nrHwo49bUNLtkgnUHoKNoLYreLQvAjW7PeiPmT/xkvz7MC3jE5P/hA\n" +
            "rZ5xvV/ZxpiRVDuDT0G+uCoIuBjY4HpvMgOJdsqxKtK1NI1dodPyjxVmMdjG6+1X\n" +
            "Kd5GtbPeaLRx1Kpe/NkfGAruW4TCvuUm2G1zHs71ePmYSPJjE6FDOnWWqjtQIgXg\n" +
            "OauLK5GGqW/2PvCWXA==\n" +
            "-----END CERTIFICATE-----";

    @Test(expected = ProcessingException.class)
    public void makeRequestToGoogleApi_useDefaultSSLContext_sslHandshakeFailsAndThrowsException() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("www.google.com")
             .withTrustStore(keyStore)
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
    public void makeRequestToWrongApi_addWrongApiSslCertificate_sslHandshakeShouldSucceedButRequestsReturns404andMIEEisThrown() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://sid.demo.sk.ee/smart-id-rp/v1/")
             .withTrustedCertificates("-----BEGIN CERTIFICATE-----\nMIIFIjCCBAqgAwIBAgIQBH3ZvDVJl5qtCPwQJSruujANBgkqhkiG9w0BAQsFADBNMQswCQYDVQQG\nEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5EaWdpQ2VydCBTSEEyIFNlY3Vy\nZSBTZXJ2ZXIgQ0EwHhcNMTcwODAxMDAwMDAwWhcNMjAxMDAyMTIwMDAwWjB0MQswCQYDVQQGEwJF\nRTEQMA4GA1UEBxMHVGFsbGlubjEbMBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMR0wGwYDVQQL\nExRWYWx1ZS1hZGRlZCBTZXJ2aWNlczEXMBUGA1UEAxMOc2lkLmRlbW8uc2suZWUwggEiMA0GCSqG\nSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDGFOOk4KzH95NP2dWUuPIvv4RGj3Zvk/Y3ZwavDaPzUkKS\nY9jgiI8EHYIiKde10bqfMeZ1N4No2orwzTtzMP2rqLwGd8ZYSFyF8pymxx0TY0w4yP1MWOq+MQ/6\n5fdBOgOXyhEoIHVWbbAJMmJaH0ozyKwXSKElHNzvKemDTHt7i/NKRG6oBexl3y6KEzKU37mg+scV\nr1i9hPlSO+ymvVUN+VCr1GteNuiFcpRdToVl9rXjvD2mqZfokD5VOuwPwuOecIIqjTpd87kzlgka\nlQfijx1jOBwVx2Hx+wgASiMy2cfHqXlkBvpvi4HTvjK/DMv4C2AqKJHlwjShceuESCH7AgMBAAGj\nggHVMIIB0TAfBgNVHSMEGDAWgBQPgGEcgjFh1S8o541GOLQs4cbZ4jAdBgNVHQ4EFgQUvTSpJnBN\ntfuuL2YY3AKaIPxXljMwGQYDVR0RBBIwEIIOc2lkLmRlbW8uc2suZWUwDgYDVR0PAQH/BAQDAgWg\nMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBrBgNVHR8EZDBiMC+gLaArhilodHRwOi8v\nY3JsMy5kaWdpY2VydC5jb20vc3NjYS1zaGEyLWcxLmNybDAvoC2gK4YpaHR0cDovL2NybDQuZGln\naWNlcnQuY29tL3NzY2Etc2hhMi1nMS5jcmwwTAYDVR0gBEUwQzA3BglghkgBhv1sAQEwKjAoBggr\nBgEFBQcCARYcaHR0cHM6Ly93d3cuZGlnaWNlcnQuY29tL0NQUzAIBgZngQwBAgIwfAYIKwYBBQUH\nAQEEcDBuMCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdpY2VydC5jb20wRgYIKwYBBQUHMAKG\nOmh0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFNIQTJTZWN1cmVTZXJ2ZXJDQS5j\ncnQwDAYDVR0TAQH/BAIwADANBgkqhkiG9w0BAQsFAAOCAQEAtr3K/2vKMH75bbEKrEorjxEsEOQo\npcIhBU5mOVVU5XO+xlrL6NvjyCV47Z9N+uEq4X59YTki23/NGMS85Mm+gl1wq8oPRdNSpNAVhrNY\nNYSFYkvVdFELKmVkep53D2YiB0ygOWghk9JI6UX/kWxBr5N2Qc4+eRKAjlm3vf/HGmOaG2LRbSLL\nPmp6VDQebv2P53rqYdzUpR/qQWHyMtTnku/i0eCY1UCkZHoxLV5vbztAT9kWS0s1d38yDqfljGSW\n/jbdu3P2jkR6PhH5Lupe24SN7jpKDfQJ8oDx6RTM8op7BvL67e6bVW8PzYZCI5BW7ZxEq85+2zIL\nwcEt/pk+DA==\n-----END CERTIFICATE-----")
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

    @Test(expected = ProcessingException.class)
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
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withTrustStore(keyStore)
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
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(is, "changeit".toCharArray());
        is.close();

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withTrustStore(keyStore)
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
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustStore(keyStore)
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

    @Test(expected = ProcessingException.class)
    public void makeRequestToApi_loadSslContextFromKeyStore_emptyKeystore_sslHandshakeFailsAndThrowsException() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustStore(keyStore)
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

    @Test(expected = ProcessingException.class)
    public void makeRequestToApi_loadSslContextFromKeyStore_wrongSslCert_sslHandshakeFailsAndThrowsException() throws Exception {
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/wrong_ssl_cert.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withTrustStore(keyStore)
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

    @Test(expected = ProcessingException.class)
    public void makeRequestToApi_buildWithSSLContext_wrongSslCert_sslHandshakeFailsAndThrowsException() throws Exception {
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/wrong_ssl_cert.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(keyStore);
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

    @Test(expected = ProcessingException.class)
    public void makeRequestToApi_buildWithSSLContext_emptyKeyStore_sslHandshakeFailsAndThrowsException() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(keyStore);
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
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(keyStore);
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

    @Test(expected = ProcessingException.class)
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
