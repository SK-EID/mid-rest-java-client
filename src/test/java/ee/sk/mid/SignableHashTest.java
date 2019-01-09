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
import org.junit.Before;
import org.junit.Test;

import static ee.sk.mid.mock.TestData.HASH_TO_SIGN;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA384_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SignableHashTest {

    private SignableHash hashToSign;

    @Before
    public void setUp() {
        hashToSign = new SignableHash();
    }

    @Test(expected = InvalidBase64CharacterException.class)
    public void setInvalidHashInBase64_shouldThrowException() {
        hashToSign.setHashInBase64("!IsNotValidBase64Character");
    }

    @Test
    public void calculateVerificationCode_withSHA256() {
        hashToSign.setHash(DigestCalculator.calculateDigest(HASH_TO_SIGN, HashType.SHA256));
        hashToSign.setHashType(HashType.SHA256);
        assertThat(hashToSign.calculateVerificationCode(), is("0108"));
        assertThat(hashToSign.areFieldsFilled(), is(true));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA256() {
        hashToSign.setHashInBase64(SHA256_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA256);
        assertThat(hashToSign.calculateVerificationCode(), is("0108"));
        assertThat(hashToSign.areFieldsFilled(), is(true));
    }

    @Test
    public void calculateVerificationCode_withSHA384() {
        hashToSign.setHash(DigestCalculator.calculateDigest(HASH_TO_SIGN, HashType.SHA384));
        hashToSign.setHashType(HashType.SHA384);
        assertThat(hashToSign.calculateVerificationCode(), is("5775"));
        assertThat(hashToSign.areFieldsFilled(), is(true));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA384() {
        hashToSign.setHashInBase64(SHA384_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA384);
        assertThat(hashToSign.calculateVerificationCode(), is("5775"));
        assertThat(hashToSign.areFieldsFilled(), is(true));
    }

    @Test
    public void calculateVerificationCode_withSHA512() {
        hashToSign.setHash(DigestCalculator.calculateDigest(HASH_TO_SIGN, HashType.SHA512));
        hashToSign.setHashType(HashType.SHA512);
        assertThat(hashToSign.calculateVerificationCode(), is("4677"));
        assertThat(hashToSign.areFieldsFilled(), is(true));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA512() {
        hashToSign.setHashInBase64(SHA512_HASH_IN_BASE64);
        hashToSign.setHashType(HashType.SHA512);
        assertThat(hashToSign.calculateVerificationCode(), is("4677"));
        assertThat(hashToSign.areFieldsFilled(), is(true));
    }

    @Test
    public void checkFields_withoutHashType() {
        hashToSign.setHashInBase64(SHA512_HASH_IN_BASE64);
        assertThat(hashToSign.areFieldsFilled(), is(false));
    }

    @Test
    public void checkFields_withoutHash() {
        hashToSign.setHashType(HashType.SHA512);
        assertThat(hashToSign.areFieldsFilled(), is(false));
    }

    @Test
    public void checkFields_withoutHash_andWithoutHashType() {
        assertThat(hashToSign.areFieldsFilled(), is(false));
    }
}
