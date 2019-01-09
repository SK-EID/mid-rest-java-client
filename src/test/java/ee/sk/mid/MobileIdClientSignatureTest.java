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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.exception.*;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.*;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertSignaturePolled;
import static ee.sk.mid.mock.MobileIdRestServiceStub.*;
import static ee.sk.mid.mock.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MobileIdClientSignatureTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);

    private MobileIdClient client;

    @Before
    public void setUp() throws IOException {
        client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .build();
        stubRequestWithResponse("/mid-api/signature", "requests/signatureRequest.json", "responses/signatureResponse.json");
        stubRequestWithResponse("/mid-api/signature", "requests/signatureRequestWithDisplayText.json", "responses/signatureResponse.json");
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusForSuccessfulSigningRequest.json");
    }

    @Test
    public void sign() {
        MobileIdSignature signature = createValidSignature(client);

        assertSignatureCreated(signature);
    }

    @Test
    public void sign_withSignableHash() {
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
            .build();

        assertCorrectSignatureRequestMade(request);

        SignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MobileIdSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test
    public void sign_withSignableData() {
        SignableData dataToSign = new SignableData(DATA_TO_SIGN);
        dataToSign.setHashType(HashType.SHA256);

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableData(dataToSign)
                .withLanguage(Language.EST)
                .build();

        assertCorrectSignatureRequestMade(request);

        SignatureResponse response = client.getMobileIdConnector().sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));


        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);

        MobileIdSignature signature = client.createMobileIdSignature(sessionStatus);
        assertSignatureCreated(signature);
    }

    @Test
    public void sign_withDisplayText() {
        SignableHash hashToSign = new SignableHash();
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);

        assertThat(hashToSign.calculateVerificationCode(), is("0108"));

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

    @Test(expected = SessionTimeoutException.class)
    public void sign_whenTimeout_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenTimeout.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void sign_whenResponseRetrievingError_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenError.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = NotMIDClientException.class)
    public void sign_whenNotMIDClient_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenNotMIDClient.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = ExpiredException.class)
    public void sign_whenMSSPTransactionExpired_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenExpiredTransaction.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = UserCancellationException.class)
    public void sign_whenUserCancelled_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenUserCancelled.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = MIDNotReadyException.class)
    public void sign_whenMIDNotReady_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenMIDNotReady.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = SimNotAvailableException.class)
    public void sign_whenSimNotAvailable_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenPhoneAbsent.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = DeliveryException.class)
    public void sign_whenDeliveryError_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenDeliveryError.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = InvalidCardResponseException.class)
    public void sign_whenInvalidCardResponse_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenSimError.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = SignatureHashMismatchException.class)
    public void sign_whenSignatureHashMismatch_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusWhenSignatureHashMismatch.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void sign_whenGettingResponseFailed_shouldThrowException() throws IOException {
        stubInternalServerErrorResponse("/mid-api/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = ResponseNotFoundException.class)
    public void sign_whenResponseNotFound_shouldThrowException() throws IOException {
        stubNotFoundResponse("/mid-api/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = ParameterMissingException.class)
    public void sign_withWrongRequestParams_shouldThrowException() throws IOException {
        stubBadRequestResponse("/mid-api/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test(expected = UnauthorizedException.class)
    public void sign_withWrongAuthenticationParams_shouldThrowException() throws IOException {
        stubUnauthorizedResponse("/mid-api/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test
    public void setPollingSleepTimeoutForSignatureCreation() throws Exception {
        stubSessionStatusWithState("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusRunning.json", STARTED, "COMPLETE");
        stubSessionStatusWithState("/mid-api/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00", "responses/sessionStatusForSuccessfulSigningRequest.json", "COMPLETE", STARTED);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
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

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withNetworkConnectionConfig(clientConfig)
                .build();

        makeValidSignatureRequest(client);

        verify(postRequestedFor(urlEqualTo("/mid-api/signature"))
                .withHeader(headerName, equalTo(headerValue)));
    }

    private long measureSigningDuration(MobileIdClient client) {
        long startTime = System.currentTimeMillis();
        MobileIdSignature signature = createValidSignature(client);
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
