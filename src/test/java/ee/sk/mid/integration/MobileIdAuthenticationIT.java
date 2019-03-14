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

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertAuthenticationCreated;
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import ee.sk.mid.AuthenticationIdentity;
import ee.sk.mid.AuthenticationResponseValidator;
import ee.sk.mid.DisplayTextFormat;
import ee.sk.mid.HashType;
import ee.sk.mid.Language;
import ee.sk.mid.MobileIdAuthentication;
import ee.sk.mid.MobileIdAuthenticationHashToSign;
import ee.sk.mid.MobileIdAuthenticationResult;
import ee.sk.mid.MobileIdClient;
import ee.sk.mid.categories.IntegrationTest;
import ee.sk.mid.exception.DeliveryException;
import ee.sk.mid.exception.InvalidUserConfigurationException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.NotMidClientException;
import ee.sk.mid.exception.PhoneNotAvailableException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.exception.UserCancellationException;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTest.class})
public class MobileIdAuthenticationIT {

    private MobileIdClient client;

    @Before
    public void setUp() {
        client = MobileIdClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();
    }

    @Test
    public void authenticate() {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        assertThat(authenticationHash.calculateVerificationCode().length(), is(4));
        assertThat(Integer.valueOf(authenticationHash.calculateVerificationCode()), allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(8192)));

        MobileIdAuthentication authentication = createAndSendAuthentication(client, VALID_PHONE, VALID_NAT_IDENTITY, authenticationHash);

        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());

        AuthenticationResponseValidator validator = new AuthenticationResponseValidator();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertAuthenticationResultValid(authenticationResult);
    }

    @Test
    public void authenticate_withDisplayText() {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(authenticationHash)
            .withLanguage(Language.EST)
            .withDisplayText("Log into internet banking system")
            .withDisplayTextFormat(DisplayTextFormat.GSM7)
            .build();

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);

        MobileIdAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
    }

    @Test
    public void authenticate_withDisplayTextUcs2() {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY)
            .withHashToSign(authenticationHash)
            .withLanguage(Language.RUS)
            .withDisplayText("Войти")
            .withDisplayTextFormat(DisplayTextFormat.UCS2)
            .build();

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        assertThat(response.getSessionID(), not(isEmptyOrNullString()));

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        assertAuthenticationPolled(sessionStatus);

        MobileIdAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());
    }

    @Test(expected = NotMidClientException.class)
    public void authenticate_whenNotMIDClient_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_NOT_MID_CLIENT, VALID_NAT_IDENTITY_NOT_MID_CLIENT);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void authenticate_whenMSSPTransactionExpired_shouldThrowException() {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType(HashType.SHA512)
            .build();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber(VALID_PHONE_EXPIRED_TRANSACTION)
            .withNationalIdentityNumber(VALID_NAT_IDENTITY_EXPIRED_TRANSACTION)
            .withHashToSign(authenticationHash)
            .withLanguage(Language.EST)
            .build();

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);
        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(), AUTHENTICATION_SESSION_PATH);
        client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }

    @Test(expected = UserCancellationException.class)
    public void authenticate_whenUserCancelled_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_USER_CANCELLED, VALID_NAT_IDENTITY_USER_CANCELLED);
    }

    @Test(expected = PhoneNotAvailableException.class)
    public void authenticate_whenSimNotAvailable_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_ABSENT, VALID_NAT_IDENTITY_ABSENT);
    }

    @Test(expected = DeliveryException.class)
    public void authenticate_whenDeliveryError_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_DELIVERY_ERROR, VALID_NAT_IDENTITY_DELIVERY_ERROR);
    }

    @Test(expected = DeliveryException.class)
    public void authenticate_whenInvalidCardResponse_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_SIM_ERROR, VALID_NAT_IDENTITY_SIM_ERROR);
    }

    @Test(expected = InvalidUserConfigurationException.class)
    public void authenticate_whenSignatureHashMismatch_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE_SIGNATURE_HASH_MISMATCH, VALID_NAT_IDENTITY_SIGNATURE_HASH_MISMATCH);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongPhoneNumber_shouldThrowException() {
        makeAuthenticationRequest(client, WRONG_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongNationalIdentityNumber_shouldThrowException() {
        makeAuthenticationRequest(client, VALID_PHONE, WRONG_NAT_IDENTITY);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongRelyingPartyUUID_shouldThrowException() {
        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(WRONG_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withWrongRelyingPartyName_shouldThrowException() {

        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(WRONG_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = UnauthorizedException.class)
    public void authenticate_withUnknownRelyingPartyUUID_shouldThrowException() {

        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(UNKNOWN_RELYING_PARTY_UUID)
            .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    @Test(expected = UnauthorizedException.class)
    public void authenticate_withUnknownRelyingPartyName_shouldThrowException() {

        MobileIdClient client = MobileIdClient.newBuilder()
            .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
            .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
            .withHostUrl(DEMO_HOST_URL)
            .build();

        makeAuthenticationRequest(client, VALID_PHONE, VALID_NAT_IDENTITY);
    }

    private void assertAuthenticationResultValid(MobileIdAuthenticationResult authenticationResult) {
        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
        assertAuthenticationIdentityValid(authenticationResult.getAuthenticationIdentity());
    }

    private void assertAuthenticationIdentityValid(AuthenticationIdentity authenticationIdentity) {
        assertThat(authenticationIdentity.getGivenName(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getSurName(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getIdentityCode(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getCountry(), not(isEmptyOrNullString()));
    }
}
