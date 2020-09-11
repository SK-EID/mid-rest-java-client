package ee.sk.mid.interactive;

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

import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.assertAuthenticationCreated;
import static ee.sk.mid.mock.MobileIdRestServiceRequestDummy.createAndSendAuthentication;
import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_NAME;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.Scanner;

import ee.sk.mid.MidAuthentication;
import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidAuthenticationIdentity;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidAuthenticationResult;
import ee.sk.mid.MidClient;
import ee.sk.mid.integration.MobileIdSSL_IT;


public class MobileIdAuthenticationInteractive {

    private static KeyStore keystoreWithDemoServerCertificate;

    static {
        try {
            InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
            keystoreWithDemoServerCertificate = KeyStore.getInstance("JKS");
            keystoreWithDemoServerCertificate.load(is, "changeit".toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MidClient client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withTrustStore(keystoreWithDemoServerCertificate)
                .build();


    public static void main(String[] args) {

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();
        String verificationCode = authenticationHash.calculateVerificationCode();

        Scanner scanner = new Scanner( System.in );

        System.out.println( "Enter your phone number (starting +37...: ");
        String phoneNr = scanner.nextLine();
        System.out.println( "phone = " + phoneNr );

        System.out.println( "Enter your ID code: ");
        String idCode = scanner.nextLine();
        System.out.println( "idCode = " + idCode);

        System.out.println("Verification code is " + verificationCode);

        MidAuthentication authentication = createAndSendAuthentication(client, phoneNr, idCode, authenticationHash);

        assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());

        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(client.getTrustStore());
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertAuthenticationResultValid(authenticationResult);
    }

    private static void assertAuthenticationResultValid(MidAuthenticationResult authenticationResult) {
        assertThat(authenticationResult.isValid(), is(false)); // When running against production, change to "true"

        if (!authenticationResult.getErrors().isEmpty()) {
            System.out.println(authenticationResult.getErrors().get(0));
        }

        assertThat(authenticationResult.getErrors().isEmpty(), is(false)); // When running against production, there should be no errors
        assertThat(authenticationResult.getErrors().get(0), is("Signer's certificate is not trusted"));
        assertThat(authenticationResult.getErrors().size(), is(1));
        assertAuthenticationIdentityValid(authenticationResult.getAuthenticationIdentity());
    }

    private static void assertAuthenticationIdentityValid(MidAuthenticationIdentity authenticationIdentity) {
        assertThat(authenticationIdentity.getGivenName(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getSurName(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getIdentityCode(), not(isEmptyOrNullString()));
        assertThat(authenticationIdentity.getCountry(), not(isEmptyOrNullString()));
    }
}
