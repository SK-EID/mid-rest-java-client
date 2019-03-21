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
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.mock.MobileIdConnectorSpy;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidRestConnector;
import ee.sk.mid.rest.dao.request.MidCertificateRequest;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import org.junit.Before;
import org.junit.Test;

public class CertificateRequestBuilderTest {

    private MobileIdConnectorSpy connector;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorSpy();
        connector.setCertificateChoiceResponseToRespond(createDummyCertificateChoiceResponse());
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withoutRelyingPartyUUID_shouldThrowException() {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.getCertificate(request);

    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withoutRelyingPartyName_shouldThrowException() {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .build();
        connector.getCertificate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withoutPhoneNumber_shouldThrowException() {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.getCertificate(request);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withoutNationalIdentityNumber_shouldThrowException() {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .build();

        MidConnector connector = MidRestConnector.newBuilder()
            .withEndpointUrl(LOCALHOST_URL)
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .build();
        connector.getCertificate(request);
    }

    @Test(expected = MidNotMidClientException.class)
    public void getCertificate_withCertificateNotPresent_shouldThrowException() {
        connector.getCertificateChoiceResponseToRespond().setResult("NOT_FOUND");
        makeCertificateRequest(connector);
    }

    @Test(expected = MidNotMidClientException.class)
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

    private void makeCertificateRequest(MidConnector connector) {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .build();

        MidCertificateChoiceResponse response = connector.getCertificate(request);

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(LOCALHOST_URL)
            .build();

        client.createMobileIdCertificate(response);
    }

    private static MidCertificateChoiceResponse createDummyCertificateChoiceResponse() {
        MidCertificateChoiceResponse certificateChoiceResponse = new MidCertificateChoiceResponse();
        certificateChoiceResponse.setResult("OK");
        certificateChoiceResponse.setCert(AUTH_CERTIFICATE_EE);
        return certificateChoiceResponse;
    }
}
