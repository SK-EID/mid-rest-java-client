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

import static ee.sk.mid.MidSignatureVerifier.verifyWithECDSA;
import static ee.sk.mid.MidSignatureVerifier.verifyWithRSA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import ee.sk.mid.exception.MidInternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidAuthenticationResponseValidator {

    private static final Logger logger = LoggerFactory.getLogger( MidAuthenticationResponseValidator.class);

    private List<X509Certificate> trustedCACertificates = new ArrayList<>();

    public MidAuthenticationResponseValidator() {
        initializeTrustedCACertificatesFromKeyStore();
    }

    public MidAuthenticationResult validate(MidAuthentication authentication) {
        validateAuthentication(authentication);
        MidAuthenticationResult authenticationResult = new MidAuthenticationResult();
        MidAuthenticationIdentity identity = constructAuthenticationIdentity(authentication.getCertificate());
        authenticationResult.setAuthenticationIdentity(identity);
        if (!isResultOk(authentication)) {
            authenticationResult.setValid(false);
            authenticationResult.addError( MidAuthenticationError.INVALID_RESULT);
        }
        if (!isSignatureValid(authentication)) {
            authenticationResult.setValid(false);
            authenticationResult.addError( MidAuthenticationError.SIGNATURE_VERIFICATION_FAILURE);
        }
        if (!isCertificateValid(authentication.getCertificate())) {
            authenticationResult.setValid(false);
            authenticationResult.addError( MidAuthenticationError.CERTIFICATE_EXPIRED);
        }
        if (!isCertificateTrusted(authentication.getCertificate())) {
            authenticationResult.setValid(false);
            authenticationResult.addError(MidAuthenticationError.CERTIFICATE_NOT_TRUSTED);
        }
        return authenticationResult;
    }

    private void validateAuthentication(MidAuthentication authentication) {
        if (authentication.getCertificate() == null) {
            logger.error("Certificate is not present in the authentication response");
            throw new MidInternalErrorException("Certificate is not present in the authentication response");
        }
        if (authentication.getSignatureValueInBase64().isEmpty()) {
            logger.error("Signature is not present in the authentication response");
            throw new MidInternalErrorException("Signature is not present in the authentication response");
        }
        if (authentication.getHashType() == null) {
            logger.error("Hash type is not present in the authentication response");
            throw new MidInternalErrorException("Hash type is not present in the authentication response");
        }
    }

    MidAuthenticationIdentity constructAuthenticationIdentity(X509Certificate certificate) {
        MidAuthenticationIdentity identity = new MidAuthenticationIdentity();
        try {
            LdapName ln = new LdapName(certificate.getSubjectDN().getName());
            for (Rdn rdn : ln.getRdns()) {
                String type = rdn.getType().toUpperCase();
                switch (type) {
                    case "GIVENNAME":
                        identity.setGivenName(rdn.getValue().toString());
                        break;
                    case "SURNAME":
                        identity.setSurName(rdn.getValue().toString());
                        break;
                    case "SERIALNUMBER":
                        identity.setIdentityCode(getIdentityNumber(rdn.getValue().toString()));
                        break;
                    case "C":
                        identity.setCountry(rdn.getValue().toString());
                        break;
                    default:
                        break;
                }
            }
            return identity;
        } catch (InvalidNameException e) {
            logger.error("Error getting authentication identity from the certificate", e);
            throw new MidInternalErrorException("Error getting authentication identity from the certificate", e);
        }
    }

    private String getIdentityNumber(String serialNumber) {
        return serialNumber.replaceAll("^PNO[A-Z][A-Z]-", "");
    }

    private boolean isResultOk(MidAuthentication authentication) {
        return "OK".equalsIgnoreCase(authentication.getResult());
    }

    private boolean isSignatureValid(MidAuthentication authentication) {
        PublicKey publicKey = authentication.getCertificate().getPublicKey();

        switch (publicKey.getAlgorithm()) {
            case "RSA":
                return verifyWithRSA(publicKey, authentication);
            case "EC":
                return verifyWithECDSA(publicKey, authentication);
            default:
                throw new IllegalArgumentException("Unsupported algorithm " + publicKey.getAlgorithm());
        }
    }

    private void initializeTrustedCACertificatesFromKeyStore() {
        try (InputStream is = MidAuthenticationResponseValidator.class.getResourceAsStream("/trusted_certificates.jks")) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(is, "changeit".toCharArray());
            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                logger.error(certificate.toString());
                addTrustedCACertificate(certificate);
            }
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            logger.error("Error initializing trusted CA certificates", e);
            throw new MidInternalErrorException("Error initializing trusted CA certificates", e);
        }
    }

    public void addTrustedCACertificate(File certificateFile) throws IOException, CertificateException {
        addTrustedCACertificate(Files.readAllBytes(certificateFile.toPath()));
    }

    public void addTrustedCACertificate(byte[] certificateBytes) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCertificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        addTrustedCACertificate(caCertificate);
    }

    public void addTrustedCACertificate(X509Certificate certificate) {
        trustedCACertificates.add(certificate);
    }

    private boolean isCertificateValid(X509Certificate certificate) {
        return !certificate.getNotAfter().before(new Date());
    }

    private boolean isCertificateTrusted(X509Certificate certificate) {
        for (X509Certificate trustedCACertificate : trustedCACertificates) {
            try {
                certificate.verify(trustedCACertificate.getPublicKey());
                return true;
            } catch (SignatureException e) {
                continue;
            } catch (GeneralSecurityException e) {
                logger.warn("Error verifying signer's certificate: " + certificate.getSubjectDN() + " against CA certificate: " + trustedCACertificate.getSubjectDN(), e);
                continue;
            }
        }
        return false;
    }
}
