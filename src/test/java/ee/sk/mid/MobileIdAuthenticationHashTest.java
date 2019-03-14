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

import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.security.SecureRandom;

import ee.sk.mid.exception.MissingOrInvalidParameterException;
import org.junit.Test;

public class MobileIdAuthenticationHashTest {

    @Test
    public void shouldGenerateRandomHashOfDefaultType_hasSHA256HashType() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfDefaultType();

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA256));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(44));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA256_hashHasCorrectTypeAndLength() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfType(HashType.SHA256);

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA256));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(44));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA384_hashHasCorrectTypeAndLength() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfType(HashType.SHA384);

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA384));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(64));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA512_hashHasCorrectTypeAndLength() {
        MobileIdAuthenticationHashToSign mobileIdAuthenticationHash = MobileIdAuthenticationHashToSign.generateRandomHashOfType(HashType.SHA512);

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA512));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(88));
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withHashType_withoutHash_shouldThrowException() {
        MobileIdAuthenticationHashToSign.newBuilder()
            .withHashType(HashType.SHA512)
            .build();
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withHashInBase64_withoutHashType_shouldThrowException() {
        MobileIdAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .build();
    }

    @Test(expected = MissingOrInvalidParameterException.class)
    public void authenticate_withHash_withoutHashType_shouldThrowException() {
        byte[] randomBytes = new byte[HashType.SHA256.getLengthInBytes()];
        new SecureRandom().nextBytes(randomBytes);

        MobileIdAuthenticationHashToSign.newBuilder()
            .withHash(randomBytes)
            .build();
    }

    @Test
    public void calculateVerificationCode_notNull() {
        MobileIdAuthenticationHashToSign authenticationHash = MobileIdAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType(HashType.SHA512)
            .build();

        assertThat(authenticationHash.calculateVerificationCode(), is(notNullValue()));
    }

}
