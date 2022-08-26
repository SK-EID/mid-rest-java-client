package ee.sk.mid.mock;

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

import static ee.sk.mid.mock.TestData.AUTHENTICATION_SESSION_PATH;
import static ee.sk.mid.mock.TestData.DATA_TO_SIGN;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNATURE_SESSION_PATH;
import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import ee.sk.mid.MidAuthentication;
import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidClient;
import ee.sk.mid.MidHashToSign;
import ee.sk.mid.MidHashType;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.MidSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidCertificateRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.apache.commons.codec.binary.Base64;

public class MobileIdRestServiceRequestDummy {
    public static MidSignatureRequest createValidSignatureRequest() {
        return createSignatureRequest(DEMO_RELYING_PARTY_UUID, DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
    }
    public static MidSignatureRequest createSignatureRequest(String UUID, String name, String phoneNumber, String nationalIdentityNumber) {
        MidSignatureRequest request = new MidSignatureRequest();
        request.setRelyingPartyUUID(UUID);
        request.setRelyingPartyName(name);
        request.setPhoneNumber(phoneNumber);
        request.setNationalIdentityNumber(nationalIdentityNumber);
        request.setHash(calculateHashInBase64( MidHashType.SHA256));
        request.setHashType( MidHashType.SHA256);
        request.setLanguage( MidLanguage.EST);
        return request;
    }
    public static MidAuthenticationRequest createValidAuthenticationRequest() {
        return createAuthenticationRequest(DEMO_RELYING_PARTY_UUID, DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
    }
    public static MidAuthenticationRequest createAuthenticationRequest(String UUID, String name, String phoneNumber, String nationalIdentityNumber) {
        return MidAuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(UUID)
                .withRelyingPartyName(name)
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .withHashToSign( calculateMobileIdAuthenticationHash())
                .withLanguage( MidLanguage.EST)
                .build();
    }
    public static X509Certificate getCertificate(MidClient client) {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        assertCorrectCertificateRequestMade(request);

        MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);
        return client.createMobileIdCertificate(response);
    }
    public static MidSignature createValidSignature(MidClient client) {
        return createSignature(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
    public static MidSignature createSignature(MidClient client, String phoneNumber, String nationalIdentityNumber) {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .withHashToSign(hashToSign)
                .withLanguage( MidLanguage.EST)
                .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        return client.createMobileIdSignature(sessionStatus);
    }

    public static MidAuthentication createAndSendAuthentication(MidClient client, String phoneNumber, String nationalIdentityNumber, MidAuthenticationHashToSign authenticationHash) {
        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .withHashToSign(authenticationHash)
                .withLanguage( MidLanguage.EST)
                .build();

        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        return client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }
    public static MidAuthentication sendAuthentication(MidClient client, MidAuthenticationRequest request, MidAuthenticationHashToSign authenticationHash) {
        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        return client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }
    public static void makeValidCertificateRequest(MidClient client) {
        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
    public static void makeCertificateRequest(MidClient client, String phoneNumber, String nationalIdentityNumber) {
        MidCertificateRequest request = MidCertificateRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .build();

        MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);
        client.createMobileIdCertificate(response);
    }
    public static void makeValidSignatureRequest(MidClient client) {
        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
    public static void makeSignatureRequest(MidClient client, String phoneNumber, String nationalIdentityNumber) {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA256)
            .build();

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withRelyingPartyUUID(client.getRelyingPartyUUID())
            .withRelyingPartyName(client.getRelyingPartyName())
            .withPhoneNumber(phoneNumber)
            .withNationalIdentityNumber(nationalIdentityNumber)
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.EST)
            .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        client.createMobileIdSignature(sessionStatus);
    }
    public static void makeValidAuthenticationRequest(MidClient client) {
        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }
    public static void makeAuthenticationRequest(MidClient client, String phoneNumber, String nationalIdentityNumber) {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA512)
            .build();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withRelyingPartyUUID(client.getRelyingPartyUUID())
            .withRelyingPartyName(client.getRelyingPartyName())
            .withPhoneNumber(phoneNumber)
            .withNationalIdentityNumber(nationalIdentityNumber)
            .withHashToSign(authenticationHash)
            .withLanguage( MidLanguage.EST)
            .build();

        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    private static String calculateHashInBase64(MidHashType hashType) {
        byte[] digestValue = hashType.calculateDigest(DATA_TO_SIGN);
        return Base64.encodeBase64String(digestValue);
    }

    private static MidAuthenticationHashToSign calculateMobileIdAuthenticationHash() {
        byte[] digestValue = MidHashType.SHA512.calculateDigest(DATA_TO_SIGN);

        return MidAuthenticationHashToSign.newBuilder()
            .withHashInBase64(Base64.encodeBase64String(digestValue))
            .withHashType( MidHashType.SHA512)
            .build();
    }

    public static void assertCorrectCertificateRequestMade(MidCertificateRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
    }

    public static void assertCorrectSignatureRequestMade(MidSignatureRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
        assertThat(request.getHash(), is(SHA256_HASH_IN_BASE64));
        assertThat(request.getHashType(), is( MidHashType.SHA256));
        assertThat(request.getLanguage(), is( MidLanguage.EST));
    }

    public static void assertMadeCorrectAuthenticationRequesWithSHA256(MidAuthenticationRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
        assertThat(request.getHash(), is(SHA256_HASH_IN_BASE64));
        assertThat(request.getHashType(), is( MidHashType.SHA256));
        assertThat(request.getLanguage(), is( MidLanguage.EST));
    }
    public static void assertCorrectAuthenticationRequestMade(MidAuthenticationRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
        assertThat(request.getHash(), is(SHA512_HASH_IN_BASE64));
        assertThat(request.getHashType(), is( MidHashType.SHA512));
        assertThat(request.getLanguage(), is( MidLanguage.EST));
    }

    public static void assertCertificateCreated(X509Certificate certificate) {
        assertThat(certificate, is(notNullValue()));
    }

    public static void assertSignatureCreated(MidSignature signature) {
        assertThat(signature, is(notNullValue()));
        assertThat(signature.getValueInBase64(), not(org.hamcrest.text.IsEmptyString.isEmptyOrNullString()));
        assertThat(signature.getAlgorithmName(), not(emptyOrNullString()));
    }

    public static void assertAuthenticationCreated(MidAuthentication authentication, String expectedHashToSignInBase64) {
        assertThat(authentication, is(notNullValue()));
        assertThat(authentication.getResult(), not(emptyOrNullString()));
        assertThat(authentication.getSignatureValueInBase64(), not(emptyOrNullString()));
        assertThat(authentication.getCertificate(), is(notNullValue()));
        assertThat(authentication.getSignedHashInBase64(), is(expectedHashToSignInBase64));
        assertThat(authentication.getHashType(), is( MidHashType.SHA256));
    }

    public static void assertCanCallValidate(MidAuthentication authentication, KeyStore trustStore) {
        new MidAuthenticationResponseValidator(trustStore).validate(authentication);

    }
}
