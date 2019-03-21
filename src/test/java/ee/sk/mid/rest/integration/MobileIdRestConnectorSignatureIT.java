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

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectSignatureRequestMade;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createSignatureRequest;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertSignaturePolled;
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
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdRestConnectorSignatureIT {

    private static final String SIGNATURE_SESSION_PATH = "/signature/session/{sessionId}";

    private MidConnector connector;

    @Before
    public void setUp() {
        connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .build();
    }

    @Test
    public void sign() throws Exception {
        MidSignatureRequest request = createSignatureRequest(DEMO_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        assertCorrectSignatureRequestMade(request);

        MidSignatureResponse response = connector.sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = pollSessionStatus(connector, response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);
    }

    @Test
    public void sign_withDisplayText() throws InterruptedException {
        MidSignatureRequest request = createSignatureRequest(DEMO_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        request.setDisplayText("Authorize transfer of 10 euros");
        assertCorrectSignatureRequestMade(request);

        MidSignatureResponse response = connector.sign(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = pollSessionStatus(connector, response.getSessionID(), SIGNATURE_SESSION_PATH);
        assertSignaturePolled(sessionStatus);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongPhoneNumber_shouldThrowException() {
        MidSignatureRequest request = createSignatureRequest(DEMO_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, WRONG_PHONE, VALID_NAT_IDENTITY);
        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongNationalIdentityNumber_shouldThrowException() {
        MidSignatureRequest request = createSignatureRequest(DEMO_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, WRONG_NAT_IDENTITY);
        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongRelyingPartyUUID_shouldThrowException() {
        MidSignatureRequest request = createSignatureRequest(WRONG_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.sign(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void sign_withWrongRelyingPartyName_shouldThrowException() {
        MidSignatureRequest request = createSignatureRequest(DEMO_RELYING_PARTY_UUID, WRONG_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.sign(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void sign_withUnknownRelyingPartyUUID_shouldThrowException() {
        MidSignatureRequest request = createSignatureRequest(DEMO_RELYING_PARTY_UUID, UNKNOWN_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.sign(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void sign_withUnknownRelyingPartyName_shouldThrowException() {
        MidSignatureRequest request = createSignatureRequest(UNKNOWN_RELYING_PARTY_UUID,
            DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
        connector.sign(request);
    }
}
