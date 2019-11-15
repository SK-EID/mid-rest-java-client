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

import java.io.Serializable;
import java.util.Arrays;

import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.apache.commons.codec.binary.Base64;

public class MidHashToSign implements Serializable {

    protected byte[] hash;
    protected MidHashType hashType;

    protected MidHashToSign(HashToSignBuilder builder) {
        this.hashType = builder.hashType;

        if (builder.dataToHash != null) {
            this.hash = builder.hashType.calculateDigest(builder.dataToHash);
        }
        else {
            this.hash = builder.hash;
        }
    }

    public byte[] getHash() {
        return hash.clone();
    }

    public String getHashInBase64() {
        return Base64.encodeBase64String(hash);
    }

    public MidHashType getHashType() {
        return hashType;
    }

    public String calculateVerificationCode() {
        return MidVerificationCodeCalculator.calculateMobileIdVerificationCode(hash);
    }

    public static HashToSignBuilder newBuilder() {
        return new HashToSignBuilder();
    }

    public static class HashToSignBuilder {
        protected byte[] hash;
        protected MidHashType hashType;
        private byte[] dataToHash;

        public HashToSignBuilder withDataToHash(byte[] dataToSign) {
            if (dataToSign == null || dataToSign.length == 0) {
                throw new MidMissingOrInvalidParameterException("Cannot pass empty dataToHash value");
            }

            this.dataToHash = Arrays.copyOf(dataToSign, dataToSign.length);
            return this;
        }

        public HashToSignBuilder withHash(byte[] hash) {
            if (hash == null || hash.length == 0) {
                throw new MidMissingOrInvalidParameterException("Cannot pass empty hash value");
            }
            this.hash = hash;

            return this;
        }

        public HashToSignBuilder withHashInBase64(String hashInBase64) {
            if (Base64.isBase64(hashInBase64)) {
                this.hash = Base64.decodeBase64(hashInBase64);
            } else {
                throw new MidMissingOrInvalidParameterException("hash is not valid Base64 encoded string");
            }
            return this;
        }

        public HashToSignBuilder withHashType(MidHashType hashType) {
            this.hashType = hashType;
            return this;
        }

        public MidHashToSign build() {
            validateFields();

            return new MidHashToSign(this);
        }

        void validateFields() {
            if (hashType == null) {
                throw new MidMissingOrInvalidParameterException("Missing hash type");
            }

            if (hash == null && dataToHash == null) {
                throw new MidMissingOrInvalidParameterException("Missing hash or dataToHash");
            }

            if (hash != null && dataToHash != null) {
                throw new IllegalArgumentException("You can only pass in either hash or dataToHash but not both");
            }

        }
    }
}
