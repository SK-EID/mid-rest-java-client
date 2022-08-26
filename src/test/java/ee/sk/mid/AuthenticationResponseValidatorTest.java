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

import static ee.sk.mid.TestUtil.fileToX509Certificate;
import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_EE;
import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_LT;
import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_LV;
import static ee.sk.mid.mock.TestData.ECC_CERTIFICATE;
import static ee.sk.mid.mock.TestData.INVALID_SIGNATURE_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNED_ECC_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNED_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.VALID_ECC_SIGNATURE_IN_BASE64;
import static ee.sk.mid.mock.TestData.VALID_SIGNATURE_IN_BASE64;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.security.cert.X509Certificate;
import java.util.Collections;

import ee.sk.mid.exception.MidInternalErrorException;
import org.junit.Test;

public class AuthenticationResponseValidatorTest {


    @Test
    public void validate_whenRSA_shouldReturnValidAuthenticationResult() throws Exception{
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));


        MidAuthentication authentication = createValidMobileIdAuthentication();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenECC_shouldReturnValidAuthenticationResult() throws Exception{
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2011.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = createMobileIdAuthenticationWithECC();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenCertificateNotTrusted_shouldReturnCertificateNotTrusted() throws Exception {
        MidAuthentication authentication = MidAuthentication.newBuilder()
                .withResult("ok")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate( MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();

        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2011.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.getErrors(), hasItem(equalTo("Certificate that was returned is not signed by CA that is configured as trusted in mid-rest-java-client")));
    }

    @Test
    public void validate_whenResultLowerCase_shouldReturnValidAuthenticationResult() throws Exception {
        MidAuthentication authentication = MidAuthentication.newBuilder()
                .withResult("ok")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate( MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();

        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenResultNotOk_shouldReturnInvalidAuthenticationResult() throws Exception {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = createMobileIdAuthenticationWithInvalidResult();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), contains("Response result verification failed"));
    }

    @Test
    public void validate_whenSignatureVerificationFails_shouldThrowException() {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = createMobileIdAuthenticationWithInvalidSignature();

        Exception e = null;
        try {
            validator.validate(authentication);
        }
        catch (MidInternalErrorException miee) {
            e = miee;
        }

        assertThat(e, is(notNullValue()));
    }

    @Test
    public void validate_whenSignersCertExpired_shouldReturnInvalidAuthenticationResult() {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = createMobileIdAuthenticationWithExpiredCertificate();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), hasItem(equalTo("Signer's certificate expired")));
    }

    @Test
    public void validate_shouldReturnValidIdentity() throws Exception {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = createValidMobileIdAuthentication();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.getAuthenticationIdentity().getGivenName(), is("MARY ÄNN"));
        assertThat(authenticationResult.getAuthenticationIdentity().getSurName(), is("O’CONNEŽ-ŠUSLIK TESTNUMBER"));
        assertThat(authenticationResult.getAuthenticationIdentity().getIdentityCode(), is("60001019906"));
        assertThat(authenticationResult.getAuthenticationIdentity().getCountry(), is("EE"));
    }

    @Test(expected = MidInternalErrorException.class)
    public void validate_whenCertificateIsNull_shouldThrowException() throws Exception {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = MidAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(null)
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();

        validator.validate(authentication);
    }

    @Test(expected = MidInternalErrorException.class)
    public void validate_whenSignatureIsEmpty_shouldThrowException() {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = MidAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64("")
                .withCertificate( MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();

        validator.validate(authentication);
    }

    @Test(expected = MidInternalErrorException.class)
    public void validate_whenHashTypeIsNull_shouldThrowException() {
        X509Certificate caCertificate = fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(Collections.singletonList(caCertificate));

        MidAuthentication authentication = MidAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate( MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(null)
                .build();

        validator.validate(authentication);
    }

    private MidAuthentication createValidMobileIdAuthentication() {
        return createMobileIdAuthentication("OK", VALID_SIGNATURE_IN_BASE64);
    }

    private MidAuthentication createMobileIdAuthenticationWithInvalidResult() {
        return createMobileIdAuthentication("NOT OK", VALID_SIGNATURE_IN_BASE64);
    }

    private MidAuthentication createMobileIdAuthenticationWithInvalidSignature() {
        return createMobileIdAuthentication("OK", INVALID_SIGNATURE_IN_BASE64);
    }

    private MidAuthentication createMobileIdAuthenticationWithExpiredCertificate() {
        String expiredCertificate = "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";
        X509Certificate expiredX509Certificate = MidCertificateParser.parseX509Certificate(expiredCertificate);

        return MidAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(expiredX509Certificate)
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();
    }

    private MidAuthentication createMobileIdAuthentication(String result, String signatureInBase64) {
        return MidAuthentication.newBuilder()
                .withResult(result)
                .withSignatureValueInBase64(signatureInBase64)
                .withCertificate( MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();
    }

    private MidAuthentication createMobileIdAuthenticationWithECC() {
        return MidAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_ECC_SIGNATURE_IN_BASE64)
                .withCertificate( MidCertificateParser.parseX509Certificate(ECC_CERTIFICATE))
                .withSignedHashInBase64(SIGNED_ECC_HASH_IN_BASE64)
                .withHashType( MidHashType.SHA512)
                .build();
    }

    @Test
    public void constructAuthenticationIdentity_withEECertificate() {
        X509Certificate certificateEe = MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE);
        MidAuthenticationIdentity authenticationIdentity = MidAuthenticationResponseValidator.constructAuthenticationIdentity(certificateEe);

        assertThat(authenticationIdentity.getGivenName(), is("MARY ÄNN"));
        assertThat(authenticationIdentity.getSurName(), is("O’CONNEŽ-ŠUSLIK TESTNUMBER"));
        assertThat(authenticationIdentity.getIdentityCode(), is("60001019906"));
        assertThat(authenticationIdentity.getCountry(), is("EE"));
    }

    @Test
    public void constructAuthenticationIdentity_withLVCertificate() {
        X509Certificate certificateLv = MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_LV);
        MidAuthenticationIdentity authenticationIdentity = MidAuthenticationResponseValidator.constructAuthenticationIdentity(certificateLv);

        assertThat(authenticationIdentity.getGivenName(), is("FORENAME-010117-21234"));
        assertThat(authenticationIdentity.getSurName(), is("SURNAME-010117-21234"));
        assertThat(authenticationIdentity.getIdentityCode(), is("010117-21234"));
        assertThat(authenticationIdentity.getCountry(), is("LV"));
    }

    @Test
    public void constructAuthenticationIdentity_withLTCertificate() {
        X509Certificate certificateLt = MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_LT);
        MidAuthenticationIdentity authenticationIdentity = MidAuthenticationResponseValidator.constructAuthenticationIdentity(certificateLt);

        assertThat(authenticationIdentity.getGivenName(), is("FORENAMEPNOLT-36009067968"));
        assertThat(authenticationIdentity.getSurName(), is("SURNAMEPNOLT-36009067968"));
        assertThat(authenticationIdentity.getIdentityCode(), is("36009067968"));
        assertThat(authenticationIdentity.getCountry(), is("LT"));
    }
}
