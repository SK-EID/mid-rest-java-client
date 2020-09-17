package ee.sk.mid.rest;

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
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidSignatureRequest;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubBadRequestResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubInternalServerErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubNotFoundResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubRequestWithResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubServiceUnavailableErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubUnauthorizedResponse;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.ClientRequestHeaderFilter;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidServiceUnavailableException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Rule;
import org.junit.Test;

public class MobileIdRestConnectorSignatureTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);


    @Test
    public void sign() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubRequestWithResponse("/signature", "requests/signatureRequest.json", "responses/signatureResponse.json");
        MidSignatureRequest request = createValidSignatureRequest();
        MidSignatureResponse response = connector.sign(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("2c52caf4-13b0-41c4-bdc6-aa268403cc00"));
    }

    @Test
    public void legacyResponseWithSessionId() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubRequestWithResponse("/signature", "requests/signatureRequest.json", "responses/signatureResponseLegacySessionId.json");
        MidSignatureRequest request = createValidSignatureRequest();
        MidSignatureResponse response = connector.sign(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("33333333-3333-3333-3333-333333333333"));
    }

    @Test
    public void sign_withDisplayText() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubRequestWithResponse("/signature", "requests/signatureRequestWithDisplayText.json", "responses/signatureResponse.json");
        MidSignatureRequest request = createValidSignatureRequest();
        request.setDisplayText("Authorize transfer of 10 euros");
        MidSignatureResponse response = connector.sign(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("2c52caf4-13b0-41c4-bdc6-aa268403cc00"));
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenGettingResponseFailed_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubInternalServerErrorResponse("/signature", "requests/signatureRequest.json");
        MidSignatureRequest request = createValidSignatureRequest();
        connector.sign(request);
    }

    @Test(expected = MidServiceUnavailableException.class)
    public void authenticate_whenHttpStatusCode503_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
             .withEndpointUrl(LOCALHOST_URL)
             .build();

        stubServiceUnavailableErrorResponse("/signature", "requests/signatureRequest.json");
        MidSignatureRequest request = createValidSignatureRequest();
        connector.sign(request);
    }

    @Test(expected = MidInternalErrorException.class)
    public void sign_whenResponseNotFound_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubNotFoundResponse("/signature", "requests/signatureRequest.json");
        MidSignatureRequest request = createValidSignatureRequest();
        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongRequestParams_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubBadRequestResponse("/signature", "requests/signatureRequest.json");
        MidSignatureRequest request = createValidSignatureRequest();
        connector.sign(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void sign_withWrongAuthenticationParams_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();

        stubUnauthorizedResponse("/signature", "requests/signatureRequest.json");
        MidSignatureRequest request = createValidSignatureRequest();
        connector.sign(request);
    }

    @Test
    public void verifyCustomRequestHeaderPresent_whenSigning() {
        String headerName = "custom-header";
        String headerValue = "Sign";

        Map<String, String> headers = new HashMap<>();
        headers.put(headerName, headerValue);
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withClientConfig(getClientConfigWithCustomRequestHeader(headers))
            .build();

        stubRequestWithResponse("/signature", "requests/signatureRequest.json", "responses/signatureResponse.json");
        MidSignatureRequest request = createValidSignatureRequest();
        connector.sign(request);

        verify(postRequestedFor(urlEqualTo("/signature"))
                .withHeader(headerName, equalTo(headerValue)));
    }

    private ClientConfig getClientConfigWithCustomRequestHeader(Map<String, String> headers) {
        ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        clientConfig.register(new ClientRequestHeaderFilter(headers));
        return clientConfig;
    }
}
