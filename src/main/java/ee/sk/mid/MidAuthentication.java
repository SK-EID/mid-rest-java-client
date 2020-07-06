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
import java.security.cert.X509Certificate;

import ee.sk.mid.exception.MidInternalErrorException;
import org.apache.commons.codec.binary.Base64;

public class MidAuthentication implements Serializable {

    private static final Long serialVersionUID = 1L;

    private String result;
    private String signedHashInBase64;
    private MidHashType hashType;
    private String signatureValueInBase64;
    private String algorithmName;
    private X509Certificate certificate;

    private MidAuthentication(MobileIdAuthenticationBuilder builder) {
        this.result = builder.result;
        this.signedHashInBase64 = builder.signedHashInBase64;
        this.hashType = builder.hashType;
        this.signatureValueInBase64 = builder.signatureValueInBase64;
        this.algorithmName = builder.algorithmName;
        this.certificate = builder.certificate;

        if (!Base64.isBase64(signatureValueInBase64)) {
            throw new MidInternalErrorException("Returned signature value is not a valid Base64 string.");
        }
    }

    public byte[] getSignatureValue() {
        return Base64.decodeBase64(signatureValueInBase64);
    }

    public String getResult() {
        return result;
    }

    public String getSignatureValueInBase64() {
        return signatureValueInBase64;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public String getSignedHashInBase64() {
        return signedHashInBase64;
    }

    public MidHashType getHashType() {
        return hashType;
    }

    public static MobileIdAuthenticationBuilder newBuilder() {
        return new MobileIdAuthenticationBuilder();
    }

    public static class MobileIdAuthenticationBuilder {
        private String result;
        private String signedHashInBase64;
        private MidHashType hashType;
        private String signatureValueInBase64;
        private String algorithmName;
        private X509Certificate certificate;

        private MobileIdAuthenticationBuilder() {
        }

        public MobileIdAuthenticationBuilder withResult(String result) {
            this.result = result;
            return this;
        }

        public MobileIdAuthenticationBuilder withSignedHashInBase64(String signedHashInBase64) {
            this.signedHashInBase64 = signedHashInBase64;
            return this;
        }

        public MobileIdAuthenticationBuilder withHashType(MidHashType hashType) {
            this.hashType = hashType;
            return this;
        }

        public MobileIdAuthenticationBuilder withSignatureValueInBase64(String signatureValueInBase64) {
            this.signatureValueInBase64 = signatureValueInBase64;
            return this;
        }

        public MobileIdAuthenticationBuilder withAlgorithmName(String algorithmName) {
            this.algorithmName = algorithmName;
            return this;
        }

        public MobileIdAuthenticationBuilder withCertificate(X509Certificate certificate) {
            this.certificate = certificate;
            return this;
        }

        public MidAuthentication build() {
            return new MidAuthentication(this);
        }
    }

}
