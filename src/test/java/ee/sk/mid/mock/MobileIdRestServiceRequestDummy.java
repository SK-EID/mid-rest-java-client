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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.security.cert.X509Certificate;

import ee.sk.mid.AuthenticationResponseValidator;
import ee.sk.mid.HashToSign;
import ee.sk.mid.HashType;
import ee.sk.mid.Language;
import ee.sk.mid.MobileIdAuthentication;
import ee.sk.mid.MobileIdAuthenticationHashToSign;
import ee.sk.mid.MobileIdClient;
import ee.sk.mid.MobileIdSignature;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;

public class MobileIdRestServiceRequestDummy {



    public static SignatureRequest createValidSignatureRequest() {
        return createSignatureRequest(DEMO_RELYING_PARTY_UUID, DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    public static SignatureRequest createSignatureRequest(String UUID, String name, String phoneNumber, String nationalIdentityNumber) {
        SignatureRequest request = new SignatureRequest();
        request.setRelyingPartyUUID(UUID);
        request.setRelyingPartyName(name);
        request.setPhoneNumber(phoneNumber);
        request.setNationalIdentityNumber(nationalIdentityNumber);
        request.setHash(calculateHashInBase64(HashType.SHA256));
        request.setHashType(HashType.SHA256);
        request.setLanguage(Language.EST);
        return request;
    }

    public static AuthenticationRequest createValidAuthenticationRequest() {
        return createAuthenticationRequest(DEMO_RELYING_PARTY_UUID, DEMO_RELYING_PARTY_NAME, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    public static AuthenticationRequest createAuthenticationRequest(String UUID, String name, String phoneNumber, String nationalIdentityNumber) {
        return AuthenticationRequest.newBuilder()
                .withRelyingPartyUUID(UUID)
                .withRelyingPartyName(name)
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .withHashToSign( calculateMobileIdAuthenticationHash())
                .withLanguage(Language.EST)
                .build();
    }

    public static X509Certificate getCertificate(MobileIdClient client) {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withPhoneNumber(VALID_PHONE)
                .withNationalIdentityNumber(VALID_NAT_IDENTITY)
                .build();

        assertCorrectCertificateRequestMade(request);

        CertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);
        return client.createMobileIdCertificate(response);
    }

    public static MobileIdSignature createValidSignature(MobileIdClient client) {
        return createSignature(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    public static MobileIdSignature createSignature(MobileIdClient client, String phoneNumber, String nationalIdentityNumber) {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType(HashType.SHA256)
            .build();

        SignatureRequest request = SignatureRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .withHashToSign(hashToSign)
                .withLanguage(Language.EST)
                .build();

        SignatureResponse response = client.getMobileIdConnector().sign(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        return client.createMobileIdSignature(sessionStatus);
    }

    public static MobileIdAuthentication createAndSendAuthentication(MobileIdClient client, String phoneNumber, String nationalIdentityNumber, MobileIdAuthenticationHashToSign authenticationHash) {
        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .withHashToSign(authenticationHash)
                .withLanguage(Language.EST)
                .build();

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        return client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    public static MobileIdAuthentication sendAuthentication(MobileIdClient client, AuthenticationRequest request, MobileIdAuthenticationHashToSign authenticationHash) {
        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        return client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    public static void makeValidCertificateRequest(MobileIdClient client) {
        makeCertificateRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    public static void makeCertificateRequest(MobileIdClient client, String phoneNumber, String nationalIdentityNumber) {
        CertificateRequest request = CertificateRequest.newBuilder()
                .withRelyingPartyUUID(client.getRelyingPartyUUID())
                .withRelyingPartyName(client.getRelyingPartyName())
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(nationalIdentityNumber)
                .build();

        CertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);
        client.createMobileIdCertificate(response);
    }

    public static void makeValidSignatureRequest(MobileIdClient client) {
        makeSignatureRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    public static void makeSignatureRequest(MobileIdClient client, String phoneNumber, String nationalIdentityNumber) {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHashInBase64(SHA256_HASH_IN_BASE64)
            .withHashType(HashType.SHA256)
            .build();

        SignatureRequest request = SignatureRequest.newBuilder()
            .withRelyingPartyUUID(client.getRelyingPartyUUID())
            .withRelyingPartyName(client.getRelyingPartyName())
            .withPhoneNumber(phoneNumber)
            .withNationalIdentityNumber(nationalIdentityNumber)
            .withHashToSign(hashToSign)
            .withLanguage(Language.EST)
            .build();

        SignatureResponse response = client.getMobileIdConnector().sign(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), SIGNATURE_SESSION_PATH);
        client.createMobileIdSignature(sessionStatus);
    }

    public static void makeValidAuthenticationRequest(MobileIdClient client) {
        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    public static void makeAuthenticationRequest(MobileIdClient client, String phoneNumber, String nationalIdentityNumber) {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType(HashType.SHA512)
            .build();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withRelyingPartyUUID(client.getRelyingPartyUUID())
            .withRelyingPartyName(client.getRelyingPartyName())
            .withPhoneNumber(phoneNumber)
            .withNationalIdentityNumber(nationalIdentityNumber)
            .withHashToSign(authenticationHash)
            .withLanguage(Language.EST)
            .build();

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    private static String calculateHashInBase64(HashType hashType) {
        byte[] digestValue = hashType.calculateDigest(DATA_TO_SIGN);
        return Base64.encodeBase64String(digestValue);
    }

    private static MobileIdAuthenticationHashToSign calculateMobileIdAuthenticationHash() {
        byte[] digestValue = HashType.SHA512.calculateDigest(DATA_TO_SIGN);

        return MobileIdAuthenticationHashToSign.newBuilder()
            .withHashInBase64(Base64.encodeBase64String(digestValue))
            .withHashType(HashType.SHA512)
            .build();
    }

    public static void assertCorrectCertificateRequestMade(CertificateRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
    }

    public static void assertCorrectSignatureRequestMade(SignatureRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
        assertThat(request.getHash(), is(SHA256_HASH_IN_BASE64));
        assertThat(request.getHashType(), is(HashType.SHA256));
        assertThat(request.getLanguage(), is(Language.EST));
    }

    public static void assertMadeCorrectAuthenticationRequesWithSHA256(AuthenticationRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
        assertThat(request.getHash(), is(SHA256_HASH_IN_BASE64));
        assertThat(request.getHashType(), is(HashType.SHA256));
        assertThat(request.getLanguage(), is(Language.EST));
    }
    public static void assertCorrectAuthenticationRequestMade(AuthenticationRequest request) {
        assertThat(request.getPhoneNumber(), is(VALID_PHONE));
        assertThat(request.getNationalIdentityNumber(), is(VALID_NAT_IDENTITY));
        assertThat(request.getHash(), is(SHA512_HASH_IN_BASE64));
        assertThat(request.getHashType(), is(HashType.SHA512));
        assertThat(request.getLanguage(), is(Language.EST));
    }

    public static void assertCertificateCreated(X509Certificate certificate) {
        assertThat(certificate, is(notNullValue()));
    }

    public static void assertSignatureCreated(MobileIdSignature signature) {
        assertThat(signature, is(notNullValue()));
        assertThat(signature.getValueInBase64(), not(isEmptyOrNullString()));
        assertThat(signature.getAlgorithmName(), not(isEmptyOrNullString()));
    }

    public static void assertAuthenticationCreated(MobileIdAuthentication authentication, String expectedHashToSignInBase64) {
        assertThat(authentication, is(notNullValue()));
        assertThat(authentication.getResult(), not(isEmptyOrNullString()));
        assertThat(authentication.getSignatureValueInBase64(), not(isEmptyOrNullString()));
        assertThat(authentication.getCertificate(), is(notNullValue()));
        assertThat(authentication.getSignedHashInBase64(), is(expectedHashToSignInBase64));
        assertThat(authentication.getHashType(), Matchers.is(HashType.SHA256));

        AuthenticationResponseValidator validator = new AuthenticationResponseValidator();
        validator.validate(authentication);
    }
}
