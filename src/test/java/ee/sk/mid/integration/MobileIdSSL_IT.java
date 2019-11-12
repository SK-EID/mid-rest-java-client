package ee.sk.mid.integration;

import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;

import java.io.InputStream;
import java.security.KeyStore;

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
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class MobileIdSSL_IT {

    private MidClient client;

    @Test(expected = ProcessingException.class)
    public void makeRequestToGoogleApi_useDefaultSSLContext_sslHandshakeFailsAndThrowsException() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("www.google.com")
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
    public void makeRequestToApi_validSslCertificate_successfulSslHandshakeAndAuthenticationInitiation() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
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

        MidAuthenticationResponse authenticationResponse = client.getMobileIdConnector().authenticate(midAuthRequest);
        MatcherAssert.assertThat(authenticationResponse, Matchers.notNullValue());
    }

    @Test(expected = MidInternalErrorException.class)
    public void makeRequestToWrongApi_addWrongApiSslCertificate_sslHandshakeShouldSucceedButRequestsReturns404andMIEEisThrown() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://sid.demo.sk.ee/smart-id-rp/v1/")
             .withSslCertificates("-----BEGIN CERTIFICATE-----\nMIIFIjCCBAqgAwIBAgIQBH3ZvDVJl5qtCPwQJSruujANBgkqhkiG9w0BAQsFADBNMQswCQYDVQQG\nEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5EaWdpQ2VydCBTSEEyIFNlY3Vy\nZSBTZXJ2ZXIgQ0EwHhcNMTcwODAxMDAwMDAwWhcNMjAxMDAyMTIwMDAwWjB0MQswCQYDVQQGEwJF\nRTEQMA4GA1UEBxMHVGFsbGlubjEbMBkGA1UEChMSU0sgSUQgU29sdXRpb25zIEFTMR0wGwYDVQQL\nExRWYWx1ZS1hZGRlZCBTZXJ2aWNlczEXMBUGA1UEAxMOc2lkLmRlbW8uc2suZWUwggEiMA0GCSqG\nSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDGFOOk4KzH95NP2dWUuPIvv4RGj3Zvk/Y3ZwavDaPzUkKS\nY9jgiI8EHYIiKde10bqfMeZ1N4No2orwzTtzMP2rqLwGd8ZYSFyF8pymxx0TY0w4yP1MWOq+MQ/6\n5fdBOgOXyhEoIHVWbbAJMmJaH0ozyKwXSKElHNzvKemDTHt7i/NKRG6oBexl3y6KEzKU37mg+scV\nr1i9hPlSO+ymvVUN+VCr1GteNuiFcpRdToVl9rXjvD2mqZfokD5VOuwPwuOecIIqjTpd87kzlgka\nlQfijx1jOBwVx2Hx+wgASiMy2cfHqXlkBvpvi4HTvjK/DMv4C2AqKJHlwjShceuESCH7AgMBAAGj\nggHVMIIB0TAfBgNVHSMEGDAWgBQPgGEcgjFh1S8o541GOLQs4cbZ4jAdBgNVHQ4EFgQUvTSpJnBN\ntfuuL2YY3AKaIPxXljMwGQYDVR0RBBIwEIIOc2lkLmRlbW8uc2suZWUwDgYDVR0PAQH/BAQDAgWg\nMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBrBgNVHR8EZDBiMC+gLaArhilodHRwOi8v\nY3JsMy5kaWdpY2VydC5jb20vc3NjYS1zaGEyLWcxLmNybDAvoC2gK4YpaHR0cDovL2NybDQuZGln\naWNlcnQuY29tL3NzY2Etc2hhMi1nMS5jcmwwTAYDVR0gBEUwQzA3BglghkgBhv1sAQEwKjAoBggr\nBgEFBQcCARYcaHR0cHM6Ly93d3cuZGlnaWNlcnQuY29tL0NQUzAIBgZngQwBAgIwfAYIKwYBBQUH\nAQEEcDBuMCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdpY2VydC5jb20wRgYIKwYBBQUHMAKG\nOmh0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFNIQTJTZWN1cmVTZXJ2ZXJDQS5j\ncnQwDAYDVR0TAQH/BAIwADANBgkqhkiG9w0BAQsFAAOCAQEAtr3K/2vKMH75bbEKrEorjxEsEOQo\npcIhBU5mOVVU5XO+xlrL6NvjyCV47Z9N+uEq4X59YTki23/NGMS85Mm+gl1wq8oPRdNSpNAVhrNY\nNYSFYkvVdFELKmVkep53D2YiB0ygOWghk9JI6UX/kWxBr5N2Qc4+eRKAjlm3vf/HGmOaG2LRbSLL\nPmp6VDQebv2P53rqYdzUpR/qQWHyMtTnku/i0eCY1UCkZHoxLV5vbztAT9kWS0s1d38yDqfljGSW\n/jbdu3P2jkR6PhH5Lupe24SN7jpKDfQJ8oDx6RTM8op7BvL67e6bVW8PzYZCI5BW7ZxEq85+2zIL\nwcEt/pk+DA==\n-----END CERTIFICATE-----")
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
    public void makeRequestToWrongApi_useCustomSSLContext_shouldThrowException() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://sid.demo.sk.ee/smart-id-rp/v1/")
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
    public void makeRequestToLiveEnvApi_useDefaultSslContextRPdoesntExist_sslHandshakeSucceedsButThrowsUnauthorizedException() {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
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
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_ssl_cert.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withSslKeyStore(keyStore)
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
             .withSslKeyStore(keyStore)
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
             .withSslKeyStore(keyStore)
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
             .withSslContext(sslContext)
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
             .withSslContext(sslContext)
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
        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_ssl_cert.jks");
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
             .withSslContext(sslContext)
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
    public void makeRequestToApi_withDemoEnvCertificates_sslHandshakeSucceedsAndAuthenticationInitiated() throws Exception {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl(DEMO_HOST_URL)
             .withDemoEnvCertificates()
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
    public void makeRequestToLiveApi_withDemoEnvCertificates_sslHandshakeFails() throws Exception {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withDemoEnvCertificates()
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
    public void makeRequestToLiveApi_withLiveEnvCertificates_sslHandshakeSucceedsButMidUnauthorizedExceptionThrown() throws Exception {
        client = MidClient.newBuilder()
             .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
             .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
             .withHostUrl("https://mid.sk.ee/mid-api")
             .withLiveEnvCertificates()
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
