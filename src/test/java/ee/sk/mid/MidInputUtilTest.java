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

import static ee.sk.mid.mock.TestData.VALID_NAT_IDENTITY;
import static ee.sk.mid.mock.TestData.VALID_PHONE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import ee.sk.mid.exception.InvalidPhoneNumberException;
import org.junit.Test;

public class MidInputUtilTest {

    @Test
    public void validateUserInput_validPhone_shouldRemoveSpaces()
    {
        String phoneNumber = MidInputUtil.getValidatedPhoneNumber(" +372 00000 766 ");

        assertThat(phoneNumber, is(equalTo("+37200000766")));
    }

    @Test(expected = InvalidPhoneNumberException.class)
    public void validateUserInput_invalidPhone_shouldThrowException()
    {
        MidInputUtil.getValidatedPhoneNumber("123");
    }


    @Test
    public void validateUserInput_withValidData()
    {
        MidInputUtil.getValidatedPhoneNumber(VALID_PHONE);
        MidInputUtil.getValidatedNationalIdentityNumber(VALID_NAT_IDENTITY);

    }


}
