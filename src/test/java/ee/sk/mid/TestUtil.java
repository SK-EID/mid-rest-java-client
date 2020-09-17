package ee.sk.mid;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class TestUtil {

    public static X509Certificate fileToX509Certificate(String filePath) {
        try {
            File caCertificateFile = new File(TestUtil.class.getResource(filePath).getFile());
            byte[] certificateBytes = Files.readAllBytes(caCertificateFile.toPath());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
