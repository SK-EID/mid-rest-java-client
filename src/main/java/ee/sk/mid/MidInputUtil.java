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

import ee.sk.mid.exception.MidInvalidNationalIdentityNumberException;
import ee.sk.mid.exception.MidInvalidPhoneNumberException;

public class MidInputUtil {


    public static boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+\\d{8,30}");
    }

    public static boolean isNationalIdentityNumberValid(String nationalIdentityNumber) {
        return MidNationalIdentificationCodeValidator.isValid(nationalIdentityNumber);
    }

    public static void validatePhoneNumber(String phoneNumber) {
        if (!isPhoneNumberValid(phoneNumber)) {
            throw new MidInvalidPhoneNumberException(phoneNumber);
        }
    }

    public static void validateNationalIdentityNumber(String nationalIdentityNumber) {
        if (!isNationalIdentityNumberValid(nationalIdentityNumber)) {
            throw new MidInvalidNationalIdentityNumberException(nationalIdentityNumber);
        }
    }

    public static String getValidatedPhoneNumber(String phoneNumberInput) {
        String cleanedPhoneNumber = phoneNumberInput.replaceAll("\\s", "");

        validatePhoneNumber(cleanedPhoneNumber);

        return cleanedPhoneNumber;
    }

    public static String getValidatedNationalIdentityNumber(String nationalIdentityNumber) {
        String cleanedNationalIdentityNumber = nationalIdentityNumber.replaceAll("\\s", "");

        validateNationalIdentityNumber(cleanedNationalIdentityNumber);

        return cleanedNationalIdentityNumber;
    }

}
