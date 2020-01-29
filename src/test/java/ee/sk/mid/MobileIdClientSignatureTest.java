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
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.ws.rs.ProcessingException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

public class MobileIdClientSignatureTest {

    public static final String FIRST_REQUEST = "COMPLETE";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MidClient client;

    @Before
    public void setUp() {
        client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(LOCALHOST_URL)
            .build();
        stubRequestWithResponse("/signature", "requests/signatureRequest.json",
            "responses/signatureResponse.json");
        stubRequestWithResponse("/signature",
            "requests/signatureRequestWithDisplayText.json", "responses/signatureResponse.json");
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
            "responses/sessionStatusForSuccessfulSigningRequest.json");
    }

     @After
     public void reset() {
        wireMockRule.resetAll();
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

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenResponseNotFound_shouldThrowException() {
        stubNotFoundResponse("/signature", "requests/signatureRequest.json");
        makeValidSignatureRequest(client);
    }

    @Test
    public void sign_withWrongRequestParams_shouldThrowException() {
        expectedException.expect( MidMissingOrInvalidParameterException.class);
        expectedException.expectMessage("Invalid phoneNumber or nationalIdentityNumber");

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
            .withNetworkConnectionConfig(clientConfig)
            .build();

        makeValidSignatureRequest(client);

        verify(postRequestedFor(urlEqualTo("/signature"))
            .withHeader(headerName, equalTo(headerValue)));
    }

    @Test
    public void sign_responseFromServerComesSlightlyBeforeTimeout_theRequestIsSuccessful() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
                "responses/sessionStatusForSuccessfulSigningRequest.json", 4950);

        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .build();

        client.getMobileIdConnector()
                .getSessionStatus(new MidSessionStatusRequest("2c52caf4-13b0-41c4-bdc6-aa268403cc00", 3), SIGNATURE_SESSION_PATH);

    }

    @Test(expected = ProcessingException.class)
    public void sign_responseFromServerComesSlightlyAfterTimeout_timeoutExceptionIsThrown() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
                "responses/sessionStatusForSuccessfulSigningRequest.json", 5050);

        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withMaximumResponseWaitingTimeInMilliseconds(5000)
                .build();

        client.getMobileIdConnector()
                .getSessionStatus(new MidSessionStatusRequest("2c52caf4-13b0-41c4-bdc6-aa268403cc00", 3), SIGNATURE_SESSION_PATH);

    }

    @Test
    public void sign_responseIsTimedoutOnThreeRequestsAndFourthArrivesInTime_eachTimeoutShouldResultInNewRequest_totalFourRequestsMade() {
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
                "responses/sessionStatusForSuccessfulSigningRequest.json", 5050, STARTED, "TIMEOUT1");
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
                "responses/sessionStatusForSuccessfulSigningRequest.json", 5050, "TIMEOUT1", "TIMEOUT2");
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
                "responses/sessionStatusForSuccessfulSigningRequest.json", 5050, "TIMEOUT2", "TIMEOUT3");
        stubRequestWithResponse("/signature/session/2c52caf4-13b0-41c4-bdc6-aa268403cc00",
                "responses/sessionStatusForSuccessfulSigningRequest.json", 4950, "TIMEOUT3", "RECEIVE");

        MidConnector connector = spy(MidRestConnector.newBuilder()
                .withEndpointUrl(LOCALHOST_URL)
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withMaximumResponseWaitingTimeInMilliseconds(5000)
                .build());

        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withLongPollingTimeoutSeconds(3)
                .withMobileIdConnector(connector)
                .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withHashToSign(MidHashToSign.newBuilder()
                        .withHashType(MidHashType.SHA256)
                        .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
                        .build())
                .withLanguage( MidLanguage.EST)
                .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);

        Mockito.verify(connector, times(4)).getSessionStatus(any(), any());
    }

    @Test(expected = MidResponseTimeoutException.class)
    public void sign_initationRequestTimesOut_SocketTimeoutExceptionIsthrownInternally() {
        stubRequestWithResponse("/signature", "requests/signatureRequest.json",
                "responses/signatureResponse.json", 5050);

        MidConnector connector = spy(MidRestConnector.newBuilder()
                .withEndpointUrl(LOCALHOST_URL)
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withMaximumResponseWaitingTimeInMilliseconds(5000)
                .build());

        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withLongPollingTimeoutSeconds(3)
                .withMobileIdConnector(connector)
                .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withHashToSign(MidHashToSign.newBuilder()
                        .withHashType(MidHashType.SHA256)
                        .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
                        .build())
                .withLanguage( MidLanguage.EST)
                .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
    }

    @Test
    public void sign_initationRequestResponseComesSlightlyBeforeTimeout_shouldPassWithoutExceptions() {
        stubRequestWithResponse("/signature", "requests/signatureRequest.json",
                "responses/signatureResponse.json", 5050);

        MidConnector connector = spy(MidRestConnector.newBuilder()
                .withEndpointUrl(LOCALHOST_URL)
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withMaximumResponseWaitingTimeInMilliseconds(6000)
                .build());

        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withLongPollingTimeoutSeconds(3)
                .withMobileIdConnector(connector)
                .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withHashToSign(MidHashToSign.newBuilder()
                        .withHashType(MidHashType.SHA256)
                        .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
                        .build())
                .withLanguage( MidLanguage.EST)
                .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
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
