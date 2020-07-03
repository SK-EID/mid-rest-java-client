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
import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_LT;
import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_LV;
import static ee.sk.mid.mock.TestData.ECC_CERTIFICATE;
import static ee.sk.mid.mock.TestData.INVALID_SIGNATURE_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNED_ECC_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SIGNED_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.VALID_ECC_SIGNATURE_IN_BASE64;
import static ee.sk.mid.mock.TestData.VALID_SIGNATURE_IN_BASE64;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Date;

import ee.sk.mid.exception.MidInternalErrorException;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationResponseValidatorTest {

    private MidAuthenticationResponseValidator validator;

    @Before
    public void setUp() {
        validator = new MidAuthenticationResponseValidator();
    }

    @Test
    public void validate_whenRSA_shouldReturnValidAuthenticationResult() throws Exception{
        File caCertificateFile = new File(AuthenticationResponseValidatorTest.class.getResource("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt").getFile());
        validator.addTrustedCACertificate(caCertificateFile);

        MidAuthentication authentication = createValidMobileIdAuthentication();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenECC_shouldReturnValidAuthenticationResult() throws Exception{
        File caCertificateFile = new File(AuthenticationResponseValidatorTest.class.getResource("/trusted_certificates/TEST_of_ESTEID-SK_2011.pem.crt").getFile());
        validator.addTrustedCACertificate(caCertificateFile);

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


        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.getErrors(), hasItem(equalTo("Signer's certificate is not trusted")));
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

        File caCertificateFile = new File(AuthenticationResponseValidatorTest.class.getResource("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt").getFile());
        validator.addTrustedCACertificate(caCertificateFile);

        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenResultNotOk_shouldReturnInvalidAuthenticationResult() throws Exception {
        File caCertificateFile = new File(AuthenticationResponseValidatorTest.class.getResource("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt").getFile());
        validator.addTrustedCACertificate(caCertificateFile);

        MidAuthentication authentication = createMobileIdAuthenticationWithInvalidResult();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), contains("Response result verification failed"));
    }

    @Test
    public void validate_whenSignatureVerificationFails_shouldReturnInvalidAuthenticationResult() {
        MidAuthentication authentication = createMobileIdAuthenticationWithInvalidSignature();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), hasItem(equalTo("Signature verification failed")));
    }

    @Test
    public void validate_whenSignersCertExpired_shouldReturnInvalidAuthenticationResult() {
        MidAuthentication authentication = createMobileIdAuthenticationWithExpiredCertificate();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), hasItem(equalTo("Signer's certificate expired")));
    }

    @Test
    public void validate_shouldReturnValidIdentity() {
        MidAuthentication authentication = createValidMobileIdAuthentication();
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.getAuthenticationIdentity().getGivenName(), is("MARY ÄNN"));
        assertThat(authenticationResult.getAuthenticationIdentity().getSurName(), is("O’CONNEŽ-ŠUSLIK TESTNUMBER"));
        assertThat(authenticationResult.getAuthenticationIdentity().getIdentityCode(), is("60001019906"));
        assertThat(authenticationResult.getAuthenticationIdentity().getCountry(), is("EE"));
    }

    @Test(expected = MidInternalErrorException.class)
    public void validate_whenCertificateIsNull_shouldThrowException() {
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
        X509Certificate certificateSpy = spy( MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE));
        when(certificateSpy.getNotAfter()).thenReturn(DateUtils.addHours(new Date(), -1));

        return MidAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(certificateSpy)
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
        MidAuthenticationIdentity authenticationIdentity = validator.constructAuthenticationIdentity(certificateEe);

        assertThat(authenticationIdentity.getGivenName(), is("MARY ÄNN"));
        assertThat(authenticationIdentity.getSurName(), is("O’CONNEŽ-ŠUSLIK TESTNUMBER"));
        assertThat(authenticationIdentity.getIdentityCode(), is("60001019906"));
        assertThat(authenticationIdentity.getCountry(), is("EE"));
    }

    @Test
    public void constructAuthenticationIdentity_withLVCertificate() {
        X509Certificate certificateLv = MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_LV);
        MidAuthenticationIdentity authenticationIdentity = validator.constructAuthenticationIdentity(certificateLv);

        assertThat(authenticationIdentity.getGivenName(), is("FORENAME-010117-21234"));
        assertThat(authenticationIdentity.getSurName(), is("SURNAME-010117-21234"));
        assertThat(authenticationIdentity.getIdentityCode(), is("010117-21234"));
        assertThat(authenticationIdentity.getCountry(), is("LV"));
    }

    @Test
    public void constructAuthenticationIdentity_withLTCertificate() {
        X509Certificate certificateLt = MidCertificateParser.parseX509Certificate(AUTH_CERTIFICATE_LT);
        MidAuthenticationIdentity authenticationIdentity = validator.constructAuthenticationIdentity(certificateLt);

        assertThat(authenticationIdentity.getGivenName(), is("FORENAMEPNOLT-36009067968"));
        assertThat(authenticationIdentity.getSurName(), is("SURNAMEPNOLT-36009067968"));
        assertThat(authenticationIdentity.getIdentityCode(), is("36009067968"));
        assertThat(authenticationIdentity.getCountry(), is("LT"));
    }
}
