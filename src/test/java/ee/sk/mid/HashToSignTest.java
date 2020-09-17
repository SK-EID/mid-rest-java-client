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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class HashToSignTest {

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void setInvalidHashInBase64_shouldThrowException() {
        MidHashToSign.newBuilder()
            .withHashInBase64("!IsNotValidBase64String")
            .withHashType( MidHashType.SHA256)
            .build();
    }

    @Test
    public void calculateVerificationCode_withSHA256() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHash( MidHashType.SHA256.calculateDigest(HASH_TO_SIGN))
            .withHashType( MidHashType.SHA256)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("0104"));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA256() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHash( MidHashType.SHA256.calculateDigest(HASH_TO_SIGN))
            .withHashType( MidHashType.SHA256)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("0104"));
    }

    @Test
    public void calculateVerificationCode_withSHA384() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHash( MidHashType.SHA384.calculateDigest(HASH_TO_SIGN))
            .withHashType( MidHashType.SHA384)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("5781"));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA384() {

        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA384_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA384)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("5781"));
    }

    @Test
    public void calculateVerificationCode_withSHA512() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHash( MidHashType.SHA512.calculateDigest(HASH_TO_SIGN))
            .withHashType( MidHashType.SHA512)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("4667"));
    }

    @Test
    public void setHashInBase64_calculateVerificationCode_withSHA512() {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA512)
            .build();

        assertThat(hashToSign.calculateVerificationCode(), is("4667"));
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void checkFields_withoutHashType() {
        MidHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .build();
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void checkFields_withoutHash() {
        MidHashToSign.newBuilder()
            .withHashType( MidHashType.SHA512)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkFields_withHashAndDataToHash() {
        MidHashToSign.newBuilder()
            .withHashType( MidHashType.SHA512)
            .withHash( MidHashType.SHA512.calculateDigest(HASH_TO_SIGN))
            .withDataToHash(DATA_TO_SIGN)
            .build();
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void checkFields_withoutHash_withoutHashType_withoutData() {
        MidHashToSign.newBuilder().build();
    }


    @Test
    public void withDataToHash_withHashTypeSHA256() {
        MidHashToSign hashToSignFromData = MidHashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType( MidHashType.SHA256)
            .build();

        assertThat(hashToSignFromData.getHashType().getHashTypeName(), is("SHA256"));
        assertThat(hashToSignFromData.getHashInBase64(), is(SHA256_HASH_IN_BASE64));
        assertThat(hashToSignFromData.getHash(), is(Base64.decodeBase64(SHA256_HASH_IN_BASE64)));
        assertThat(hashToSignFromData.calculateVerificationCode(), is("0104"));
    }

    @Test
    public void withDataToHash_withHashTypeSHA384() {
        MidHashToSign hashToSignFromData = MidHashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType( MidHashType.SHA384)
            .build();

        assertThat(hashToSignFromData.getHashType().getHashTypeName(), is("SHA384"));
        assertThat(hashToSignFromData.getHashInBase64(), is(SHA384_HASH_IN_BASE64));
        assertThat(hashToSignFromData.getHash(), is(Base64.decodeBase64(SHA384_HASH_IN_BASE64)));
        assertThat(hashToSignFromData.calculateVerificationCode(), is("5781"));
    }

    @Test
    public void withDataToHash_withHashTypeSHA512() {
        MidHashToSign hashToSignFromData = MidHashToSign.newBuilder()
            .withDataToHash(DATA_TO_SIGN)
            .withHashType( MidHashType.SHA512)
            .build();

        assertThat(hashToSignFromData.getHashType().getHashTypeName(), is("SHA512"));
        assertThat(hashToSignFromData.getHashInBase64(), is(SHA512_HASH_IN_BASE64));
        assertThat(hashToSignFromData.getHash(), is(Base64.decodeBase64(SHA512_HASH_IN_BASE64)));
        assertThat(hashToSignFromData.calculateVerificationCode(), is("4667"));
    }
}
