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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class MobileIdAuthenticationHashTest {

    @Test
    public void shouldGenerateRandomHashOfDefaultType_hasSHA256HashType() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA256));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(44));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA256_hashHasCorrectTypeAndLength() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfType(HashType.SHA256);

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA256));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(44));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA384_hashHasCorrectTypeAndLength() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfType(HashType.SHA384);

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA384));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(64));
    }

    @Test
    public void shouldGenerateRandomHashOfType_SHA512_hashHasCorrectTypeAndLength() {
        MobileIdAuthenticationHash mobileIdAuthenticationHash = MobileIdAuthenticationHash.generateRandomHashOfType(HashType.SHA512);

        assertThat(mobileIdAuthenticationHash.getHashType(), is(HashType.SHA512));
        assertThat(mobileIdAuthenticationHash.getHashInBase64().length(), is(88));
    }

}
