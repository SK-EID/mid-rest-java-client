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

import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.SessionNotFoundException;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.SessionStatusPoller;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.request.SessionStatusRequest;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.*;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.*;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.SESSION_ID;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category({IntegrationTest.class})
public class MobileIdRestConnectorSessionIT {

    private MobileIdConnector connector;

    @Before
    public void setUp() {
        connector = new MobileIdRestConnector(DEMO_HOST_URL);
    }

    @Test
    public void getSessionStatus_forSuccessfulSigningRequest() {
        SignatureRequest signatureRequest = createValidSignatureRequest();
        assertCorrectSignatureRequestMade(signatureRequest);

        SignatureResponse signatureResponse = connector.sign(signatureRequest);
        assertThat(signatureResponse.getSessionID(), not(isEmptyOrNullString()));

        SessionStatusRequest sessionStatusRequest = new SessionStatusRequest(signatureResponse.getSessionID());
        SessionStatusPoller poller = new SessionStatusPoller(connector);
        SessionStatus sessionStatus = poller.fetchFinalSignatureSessionStatus(sessionStatusRequest.getSessionID());
        assertSignaturePolled(sessionStatus);
    }

    @Test
    public void getSessionStatus_forSuccessfulAuthenticationRequest() {
        AuthenticationRequest authenticationRequest = createValidAuthenticationRequest();
        assertCorrectAuthenticationRequestMade(authenticationRequest);

        AuthenticationResponse authenticationResponse = connector.authenticate(authenticationRequest);
        assertThat(authenticationResponse.getSessionID(), not(isEmptyOrNullString()));

        SessionStatusRequest sessionStatusRequest = new SessionStatusRequest(authenticationResponse.getSessionID());
        SessionStatusPoller poller = new SessionStatusPoller(connector);
        SessionStatus sessionStatus = poller.fetchFinalAuthenticationSessionStatus(sessionStatusRequest.getSessionID());
        assertAuthenticationPolled(sessionStatus);
    }

    @Test(expected = SessionNotFoundException.class)
    public void getSessionStatus_whenSessionStatusNotExists_shouldThrowException() {
        SessionStatusRequest request = new SessionStatusRequest(SESSION_ID);
        connector.getAuthenticationSessionStatus(request);
    }

    @Test(expected = SessionNotFoundException.class)
    public void getSessionStatus_whenSessionStatusNotFound_shouldThrowException() {
        SignatureRequest signatureRequest = createValidSignatureRequest();
        assertCorrectSignatureRequestMade(signatureRequest);

        SignatureResponse signatureResponse = connector.sign(signatureRequest);
        assertThat(signatureResponse.getSessionID(), not(isEmptyOrNullString()));

        SessionStatusRequest sessionStatusRequest = new SessionStatusRequest(signatureResponse.getSessionID());
        connector.getAuthenticationSessionStatus(sessionStatusRequest);
    }
}
