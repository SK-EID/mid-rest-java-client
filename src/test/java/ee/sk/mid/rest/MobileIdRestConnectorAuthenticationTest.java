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
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubBadRequestResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubInternalServerErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubNotFoundResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubRequestWithResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubServiceUnavailableErrorResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubUnauthorizedResponse;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.ClientRequestHeaderFilter;
import ee.sk.mid.MidHashType;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidServiceUnavailableException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MobileIdRestConnectorAuthenticationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);

    private MidConnector connector;

    @Before
    public void setUp() {
        connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();
    }

    @Test
    public void authenticate() {
        stubRequestWithResponse("/authentication", "requests/authenticationRequest.json", "responses/authenticationResponse.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        MidAuthenticationResponse response = connector.authenticate(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("1dcc1600-29a6-4e95-a95c-d69b31febcfb"));
    }

    @Test
    public void authenticate_withDisplayText() {
        stubRequestWithResponse("/authentication", "requests/authenticationRequestWithDisplayText.json", "responses/authenticationResponse.json");

        MidAuthenticationRequest request = new MidAuthenticationRequest();
        request.setRelyingPartyUUID(DEMO_RELYING_PARTY_UUID);
        request.setRelyingPartyName(DEMO_RELYING_PARTY_NAME);
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);
        request.setHash("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=");
        request.setHashType( MidHashType.SHA256);
        request.setLanguage( MidLanguage.EST);
        request.setDisplayText("Log into internet banking system");

        MidAuthenticationResponse response = connector.authenticate(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("1dcc1600-29a6-4e95-a95c-d69b31febcfb"));
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_whenGettingResponseFailed_shouldThrowException() {
        stubInternalServerErrorResponse("/authentication", "requests/authenticationRequest.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = MidServiceUnavailableException.class)
    public void authenticate_whenHttpStatusCode503_shouldThrowException() {
        stubServiceUnavailableErrorResponse("/authentication", "requests/authenticationRequest.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = MidInternalErrorException.class)
    public void authenticate_whenResponseNotFound_shouldThrowException() {
        stubNotFoundResponse("/authentication", "requests/authenticationRequest.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withWrongRequestParams_shouldThrowException() {
        stubBadRequestResponse("/authentication", "requests/authenticationRequest.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void authenticate_withWrongAuthenticationParams_shouldThrowException() {
        stubUnauthorizedResponse("/authentication", "requests/authenticationRequest.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test
    public void verifyCustomRequestHeaderPresent_whenAuthenticating() {
        String headerName = "custom-header";
        String headerValue = "Auth";

        Map<String, String> headers = new HashMap<>();
        headers.put(headerName, headerValue);
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withClientConfig(getClientConfigWithCustomRequestHeader(headers))
            .build();
        stubRequestWithResponse("/authentication", "requests/authenticationRequest.json", "responses/authenticationResponse.json");
        MidAuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);

        verify(postRequestedFor(urlEqualTo("/authentication"))
                .withHeader(headerName, equalTo(headerValue)));
    }

    private ClientConfig getClientConfigWithCustomRequestHeader(Map<String, String> headers) {
        ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        clientConfig.register(new ClientRequestHeaderFilter(headers));
        return clientConfig;
    }
}
