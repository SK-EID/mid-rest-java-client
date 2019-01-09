package ee.sk.mid.integration;

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

import ee.sk.mid.*;
import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.*;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.*;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertSignaturePolled;
import static ee.sk.mid.mock.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category({IntegrationTest.class})
public class MobileIdSignatureIT {

    private MobileIdClient client;

    @Before
    public void setUp() {
        client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();
    }

    @Test
    public void sign() {
        MobileIdSignature signature = createValidSignature(client);

        assertSignatureCreated(signature);
    }

    @Test
    public void signHash_withDisplayText() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableHash(hashToSign)
                .withLanguage(Language.EST)
                .withDisplayText("Authorize transfer of 10 euros")
                .build();

        assertCorrectSignatureRequestMade(request);

        SignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MobileIdSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test(expected = NotMIDClientException.class)
    public void sign_whenNotMIDClient_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_NOT_MID_CLIENT, VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test(expected = ExpiredException.class)
    public void sign_whenMSSPTransactionExpired_shouldThrowException() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE_EXPIRED_TRANSACTION)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY_EXPIRED_TRANSACTION)
                .withSignableHash(hashToSign)
                .withLanguage(Language.LIT)
                .build();

        SignatureResponse response = client.getMobileIdConnector().sign(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        client.createMobileIdSignature(sessionStatus);
    }

    @Test(expected = UserCancellationException.class)
    public void sign_whenUserCancelled_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_USER_CANCELLED, VALID_NAT_IDENTITY_USER_CANCELLED);
    }

    @Test(expected = SimNotAvailableException.class)
    public void sign_whenSimNotAvailable_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_ABSENT, VALID_NAT_IDENTITY_ABSENT);
    }

    @Test(expected = DeliveryException.class)
    public void sign_whenDeliveryError_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_DELIVERY_ERROR, VALID_NAT_IDENTITY_DELIVERY_ERROR);
    }

    @Test(expected = InvalidCardResponseException.class)
    public void sign_whenInvalidCardResponse_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_SIM_ERROR, VALID_NAT_IDENTITY_SIM_ERROR);
    }

    @Test(expected = SignatureHashMismatchException.class)
    public void authenticate_whenSignatureHashMismatch_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_SIGNATURE_HASH_MISMATCH, VALID_NAT_IDENTITY_SIGNATURE_HASH_MISMATCH);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withWrongPhoneNumber_shouldThrowException() {
        makeSignatureRequest(client, WRONG_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withWrongNationalIdentityNumber_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE, WRONG_NAT_IDENTITY);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withWrongRelyingPartyUUID_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(WRONG_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withWrongRelyingPartyName_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(WRONG_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = UnauthorizedException.class)
    public void sign_withUnknownRelyingPartyUUID_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = UnauthorizedException.class)
    public void sign_withUnknownRelyingPartyName_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
}
