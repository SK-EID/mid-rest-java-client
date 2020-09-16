package ee.sk.mid;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ee.sk.mid.exception.MidSslException;

public class MidTrustUtil {

    public static KeyStore createTrustStore(List<String> trustedCertificates) {
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(null, "".toCharArray());
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            int i = 0;
            for (String sslCertificate : trustedCertificates) {
                Certificate certificate = factory.generateCertificate(new ByteArrayInputStream(sslCertificate.getBytes(UTF_8)));
                trustStore.setCertificateEntry("mid_api_ssl_cert" + (++i), certificate);
            }
            return trustStore;
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new MidSslException(e.getMessage());
        }
    }

    public static SSLContext createSslContext(KeyStore trustStore) {

        try {
            SSLContext sslTrustContext = SSLContext.getInstance("TLSv1.2");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(trustStore);
            sslTrustContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslTrustContext;
        }
        catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new MidSslException(e.getMessage());
        }

    }

}
