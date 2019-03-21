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

import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.junit.Test;

public class MobileIdAuthenticationHashTest {

    @Test
    public void shouldGenerateRandomHashOfDefaultType_hasSHA256HashType() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

        assertThat(mobileIdAuthenticationHash.getHashType(), is( MidHashType.SHA256));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(44));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA256_hashHasCorrectTypeAndLength() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfType( MidHashType.SHA256);

        assertThat(mobileIdAuthenticationHash.getHashType(), is( MidHashType.SHA256));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(44));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA384_hashHasCorrectTypeAndLength() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfType( MidHashType.SHA384);

        assertThat(mobileIdAuthenticationHash.getHashType(), is( MidHashType.SHA384));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(64));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA512_hashHasCorrectTypeAndLength() {
        MidAuthenticationHashToSign mobileIdAuthenticationHash = MidAuthenticationHashToSign.generateRandomHashOfType( MidHashType.SHA512);

        assertThat(mobileIdAuthenticationHash.getHashType(), is( MidHashType.SHA512));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(88));
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withHashType_withoutHash_shouldThrowException() {
        MidAuthenticationHashToSign.newBuilder()
            .withHashType( MidHashType.SHA512)
            .build();
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withHashInBase64_withoutHashType_shouldThrowException() {
        MidAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .build();
    }

    @Test(expected = MidMissingOrInvalidParameterException.class)
    public void authenticate_withHash_withoutHashType_shouldThrowException() {
        byte[] randomBytes = new byte[MidHashType.SHA256.getLengthInBytes()];
        new SecureRandom().nextBytes(randomBytes);

        MidAuthenticationHashToSign.newBuilder()
            .withHash(randomBytes)
            .build();
    }

    @Test
    public void calculateVerificationCode_notNull() {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
            .withHashInBase64(SHA512_HASH_IN_BASE64)
            .withHashType( MidHashType.SHA512)
            .build();

        assertThat(authenticationHash.calculateVerificationCode(), is(notNullValue()));
    }

}
