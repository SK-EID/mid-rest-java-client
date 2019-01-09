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

import static ee.sk.mid.mock.SessionStatusDummy.*;
import static ee.sk.mid.mock.TestData.*;

public class AuthenticationRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setAuthenticationResponseToRespond(new AuthenticationResponse(SESSION_ID));
        connector.setSessionStatusToRespond(createDummyAuthenticationSessionStatus());
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withoutRelyingPartyUUID_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withAuthenticationHash(mobileIdAuthenticationHash)
            .withLanguage(Language.EST)
            .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);

    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withoutRelyingPartyName_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(mobileIdAuthenticationHash)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withoutPhoneNumber_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(mobileIdAuthenticationHash)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withoutNationalIdentityNumber_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withAuthenticationHash(mobileIdAuthenticationHash)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withoutHash_andWithoutSignableData_shouldThrowException() {

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withHash_withoutHashType_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = new MobileIdAuthenticationHash();
        mobileIdAuthenticationHash.setHashInBase64(SHA512_HASH_IN_BASE64);

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(mobileIdAuthenticationHash)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withHashType_withoutHash_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = new MobileIdAuthenticationHash();
        mobileIdAuthenticationHash.setHashType(HashType.SHA512);

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(mobileIdAuthenticationHash)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withoutLanguage_shouldThrowException() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(mobileIdAuthenticationHash)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.authenticate(request);
    }

    @Test(expected = SessionTimeoutException.class)
    public void authenticate_withTimeout_shouldThrowException() {
        connector.setSessionStatusToRespond(createTimeoutSessionStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void authenticate_withResponseRetrievingError_shouldThrowException() {
        connector.setSessionStatusToRespond(createResponseRetrievingErrorStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = NotMIDClientException.class)
    public void authenticate_withNotMIDClient_shouldThrowException() {
        connector.setSessionStatusToRespond(createNotMIDClientStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = ExpiredException.class)
    public void authenticate_withMSSPTransactionExpired_shouldThrowException() {
        connector.setSessionStatusToRespond(createMSSPTransactionExpiredStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = UserCancellationException.class)
    public void authenticate_withUserCancellation_shouldThrowException() {
        connector.setSessionStatusToRespond(createUserCancellationStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = MIDNotReadyException.class)
    public void authenticate_withMIDNotReady_shouldThrowException() {
        connector.setSessionStatusToRespond(createMIDNotReadyStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = SimNotAvailableException.class)
    public void authenticate_withSimNotAvailable_shouldThrowException() {
        connector.setSessionStatusToRespond(createSimNotAvailableStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = DeliveryException.class)
    public void authenticate_withDeliveryError_shouldThrowException() {
        connector.setSessionStatusToRespond(createDeliveryErrorStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = InvalidCardResponseException.class)
    public void authenticate_withInvalidCardResponse_shouldThrowException() {
        connector.setSessionStatusToRespond(createInvalidCardResponseStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = SignatureHashMismatchException.class)
    public void authenticate_withSignatureHashMismatch_shouldThrowException() {
        connector.setSessionStatusToRespond(createSignatureHashMismatchStatus());
        makeAuthenticationRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void authenticate_withResultMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult(null);
        makeAuthenticationRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void authenticate_withResultBlankInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult("");
        makeAuthenticationRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void authenticate_withSignatureMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setSignature(null);
        makeAuthenticationRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void authenticate_withCertificateBlankInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setCert("");
        makeAuthenticationRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void authenticate_withCertificateMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setCert(null);
        makeAuthenticationRequest(connector);
    }

    private void makeAuthenticationRequest(MobileIdConnector connector) {
        MobileIdAuthenticationHash authenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(authenticationHash)
                .withLanguage(Language.EST)
                .build();

        AuthenticationResponse response = connector.authenticate(request);

        SessionStatusPoller poller = new SessionStatusPoller(connector);
        SessionStatus sessionStatus = poller.fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .build();

        client.createMobileIdAuthentication(sessionStatus, authenticationHash.getHashInBase64(), authenticationHash.getHashType());
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
