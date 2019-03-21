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

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCorrectCertificateRequestMade;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static ee.sk.mid.mock.TestData.WRONG_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.WRONG_PHONE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.dao.request.MidCertificateRequest;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdRestConnectorCertificateIT {

    private MidConnector connector;

    @Before
    public void setUp() {
        connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
    }

    @Test
    public void getCertificate() {
        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        assertCorrectCertificateRequestMade(request);

        MidCertificateChoiceResponse response = connector.getCertificate(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is("OK"));
        assertThat(response.getCert(), not(isEmptyOrNullString()));
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongPhoneNumber_shouldThrowException() {
        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(WRONG_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);


        connector.getCertificate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongNationalIdentityNumber_shouldThrowException() {
        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(WRONG_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongRelyingPartyUUID_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID("wrong_UUID")
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void getCertificate_withWrongRelyingPartyName_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName("wrong name")
            .build();

        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyUUID_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
            .build();

        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyName_shouldThrowException() {
        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        MidCertificateRequest request = new MidCertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }
}
