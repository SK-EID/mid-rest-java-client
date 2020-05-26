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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class NationalIdentificationCodeValidatorTest {

    @Test
    public void shouldConsiderValid() {
        boolean isValid = MidNationalIdentificationCodeValidator.isValid("37605030299");

        assertThat(isValid, is(true));
    }

    @Test
    public void shouldConsiderInvalid_month13() {
        boolean isValid = MidNationalIdentificationCodeValidator.isValid("60013019909");

        assertThat(isValid, is(false));
    }
    
    @Test
    public void shouldConsiderInvalid_lengthMismatch() {
        boolean isValid = MidNationalIdentificationCodeValidator.isValid("376050302993");

        assertThat(isValid, is(false));
    }
    
    @Test
    public void shouldConsiderInvalid_notNumber() {
        boolean isValid = MidNationalIdentificationCodeValidator.isValid("3760503029y");

        assertThat(isValid, is(false));
    }

    @Test
    public void shouldCalculateControlDigit() {
        int controlDigit = MidNationalIdentificationCodeValidator.calculateControlDigit("3760503029");

        assertThat(controlDigit, is(9));
    }

    @Test
    public void shouldGetBirthDate__year1800() {
        LocalDate birthDate = MidNationalIdentificationCodeValidator.getBirthDate("17605030299");

        assertThat(birthDate, is(LocalDate.of(1876, 5, 3)));
    }

    @Test
    public void shouldGetBirthDate_year1900() {
        LocalDate birthDate = MidNationalIdentificationCodeValidator.getBirthDate("37605030299");

        assertThat(birthDate, is(LocalDate.of(1976, 5, 3)));
    }

    @Test
    public void shouldGetBirthDate__year2000() {
        LocalDate birthDate = MidNationalIdentificationCodeValidator.getBirthDate("60605030299");

        assertThat(birthDate, is(LocalDate.of(2006, 5, 3)));
    }
}
