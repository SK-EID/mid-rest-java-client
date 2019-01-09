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

import ee.sk.mid.exception.CertificateNotPresentException;
import ee.sk.mid.exception.ExpiredException;
import ee.sk.mid.exception.ParameterMissingException;
import ee.sk.mid.exception.TechnicalErrorException;
import ee.sk.mid.mock.MobileIdConnectorSpy;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.MobileIdRestConnector;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import org.junit.Before;
import org.junit.Test;

import static ee.sk.mid.mock.TestData.*;

public class CertificateRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setCertificateChoiceResponseToRespond(createDummyCertificateChoiceResponse());
    }

    @Test(expected = ParameterMissingException.class)
    public void getCertificate_withoutRelyingPartyUUID_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.getCertificate(request);

    }

    @Test(expected = ParameterMissingException.class)
    public void getCertificate_withoutRelyingPartyName_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.getCertificate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void getCertificate_withoutPhoneNumber_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.getCertificate(request);
    }

    @Test(expected = ParameterMissingException.class)
    public void getCertificate_withoutNationalIdentityNumber_shouldThrowException() {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .build();

        MobileIdConnector connector = new MobileIdRestConnector(LOCALHOST_URL);
        connector.getCertificate(request);
    }

    @Test(expected = CertificateNotPresentException.class)
    public void getCertificate_withCertificateNotPresent_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("NOT_FOUND");
        makeCertificateRequest(connector);
    }

    @Test(expected = ExpiredException.class)
    public void getCertificate_withInactiveCertificateFound_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("NOT_ACTIVE");
        makeCertificateRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void getCertificate_withResultMissingInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult(null);
        makeCertificateRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void getCertificate_withResultBlankInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("");
        makeCertificateRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void getCertificate_withCertificateMissingInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setCert(null);
        makeCertificateRequest(connector);
    }

    @Test(expected = TechnicalErrorException.class)
    public void getCertificate_withCertificateBlankInResponse_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setCert("");
        makeCertificateRequest(connector);
    }

    private void makeCertificateRequest(MobileIdConnector connector) {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        CertificateChoiceResponse response = connector.getCertificate(request);

        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(VALID_RELYING_PARTY_UUID)
                .withRelyingPartyName(VALID_RELYING_PARTY_NAME)
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
