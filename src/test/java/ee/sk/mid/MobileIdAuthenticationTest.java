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

import ee.sk.mid.exception.InvalidBase64CharacterException;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;

import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_EE;
import static ee.sk.mid.mock.TestData.SIGNED_HASH_IN_BASE64;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MobileIdAuthenticationTest {

    @Test(expected = InvalidBase64CharacterException.class)
    public void setInvalidValueInBase64_shouldThrowException() {

        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withSignatureValueInBase64("!IsNotValidBase64Character")
                .build();

        authentication.getSignatureValue();
    }

    @Test
    public void getSignatureValueInBase64() {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64("SEFDS0VSTUFO")
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64(SIGNED_HASH_IN_BASE64)
                .withHashType(HashType.SHA512)
                .build();

        assertThat(authentication.getSignatureValueInBase64(), is("SEFDS0VSTUFO"));
    }

    @Test
    public void getSignatureValueInBytes() {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64("SEFDS0VSTUFO")
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64("K74MSLkafRuKZ1Ooucvh2xa4Q3nz+R/hFWIShN96SPHNcem+uQ6mFMe9kkJQqp5EaoZnJeaFpl310TmlzRgNyQ==")
                .withHashType(HashType.SHA512)
                .build();

        assertThat(authentication.getSignatureValue(), is("HACKERMAN".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void createMobileIdAuthentication() throws CertificateEncodingException {
        MobileIdAuthentication authentication = MobileIdAuthentication.newBuilder()
                .withResult("OK")
                .withSignatureValueInBase64("SEFDS0VSTUFO")
                .withCertificate(CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE))
                .withSignedHashInBase64("K74MSLkafRuKZ1Ooucvh2xa4Q3nz+R/hFWIShN96SPHNcem+uQ6mFMe9kkJQqp5EaoZnJeaFpl310TmlzRgNyQ==")
                .withHashType(HashType.SHA512)
                .withAlgorithmName(HashType.SHA512.getAlgorithmName())
                .build();

        assertThat(authentication.getResult(), is("OK"));
        assertThat(authentication.getSignatureValueInBase64(), is("SEFDS0VSTUFO"));
        assertThat(authentication.getAlgorithmName(), is("SHA-512"));
        assertThat(Base64.encodeBase64String(authentication.getCertificate().getEncoded()), is(AUTH_CERTIFICATE_EE));
        assertThat(authentication.getSignedHashInBase64(), is("K74MSLkafRuKZ1Ooucvh2xa4Q3nz+R/hFWIShN96SPHNcem+uQ6mFMe9kkJQqp5EaoZnJeaFpl310TmlzRgNyQ=="));
        assertThat(authentication.getHashType(), is(HashType.SHA512));
    }
}
