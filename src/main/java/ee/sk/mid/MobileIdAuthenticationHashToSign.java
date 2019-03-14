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

import static ee.sk.mid.HashType.SHA256;

import java.security.SecureRandom;

public class MobileIdAuthenticationHashToSign extends HashToSign {

    private MobileIdAuthenticationHashToSign(MobileIdAuthenticationHashToSignBuilder builder) {
        super(builder);
    }

    private static HashType DEFAULT_HASH_TYPE = SHA256;

    public static MobileIdAuthenticationHashToSign generateRandomHashOfDefaultType() {
        return generateRandomHashOfType(DEFAULT_HASH_TYPE);
    }

    public static MobileIdAuthenticationHashToSign generateRandomHashOfType(HashType hashType) {
        return MobileIdAuthenticationHashToSign.newBuilder()
            .withHash(getRandomBytes(hashType.getLengthInBytes()))
            .withHashType(hashType)
            .build();
    }

    public static MobileIdAuthenticationHashToSignBuilder newBuilder() {
        return new MobileIdAuthenticationHashToSignBuilder();
    }


    private static byte[] getRandomBytes(int lengthInBytes) {
        byte[] randomBytes = new byte[lengthInBytes];
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }

    public static class MobileIdAuthenticationHashToSignBuilder extends HashToSignBuilder {

        @Override
        public MobileIdAuthenticationHashToSignBuilder withHash(byte[] hash) {
            super.withHash(hash);
            return this;
        }

        @Override
        public MobileIdAuthenticationHashToSignBuilder withHashInBase64(String hashInBase64) {
            super.withHashInBase64(hashInBase64);
            return this;
        }

        @Override
        public MobileIdAuthenticationHashToSignBuilder withHashType(HashType hashType) {
            super.withHashType(hashType);
            return this;
        }

        @Override
        public MobileIdAuthenticationHashToSign build() {
            validateFields();
            return new MobileIdAuthenticationHashToSign(this);
        }
    }
}
