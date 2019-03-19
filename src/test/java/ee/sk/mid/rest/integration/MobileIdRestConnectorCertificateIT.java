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
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdRestConnectorCertificateIT {

    private MobileIdConnector connector;

    @Before
    public void setUp() {
        connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
    }

    @Test
    public void getCertificate() {
        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        assertCorrectCertificateRequestMade(request);

        CertificateChoiceResponse response = connector.getCertificate(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is("OK"));
        assertThat(response.getCert(), not(isEmptyOrNullString()));
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongPhoneNumber_shouldThrowException() {
        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(WRONG_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);


        connector.getCertificate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongNationalIdentityNumber_shouldThrowException() {
        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(WRONG_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongRelyingPartyUUID_shouldThrowException() {
        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID("wrong_UUID")
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void getCertificate_withWrongRelyingPartyName_shouldThrowException() {
        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName("wrong name")
            .build();

        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyUUID_shouldThrowException() {
        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
            .build();

        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyName_shouldThrowException() {
        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(DEMO_HOST_URL)
            .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();

        CertificateRequest request = new CertificateRequest();
        request.setPhoneNumber(VALID_PHONE);
        request.setNationalIdentityNumber(VALID_NAT_IDENTITY);

        connector.getCertificate(request);
    }
}
