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

import static ee.sk.mid.AuthenticationRequestBuilderTest.SERVER_SSL_CERTIFICATE;
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
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static ee.sk.mid.mock.TestData.SESSION_ID;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNATURE_SESSION_PATH;
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
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.junit.Before;
import org.junit.Test;

public class SignatureRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setSignatureResponseToRespond(new MidSignatureResponse(SESSION_ID));
        connector.setSessionStatusToRespond(createDummySignatureSessionStatus());
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withoutRelyingPartyUUID_shouldThrowException() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withoutRelyingPartyName_shouldThrowException() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .build();
        
        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withoutPhoneNumber_shouldThrowException() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withoutNationalIdentityNumber_shouldThrowException() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withoutHashToSign_shouldThrowException() {
        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withLanguage( MidLanguage.EST)
                .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.sign(request);
    }


    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withoutLanguage_shouldThrowException() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(hashToSign)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        connector.sign(request);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void sign_withTimeout_shouldThrowException() {
        connector.setSessionStatusToRespond(createTimeoutSessionStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_withResponseRetrievingError_shouldThrowException() {
        connector.setSessionStatusToRespond(createResponseRetrievingErrorStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidNotMidClientException.class)
    public void sign_withNotMIDClient_shouldThrowException() {
        connector.setSessionStatusToRespond(createNotMIDClientStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void sign_withMSSPTransactionExpired_shouldThrowException() {
        connector.setSessionStatusToRespond(createMSSPTransactionExpiredStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidUserCancellationException.class)
    public void sign_withUserCancellation_shouldThrowException() {
        connector.setSessionStatusToRespond(createUserCancellationStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_withMIDNotReady_shouldThrowException() {
        connector.setSessionStatusToRespond(createMIDNotReadyStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidPhoneNotAvailableException.class)
    public void sign_withSimNotAvailable_shouldThrowException() {
        connector.setSessionStatusToRespond(createSimNotAvailableStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidDeliveryException.class)
    public void sign_withDeliveryError_shouldThrowException() {
        connector.setSessionStatusToRespond(createDeliveryErrorStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidDeliveryException.class)
    public void sign_withInvalidCardResponse_shouldThrowException() {
        connector.setSessionStatusToRespond(createInvalidCardResponseStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidInvalidUserConfigurationException.class)
    public void sign_withSignatureHashMismatch_shouldThrowException() {
        connector.setSessionStatusToRespond(createSignatureHashMismatchStatus());
        makeSignatureRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_withResultMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult(null);
        makeSignatureRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_withResultBlankInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setResult("");
        makeSignatureRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_withSignatureMissingInResponse_shouldThrowException() {
        connector.getSessionStatusToRespond().setSignature(null);
        makeSignatureRequest(connector);
    }

    private static MidSessionStatus createDummySignatureSessionStatus() {
        MidSessionStatus sessionStatus = new MidSessionStatus();
        sessionStatus.setState("COMPLETE");
        sessionStatus.setResult("OK");
        MidSessionSignature signature = new MidSessionSignature();
        signature.setValue("luvjsi1+1iLN9yfDFEh/BE8h");
        signature.setAlgorithm("sha256WithRSAEncryption");
        sessionStatus.setSignature(signature);
        return sessionStatus;
    }

    private void makeSignatureRequest(MidConnector connector) {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withHashToSign(hashToSign)
                .withLanguage( MidLanguage.EST)
                .build();

        MidSignatureResponse response = connector.sign(request);

        MidSessionStatusPoller poller = MidSessionStatusPoller.newBuilder()
            .withConnector(connector)
            .build();

        MidSessionStatus sessionStatus = poller.fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);

        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withTrustedCertificates(SERVER_SSL_CERTIFICATE)
                .build();

        client.createMobileIdSignature(sessionStatus);
    }
}
