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
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertAuthenticationCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectAuthenticationRequestMade;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertMadeCorrectAuthenticationRequesWithSHA256;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createAndSendAuthentication;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.makeValidAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.sendAuthentication;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertAuthenticationPolled;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubBadRequestResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubInternalServerErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubNotFoundResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubRequestWithResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubSessionStatusWithState;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubUnauthorizedResponse;
import static ee.sk.mid.mock.TestData.AUTHENTICATION_SESSION_PATH;
import static ee.sk.mid.mock.TestData.DATA_TO_SIGN;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MobileIdClientAuthenticationTest {

  public static final String FIRST_REQUEST_DONE = "FIRST_REQUEST_DONE";
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(18089);

  private MidClient client;

  @Before
  public void setUp() {
    client = MidClient.newBuilder()
        .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
        .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
        .withHostUrl(LOCALHOST_URL)
        .build();
    stubRequestWithResponse("/authentication", "requests/authenticationRequest.json",
        "responses/authenticationResponse.json");
    stubRequestWithResponse("/authentication", "requests/authenticationRequestSHA256.json",
        "responses/authenticationResponse.json");
    stubRequestWithResponse("/authentication",
        "requests/authenticationRequestWithDisplayText.json",
        "responses/authenticationResponse.json");

    stubRequestWithResponse("/authentication",
        "requests/authenticationRequestWithDisplayTextAndFormat.json",
        "responses/authenticationResponse.json");
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusForSuccessfulAuthenticationRequest.json");
  }

  @Test
  public void authenticate_withHashAndWithoutDisplayText() {
      MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
          .withHashInBase64(SHA256_HASH_IN_BASE64)
          .withHashType( MidHashType.SHA256)
          .build();

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber(VALID_PHONE)
        .withNationalIdentityNumber(VALID_NAT_IDENTITY)
        .withHashToSign(authenticationHash)
        .withLanguage( MidLanguage.EST)
        .build();

    MidAuthentication authentication = sendAuthentication(client, request, authenticationHash);

    assertThat(authentication, is(notNullValue()));
    assertThat(authentication.getResult(), not(isEmptyOrNullString()));
    assertThat(authentication.getSignatureValueInBase64(), not(isEmptyOrNullString()));
    assertThat(authentication.getCertificate(), is(notNullValue()));
    assertThat(authentication.getHashType(), Matchers.is( MidHashType.SHA256));

    MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator();
    MidAuthenticationResult mobileIdAuthenticationResult = validator.validate(authentication);

    assertThat(mobileIdAuthenticationResult.isValid(), is(false));
    assertThat(mobileIdAuthenticationResult.getErrors(), contains("Signature verification failed"));
  }

  @Test
  public void authenticate_withWithDisplayTextFormatUcs2_correctRequestCreated() {

    stubRequestWithResponse("/authentication", "requests/authenticationRequestUcs2.json",
        "responses/authenticationResponse.json");

      MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
          .withHashInBase64(SHA256_HASH_IN_BASE64)
          .withHashType( MidHashType.SHA256)
          .build();

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber(VALID_PHONE)
        .withNationalIdentityNumber(VALID_NAT_IDENTITY)
        .withHashToSign(authenticationHash)
        .withLanguage( MidLanguage.RUS)
        .withDisplayText("Войти")
        .withDisplayTextFormat( MidDisplayTextFormat.UCS2)
        .build();

    sendAuthentication(client, request, authenticationHash);

  }

  @Test
  public void authenticate_withHashToSing() {
    MidHashToSign hashToSign = MidHashToSign.newBuilder()
        .withDataToHash(DATA_TO_SIGN)
        .withHashType( MidHashType.SHA512)
        .build();

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber(VALID_PHONE)
        .withNationalIdentityNumber(VALID_NAT_IDENTITY)
        .withHashToSign(hashToSign)
        .withLanguage( MidLanguage.EST)
        .build();

    assertCorrectAuthenticationRequestMade(request);

    MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
    assertThat(response.getSessionID(), not(isEmptyOrNullString()));

    MidSessionStatus sessionStatus = client.getSessionStatusPoller()
            .fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
    assertAuthenticationPolled(sessionStatus);

    MidAuthentication authentication =client.createMobileIdAuthentication(sessionStatus, hashToSign);

    assertThat(authentication, is(notNullValue()));
    assertThat(authentication.getResult(), not(isEmptyOrNullString()));
    assertThat(authentication.getSignatureValueInBase64(), not(isEmptyOrNullString()));
    assertThat(authentication.getCertificate(), is(notNullValue()));
    assertThat(authentication.getSignedHashInBase64(), is(hashToSign.getHashInBase64()));
    assertThat(authentication.getHashType(), Matchers.is( MidHashType.SHA512));

    MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator();
    MidAuthenticationResult mobileIdAuthenticationResult = validator.validate(authentication);

    assertThat(mobileIdAuthenticationResult.isValid(), is(false));
    assertThat(mobileIdAuthenticationResult.getErrors(), contains("Signature verification failed"));
  }

  @Test
  public void authenticate_withDisplayText() {

      MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
          .withHashInBase64(SHA256_HASH_IN_BASE64)
          .withHashType( MidHashType.SHA256)
          .build();
    assertThat(authenticationHash.calculateVerificationCode(), is("0104"));

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber(VALID_PHONE)
        .withNationalIdentityNumber(VALID_NAT_IDENTITY)
        .withHashToSign(authenticationHash)
        .withLanguage( MidLanguage.EST)
        .withDisplayText("Log into internet banking system")
        .build();

    assertMadeCorrectAuthenticationRequesWithSHA256(request);

    MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
    assertThat(response.getSessionID(), not(isEmptyOrNullString()));

    MidSessionStatus
        sessionStatus =
        client.getSessionStatusPoller()
            .fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
    assertAuthenticationPolled(sessionStatus);

    MidAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
  }

  @Test
  public void authenticate_withDisplayTextAndWithDisplayTextFormat() {
    MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
        .withHashInBase64(SHA256_HASH_IN_BASE64)
        .withHashType( MidHashType.SHA256)
        .build();

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber(VALID_PHONE)
        .withNationalIdentityNumber(VALID_NAT_IDENTITY)
        .withHashToSign(authenticationHash)
        .withLanguage( MidLanguage.EST)
        .withDisplayText("войти")
        .withDisplayTextFormat( MidDisplayTextFormat.UCS2)
        .build();

    MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
    assertThat(response.getSessionID(), not(isEmptyOrNullString()));

    MidSessionStatus sessionStatus = client.getSessionStatusPoller()
            .fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
    assertAuthenticationPolled(sessionStatus);

    MidAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
  }

  @Test(expected = MidSessionTimeoutException.class)
  public void authenticate_whenTimeout_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenTimeout.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidInternalErrorException.class)
  public void authenticate_whenResponseRetrievingError_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenError.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidNotMidClientException.class)
  public void authenticate_whenNotMIDClient_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenNotMIDClient.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidSessionTimeoutException.class)
  public void authenticate_whenMSSPTransactionExpired_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenExpiredTransaction.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidUserCancellationException.class)
  public void authenticate_whenUserCancelled_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenUserCancelled.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidInternalErrorException.class)
  public void authenticate_whenMIDNotReady_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenMIDNotReady.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidPhoneNotAvailableException.class)
  public void authenticate_whenSimNotAvailable_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenPhoneAbsent.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidDeliveryException.class)
  public void authenticate_whenDeliveryError_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenDeliveryError.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidDeliveryException.class)
  public void authenticate_whenInvalidCardResponse_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenSimError.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidInvalidUserConfigurationException.class)
  public void authenticate_whenSignatureHashMismatch_shouldThrowException() {
    stubRequestWithResponse("/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusWhenSignatureHashMismatch.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidInternalErrorException.class)
  public void authenticate_whenGettingResponseFailed_shouldThrowException() {
    stubInternalServerErrorResponse("/authentication",
        "requests/authenticationRequest.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidInternalErrorException.class)
  public void authenticate_whenResponseNotFound_shouldThrowException() {
    stubNotFoundResponse("/authentication", "requests/authenticationRequest.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidMissingOrInvalidParameterException.class)
  public void authenticate_withWrongRequestParams_shouldThrowException() {
    stubBadRequestResponse("/authentication", "requests/authenticationRequest.json");
    makeValidAuthenticationRequest(client);
  }

  @Test(expected = MidUnauthorizedException.class)
  public void authenticate_withWrongAuthenticationParams_shouldThrowException() {
    stubUnauthorizedResponse("/authentication", "requests/authenticationRequest.json");
    makeValidAuthenticationRequest(client);
  }

  @Test
  public void setPollingSleepTimeoutForAuthentication() {
    stubSessionStatusWithState(
        "/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusRunning.json", STARTED, FIRST_REQUEST_DONE, 0);
    stubSessionStatusWithState(
        "/authentication/session/1dcc1600-29a6-4e95-a95c-d69b31febcfb",
        "responses/sessionStatusForSuccessfulAuthenticationRequest.json", FIRST_REQUEST_DONE, STARTED, 0);

    MidClient client = MidClient.newBuilder()
        .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
        .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
        .withHostUrl(LOCALHOST_URL)
        .withPollingSleepTimeoutSeconds(2)
        .build();

    long duration = measureAuthenticationDuration(client);
    assertThat("Duration is " + duration, duration > 2000L, is(true));
    assertThat("Duration is " + duration, duration < 3000L, is(true));
  }

  @Test
  public void setLongPollingTimeoutForAuthentication() {

    stubRequestWithResponse("/authentication", "requests/authenticationRequestUcs2.json",
        "responses/authenticationResponseSession2222.json");

    stubSessionStatusWithState(
        "/authentication/session/22222222-2222-2222-2222-222222222222?timeoutMs=3000",
        "responses/sessionStatusRunning.json", STARTED, FIRST_REQUEST_DONE, 3000);
    stubSessionStatusWithState(
        "/authentication/session/22222222-2222-2222-2222-222222222222?timeoutMs=3000",
        "responses/sessionStatusForSuccessfulAuthenticationRequest.json", FIRST_REQUEST_DONE, STARTED, 500);


    MidClient client = MidClient.newBuilder()
        .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
        .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
        .withHostUrl(LOCALHOST_URL)
        .withPollingSleepTimeoutSeconds(0)
        .withLongPollingTimeoutSeconds(3)
        .build();

    MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
        .withHashInBase64(SHA256_HASH_IN_BASE64)
        .withHashType( MidHashType.SHA256)
        .build();

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber(VALID_PHONE)
        .withNationalIdentityNumber(VALID_NAT_IDENTITY)
        .withHashToSign(authenticationHash)
        .withLanguage( MidLanguage.RUS)
        .withDisplayText("Войти")
        .withDisplayTextFormat( MidDisplayTextFormat.UCS2)
        .build();

    MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
    MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);

    client.createMobileIdAuthentication(sessionStatus, authenticationHash);

  }

  @Test
  public void verifyAuthentication_withNetworkConnectionConfigurationHavingCustomHeader() {
    String headerName = "custom-header";
    String headerValue = "Auth";

    Map<String, String> headersToAdd = new HashMap<>();
    headersToAdd.put(headerName, headerValue);
    ClientConfig clientConfig = getClientConfigWithCustomRequestHeaders(headersToAdd);

    MidClient client = MidClient.newBuilder()
        .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
        .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
        .withHostUrl(LOCALHOST_URL)
        .withNetworkConnectionConfig(clientConfig)
        .build();

    makeValidAuthenticationRequest(client);

    verify(postRequestedFor(urlEqualTo("/authentication"))
        .withHeader(headerName, equalTo(headerValue)));
  }

  @Test
  public void verifyMobileIdConnector_whenConnectorIsNotProvided() {
    MidConnector connector = client.getMobileIdConnector();
    assertThat(connector instanceof MidRestConnector, is(true));
  }

  @Test
  public void verifyMobileIdConnector_whenConnectorIsProvided() {
    final String mock = "Mock";
    MidSessionStatus status = mock( MidSessionStatus.class);
    when(status.getState()).thenReturn(mock);
    MidConnector connector = mock( MidConnector.class);
    when(connector.getSessionStatus(null, null)).thenReturn(status);

    MidClient client = MidClient.newBuilder()
        .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
        .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
        .withHostUrl(LOCALHOST_URL)
        .withMobileIdConnector(connector)
        .build();

    assertThat(client.getMobileIdConnector().getSessionStatus(null, null).getState(), is(mock));
  }

  private long measureAuthenticationDuration(MidClient client) {
    long startTime = System.currentTimeMillis();
    MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
        .withHashInBase64(SHA512_HASH_IN_BASE64)
        .withHashType( MidHashType.SHA512)
        .build();

    createAndSendAuthentication(client, VALID_PHONE, VALID_NAT_IDENTITY, authenticationHash);
    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }

  private ClientConfig getClientConfigWithCustomRequestHeaders(Map<String, String> headers) {
    ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
    clientConfig.register(new ClientRequestHeaderFilter(headers));
    return clientConfig;
  }
}
