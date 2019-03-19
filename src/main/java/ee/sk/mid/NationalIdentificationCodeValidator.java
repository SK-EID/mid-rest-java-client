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

import java.time.DateTimeException;
import java.time.LocalDate;

public class NationalIdentificationCodeValidator {
    private static int[] MULTIPLIERS1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
    private static int[] MULTIPLIERS2 = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

    public static boolean isValid(String idCode) {
        if (idCode == null || idCode.length() != 11) {
            return false;
        }
        int controlDigit = Integer.parseInt(idCode.substring(10));

        if (controlDigit != calculateControlDigit(idCode)) {
            return false;
        }

        try {
            getBirthDate(idCode);
        } catch (DateTimeException ex) {
            return false;
        }
        return true;
    }

    public static int calculateControlDigit(String idCode) {

        int mod = multiplyDigits(idCode, MULTIPLIERS1);
        if (mod == 10) {
            mod = multiplyDigits(idCode, MULTIPLIERS2);
        }
        return mod%10;
    }

    private static int multiplyDigits(String code, int[] multipliers) {
        int total = 0;

        for (int i = 0; i < 10; i++) {
            total += Integer.parseInt(code.charAt(i)+"") * multipliers[i];
        }
        return total % 11;
    }


    public static LocalDate getBirthDate(String idCode) {
        int year = Integer.parseInt(idCode.substring(1, 3));
        int month = Integer.parseInt(idCode.substring(3, 5));
        int dayOfMonth = Integer.parseInt(idCode.substring(5, 7));

        int firstNumber = Integer.parseInt(idCode.substring(0, 1));

        switch (firstNumber) {
            case 5:
            case 6:
                year += 100;
            case 3:
            case 4:
                year += 100;
            default:
                year += 1800;
        }
        return LocalDate.of(year, month, dayOfMonth);
    }
}
