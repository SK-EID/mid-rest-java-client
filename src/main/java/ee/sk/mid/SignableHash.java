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

import java.io.Serializable;

import static org.apache.commons.codec.binary.Base64.isBase64;

public class SignableHash implements Serializable {

    private byte[] hash;
    private HashType hashType;

    void setHash(byte[] hash) {
        this.hash = hash;

    }

    public String getHashInBase64() {
        return Base64.encodeBase64String(hash);
    }

    public void setHashInBase64(String hashInBase64) throws InvalidBase64CharacterException {
        if (isBase64(hashInBase64)) {
            hash = Base64.decodeBase64(hashInBase64);
        } else {
            throw new InvalidBase64CharacterException();
        }
    }

    public HashType getHashType() {
        return hashType;
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

    public String calculateVerificationCode() {
        return VerificationCodeCalculator.calculateMobileIdVerificationCode(hash);
    }

    public boolean areFieldsFilled() {
        return hashType != null && hash != null && hash.length > 0;
    }
}
