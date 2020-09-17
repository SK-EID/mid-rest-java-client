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

import static ee.sk.mid.mock.MobileIdRestServiceStub.stubNotFoundResponse;
import static ee.sk.mid.mock.MobileIdRestServiceStub.stubRequestWithResponse;
import static ee.sk.mid.mock.SessionStatusDummy.assertErrorSessionStatus;
import static ee.sk.mid.mock.SessionStatusDummy.assertSuccessfulSessionStatus;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MobileIdRestConnectorSessionTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(18089);

    private MidConnector connector;

    @Before
    public void setUp() {
        connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .build();
    }

    @Test(expected = MidSessionNotFoundException.class)
    public void getNotExistingSessionStatus() {
        stubNotFoundResponse("/authentication/session/de305d54-75b4-431b-adb2-eb6b9e546016");
        MidSessionStatusRequest request = new MidSessionStatusRequest("de305d54-75b4-431b-adb2-eb6b9e546016");
        connector.getAuthenticationSessionStatus(request);
    }

    @Test
    public void getRunningSessionStatus() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusRunning.json");

        assertThat(sessionStatus, is(notNullValue()));
        assertThat(sessionStatus.getState(), is("RUNNING"));
    }

    @Test
    public void getSessionStatus_forSuccessfulAuthenticationRequest() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusForSuccessfulAuthenticationRequest.json");

        assertSuccessfulSessionStatus(sessionStatus);

        assertThat(sessionStatus.getSignature(), is(notNullValue()));
        assertThat(sessionStatus.getSignature().getValue(), startsWith("luvjsi1+1iLN9yfDFEh/BE8hXtAKhAIxilv"));
        assertThat(sessionStatus.getSignature().getAlgorithm(), is("sha256WithRSAEncryption"));
        assertThat(sessionStatus.getCert(), startsWith("MIIHhjCCBW6gAwIBAgIQDNYLtVwrKURYStr"));
    }

    @Test
    public void getSessionStatus_whenTimeout() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenTimeout.json");
        assertErrorSessionStatus(sessionStatus, "TIMEOUT");
    }

    @Test
    public void getSessionStatus_whenError() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenError.json");
        assertErrorSessionStatus(sessionStatus, "ERROR");
    }

    @Test
    public void getSessionStatus_whenNotMIDClient() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenNotMIDClient.json");
        assertErrorSessionStatus(sessionStatus, "NOT_MID_CLIENT");
    }

    @Test
    public void getSessionStatus_whenExpiredTransaction() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenExpiredTransaction.json");
        assertErrorSessionStatus(sessionStatus, "EXPIRED_TRANSACTION");
    }

    @Test
    public void getSessionStatus_whenUserCancelled() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenUserCancelled.json");
        assertErrorSessionStatus(sessionStatus, "USER_CANCELLED");
    }

    @Test
    public void getSessionStatus_whenMIDNotReady() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenMIDNotReady.json");
        assertErrorSessionStatus(sessionStatus, "MID_NOT_READY");
    }

    @Test
    public void getSessionStatus_whenPhoneAbsent() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenPhoneAbsent.json");
        assertErrorSessionStatus(sessionStatus, "PHONE_ABSENT");
    }

    @Test
    public void getSessionStatus_whenDeliveryError() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenDeliveryError.json");
        assertErrorSessionStatus(sessionStatus, "DELIVERY_ERROR");
    }

    @Test
    public void getSessionStatus_whenSimError() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenSimError.json");
        assertErrorSessionStatus(sessionStatus, "SIM_ERROR");
    }

    @Test
    public void getSessionStatus_whenSignatureHashMismatch() {
        MidSessionStatus sessionStatus = getStubbedSessionStatusWithResponse("responses/sessionStatusWhenSignatureHashMismatch.json");
        assertErrorSessionStatus(sessionStatus, "SIGNATURE_HASH_MISMATCH");
    }

    private MidSessionStatus getStubbedSessionStatusWithResponse(String responseFile) {
        stubRequestWithResponse("/authentication/session/de305d54-75b4-431b-adb2-eb6b9e546016", responseFile);
        MidSessionStatusRequest request = new MidSessionStatusRequest("de305d54-75b4-431b-adb2-eb6b9e546016");
        return connector.getAuthenticationSessionStatus(request);
    }
}
