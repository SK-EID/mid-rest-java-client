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

import static ee.sk.mid.integration.MobileIdSSL_IT.DEMO_SERVER_CERT_EXPIRATION_DATE;
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
import static org.junit.Assume.assumeTrue;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDate;

import ee.sk.mid.MidClient;
import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.mock.MobileIdRestServiceRequestDummy;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdCertificateIT {

    @Test
    public void getCertificate() throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore keystoreWithDemoServerCertificate = KeyStore.getInstance("JKS");
        keystoreWithDemoServerCertificate.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .withTrustStore(keystoreWithDemoServerCertificate)
            .build();

        X509Certificate certificate = MobileIdRestServiceRequestDummy.getCertificate(client);

        assertCertificateCreated(certificate);
    }

    @Test(expected = MidNotMidClientException.class)
    public void getCertificate_whenCertificateNotPresent_shouldThrowException() throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore keystoreWithDemoServerCertificate = KeyStore.getInstance("JKS");
        keystoreWithDemoServerCertificate.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .withTrustStore(keystoreWithDemoServerCertificate)
            .build();

        makeCertificateRequest(client, VALID_PHONE_NOT_MID_CLIENT, VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongPhoneNumber_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeCertificateRequest(client, WRONG_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongNationalIdentityNumber_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, WRONG_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongRelyingPartyUUID_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(WRONG_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void getCertificate_withWrongRelyingPartyName_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(WRONG_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyUUID_shouldThrowException() throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withTrustStore(trustStore)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void getCertificate_withUnknownRelyingPartyName_shouldThrowException() throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStoreWithDemoServerCertificate = KeyStore.getInstance("JKS");
        trustStoreWithDemoServerCertificate.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withTrustStore(trustStoreWithDemoServerCertificate)
                .build();

        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
}
