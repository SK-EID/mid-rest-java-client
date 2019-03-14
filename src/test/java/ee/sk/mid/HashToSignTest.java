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

import static ee.sk.mid.mock.TestData.DATA_TO_SIGN;
import static ee.sk.mid.mock.TestData.HASH_TO_SIGN;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA384_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import ee.sk.mid.exception.MissingOrInvalidParameterException;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class HashToSignTest {

    @Test(expected = MissingOrInvalidParameterException.class)
    public void setInvalidHashInBase64_shouldThrowException() {
        HashToSign.newBuilder()
            .withHashInBase64("!IsNotValidBase64String")
            .withHashType(HashType.SHA256)
            .build();
    }

    @Test
    public void calculateVerificationCode_withSHA256() {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHash(HashType.SHA256.calculateDigest(HASH_TO_SIGN))
            .withHashType(HashType.SHA256)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("0104"));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA256() {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHash(HashType.SHA256.calculateDigest(HASH_TO_SIGN))
            .withHashType(HashType.SHA256)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("0104"));
    }

    @Test
    public void calculateVerificationCode_withSHA384() {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHash(HashType.SHA384.calculateDigest(HASH_TO_SIGN))
            .withHashType(HashType.SHA384)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("5781"));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA384() {

        HashToSign hashToSign = HashToSign.newBuilder()
            .withHashInBase64(SHA384_HASH_IN_BASE64)
            .withHashType(HashType.SHA384)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("5781"));
    }

    @Test
    public void calculateVerificationCode_withSHA512() {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHash(HashType.SHA512.calculateDigest(HASH_TO_SIGN))
            .withHashType(HashType.SHA512)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("4667"));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA512() {
        HashToSign hashToSign = HashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType(HashType.SHA512)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("4667"));
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void checkFields_withoutHashType() {
        HashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .build();
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void checkFields_withoutHash() {
        HashToSign.newBuilder()
            .withHashType(HashType.SHA512)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkFields_withHashAndDataToHash() {
        HashToSign.newBuilder()
            .withHashType(HashType.SHA512)
            .withHash(HashType.SHA512.calculateDigest(HASH_TO_SIGN))
            .withDataToHash(DATA_TO_SIGN)
            .build();
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void checkFields_withoutHash_withoutHashType_withoutData() {
        HashToSign.newBuilder().build();
    }


    @Test
    public void withDataToHash_withHashTypeSHA256() {
        HashToSign hashToSignFromData = HashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType(HashType.SHA256)
            .build();

        assertThat(hashToSignFromData.getHashType().getHashTypeName(), is("SHA256"));
        assertThat(hashToSignFromData.getHashInBase64(), is(SHA256_HASH_IN_BASE64));
        assertThat(hashToSignFromData.getHash(), is(Base64.decodeBase64(SHA256_HASH_IN_BASE64)));
        assertThat(hashToSignFromData.calculateVerificationCode(), is("0104"));
    }

    @Test
    public void withDataToHash_withHashTypeSHA384() {
        HashToSign hashToSignFromData = HashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType(HashType.SHA384)
            .build();

        assertThat(hashToSignFromData.getHashType().getHashTypeName(), is("SHA384"));
        assertThat(hashToSignFromData.getHashInBase64(), is(SHA384_HASH_IN_BASE64));
        assertThat(hashToSignFromData.getHash(), is(Base64.decodeBase64(SHA384_HASH_IN_BASE64)));
        assertThat(hashToSignFromData.calculateVerificationCode(), is("5781"));
    }

    @Test
    public void withDataToHash_withHashTypeSHA512() {
        HashToSign hashToSignFromData = HashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType(HashType.SHA512)
            .build();

        assertThat(hashToSignFromData.getHashType().getHashTypeName(), is("SHA512"));
        assertThat(hashToSignFromData.getHashInBase64(), is(SHA512_HASH_IN_BASE64));
        assertThat(hashToSignFromData.getHash(), is(Base64.decodeBase64(SHA512_HASH_IN_BASE64)));
        assertThat(hashToSignFromData.calculateVerificationCode(), is("4667"));
    }
}
