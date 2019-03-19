package ee.sk.mid.rest.integration;

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

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectAuthenticationRequestMade;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertAuthenticationPolled;
import static ee.sk.mid.mock.SessionStatusPollerDummy.pollSessionStatus;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static ee.sk.mid.mock.TestData.WRONG_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.WRONG_PHONE;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_UUID;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdRestConnectorAuthenticationIT {

    private static final String AUTHENTICATION_SESSION_PATH = "/authentication/session/{sessionId}";

    private MobileIdConnector connector;

    @Before
    public void setUp() {
        connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .build();
    }

    @Test
    public void authenticate() throws Exception {
        AuthenticationRequest request = createValidAuthenticationRequest();
        assertCorrectAuthenticationRequestMade(request);

        AuthenticationResponse response = connector.authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = pollSessionStatus(connector, response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);
    }

    @Test
    public void authenticate_withDisplayText() throws InterruptedException {
        AuthenticationRequest request = createValidAuthenticationRequest();
        request.setDisplayText("Log into internet banking system");
        assertCorrectAuthenticationRequestMade(request);

        AuthenticationResponse response = connector.authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = pollSessionStatus(connector, response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongPhoneNumber_shouldThrowException() {
        AuthenticationRequest request = createAuthenticationRequest(DEMO_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, WRONG_PHONE, VALID_NAT_IDENTITY);
        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongNationalIdentityNumber_shouldThrowException() {
        AuthenticationRequest request = createAuthenticationRequest(DEMO_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, WRONG_NAT_IDENTITY);
        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongRelyingPartyUUID_shouldThrowException() {
        AuthenticationRequest request = createAuthenticationRequest(WRONG_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.authenticate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongRelyingPartyName_shouldThrowException() {
        AuthenticationRequest request = createAuthenticationRequest(DEMO_RELYING_PARTY_UUID, WRONG_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.authenticate(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void authenticate_withUnknownRelyingPartyUUID_shouldThrowException() {
        AuthenticationRequest request = createAuthenticationRequest(DEMO_RELYING_PARTY_UUID, UNKNOWN_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.authenticate(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void authenticate_withUnknownRelyingPartyName_shouldThrowException() {
        AuthenticationRequest request = createAuthenticationRequest(UNKNOWN_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.authenticate(request);
    }
}
