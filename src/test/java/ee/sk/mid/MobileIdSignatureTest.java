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

import java.nio.charset.StandardCharsets;

import ee.sk.mid.exception.MidInternalErrorException;
import org.junit.Test;

public class MobileIdSignatureTest {

    @Test(expected = MidInternalErrorException.class)
    public void setInvalidValueInBase64_shouldThrowException() {

        MidSignature signature = MidSignature.newBuilder()
                .withValueInBase64("!IsNotValidBase64Character")
                .build();

        signature.getValue();
    }

    @Test
    public void getSignatureValueInBase64() {

        MidSignature signature = MidSignature.newBuilder()
                .withValueInBase64("SEFDS0VSTUFO")
                .build();

        assertThat(signature.getValueInBase64(), is("SEFDS0VSTUFO"));
    }

    @Test
    public void getSignatureValueInBytes() {
        MidSignature signature = MidSignature.newBuilder()
                .withValueInBase64("SEFDS0VSTUFO")
                .build();

        assertThat(signature.getValue(), is("HACKERMAN".getBytes(StandardCharsets.UTF_8)));
    }
}
