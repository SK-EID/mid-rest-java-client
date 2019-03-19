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

import static ee.sk.mid.mock.TestData.VALID_SIGNATURE_IN_BASE64;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import ee.sk.mid.exception.DeliveryException;
import ee.sk.mid.exception.InvalidNationalIdentityNumberException;
import ee.sk.mid.exception.InvalidPhoneNumberException;
import ee.sk.mid.exception.InvalidUserConfigurationException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.NotMidClientException;
import ee.sk.mid.exception.PhoneNotAvailableException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.exception.UserCancellationException;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These tests contain snippets used in Readme.md
 * This is needed to guarantee that tests compile.
 * If anything changes in this class (ecept setUp method) the changes must be reflected in Readme.md
 * These are not real tests!
 */
public class ReadmeTest {

    private static final Logger logger = LoggerFactory.getLogger(CertificateParser.class);

    MobileIdClient client;

    MobileIdAuthentication authentication;

    MobileIdAuthenticationResult authenticationResult;

    @Before
    public void setUp() {
        client = MobileIdClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .build();

        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.newBuilder()
            .withHashType(HashType.SHA512)
            .withHashInBase64("XXX")
            .build();

        authentication = MobileIdAuthentication.newBuilder()
            .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
            .build();
        authenticationResult = new MobileIdAuthenticationResult();
    }

    @Test
    public void documentConfigureTheClient() {

        MobileIdClient client = MobileIdClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .build();
    }


    @Test
    public void documentClientWithLongPollingTimeout() {

        MobileIdClient client = MobileIdClient.newBuilder()
            // set hostUrl, hRelyingParty UUID & Name
            .withLongPollingTimeoutSeconds(60)
            .build();
    }

    @Test
    public void documentWithPollingSleepTimeoutSeconds() {

        MobileIdClient client = MobileIdClient.newBuilder()
            // set hostUrl, hRelyingParty UUID & Name
            .withPollingSleepTimeoutSeconds(2)
            .build();
    }


    @Test
    public void documentValidateUserInput() {


        try {
            String nationalIdentityNumber = MidInputUtil.getValidatedNationalIdentityNumber("<national identity number entered by user>");
            String phoneNumber = MidInputUtil.getValidatedPhoneNumber("<phone number entered by user>");
        }
        catch (InvalidNationalIdentityNumberException e) {
            logger.info("User entered invalid national identity number");
            // display error
        }
        catch (InvalidPhoneNumberException e) {
            logger.info("User entered invalid phone number");
            // display error
        }


    }


    @Test(expected = NotMidClientException.class)
    public void documentRetrieveCert() {

        CertificateRequest request = CertificateRequest.newBuilder()
            .withPhoneNumber("+37060000666")
            .withNationalIdentityNumber("60001019906")
            .build();

        CertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);

        client.createMobileIdCertificate(response);
    }


    @Test
    public void documentCreateFromExistingData() {
        byte[] data = "MY_DATA".getBytes(StandardCharsets.UTF_8);

        HashToSign hashToSign = HashToSign.newBuilder()
            .withDataToHash(data)
            .withHashType(HashType.SHA256)
            .build();

        String verificationCode = hashToSign.calculateVerificationCode();

        SignatureRequest request = SignatureRequest.newBuilder()
            .withPhoneNumber("+37200000766")
            .withNationalIdentityNumber("60001019906")
            .withHashToSign(hashToSign)
            .withLanguage(Language.ENG)
            .withDisplayText("Sign document?")
            .withDisplayTextFormat(DisplayTextFormat.GSM7)
            .build();

        SignatureResponse response = client.getMobileIdConnector().sign(request);

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
            "/signature/session/{sessionId}");

        MobileIdSignature signature = client.createMobileIdSignature(sessionStatus);
    }

    @Test
    public void documentCreateSignatureFromExistingHash() {

        HashToSign hashToSign = HashToSign.newBuilder()
            .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
            .withHashType(HashType.SHA256)
            .build();

    }

    @Test
    public void documentGetAuthenticationResponse() {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        String verificationCode = authenticationHash.calculateVerificationCode();

        AuthenticationRequest request = AuthenticationRequest.newBuilder()
            .withPhoneNumber("+37200000766")
            .withNationalIdentityNumber("60001019906")
            .withHashToSign(authenticationHash)
            .withLanguage(Language.ENG)
            .withDisplayText("Log into self-service?")
            .withDisplayTextFormat(DisplayTextFormat.GSM7)
            .build();

        AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);

        SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
            "/authentication/session/{sessionId}");

        MobileIdAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }


    @Test(expected = MidInternalErrorException.class)
    public void documentHowToVerifyAuthenticationResult() {
        AuthenticationResponseValidator validator = new AuthenticationResponseValidator();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void documentGettingErrors() {
        List<String> errors = authenticationResult.getErrors();

    }

    @Test(expected = NullPointerException.class)
    public void documentAuthenticationIdentityUsage() {
        AuthenticationIdentity authenticationIdentity = authenticationResult.getAuthenticationIdentity();
        String givenName = authenticationIdentity.getGivenName();
        String surName = authenticationIdentity.getSurName();
        String identityCode = authenticationIdentity.getIdentityCode();
        String country = authenticationIdentity.getCountry();
    }

    @Test
    public void documentCatchingErrors() {

        try {
            // perform authentication or signing
        }
        catch (UserCancellationException e) {
            logger.info("User cancelled operation from his/her phone.");
            // display error
        }
        catch (NotMidClientException e) {
            logger.info("User is not a MID client or user's certificates are revoked.");
            // display error
        }
        catch (MidSessionTimeoutException e) {
            logger.info("User did not type in PIN code or communication error.");
            // display error
        }
        catch (PhoneNotAvailableException e) {
            logger.info("Unable to reach phone/SIM card. User needs to check if phone has coverage.");
            // display error
        }
        catch (DeliveryException e) {
            logger.info("Error communicating with the phone/SIM card.");
            // display error
        }
        catch (InvalidUserConfigurationException e) {
            logger.info("Mobile-ID configuration on user's SIM card differs from what is configured on service provider's side. User needs to contact his/her mobile operator.");
            // display error
        }
        catch (MidSessionNotFoundException | MissingOrInvalidParameterException | UnauthorizedException e) {
            logger.error("Integrator-side error with MID integration or configuration", e);
            // navigate to error page
        }
        catch (MidInternalErrorException e) {
            logger.warn("MID service returned internal error that cannot be handled locally.");
            // navigate to error page
        }

    }

    @Test
    public void documentCatchingCertificateRequestErrors() {

        try {
            // request user signing certificates
        }
        catch (NotMidClientException e) {
            logger.info("User is not a MID client or user's certificates are revoked");
        }
        catch (MissingOrInvalidParameterException | UnauthorizedException e) {
            logger.error("Integrator-side error with MID integration (including insufficient input validation) or configuration", e);
        }
        catch (MidInternalErrorException e) {
            logger.warn("MID service returned internal error that cannot be handled locally.");
        }


    }



}
