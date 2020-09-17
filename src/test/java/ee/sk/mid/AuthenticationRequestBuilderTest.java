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

import static ee.sk.mid.integration.MobileIdSSL_IT.DEMO_SERVER_CERT;
import static ee.sk.mid.mock.SessionStatusDummy.createDeliveryErrorStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createInvalidCardResponseStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createMIDNotReadyStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createMSSPTransactionExpiredStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createNotMIDClientStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createResponseRetrievingErrorStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createSignatureHashMismatchStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createSimNotAvailableStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createTimeoutSessionStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createUserCancellationStatus;
import static ee.sk.mid.mock.TestData.AUTHENTICATION_SESSION_PATH;
import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_EE;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static ee.sk.mid.mock.TestData.SESSION_ID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;

import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.mock.MobileIdConnectorSpy;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.MidSessionStatusPoller;
import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationRequestBuilderTest {

    public static final String SERVER_SSL_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
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
            "-----END CERTIFICATE-----\n";
    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setAuthenticationResponseToRespond(new MidAuthenticationResponse(SESSION_ID));
        connector.setSessionStatusToRespond(createDummyAuthenticationSessionStatus());
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withoutRelyingPartyUUID_shouldThrowException() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        connector.authenticate(request);

    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withoutRelyingPartyName_shouldThrowException() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withoutPhoneNumber_shouldThrowException() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.authenticate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withoutNationalIdentityNumber_shouldThrowException() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withoutHashToSign_shouldThrowException() {

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withoutLanguage_shouldThrowException() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void authenticate_withTimeout_shouldThrowException() {
        connector.setSessionStatusToRespond(createTimeoutSessionStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withResponseRetrievingError_shouldThrowException() {
        connector.setSessionStatusToRespond(createResponseRetrievingErrorStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidNotMidClientException.class)
    public void authenticate_withNotMIDClient_shouldThrowException() {
        connector.setSessionStatusToRespond(createNotMIDClientStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void authenticate_withMSSPTransactionExpired_shouldThrowException() {
        connector.setSessionStatusToRespond(createMSSPTransactionExpiredStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidUserCancellationException.class)
    public void authenticate_withUserCancellation_shouldThrowException() {
        connector.setSessionStatusToRespond(createUserCancellationStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withMIDNotReady_shouldThrowException() {
        connector.setSessionStatusToRespond(createMIDNotReadyStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidPhoneNotAvailableException.class)
    public void authenticate_withSimNotAvailable_shouldThrowException() {
        connector.setSessionStatusToRespond(createSimNotAvailableStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidDeliveryException.class)
    public void authenticate_withDeliveryError_shouldThrowException() {
        connector.setSessionStatusToRespond(createDeliveryErrorStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidDeliveryException.class)
    public void authenticate_withInvalidCardResponse_shouldThrowException() {
        connector.setSessionStatusToRespond(createInvalidCardResponseStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInvalidUserConfigurationException.class)
    public void authenticate_withSignatureHashMismatch_shouldThrowException() {
        connector.setSessionStatusToRespond(createSignatureHashMismatchStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withResultMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult(null);
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withResultBlankInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult("");
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withSignatureMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setSignature(null);
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withCertificateBlankInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setCert("");
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withCertificateMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setCert(null);
        makeAuthenticationRequest(connector);
    }

    private void makeAuthenticationRequest(MidConnector connector) {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(authenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidAuthenticationResponse response = connector.authenticate(request);

        MidSessionStatusPoller poller = MidSessionStatusPoller.newBuilder()
            .withConnector(connector)
            .build();

        MidSessionStatus sessionStatus = poller.fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);

        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withTrustedCertificates(DEMO_SERVER_CERT)
                .build();

        client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    public static MidSessionStatus createDummyAuthenticationSessionStatus() {
        MidSessionSignature signature = new MidSessionSignature();
        signature.setValue("c2FtcGxlIHNpZ25hdHVyZQ0K");
        signature.setAlgorithm("sha512WithRSAEncryption");
        MidSessionStatus sessionStatus = new MidSessionStatus();
        sessionStatus.setState("COMPLETE");
        sessionStatus.setResult("OK");
        sessionStatus.setSignature(signature);
        sessionStatus.setCert(AUTH_CERTIFICATE_EE);
        return sessionStatus;
    }
}
