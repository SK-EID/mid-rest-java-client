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

import static ee.sk.mid.integration.MobileIdSsIT.DEMO_SERVER_CERT_EXPIRATION_DATE;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectSignatureRequestMade;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertSignatureCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidSignature;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.makeSignatureRequest;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertSignaturePolled;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNATURE_SESSION_PATH;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_ABSENT;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_DELIVERY_ERROR;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_EXPIRED_TRANSACTION;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_NOT_MID_CLIENT;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_SIGNATURE_HASH_MISMATCH;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_SIM_ERROR;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_USER_CANCELLED;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static ee.sk.mid.mock.TestData.VALID_PHONE_ABSENT;
import static ee.sk.mid.mock.TestData.VALID_PHONE_DELIVERY_ERROR;
import static ee.sk.mid.mock.TestData.VALID_PHONE_EXPIRED_TRANSACTION;
import static ee.sk.mid.mock.TestData.VALID_PHONE_NOT_MID_CLIENT;
import static ee.sk.mid.mock.TestData.VALID_PHONE_SIGNATURE_HASH_MISMATCH;
import static ee.sk.mid.mock.TestData.VALID_PHONE_SIM_ERROR;
import static ee.sk.mid.mock.TestData.VALID_PHONE_USER_CANCELLED;
import static ee.sk.mid.mock.TestData.WRONG_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.WRONG_PHONE;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeTrue;

import java.io.InputStream;
import java.security.KeyStore;
import java.time.LocalDate;

import ee.sk.mid.MidClient;
import ee.sk.mid.MidHashToSign;
import ee.sk.mid.MidHashType;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.MidSignature;
import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category({IntegrationTest.class})
public class MobileIdSignatureIT {

    private MidClient client;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception{
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
    }

    @Test
    public void sign() {
        MidSignature signature = createValidSignature(client);

        assertSignatureCreated(signature);
    }

    @Test
    public void signHash_withDisplayText() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withHashToSign(hashToSign)
                .withLanguage( MidLanguage.EST)
                .withDisplayText("Authorize transfer of 10 euros")
                .build();

        assertCorrectSignatureRequestMade(request);

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MidSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test(expected = MidNotMidClientException.class)
    public void sign_whenNotMIDClient_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_NOT_MID_CLIENT, VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test
    public void sign_invalidPhoneNumber_shouldThrowException() {
        expectedEx.expect( MidMissingOrInvalidParameterException.class);
        expectedEx.expectMessage("phoneNumber must contain of + and numbers(8-30)");

        makeSignatureRequest(client, "222", VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void sign_whenMSSPTransactionExpired_shouldThrowException() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE_EXPIRED_TRANSACTION)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY_EXPIRED_TRANSACTION)
                .withHashToSign(hashToSign)
                .withLanguage( MidLanguage.LIT)
                .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        client.createMobileIdSignature(sessionStatus);
    }

    @Test(expected = MidUserCancellationException.class)
    public void sign_whenUserCancelled_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_USER_CANCELLED, VALID_NAT_IDENTITY_USER_CANCELLED);
    }

    @Test(expected = MidPhoneNotAvailableException.class)
    public void sign_whenSimNotAvailable_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_ABSENT, VALID_NAT_IDENTITY_ABSENT);
    }

    @Test(expected = MidDeliveryException.class)
    public void sign_whenDeliveryError_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_DELIVERY_ERROR, VALID_NAT_IDENTITY_DELIVERY_ERROR);
    }

    @Test(expected = MidDeliveryException.class)
    public void sign_whenInvalidCardResponse_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_SIM_ERROR, VALID_NAT_IDENTITY_SIM_ERROR);
    }

    @Test(expected = MidInvalidUserConfigurationException.class)
    public void authenticate_whenSignatureHashMismatch_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE_SIGNATURE_HASH_MISMATCH, VALID_NAT_IDENTITY_SIGNATURE_HASH_MISMATCH);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongPhoneNumber_shouldThrowException() {
        makeSignatureRequest(client, WRONG_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongNationalIdentityNumber_shouldThrowException() {
        makeSignatureRequest(client, VALID_PHONE, WRONG_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongRelyingPartyUUID_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(WRONG_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongRelyingPartyName_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(WRONG_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void sign_withUnknownRelyingPartyUUID_shouldThrowException() throws Exception{
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSsIT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());


        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .withTrustStore(trustStore)
            .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void sign_withUnknownRelyingPartyName_shouldThrowException() throws Exception{
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSsIT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .withTrustStore(trustStore)
            .build();

        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
}
