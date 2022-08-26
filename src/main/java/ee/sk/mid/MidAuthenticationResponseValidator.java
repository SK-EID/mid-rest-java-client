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

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidAuthenticationResponseValidator {

    private static final Logger logger = LoggerFactory.getLogger( MidAuthenticationResponseValidator.class);

    private List<X509Certificate> trustedCACertificates /* = new ArrayList<>()*/;

    public MidAuthenticationResponseValidator(MidClient client) {

        KeyStore trustStore = client.getTrustStore();
        if (trustStore == null) {
            throw new MidMissingOrInvalidParameterException("You need to add a trust store to client");
        }

        initializeTrustedCACertificatesFromTrustStore(trustStore);
    }

    public MidAuthenticationResponseValidator(KeyStore trustStore) {
        if (trustStore == null) {
            throw new MidMissingOrInvalidParameterException("trustStore cannot be null");
        }
        initializeTrustedCACertificatesFromTrustStore(trustStore);
    }

    public MidAuthenticationResponseValidator(List<X509Certificate> trustedCACertificates) {
        this.trustedCACertificates = new ArrayList<>(trustedCACertificates);
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

    public static MidAuthenticationIdentity constructAuthenticationIdentity(X509Certificate certificate) {
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

    private static String getIdentityNumber(String serialNumber) {
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

    private void initializeTrustedCACertificatesFromTrustStore(KeyStore trustStore) {
        List<X509Certificate> certificatesFromTrustStore = new ArrayList<>();

        try {
            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                logger.debug("adding trusted ca certificate: {}", certificate);
                certificatesFromTrustStore.add(certificate);
            }
        } catch (KeyStoreException e) {
            logger.error("Error initializing trusted CA certificates", e);
            throw new MidInternalErrorException("Error initializing trusted CA certificates", e);
        }
        this.trustedCACertificates = certificatesFromTrustStore;
    }

    private boolean isCertificateValid(X509Certificate certificate) {
        return !certificate.getNotAfter().before(new Date());
    }

    private boolean isCertificateTrusted(X509Certificate certificate) {
        for (X509Certificate trustedCACertificate : trustedCACertificates) {
            try {
                certificate.verify(trustedCACertificate.getPublicKey());
                return true;
            } catch (GeneralSecurityException e) {
                logger.warn("Error verifying signer's certificate: " + certificate.getSubjectDN() + " against CA certificate: " + trustedCACertificate.getSubjectDN(), e);
            }
        }
        return false;
    }
}
