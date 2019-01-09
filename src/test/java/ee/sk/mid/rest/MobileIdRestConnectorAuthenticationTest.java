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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.ClientRequestHeaderFilter;
import ee.sk.mid.HashType;
import ee.sk.mid.Language;
import ee.sk.mid.exception.ParameterMissingException;
import ee.sk.mid.exception.ResponseNotFoundException;
import ee.sk.mid.exception.ResponseRetrievingException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceStub.*;
import static ee.sk.mid.mock.TestData.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MobileIdRestConnectorAuthenticationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);

    private MobileIdConnector connector;

    @Before
    public void setUp() {
        connector = new MobileIdRestConnector(LOCALHOST_URL);
    }

    @Test
    public void authenticate() throws IOException {
        stubRequestWithResponse("/mid-api/authentication", "requests/authenticationRequest.json", "responses/authenticationResponse.json");
        AuthenticationRequest request = createValidAuthenticationRequest();
        AuthenticationResponse response = connector.authenticate(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("1dcc1600-29a6-4e95-a95c-d69b31febcfb"));
    }

    @Test
    public void authenticate_withDisplayText() throws IOException {
        stubRequestWithResponse("/mid-api/authentication", "requests/authenticationRequestWithDisplayText.json", "responses/authenticationResponse.json");

        AuthenticationRequest request = new AuthenticationRequest();
        request.setRelyingPartyUUID(VALID_RELYING_PARTY_UUID);
        request.setRelyingPartyName(VALID_RELYING_PARTY_NAME);
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);
        request.setHash("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=");
        request.setHashType(HashType.SHA256);
        request.setLanguage(Language.EST);
        request.setDisplayText("Log into internet banking system");

        AuthenticationResponse response = connector.authenticate(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getSessionID(), is("1dcc1600-29a6-4e95-a95c-d69b31febcfb"));
    }

    @Test(expected = ResponseRetrievingException.class)
    public void authenticate_whenGettingResponseFailed_shouldThrowException() throws IOException {
        stubInternalServerErrorResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        AuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = ResponseNotFoundException.class)
    public void authenticate_whenResponseNotFound_shouldThrowException() throws IOException {
        stubNotFoundResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        AuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void authenticate_withWrongRequestParams_shouldThrowException() throws IOException {
        stubBadRequestResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        AuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void authenticate_withWrongAuthenticationParams_shouldThrowException() throws IOException {
        stubUnauthorizedResponse("/mid-api/authentication", "requests/authenticationRequest.json");
        AuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);
    }

    @Test
    public void verifyCustomRequestHeaderPresent_whenAuthenticating() throws IOException {
        String headerName = "custom-header";
        String headerValue = "Auth";

        Map<String, String> headers = new HashMap<>();
        headers.put(headerName, headerValue);
        connector = new MobileIdRestConnector("http://localhost:18089", getClientConfigWithCustomRequestHeader(headers));
        stubRequestWithResponse("/mid-api/authentication", "requests/authenticationRequest.json", "responses/authenticationResponse.json");
        AuthenticationRequest request = createValidAuthenticationRequest();
        connector.authenticate(request);

        verify(postRequestedFor(urlEqualTo("/mid-api/authentication"))
                .withHeader(headerName, equalTo(headerValue)));
    }

    private ClientConfig getClientConfigWithCustomRequestHeader(Map<String, String> headers) {
        ClientConfig clientConfig = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        clientConfig.register(new ClientRequestHeaderFilter(headers));
        return clientConfig;
    }
}
