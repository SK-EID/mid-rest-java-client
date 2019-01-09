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
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.junit.Before;
import org.junit.Test;

import static ee.sk.mid.mock.SessionStatusDummy.*;
import static ee.sk.mid.mock.TestData.*;

public class SignatureRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setSignatureResponseToRespond(new SignatureResponse(SESSION_ID));
        connector.setSessionStatusToRespond(createDummySignatureSessionStatus());
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withoutRelyingPartyUUID_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withoutRelyingPartyName_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withoutPhoneNumber_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withoutNationalIdentityNumber_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withoutSignableHash_andWithoutSignableData_shouldThrowException() {
        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withSignableHash_withoutHashType_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withSignableHash_withoutHash_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withoutLanguage_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.sign(request);
    }

    @Test(expected = SessionTimeoutException.class)
    public void sign_withTimeout_shouldThrowException() {
        connector.setSessionStatusToRespond(createTimeoutSessionStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void sign_withResponseRetrievingError_shouldThrowException() {
        connector.setSessionStatusToRespond(createResponseRetrievingErrorStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = NotMIDClientException.class)
    public void sign_withNotMIDClient_shouldThrowException() {
        connector.setSessionStatusToRespond(createNotMIDClientStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = ExpiredException.class)
    public void sign_withMSSPTransactionExpired_shouldThrowException() {
        connector.setSessionStatusToRespond(createMSSPTransactionExpiredStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = UserCancellationException.class)
    public void sign_withUserCancellation_shouldThrowException() {
        connector.setSessionStatusToRespond(createUserCancellationStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MIDNotReadyException.class)
    public void sign_withMIDNotReady_shouldThrowException() {
        connector.setSessionStatusToRespond(createMIDNotReadyStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = SimNotAvailableException.class)
    public void sign_withSimNotAvailable_shouldThrowException() {
        connector.setSessionStatusToRespond(createSimNotAvailableStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = DeliveryException.class)
    public void sign_withDeliveryError_shouldThrowException() {
        connector.setSessionStatusToRespond(createDeliveryErrorStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = InvalidCardResponseException.class)
    public void sign_withInvalidCardResponse_shouldThrowException() {
        connector.setSessionStatusToRespond(createInvalidCardResponseStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = SignatureHashMismatchException.class)
    public void sign_withSignatureHashMismatch_shouldThrowException() {
        connector.setSessionStatusToRespond(createSignatureHashMismatchStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void sign_withResultMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult(null);
        makeSignatureRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void sign_withResultBlankInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult("");
        makeSignatureRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void sign_withSignatureMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setSignature(null);
        makeSignatureRequest(connector);
    }

    private static SessionStatus createDummySignatureSessionStatus() {
        SessionStatus sessionStatus = new SessionStatus();
        sessionStatus.setState("COMPLETE");
        sessionStatus.setResult("OK");
        SessionSignature signature = new SessionSignature();
        signature.setValue("luvjsi1+1iLN9yfDFEh/BE8h");
        signature.setAlgorithm("sha256WithRSAEncryption");
        sessionStatus.setSignature(signature);
        return sessionStatus;
    }

    private void makeSignatureRequest(MobileIdConnector connector) {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .build();

        SignatureResponse response = connector.sign(request);

        SessionStatusPoller poller = new SessionStatusPoller(connector);
        SessionStatus sessionStatus = poller.fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .build();

        client.createMobileIdSignature(sessionStatus);
    }
}
