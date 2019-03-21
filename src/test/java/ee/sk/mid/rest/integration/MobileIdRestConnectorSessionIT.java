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
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectSignatureRequestMade;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createValidSignatureRequest;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertAuthenticationPolled;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertSignaturePolled;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.SESSION_ID;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.MidSessionStatusPoller;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdRestConnectorSessionIT {

    private MidConnector connector;

    @Before
    public void setUp() {
        connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .build();
    }

    @Test
    public void getSessionStatus_forSuccessfulSigningRequest() {
        MidSignatureRequest signatureRequest = createValidSignatureRequest();
        assertCorrectSignatureRequestMade(signatureRequest);

        MidSignatureResponse signatureResponse = connector.sign(signatureRequest);
        assertThat(signatureResponse.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatusRequest sessionStatusRequest = new MidSessionStatusRequest(signatureResponse.getSessionID());
        MidSessionStatusPoller poller = MidSessionStatusPoller.newBuilder()
            .withConnector(connector)
            .build();

        MidSessionStatus sessionStatus = poller.fetchFinalSignatureSessionStatus(sessionStatusRequest.getSessionID());
        assertSignaturePolled(sessionStatus);
    }

    @Test
    public void getSessionStatus_forSuccessfulAuthenticationRequest() {
        MidAuthenticationRequest authenticationRequest = createValidAuthenticationRequest();
        assertCorrectAuthenticationRequestMade(authenticationRequest);

        MidAuthenticationResponse authenticationResponse = connector.authenticate(authenticationRequest);
        assertThat(authenticationResponse.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatusRequest sessionStatusRequest = new MidSessionStatusRequest(authenticationResponse.getSessionID());
        MidSessionStatusPoller poller = MidSessionStatusPoller.newBuilder()
            .withConnector(connector)
            .build();

        MidSessionStatus sessionStatus = poller.fetchFinalAuthenticationSessionStatus(sessionStatusRequest.getSessionID());
        assertAuthenticationPolled(sessionStatus);
    }

    @Test(expected = MidSessionNotFoundException.class)
    public void getSessionStatus_whenSessionStatusNotExists_shouldThrowException() {
        MidSessionStatusRequest request = new MidSessionStatusRequest(SESSION_ID);
        connector.getAuthenticationSessionStatus(request);
    }

    @Test(expected = MidSessionNotFoundException.class)
    public void getSessionStatus_whenSessionStatusNotFound_shouldThrowException() {
        MidSignatureRequest signatureRequest = createValidSignatureRequest();
        assertCorrectSignatureRequestMade(signatureRequest);

        MidSignatureResponse signatureResponse = connector.sign(signatureRequest);
        assertThat(signatureResponse.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatusRequest sessionStatusRequest = new MidSessionStatusRequest(signatureResponse.getSessionID());
        connector.getAuthenticationSessionStatus(sessionStatusRequest);
    }
}
