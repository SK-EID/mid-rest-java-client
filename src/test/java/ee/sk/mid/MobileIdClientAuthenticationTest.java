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
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.hamcrest.Matchers;
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
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertAuthenticationPolled;
import static ee.sk.mid.mock.MobileIdRestServiceStub.*;
import static ee.sk.mid.mock.TestData.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MobileIdClientAuthenticationTest {

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
        stubRequestWithResponse("/mid-api/authentication", "requests/authenticationRequest.json", "responses/authenticationResponse.json");
        stubRequestWithResponse("/mid-api/authentication", "requests/authenticationRequestSHA256.json", "responses/authenticationResponse.json");
        stubRequestWithResponse("/mid-api/authentication", "requests/authenticationRequestWithDisplayText.json", "responses/authenticationResponse.json");
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusForSuccessfulAuthenticationRequest.json");
    }

    @Test
    public void authenticate_withHash() {
        MobileIdAuthenticationHash authenticationHash = createAuthenticationSHA256Hash();
        assertThat(authenticationHash.calculateVerificationCode(), is("0108"));

        MobileIdAuthentication authentication = createAndSendAuthentication(client, VALID_PHONE, VALID_NAT_IDENTITY, authenticationHash);

        assertThat(authentication, is(notNullValue()));
        assertThat(authentication.getResult(), not(isEmptyOrNullString()));
        assertThat(authentication.getSignatureValueInBase64(), not(isEmptyOrNullString()));
        assertThat(authentication.getCertificate(), is(notNullValue()));
        assertThat(authentication.getHashType(), Matchers.is(HashType.SHA256));

        AuthenticationResponseValidator validator = new AuthenticationResponseValidator();
        MobileIdAuthenticationResult mobileIdAuthenticationResult = validator.validate(authentication);

        assertThat(mobileIdAuthenticationResult.isValid(), is(false));
        assertThat(mobileIdAuthenticationResult.getErrors(), contains("Signature verification failed"));
    }

    @Test
    public void authenticate_withSignableData() {
        SignableData dataToSign = new SignableData(DATA_TO_SIGN);
        dataToSign.setHashType(HashType.SHA512);

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withSignableData(dataToSign)
                .withLanguage(Language.EST)
                .build();

        assertCorrectAuthenticationRequestMade(request);

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);

        MobileIdAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, dataToSign.calculateHashInBase64(), dataToSign.getHashType());

        assertThat(authentication, is(notNullValue()));
        assertThat(authentication.getResult(), not(isEmptyOrNullString()));
        assertThat(authentication.getSignatureValueInBase64(), not(isEmptyOrNullString()));
        assertThat(authentication.getCertificate(), is(notNullValue()));
        assertThat(authentication.getSignedHashInBase64(), is(dataToSign.calculateHashInBase64()));
        assertThat(authentication.getHashType(), Matchers.is(HashType.SHA512));

        AuthenticationResponseValidator validator = new AuthenticationResponseValidator();
        MobileIdAuthenticationResult mobileIdAuthenticationResult = validator.validate(authentication);

        assertThat(mobileIdAuthenticationResult.isValid(), is(false));
        assertThat(mobileIdAuthenticationResult.getErrors(), contains("Signature verification failed"));
    }

    @Test
    public void authenticate_withDisplayText() {
        MobileIdAuthenticationHash authenticationHash = createAuthenticationSHA256Hash();
        assertThat(authenticationHash.calculateVerificationCode(), is("0108"));

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .withAuthenticationHash(authenticationHash)
                .withLanguage(Language.EST)
                .withDisplayText("Log into internet banking system")
                .build();

        assertMadeCorrectAuthenticationRequesWithSHA256(request);

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);

        MobileIdAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash.getHashInBase64(), authenticationHash.getHashType());
        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
    }

    @Test(expected = SessionTimeoutException.class)
    public void authenticate_whenTimeout_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenTimeout.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void authenticate_whenResponseRetrievingError_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenError.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = NotMIDClientException.class)
    public void authenticate_whenNotMIDClient_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenNotMIDClient.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = ExpiredException.class)
    public void authenticate_whenMSSPTransactionExpired_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenExpiredTransaction.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = UserCancellationException.class)
    public void authenticate_whenUserCancelled_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenUserCancelled.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = MIDNotReadyException.class)
    public void authenticate_whenMIDNotReady_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenMIDNotReady.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = SimNotAvailableException.class)
    public void authenticate_whenSimNotAvailable_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenPhoneAbsent.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = DeliveryException.class)
    public void authenticate_whenDeliveryError_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenDeliveryError.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = InvalidCardResponseException.class)
    public void authenticate_whenInvalidCardResponse_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenSimError.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = SignatureHashMismatchException.class)
    public void authenticate_whenSignatureHashMismatch_shouldThrowException() throws IOException {
        stubRequestWithResponse("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusWhenSignatureHashMismatch.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void authenticate_whenGettingResponseFailed_shouldThrowException() throws IOException {
        stubInternalServerErrorResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = ResponseNotFoundException.class)
    public void authenticate_whenResponseNotFound_shouldThrowException() throws IOException {
        stubNotFoundResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withWrongRequestParams_shouldThrowException() throws IOException {
        stubBadRequestResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        makeValidAuthenticationRequest(client);
    }

    @Test(expected = UnauthorizedException.class)
    public void authenticate_withWrongAuthenticationParams_shouldThrowException() throws IOException {
        stubUnauthorizedResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        makeValidAuthenticationRequest(client);
    }

    @Test
    public void setPollingSleepTimeoutForAuthentication() throws IOException {
        stubSessionStatusWithState("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusRunning.json", STARTED, "COMPLETE");
        stubSessionStatusWithState("/mid-api/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb", "responses/sessionStatusForSuccessfulAuthenticationRequest.json", "COMPLETE", STARTED);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withPollingSleepTimeoutSeconds(2)
                .build();

        long duration = measureAuthenticationDuration(client);
        assertThat("Duration is " + duration, duration > 2000L, is(true));
        assertThat("Duration is " + duration, duration < 3000L, is(true));
    }

    @Test
    public void verifyAuthentication_withNetworkConnectionConfigurationHavingCustomHeader() {
        String headerName = "custom-header";
        String headerValue = "Auth";

        Map<String, String> headersToAdd = new HashMap<>();
        headersToAdd.put(headerName, headerValue);
        ClientConfig clientConfig = getClientConfigWithCustomRequestHeaders(headersToAdd);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withNetworkConnectionConfig(clientConfig)
                .build();

        makeValidAuthenticationRequest(client);

        verify(postRequestedFor(urlEqualTo("/mid-api/authentication"))
                .withHeader(headerName, equalTo(headerValue)));
    }

    @Test
    public void verifyMobileIdConnector_whenConnectorIsNotProvided() {
        MobileIdConnector connector = client.getMobileIdConnector();
        assertThat(connector instanceof MobileIdRestConnector, is(true));
    }

    @Test
    public void verifyMobileIdConnector_whenConnectorIsProvided() {
        final String mock = "Mock";
        SessionStatus status = mock(SessionStatus.class);
        when(status.getState()).thenReturn(mock);
        MobileIdConnector connector = mock(MobileIdConnector.class);
        when(connector.getSessionStatus(null, null)).thenReturn(status);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withHostUrl(LOCALHOST_URL)
                .withMobileIdConnector(connector)
                .build();

        assertThat(client.getMobileIdConnector().getSessionStatus(null, null).getState(), is(mock));
    }

    private long measureAuthenticationDuration(MobileIdClient client) {
        long startTime = System.currentTimeMillis();
        MobileIdAuthenticationHash authenticationHash = createAuthenticationSHA512Hash();
        MobileIdAuthentication authentication = createAndSendAuthentication(client, VALID_PHONE, VALID_NAT_IDENTITY, authenticationHash);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private ClientConfig getClientConfigWithCustomRequestHeaders(Map<String, String> headers) {
        ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        clientConfig.register(new ClientRequestHeaderFilter(headers));
        return clientConfig;
    }
}
