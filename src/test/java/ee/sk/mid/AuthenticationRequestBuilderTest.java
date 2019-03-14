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

import ee.sk.mid.exception.DeliveryException;
import ee.sk.mid.exception.InvalidUserConfigurationException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.NotMidClientException;
import ee.sk.mid.exception.PhoneNotAvailableException;
import ee.sk.mid.exception.UserCancellationException;
import ee.sk.mid.mock.MobileIdConnectorSpy;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.SessionStatusPoller;
import ee.sk.mid.rest.dao.SessionSignature;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setAuthenticationResponseToRespond(new AuthenticationResponse(SESSION_ID));
        connector.setSessionStatusToRespond(createDummyAuthenticationSessionStatus());
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withoutRelyingPartyUUID_shouldThrowException() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage(Language.EST)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        connector.authenticate(request);

    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withoutRelyingPartyName_shouldThrowException() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage(Language.EST)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withoutPhoneNumber_shouldThrowException() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage(Language.EST)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withoutNationalIdentityNumber_shouldThrowException() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withHashToSign(mobileIdAuthenticationHash)
            .withLanguage(Language.EST)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withoutHashToSign_shouldThrowException() {

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withLanguage(Language.EST)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withoutLanguage_shouldThrowException() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(mobileIdAuthenticationHash)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
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

    @Test(expected = NotMidClientException.class)
    public void authenticate_withNotMIDClient_shouldThrowException() {
        connector.setSessionStatusToRespond(createNotMIDClientStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void authenticate_withMSSPTransactionExpired_shouldThrowException() {
        connector.setSessionStatusToRespond(createMSSPTransactionExpiredStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = UserCancellationException.class)
    public void authenticate_withUserCancellation_shouldThrowException() {
        connector.setSessionStatusToRespond(createUserCancellationStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_withMIDNotReady_shouldThrowException() {
        connector.setSessionStatusToRespond(createMIDNotReadyStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = PhoneNotAvailableException.class)
    public void authenticate_withSimNotAvailable_shouldThrowException() {
        connector.setSessionStatusToRespond(createSimNotAvailableStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = DeliveryException.class)
    public void authenticate_withDeliveryError_shouldThrowException() {
        connector.setSessionStatusToRespond(createDeliveryErrorStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = DeliveryException.class)
    public void authenticate_withInvalidCardResponse_shouldThrowException() {
        connector.setSessionStatusToRespond(createInvalidCardResponseStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = InvalidUserConfigurationException.class)
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

    private void makeAuthenticationRequest(MobileIdConnector connector) {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(authenticationHash)
            .withLanguage(Language.EST)
            .build();

        AuthenticationResponse response = connector.authenticate(request);

        SessionStatusPoller poller = SessionStatusPoller.newBuilder()
            .withConnector(connector)
            .build();

        SessionStatus sessionStatus = poller.fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .build();

        client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    public static SessionStatus createDummyAuthenticationSessionStatus() {
        SessionSignature signature = new SessionSignature();
        signature.setValue("c2FtcGxlIHNpZ25hdHVyZQ0K");
        signature.setAlgorithm("sha512WithRSAEncryption");
        SessionStatus sessionStatus = new SessionStatus();
        sessionStatus.setState("COMPLETE");
        sessionStatus.setResult("OK");
        sessionStatus.setSignature(signature);
        sessionStatus.setCert(AUTH_CERTIFICATE_EE);
        return sessionStatus;
    }
}
