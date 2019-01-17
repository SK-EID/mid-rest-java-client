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

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VerificationCodeCalculatorTest {

    private static final String HACKERMAN_SHA256 = "HACKERMAN_SHA256";
    private static final String HACKERMAN_SHA384 = "HACKERMAN_SHA384";
    private static final String HACKERMAN_SHA512 = "HACKERMAN_SHA512";

    @Test
    public void calculateVerificationCode_withSHA256() {
        String verificationCode = calculateVerificationCode(getStringDigest(HACKERMAN_SHA256, HashType.SHA256));
        assertThat(verificationCode, is("5924"));
    }

    @Test
    public void calculateVerificationCode_withSHA384() {
        String verificationCode = calculateVerificationCode(getStringDigest(HACKERMAN_SHA384, HashType.SHA384));
        assertThat(verificationCode,is("7228"));
    }

    @Test
    public void calculateVerificationCode_withSHA512() {
        String verificationCode = calculateVerificationCode(getStringDigest(HACKERMAN_SHA512, HashType.SHA512));
        assertThat(verificationCode,is("3922"));
    }

    @Test
    public void calculateVerificationCode_withTooShortHash() {
        String verificationCode = calculateVerificationCode(new byte[] {1, 2, 3, 4});
        assertThat(verificationCode,is("0000"));
    }

    @Test
    public void calculateVerificationCode_withNullHash() {
        String verificationCode = calculateVerificationCode(null);
        assertThat(verificationCode,is("0000"));
    }

    private byte[] getStringDigest(String hash, HashType hashType) {
        return DigestCalculator.calculateDigest(hash.getBytes(StandardCharsets.UTF_8), hashType);
    }

    private String calculateVerificationCode(byte[] dummyDocumentHash) {
        return VerificationCodeCalculator.calculateMobileIdVerificationCode(dummyDocumentHash);
    }
}
