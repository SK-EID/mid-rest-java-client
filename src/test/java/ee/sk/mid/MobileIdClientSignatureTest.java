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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static ee.sk.mid.AuthenticationRequestBuilderTest.SERVER_SSL_CERTIFICATE;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectSignatureRequestMade;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertSignatureCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidSignature;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.makeValidSignatureRequest;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertSignaturePolled;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubBadRequestResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubInternalServerErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubNotFoundResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubRequestWithResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubServiceUnavailableErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubSessionStatusWithState;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubUnauthorizedResponse;
import static ee.sk.mid.mock.TestData.DATA_TO_SIGN;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNATURE_SESSION_PATH;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidServiceUnavailableException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MobileIdClientSignatureTest {

    public static final String FIRST_REQUEST = "COMPLETE";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private MidClient client;

    @Before
    public void setUp() {
        client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(LOCALHOST_URL)
            .withTrustedCertificates(SERVER_SSL_CERTIFICATE)
            .build();
        stubRequestWithResponse("/signature", "requests/signatureRequest.json",
            "responses/signatureResponse.json");
        stubRequestWithResponse("/signature",
            "requests/signatureRequestWithDisplayText.json", "responses/signatureResponse.json");
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusForSuccessfulSigningRequest.json");
    }

    @Test
    public void sign() {
        MidSignature signature = createValidSignature(client);

        assertSignatureCreated(signature);
    }

    @Test
    public void sign_withHashToSign() {
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

        assertCorrectSignatureRequestMade(request);

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = client.getSessionStatusPoller()
                .fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MidSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test
    public void sign_withHashToSignFromDataToSign() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .build();

        assertCorrectSignatureRequestMade(request);

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = client.getSessionStatusPoller()
                .fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MidSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test
    public void sign_withDisplayText() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .withDisplayText("Authorize transfer of 10 euros")
            .build();

        assertCorrectSignatureRequestMade(request);

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = client.getSessionStatusPoller()
                .fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MidSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void sign_whenTimeout_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenTimeout.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenResponseRetrievingError_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenError.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidNotMidClientException.class)
    public void sign_whenNotMIDClient_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenNotMIDClient.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void sign_whenMSSPTransactionExpired_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenExpiredTransaction.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidUserCancellationException.class)
    public void sign_whenUserCancelled_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenUserCancelled.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenMIDNotReady_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenMIDNotReady.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidPhoneNotAvailableException.class)
    public void sign_whenSimNotAvailable_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusWhenPhoneAbsent.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidDeliveryException.class)
    public void sign_whenDeliveryError_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenDeliveryError.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidDeliveryException.class)
    public void sign_whenInvalidCardResponse_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenSimError.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidInvalidUserConfigurationException.class)
    public void sign_whenSignatureHashMismatch_shouldThrowException() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenSignatureHashMismatch.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenGettingResponseFailed_shouldThrowException() {
        stubInternalServerErrorResponse("/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidServiceUnavailableException.class)
    public void sign_whenHttpStatusCode503_shouldThrowException() {
        stubServiceUnavailableErrorResponse("/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenResponseNotFound_shouldThrowException() {
        stubNotFoundResponse("/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test
    public void sign_withWrongRequestParams_shouldThrowException() {
        expectedEx.expect( MidMissingOrInvalidParameterException.class);
        expectedEx.expectMessage("Invalid phoneNumber or nationalIdentityNumber");

        stubBadRequestResponse("/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void sign_withWrongAuthenticationParams_shouldThrowException() {
        stubUnauthorizedResponse("/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test
    public void setPollingSleepTimeoutForSignatureCreation() {
        stubSessionStatusWithState("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusRunning.json", STARTED, FIRST_REQUEST, 0);
        stubSessionStatusWithState("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusForSuccessfulSigningRequest.json", FIRST_REQUEST, STARTED, 0);

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(LOCALHOST_URL)
            .withTrustedCertificates(SERVER_SSL_CERTIFICATE)
            .withPollingSleepTimeoutSeconds(2)
            .build();

        long duration = measureSigningDuration(client);
        assertThat("Duration is " + duration, duration > 2000L, is(true));
        assertThat("Duration is " + duration, duration < 3000L, is(true));
    }

    @Test
    public void verifySigning_withNetworkConnectionConfigurationHavingCustomHeader() {
        String headerName = "custom-header";
        String headerValue = "Sign";

        Map<String, String> headers = new HashMap<>();
        headers.put(headerName, headerValue);
        ClientConfig clientConfig = getClientConfigWithCustomRequestHeaders(headers);

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(LOCALHOST_URL)
            .withTrustedCertificates(SERVER_SSL_CERTIFICATE)
            .withNetworkConnectionConfig(clientConfig)
            .build();

        makeValidSignatureRequest(client);

        verify(postRequestedFor(urlEqualTo("/signature"))
            .withHeader(headerName, equalTo(headerValue)));
    }

    private long measureSigningDuration(MidClient client) {
        long startTime = System.currentTimeMillis();
        MidSignature signature = createValidSignature(client);
        assertSignatureCreated(signature);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private ClientConfig getClientConfigWithCustomRequestHeaders(Map<String, String> headers) {
        ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        clientConfig.register(new ClientRequestHeaderFilter(headers));
        return clientConfig;
    }
}
