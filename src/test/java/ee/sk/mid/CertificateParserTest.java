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

import ee.sk.mid.exception.TechnicalErrorException;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static ee.sk.mid.mock.TestData.AUTH_CERTIFICATE_EE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CertificateParserTest {

    @Test
    public void parseCertificate() throws CertificateEncodingException {
        X509Certificate x509Certificate = CertificateParser.parseX509Certificate(AUTH_CERTIFICATE_EE);
        assertThat(Base64.encodeBase64String(x509Certificate.getEncoded()), is(AUTH_CERTIFICATE_EE));
    }

    @Test(expected = TechnicalErrorException.class)
    public void parseInvalidCertificate_shouldThrowException() {
        CertificateParser.parseX509Certificate("HACKERMAN");
    }
}
