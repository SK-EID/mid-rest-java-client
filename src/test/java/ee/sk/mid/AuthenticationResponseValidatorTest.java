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

import ee.sk.mid.exception.TechnicalErrorException;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.cert.X509Certificate;
import java.util.Date;

import static ee.sk.mid.mock.TestData.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AuthenticationResponseValidatorTest {

    private AuthenticationResponseValidator validator;

    @Before
    public void setUp() {
        validator = new AuthenticationResponseValidator();
    }

    @Test
    public void validate_whenRSA_shouldReturnValidAuthenticationResult() {
        MobileIdAuthentication authentication = createValidMobileIdAuthentication();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenECC_shouldReturnValidAuthenticationResult() {
        MobileIdAuthentication authentication = createMobileIdAuthenticationWithECC();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenResultLowerCase_shouldReturnValidAuthenticationResult() {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("ok")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();

        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
    }

    @Test
    public void validate_whenResultNotOk_shouldReturnInvalidAuthenticationResult() {
        MobileIdAuthentication authentication = createMobileIdAuthenticationWithInvalidResult();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), contains("Response result verification failed"));
    }

    @Test
    public void validate_whenSignatureVerificationFails_shouldReturnInvalidAuthenticationResult() {
        MobileIdAuthentication authentication = createMobileIdAuthenticationWithInvalidSignature();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), contains("Signature verification failed"));
    }

    @Test
    public void validate_whenSignersCertExpired_shouldReturnInvalidAuthenticationResult() {
        MobileIdAuthentication authentication = createMobileIdAuthenticationWithExpiredCertificate();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(false));
        assertThat(authenticationResult.getErrors(), contains("Signer's certificate expired"));
    }

    @Test
    public void validate_shouldReturnValidIdentity() {
        MobileIdAuthentication authentication = createValidMobileIdAuthentication();
        MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.getAuthenticationIdentity().getGivenName(), is("MARY ÄNN"));
        assertThat(authenticationResult.getAuthenticationIdentity().getSurName(), is("O’CONNEŽ-ŠUSLIK TESTNUMBER"));
        assertThat(authenticationResult.getAuthenticationIdentity().getIdentityCode(), is("60001019906"));
        assertThat(authenticationResult.getAuthenticationIdentity().getCountry(), is("EE"));
    }

    @Test(expected = TechnicalErrorException.class)
    public void validate_whenCertificateIsNull_shouldThrowException() {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(null)
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();

        validator.validate(authentication);
    }

    @Test(expected = TechnicalErrorException.class)
    public void validate_whenSignatureIsEmpty_shouldThrowException() {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64("")
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();

        validator.validate(authentication);
    }

    @Test(expected = TechnicalErrorException.class)
    public void validate_whenHashTypeIsNull_shouldThrowException() {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(null)
                .build();

        validator.validate(authentication);
    }

    private MobileIdAuthentication createValidMobileIdAuthentication() {
        return createMobileIdAuthentication("OK", VALID_SIGNATURE_IN_BASE64);
    }

    private MobileIdAuthentication createMobileIdAuthenticationWithInvalidResult() {
        return createMobileIdAuthentication("NOT OK", VALID_SIGNATURE_IN_BASE64);
    }

    private MobileIdAuthentication createMobileIdAuthenticationWithInvalidSignature() {
        return createMobileIdAuthentication("OK", INVALID_SIGNATURE_IN_BASE64);
    }

    private MobileIdAuthentication createMobileIdAuthenticationWithExpiredCertificate() {
        X509Certificate certificateSpy = spy(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE));
        when(certificateSpy.getNotAfter()).thenReturn(DateUtils.addHours(new Date(), -1));

        return MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_SIGNATURE_IN_BASE64)
                .withCertificate(certificateSpy)
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();
    }

    private MobileIdAuthentication createMobileIdAuthentication(String result, String signatureInBase64) {
        return MobileIdAuthentication.newBuilder()
                .withResult(result)
                .withSignatureValueInBase64(signatureInBase64)
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();
    }

    private MobileIdAuthentication createMobileIdAuthenticationWithECC() {
        return MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64(VALID_ECC_SIGNATURE_IN_BASE64)
                .withCertificate(CertificateParser.parseX509Certificate(ECC_CERTIFICATE))
                .withSignedHashInBase64(SIGNED_ECC_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();
    }

    @Test
    public void constructAuthenticationIdentity_withEECertificate() {
        X509Certificate certificateEe = CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE);
        AuthenticationIdentity authenticationIdentity = validator.constructAuthenticationIdentity(certificateEe);

        assertThat(authenticationIdentity.getGivenName(), is("MARY ÄNN"));
        assertThat(authenticationIdentity.getSurName(), is("O’CONNEŽ-ŠUSLIK TESTNUMBER"));
        assertThat(authenticationIdentity.getIdentityCode(), is("60001019906"));
        assertThat(authenticationIdentity.getCountry(), is("EE"));
    }

    @Test
    public void constructAuthenticationIdentity_withLVCertificate() {
        X509Certificate certificateLv = CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_LV);
        AuthenticationIdentity authenticationIdentity = validator.constructAuthenticationIdentity(certificateLv);

        assertThat(authenticationIdentity.getGivenName(), is("FORENAME-010117-21234"));
        assertThat(authenticationIdentity.getSurName(), is("SURNAME-010117-21234"));
        assertThat(authenticationIdentity.getIdentityCode(), is("010117-21234"));
        assertThat(authenticationIdentity.getCountry(), is("LV"));
    }

    @Test
    public void constructAuthenticationIdentity_withLTCertificate() {
        X509Certificate certificateLt = CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_LT);
        AuthenticationIdentity authenticationIdentity = validator.constructAuthenticationIdentity(certificateLt);

        assertThat(authenticationIdentity.getGivenName(), is("FORENAMEPNOLT-36009067968"));
        assertThat(authenticationIdentity.getSurName(), is("SURNAMEPNOLT-36009067968"));
        assertThat(authenticationIdentity.getIdentityCode(), is("36009067968"));
        assertThat(authenticationIdentity.getCountry(), is("LT"));
    }
}
