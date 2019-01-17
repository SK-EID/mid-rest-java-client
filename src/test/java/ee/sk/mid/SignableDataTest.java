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

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import static ee.sk.mid.mock.TestData.DATA_TO_SIGN;
import static ee.sk.mid.mock.TestData.SHA256_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA384_HASH_IN_BASE64;
import static ee.sk.mid.mock.TestData.SHA512_HASH_IN_BASE64;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SignableDataTest {

    private SignableData signableData;

    @Before
    public void setUp() {
        signableData = new SignableData(DATA_TO_SIGN);
    }

    @Test
    public void signableData_withSHA256() {
        signableData.setHashType(HashType.SHA256);
        assertThat(signableData.getHashType().getHashTypeName(), is("SHA256"));
        assertThat(signableData.calculateHashInBase64(), is(SHA256_HASH_IN_BASE64));
        assertThat(signableData.calculateHash(), is(Base64.decodeBase64(SHA256_HASH_IN_BASE64)));
        assertThat(signableData.calculateVerificationCode(), is("0108"));
    }

    @Test
    public void signableData_withSHA384() {
        signableData.setHashType(HashType.SHA384);
        assertThat(signableData.getHashType().getHashTypeName(), is("SHA384"));
        assertThat(signableData.calculateHashInBase64(), is(SHA384_HASH_IN_BASE64));
        assertThat(signableData.calculateHash(), is(Base64.decodeBase64(SHA384_HASH_IN_BASE64)));
        assertThat(signableData.calculateVerificationCode(), is("5775"));
    }

    @Test
    public void signableData_withSHA512() {
        signableData.setHashType(HashType.SHA512);
        assertThat(signableData.getHashType().getHashTypeName(), is("SHA512"));
        assertThat(signableData.calculateHashInBase64(), is(SHA512_HASH_IN_BASE64));
        assertThat(signableData.calculateHash(), is(Base64.decodeBase64(SHA512_HASH_IN_BASE64)));
        assertThat(signableData.calculateVerificationCode(), is("4677"));
    }
}
