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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidInvalidNationalIdentityNumberException;
import ee.sk.mid.exception.MidInvalidPhoneNumberException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidServiceUnavailableException;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidSslException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.integration.MobileIdSsIT;
import ee.sk.mid.mock.TestData;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidCertificateRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These tests contain snippets used in Readme.md
 * This is needed to guarantee that tests compile.
 * If anything changes in this class (except setUp method) the changes must be reflected in Readme.md
 * These are not real tests!
 */
public class ReadmeTest {

    private static final Logger logger = LoggerFactory.getLogger( MidCertificateParser.class);

    MidClient client;

    MidAuthentication authentication;

    MidAuthenticationResult authenticationResult;

    KeyStore trustStore;

    @Before
    public void setUp() throws Exception {
        InputStream is = MobileIdSsIT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
        trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withTrustStore(trustStore)
            .build();

        authentication = MidAuthentication.newBuilder()
            .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
            .build();
        authenticationResult = new MidAuthenticationResult();
    }

    @Test
    public void documentConfigureTheClient() throws Exception {

        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withTrustStore(trustStore)
            .build();
    }

    @Test
    public void documentConfigureTheClientTrustStore() throws Exception {

        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
                .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
                .withRelyingPartyName("DEMO")
                .withHostUrl("https://tsp.demo.sk.ee/mid-api")
                .withTrustStore(trustStore)
                .build();
    }

    @Test
    public void documentConfigureTheClientWithTrustSslContext() throws Exception {

        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        SSLContext trustSslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);
        trustSslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        client = MidClient.newBuilder()
                .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
                .withRelyingPartyName("DEMO")
                .withHostUrl("https://tsp.demo.sk.ee/mid-api")
                .withTrustSslContext(trustSslContext)
                .build();
    }

    @Test(expected = MidSslException.class)
    public void documentConfigureTheClientWithTrustedCertificatesList() {

        client = MidClient.newBuilder()
                .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
                .withRelyingPartyName("DEMO")
                .withHostUrl("https://tsp.demo.sk.ee/mid-api")
                .withTrustedCertificates("PEM encoded cert 1", "PEM encoded cert 2")
                .build();
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void documentClientWithLongPollingTimeout() {

        MidClient client = MidClient.newBuilder()
            // set hostUrl, relyingPartyUUID, relyingPartyName and trustStore/trustSslContext
            .withLongPollingTimeoutSeconds(60)
            .build();
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void documentWithPollingSleepTimeoutSeconds() {

        MidClient client = MidClient.newBuilder()
            // set hostUrl, relyingPartyUUID, relyingPartyName and trustStore/trustSslContext
            .withPollingSleepTimeoutSeconds(2)
            .build();
    }

    @Test(expected = MidNotMidClientException.class)
    public void documentRetrieveCert() {

        MidCertificateRequest request = MidCertificateRequest.newBuilder()
            .withPhoneNumber("+37200000266")
            .withNationalIdentityNumber("60001019939")
            .build();

        MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);

        X509Certificate certificate = client.createMobileIdCertificate(response);
    }

    @Test
    public void documentCreateFromExistingData() {
        byte[] data = "MY_DATA".getBytes(StandardCharsets.UTF_8);

        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withDataToHash(data)
            .withHashType( MidHashType.SHA256)
            .build();

        String verificationCode = hashToSign.calculateVerificationCode();
        System.out.println("Verification code is " + verificationCode);

        MidSignatureRequest request = MidSignatureRequest.newBuilder()
            .withPhoneNumber("+37200000766")
            .withNationalIdentityNumber("60001019906")
            .withHashToSign(hashToSign)
            .withLanguage( MidLanguage.ENG)
            .withDisplayText("Sign document?")
            .withDisplayTextFormat( MidDisplayTextFormat.GSM7)
            .build();

        MidSignatureResponse response = client.getMobileIdConnector().sign(request);

        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
            "/signature/session/{sessionId}");

        MidSignature signature = client.createMobileIdSignature(sessionStatus);
        System.out.println("Base64 value of created signature: " + signature.getValueInBase64());
    }

    @Test
    public void documentCreateSignatureFromExistingHash() {

        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
            .withHashType( MidHashType.SHA256)
            .build();

    }

    @Test
    public void documentGetAuthenticationResponse() {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        String verificationCode = authenticationHash.calculateVerificationCode();
        System.out.println("Verification code is " + verificationCode);

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber("+37200000766")
            .withNationalIdentityNumber("60001019906")
            .withHashToSign(authenticationHash)
            .withLanguage( MidLanguage.ENG)
            .withDisplayText("Log into self-service?")
            .withDisplayTextFormat( MidDisplayTextFormat.GSM7)
            .build();

        MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);

        MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
            "/authentication/session/{sessionId}");

        MidAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
    }


    @Test(expected = MidInternalErrorException.class)
    public void documentHowToVerifyAuthenticationResult() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(trustStore);
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void documentGettingErrors() {
        System.out.println("Following errors occurred: " + authenticationResult.getErrors());
    }

    @Test(expected = NullPointerException.class)
    public void documentAuthenticationIdentityUsage() {
        MidAuthenticationIdentity authenticationIdentity = authenticationResult.getAuthenticationIdentity();
        String givenName = authenticationIdentity.getGivenName();
        String surname = authenticationIdentity.getSurName();
        String identityCode = authenticationIdentity.getIdentityCode();
        String country = authenticationIdentity.getCountry();

        System.out.printf("Welcome %s %s (#%s) from %s" , givenName, surname, identityCode, country);
    }

    @SuppressWarnings("EmptyTryBlock")
    @Test
    public void documentCatchingErrors() {

        try {
            // perform authentication or signing
        }
        catch (MidUserCancellationException e) {
            logger.info("User cancelled operation from his/her phone.");
            // display error
        }
        catch (MidNotMidClientException e) {
            logger.info("User is not a MID client or user's certificates are revoked.");
            // display error
        }
        catch (MidSessionTimeoutException e) {
            logger.info("User did not type in PIN code or communication error.");
            // display error
        }
        catch (MidPhoneNotAvailableException e) {
            logger.info("Unable to reach phone/SIM card. User needs to check if phone has coverage.");
            // display error
        }
        catch (MidDeliveryException e) {
            logger.info("Error communicating with the phone/SIM card.");
            // display error
        }
        catch (MidInvalidUserConfigurationException e) {
            logger.info("Mobile-ID configuration on user's SIM card differs from what is configured on service provider's side. User needs to contact his/her mobile operator.");
            // display error
        }
        catch (MidSessionNotFoundException | MidMissingOrInvalidParameterException | MidUnauthorizedException | MidSslException e) {
            logger.error("Integrator-side error with MID integration or configuration", e);
            // navigate to error page
        }
        catch (MidServiceUnavailableException e) {
            logger.warn("MID service is currently unavailable. Please try again later.");
            // navigate to error page
        }
        catch (MidInternalErrorException e) {
            logger.warn("MID service returned internal error that cannot be handled locally.");
            // navigate to error page
        }

    }

    @SuppressWarnings("EmptyTryBlock")
    @Test
    public void documentCatchingCertificateRequestErrors() {

        try {
            // request user signing certificates
        }
        catch (MidNotMidClientException e) {
            logger.info("User is not a MID client or user's certificates are revoked");
        }
        catch (MidMissingOrInvalidParameterException | MidUnauthorizedException e) {
            logger.error("Integrator-side error with MID integration (including insufficient input validation) or configuration", e);
        }
        catch (MidInternalErrorException e) {
            logger.warn("MID service returned internal error that cannot be handled locally.");
        }
    }

    @Test
    public void documentValidateUserInput() {

        try {
            String nationalIdentityNumber = MidInputUtil.getValidatedNationalIdentityNumber("<national identity number entered by user>");
            String phoneNumber = MidInputUtil.getValidatedPhoneNumber("<phone number entered by user>");
        }
        catch (MidInvalidNationalIdentityNumberException e) {
            logger.info("User entered invalid national identity number");
            // display error
        }
        catch (MidInvalidPhoneNumberException e) {
            logger.info("User entered invalid phone number");
            // display error
        }


    }

    @Test
    @Ignore("you need to run a proxy to run this test")
    public void document_setProxy_withJbossRestEasy() throws Exception {
        // in order to run this test you can set up a proxy server locally
        //docker run -d --name squid-container -e TZ=UTC -p 3128:3128 ubuntu/squid:5.2-22.04_beta


        // CODE EXAMPLE STARTS HERE
        org.jboss.resteasy.client.jaxrs.ResteasyClient resteasyClient =
             new org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl()
             .defaultProxy("127.0.0.1", 3128, "http")
             .build();
        MidClient client = MidClient.newBuilder()
             .withHostUrl("https://tsp.demo.sk.ee/mid-api")
             .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
             .withRelyingPartyName("DEMO")
             .withConfiguredClient(resteasyClient)
             .withTrustStore(trustStore)
             .build();

        // CODE EXAMPLE ENDS HERE

        MidCertificateRequest request = MidCertificateRequest.newBuilder()
             .withPhoneNumber("+37200000766")
             .withNationalIdentityNumber("60001019906")
             .build();

        MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);

        X509Certificate certificate = client.createMobileIdCertificate(response);

        MidAuthenticationIdentity identity = MidAuthenticationResponseValidator.constructAuthenticationIdentity(certificate);

        assertThat(identity.getGivenName(), is("MARY ÄNN"));
    }

    @Test
    @Ignore("you need to run a proxy to run this test")
    public void document_setProxy_withJersey() throws Exception {
        // in order to run this test you first have to set up a proxy server locally
        //docker run -d --name squid-container -e TZ=UTC -p 3128:3128 ubuntu/squid:5.2-22.04_beta

        org.glassfish.jersey.client.ClientConfig clientConfig =
             new org.glassfish.jersey.client.ClientConfig();
        clientConfig.property(ClientProperties.PROXY_URI, "127.0.0.1:3128");
        MidClient client = MidClient.newBuilder()
             .withHostUrl("https://tsp.demo.sk.ee/mid-api")
             .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
             .withRelyingPartyName("DEMO")
             .withTrustStore(trustStore)
             .withNetworkConnectionConfig(clientConfig)
             .build();

        // CODE EXAMPLE ENDS HERE

        MidCertificateRequest request = MidCertificateRequest.newBuilder()
             .withPhoneNumber("+37200000766")
             .withNationalIdentityNumber("60001019906")
             .build();

        MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);

        X509Certificate certificate = client.createMobileIdCertificate(response);

        MidAuthenticationIdentity identity = MidAuthenticationResponseValidator.constructAuthenticationIdentity(certificate);

        assertThat(identity.getGivenName(), is("MARY ÄNN"));

    }

}
