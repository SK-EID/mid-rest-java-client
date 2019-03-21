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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class VerificationCodeCalculatorTest {

    private static final String HACKERMAN_SHA256 = "HACKERMAN_SHA256";
    private static final String HACKERMAN_SHA384 = "HACKERMAN_SHA384";
    private static final String HACKERMAN_SHA512 = "HACKERMAN_SHA512";

    @Test
    public void calculateVerificationCode_verifyInstructionInMidDocumentation() {
        String hashValueInExample = "2f665f6a6999e0ef0752e00ec9f453adf59d8cb6";
        System.out.println("Hash value: " + hashValueInExample);

        String startBinString = toBinaryString(hashValueInExample.substring(0, 2));
        String endBinString = toBinaryString(hashValueInExample.substring(hashValueInExample.length()-2));
        System.out.println("Binary representation of hash: " + startBinString + " ... " + endBinString);

        String sixBitsFromBeginning = startBinString.substring(0, 6);
        String sevenBitsFromEnd = endBinString.substring(1);

        String finalBinaryString = sixBitsFromBeginning + sevenBitsFromEnd;
        System.out.println("Verification code – binary value: " + finalBinaryString);

        int controlCodeDec = Integer.parseInt(finalBinaryString, 2);
        String controlCodeAs4digits = StringUtils.leftPad(controlCodeDec + "", 4, "0");
        System.out.println("Verification code – value as 4 digits: " + controlCodeAs4digits);

        assertThat(controlCodeAs4digits, is("1462"));

        byte[] hashValueBytes = DatatypeConverter.parseHexBinary(hashValueInExample);
        String codeWithCalculator = MidVerificationCodeCalculator.calculateMobileIdVerificationCode(hashValueBytes);

        assertThat(codeWithCalculator, is("1462"));
    }

    private static String toBinaryString(String oneByteHex) {
        long hexNumber = Long.parseLong(oneByteHex, 16);
        String binNumberString = Long.toString(hexNumber, 2);
        return StringUtils.leftPad(binNumberString, 8, "0");
    }

    @Test
    public void calculateVerificationCode_verifyExampleMidInDocumentation() {
        String hashValueInExample = "2f665f6a6999e0ef0752e00ec9f453adf59d8cb6";
        byte[] hashValueBytes = DatatypeConverter.parseHexBinary(hashValueInExample);
        String verificationCode = calculateVerificationCode(hashValueBytes);
        assertThat(verificationCode, is("1462"));
    }

    @Test
    public void calculateVerificationCode_withSHA256() {
        String verificationCode = calculateVerificationCode( MidHashType.SHA256.calculateDigest(HACKERMAN_SHA256.getBytes()));
        assertThat(verificationCode, is("6008"));
    }

    @Test
    public void calculateVerificationCode_withSHA384() {
        String verificationCode = calculateVerificationCode( MidHashType.SHA384.calculateDigest(HACKERMAN_SHA384.getBytes()));
        assertThat(verificationCode,is("7230"));
    }

    @Test
    public void calculateVerificationCode_withSHA512() {
        String verificationCode = calculateVerificationCode( MidHashType.SHA512.calculateDigest(HACKERMAN_SHA512.getBytes()));
        assertThat(verificationCode,is("3843"));
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

    private String calculateVerificationCode(byte[] dummyDocumentHash) {
        return MidVerificationCodeCalculator.calculateMobileIdVerificationCode(dummyDocumentHash);
    }
}
