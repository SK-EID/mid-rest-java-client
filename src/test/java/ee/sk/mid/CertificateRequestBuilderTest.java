package ee.sk.mid;

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

import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_EE;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.LOCALHOST_URL;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;

import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.NotMidClientException;
import ee.sk.mid.mock.MobileIdConnectorSpy;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import org.junit.Before;
import org.junit.Test;

public class CertificateRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setCertificateChoiceResponseToRespond(createDummyCertificateChoiceResponse());
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withoutRelyingPartyUUID_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.getCertificate(request);

    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withoutRelyingPartyName_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .build();
        connector.getCertificate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withoutPhoneNumber_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.getCertificate(request);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withoutNationalIdentityNumber_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .build();

        MobileIdConnector connector = MobileIdRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.getCertificate(request);
    }

    @Test(expected = NotMidClientException.class)
    public void getCertificate_withCertificateNotPresent_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("NOT_FOUND");
        makeCertificateRequest(connector);
    }

    @Test(expected = NotMidClientException.class)
    public void getCertificate_withInactiveCertificateFound_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("NOT_ACTIVE");
        makeCertificateRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getCertificate_withResultMissingInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult(null);
        makeCertificateRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getCertificate_withResultBlankInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("");
        makeCertificateRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getCertificate_withCertificateMissingInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setCert(null);
        makeCertificateRequest(connector);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getCertificate_withCertificateBlankInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setCert("");
        makeCertificateRequest(connector);
    }

    private void makeCertificateRequest(MobileIdConnector connector) {
        CertificateRequest request = CertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .build();

        CertificateChoiceResponse response = connector.getCertificate(request);

        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(LOCALHOST_URL)
            .build();

        client.createMobileIdCertificate(response);
    }

    private static CertificateChoiceResponse createDummyCertificateChoiceResponse() {
        CertificateChoiceResponse certificateChoiceResponse = new CertificateChoiceResponse();
        certificateChoiceResponse.setResult("OK");
        certificateChoiceResponse.setCert(AUTH_CERTIFICATE_EE);
        return certificateChoiceResponse;
    }
}
