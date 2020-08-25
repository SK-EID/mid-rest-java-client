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
import static ee.sk.mid.AuthenticationRequestBuilderTest.SERVER_SSL_CERTIFICATE;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCertificateCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.makeValidCertificateRequest;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubBadRequestResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubInternalServerErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubNotFoundResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubRequestWithResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubUnauthorizedResponse;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.mock.MobileIdRestServiceRequestDummy;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MobileIdClientCertificateTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(18089);

  private MidClient client;

  @Before
  public void setUp() {
    client = MidClient.newBuilder()
        .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
        .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
        .withHostUrl(LOCALHOST_URL)
        .withTrustedCertificates(SERVER_SSL_CERTIFICATE)
        .build();
    stubRequestWithResponse("/certificate", "requests/certificateChoiceRequest.json",
        "responses/certificateChoiceResponse.json");
  }

  @Test
  public void getCertificate() {
    X509Certificate certificate = MobileIdRestServiceRequestDummy.getCertificate(client);

    assertCertificateCreated(certificate);
  }

  @Test(expected = MidNotMidClientException.class)
  public void getCertificate_whenCertificateNotPresent_shouldThrowException() {
    stubRequestWithResponse("/certificate", "requests/certificateChoiceRequest.json",
        "responses/certificateChoiceResponseWhenCertificateNotFound.json");
    makeValidCertificateRequest(client);
  }

  @Test(expected = MidInternalErrorException.class)
  public void getCertificate_whenGettingResponseFailed_shouldThrowException() {
    stubInternalServerErrorResponse("/certificate",
        "requests/certificateChoiceRequest.json");
    makeValidCertificateRequest(client);
  }

  @Test(expected = MidInternalErrorException.class)
  public void getCertificate_whenResponseNotFound_shouldThrowException() {
    stubNotFoundResponse("/certificate", "requests/certificateChoiceRequest.json");
    makeValidCertificateRequest(client);
  }

  @Test(expected = MidMissingOrInvalidParameterException.class)
  public void getCertificate_withWrongRequestParams_shouldThrowException() {
    stubBadRequestResponse("/certificate", "requests/certificateChoiceRequest.json");
    makeValidCertificateRequest(client);
  }

  @Test(expected = MidUnauthorizedException.class)
  public void getCertificate_withWrongAuthenticationParams_shouldThrowException()
      {
    stubUnauthorizedResponse("/certificate", "requests/certificateChoiceRequest.json");
    makeValidCertificateRequest(client);
  }

  @Test
  public void verifyCertificateChoice_withNetworkConnectionConfigurationHavingCustomHeader() {
    String headerName = "custom-header";
    String headerValue = "Fetch";

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

    makeValidCertificateRequest(client);

    verify(postRequestedFor(urlEqualTo("/certificate"))
        .withHeader(headerName, equalTo(headerValue)));
  }

  private ClientConfig getClientConfigWithCustomRequestHeaders(Map<String, String> headers) {
    ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
    clientConfig.register(new ClientRequestHeaderFilter(headers));
    return clientConfig;
  }
}
