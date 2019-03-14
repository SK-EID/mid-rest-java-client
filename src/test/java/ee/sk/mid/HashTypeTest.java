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

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class HashTypeTest {

    private static final String INPUT_STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
        + "Curabitur feugiat neque libero, ut placerat neque placerat eu. Proin magna nisl, venenatis porttitor rutrum vitae, placerat a lectus. "
        + "Suspendisse in ultricies diam. Duis neque lacus, imperdiet ut convallis non, dictum sit amet ipsum. Pellentesque eu nisi neque. "
        + "Sed dictum dui leo. Duis nec purus imperdiet, efficitur sapien non, auctor neque.";

    private static byte[] input = INPUT_STRING.getBytes(StandardCharsets.UTF_8);

    @Test
    public void calculateDigest_sha256() {
        byte[] digest = HashType.SHA256.calculateDigest(input);
        String hex = Hex.encodeHexString(digest);

        assertThat(hex, is("5b50b91c0f65f22a788f4ce3a2559f973982f2b784979a06a0e3af341700bbbb"));
    }

    @Test
    public void calculateDigest_sha384() {
        byte[] digest = HashType.SHA384.calculateDigest(input);
        String hex = Hex.encodeHexString(digest);

        assertThat(hex, is("df96084a8f69bb49d271239220d3ee4db0f6508c1c39366a411b924f678b015f14d72a2eed788c9161a8953bc73da24f"));
    }

    @Test
    public void calculateDigest_sha512() {
        byte[] digest = HashType.SHA512.calculateDigest(input);
        String hex = Hex.encodeHexString(digest);

        assertThat(hex, is("a3bf80a6b4fd9060485cb5a585efd3813a93152f3e8a030b77e52860787b2a79aef6eb248d774d602ed886be0e53f494bd98b2cb6ceef32fdae7532bdc56e63f"));
    }
}
