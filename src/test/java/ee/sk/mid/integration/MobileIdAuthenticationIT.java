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

import static ee.sk.mid.TestUtil.fileToX509Certificate;
import static ee.sk.mid.integration.MobileIdSSL_IT.DEMO_SERVER_CERT;
import static ee.sk.mid.integration.MobileIdSSL_IT.DEMO_SERVER_CERT_EXPIRATION_DATE;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertAuthenticationCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertCanCallValidate;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createAndSendAuthentication;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.makeAuthenticationRequest;
import static ee.sk.mid.mock.MobileIdRestServiceResponseDummy.assertAuthenticationPolled;
import static ee.sk.mid.mock.TestData.AUTHENTICATION_SESSION_PATH;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_ABSENT;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_DELIVERY_ERROR;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_EXPIRED_TRANSACTION;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_NOT_MID_CLIENT;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_SIGNATURE_HASH_MISMATCH;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_SIM_ERROR;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY_USER_CANCELLED;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static ee.sk.mid.mock.TestData.VALID_PHONE_ABSENT;
import static ee.sk.mid.mock.TestData.VALID_PHONE_DELIVERY_ERROR;
import static ee.sk.mid.mock.TestData.VALID_PHONE_EXPIRED_TRANSACTION;
import static ee.sk.mid.mock.TestData.VALID_PHONE_NOT_MID_CLIENT;
import static ee.sk.mid.mock.TestData.VALID_PHONE_SIGNATURE_HASH_MISMATCH;
import static ee.sk.mid.mock.TestData.VALID_PHONE_SIM_ERROR;
import static ee.sk.mid.mock.TestData.VALID_PHONE_USER_CANCELLED;
import static ee.sk.mid.mock.TestData.WRONG_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.WRONG_PHONE;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.WRONG_RELYING_PARTY_UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeTrue;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.Collections;

import ee.sk.mid.MidAuthentication;
import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidAuthenticationIdentity;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidAuthenticationResult;
import ee.sk.mid.MidClient;
import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidHashType;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdAuthenticationIT {

    private MidClient client;

    @Before
    public void setUp() {
        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withTrustedCertificates(DEMO_SERVER_CERT)
                .build();
    }

    @Test
    public void authenticate() throws Exception{
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        assertThat(authenticationHash.calculateVerificationCode().length(), is(4));
        assertThat(Integer.valueOf(authenticationHash.calculateVerificationCode()), allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(8192)));

        MidAuthentication authentication = createAndSendAuthentication(client, VALID_PHONE, VALID_NAT_IDENTITY, authenticationHash);

        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());

        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");

        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertAuthenticationResultValid(authenticationResult);
    }

    @Test
    public void authenticate_withDisplayText() {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(authenticationHash)
            .withLanguage( MidLanguage.EST)
            .withDisplayText("Log into internet banking system")
            .withDisplayTextFormat( MidDisplayTextFormat.GSM7)
            .build();

        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);

        MidAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
        assertCanCallValidate(authentication, client.getTrustStore());
    }

    @Test
    public void authenticate_withDisplayTextUcs2() {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(authenticationHash)
            .withLanguage( MidLanguage.RUS)
            .withDisplayText("Войти")
            .withDisplayTextFormat( MidDisplayTextFormat.UCS2)
            .build();

        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);

        MidAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
        assertCanCallValidate(authentication, client.getTrustStore());
    }

    @Test(expected = MidNotMidClientException.class)
    public void authenticate_whenNotMIDClient_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_NOT_MID_CLIENT, VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void authenticate_whenMSSPTransactionExpired_shouldThrowException() {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA512)
            .build();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE_EXPIRED_TRANSACTION)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY_EXPIRED_TRANSACTION)
            .withHashToSign(authenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    @Test(expected = MidUserCancellationException.class)
    public void authenticate_whenUserCancelled_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_USER_CANCELLED, VALID_NAT_IDENTITY_USER_CANCELLED);
    }

    @Test(expected = MidPhoneNotAvailableException.class)
    public void authenticate_whenSimNotAvailable_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_ABSENT, VALID_NAT_IDENTITY_ABSENT);
    }

    @Test(expected = MidDeliveryException.class)
    public void authenticate_whenDeliveryError_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_DELIVERY_ERROR, VALID_NAT_IDENTITY_DELIVERY_ERROR);
    }

    @Test(expected = MidDeliveryException.class)
    public void authenticate_whenInvalidCardResponse_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_SIM_ERROR, VALID_NAT_IDENTITY_SIM_ERROR);
    }

    @Test(expected = MidInvalidUserConfigurationException.class)
    public void authenticate_whenSignatureHashMismatch_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_SIGNATURE_HASH_MISMATCH, VALID_NAT_IDENTITY_SIGNATURE_HASH_MISMATCH);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withWrongPhoneNumber_shouldThrowException() {
        makeAuthenticationRequest(client, WRONG_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withWrongNationalIdentityNumber_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE, WRONG_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withWrongRelyingPartyUUID_shouldThrowException() {
        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(WRONG_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withWrongRelyingPartyName_shouldThrowException() {

        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(WRONG_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void authenticate_withUnknownRelyingPartyUUID_shouldThrowException()  throws Exception {
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

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MidUnauthorizedException.class)
    public void authenticate_withUnknownRelyingPartyName_shouldThrowException()  throws Exception {
        assumeTrue("demo_server_trusted_ssl_certs.jks needs to be updated with the new certificate of tsp.demo.sk.ee server", DEMO_SERVER_CERT_EXPIRATION_DATE.isAfter(LocalDate.now()));

        InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());


        MidClient client = MidClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .withTrustStore(trustStore)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    private void assertAuthenticationResultValid(MidAuthenticationResult authenticationResult) {
        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
        assertAuthenticationIdentityValid(authenticationResult.getAuthenticationIdentity());
    }

    private void assertAuthenticationIdentityValid(MidAuthenticationIdentity authenticationIdentity) {
        assertThat(authenticationIdentity.getGivenName(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getSurName(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getIdentityCode(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getCountry(), not(isEmptyOrNullString()));
    }
}
