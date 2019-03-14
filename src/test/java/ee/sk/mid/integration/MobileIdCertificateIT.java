package ee.sk.mid.integration;

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

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCertificateCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.makeCertificateRequest;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_NOT_MID_CLIENT;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static ee.sk.mid.mock.TestData.VALID_PHONE_NOT_MID_CLIENT;
import static ee.sk.mid.mock.TestData.WRONG_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.WRONG_PHONE;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_UUID;

import java.security.cert.X509Certificate;

import ee.sk.mid.MobileIdClient;
import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.NotMidClientException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.mock.MobileIdRestServiceRequestDummy;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdCertificateIT {

    @Test
    public void getCertificate() {
        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        X509Certificate certificate = MobileIdRestServiceRequestDummy.getCertificate(client);

        assertCertificateCreated(certificate);
    }

    @Test(expected = NotMidClientException.class)
    public void getCertificate_whenCertificateNotPresent_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeCertificateRequest(client, VALID_PHONE_NOT_MID_CLIENT, VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongPhoneNumber_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeCertificateRequest(client, WRONG_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongNationalIdentityNumber_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, WRONG_NAT_IDENTITY);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongRelyingPartyUUID_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(WRONG_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void getCertificate_withWrongRelyingPartyName_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(WRONG_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = UnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyUUID_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = UnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyName_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
}
