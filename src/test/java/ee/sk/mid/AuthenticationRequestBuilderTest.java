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
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
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
