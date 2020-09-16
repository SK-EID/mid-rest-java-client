[![Build Status](https://api.travis-ci.com/SK-EID/mid-rest-java-client.svg?branch=master)](https://travis-ci.com/SK-EID/mid-rest-java-client)
[![Coverage Status](https://img.shields.io/codecov/c/github/SK-EID/mid-rest-java-client.svg)](https://codecov.io/gh/SK-EID/mid-rest-java-client)
[![License: MIT](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ee.sk.mid/mid-rest-java-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ee.sk.mid/mid-rest-java-client)

# Mobile-ID (MID) Java client
Mobile-ID Java client is a Java library that can be used for easy integration with MID REST interface (https://github.com/SK-EID/MID) of the [Mobile-ID](https://www.id.ee/index.php?id=36809).

* [Features](#features)
* [Requirements](#requirements)
* [Adding as a dependency](#adding-as-a-dependency)
* [Usage](#usage)
* [License](#license)

# Features
* Simple interface for user authentication
* Simple interface for digital signature services

# Requirements
* Java 1.8 
* Access to Mobile-ID demo environment (to run integration tests)

# Adding as a dependency
You can use the library as a dependency from the Maven Central (http://mvnrepository.com/artifact/ee.sk.mid/mid-rest-java-client)

## Maven configuration

```xml
<dependency>
    <groupId>ee.sk.mid</groupId>
    <artifactId>mid-rest-java-client</artifactId>
    <version>INSERT_VERSION_HERE</version>
</dependency>
```

## Gradle configuration

```
compile group: 'ee.sk.mid', name: 'mid-rest-java-client', version: 'INSERT_VERSION_HERE'
```

# Usage
* [Running Unit tests](#running-unit-tests)
* [Running against Demo environment](#running-against-demo-environment)
* [How to forward requests to your phone](#how-to-forward-requests-to-your-phone)
* [Client configuration](#client-configuration)
  - [Verifying the SSL connection to SK](#verifying-the-ssl-connection-to-application-provider-sk)
    - [Keeping the trusted certificates in a Trust Store file](#keeping-the-trusted-certificates-in-a-trust-store-file)
    - [Using with custom ssl context](#using-with-custom-ssl-context)
    - [Specifying trusted certificates as string list](#specifying-trusted-certificates-as-string-list)
  - [How to create a trust store](#how-to-create-a-trust-store)
    - [Updating certs in tests of mid-rest-java-client](#updating-certs-in-tests-of-mid-rest-java-client)
  - [Configuring a proxy](#configuring-a-proxy)
    - [JBoss and WildFly](#jboss-and-wildfly)
    - [Tomcat](#tomcat)
  - [Long-polling configuration](#long-polling-configuration)
  - [Calling without long polling](#calling-without-long-polling)
* [Retrieving signing certificate](#retrieving-signing-certificate)
* [Creating the signature](#creating-the-signature)
  - [Creating the signature from raw data file](#creating-the-signature-from-raw-data-file)
  - [Creating the signature from existing hash](#creating-the-signature-from-existing-hash)
* [Authentication](#authentication)
  - [Getting the authentication response](#getting-the-authentication-response)
  - [Verifying the authentication response](#verifying-the-authentication-response)
    - [Validate returned certificate is a trusted MID certificate](#validate-returned-certificate-is-a-trusted-mid-certificate)
* [Handling negative scenarios](#handling-negative-scenarios)
  - [Handling authentication and signing exceptions](#handling-authentication-and-signing-exceptions)
  - [Handling certificate retrieval exceptions](#handling-certificate-retrieval-exceptions)
* [Validating user input](#validating-user-input)

## Running unit tests

`mvn test`

## Running against Demo environment

SK ID Solutions AS hosts a public demo environment that you can use for testing your integration.
There are [test numbers](https://github.com/SK-EID/MID/wiki/Test-number-for-automated-testing-in-DEMO)
that can be used to simulate different scenarios.

The [integration tests](https://github.com/SK-EID/mid-rest-java-client/tree/master/src/test/java/ee/sk/mid/integration)
in this library have been configured to run against this Demo environment.

You can run only integration tests with:

`mvn failsafe:integration-test`

As a quick start you can also run `MobileIdAuthenticationInteractive.class` from command line
or with your IDE by running its main method.

## How to forward requests to your phone

If you have Estonian or Lithuanian Mobile ID then you can run real-life tests with your
own phone if you register your Mobile ID certificates [SK Demo environment](https://demo.sk.ee/MIDCertsReg/).
It is also possible to change the status of the certificates from there.

You can run `MobileIdAuthenticationInteractive` main method to test it out 
(enter your own phone number and national identity code), and you should get a
request to enter your PIN to phone.


## Client configuration
<!-- Do not change code samples here but instead copy from ReadmeTest.documentConfigureTheClient() -->
```java
        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        MidClient client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withTrustStore(trustStore)
            .build();
```

> **Note** that these values are demo environment specific. In production use the values provided by Application Provider.

### Verifying the SSL connection to Application Provider (SK)

The Relying Party needs to verify that it is connecting to MID API it trusts.
More info about this requirement can be found from [MID Documentation](https://github.com/SK-EID/MID#28-api-endpoint-authentication).

Server SSL certificates are valid for limited time and thus replaced regularly (about once in every 3 years).
Every time a new certificate is issued, Relying Parties are notified in advance by Application Provider, and the new certificate needs to be imported
into the Service Provider's system, or the code starts to throw errors after server certificate becomes invalid.

Following options are available to set trusted certificates.

#### Keeping the trusted certificates in a Trust Store file 

Trust Store file is passed to mid-rest-java-client (recommended):

<!-- Do not change code samples here but instead copy from ReadmeTest.documentConfigureTheClientTrustStore() -->
```java
        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());

        client = MidClient.newBuilder()
                .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
                .withRelyingPartyName("DEMO")
                .withHostUrl("https://tsp.demo.sk.ee/mid-api")
                .withTrustStore(trustStore)
                .build();
```
> **Note** You can also use trust store in P12 format. In this case replace "JKS" with "PKCS12".

Using Trust Store is preferred as you can use the same format
to keep track which certificates you trust.

Read chapter  [Validate returned certificate is a trusted MID certificate](#Validate-returned-certificate-is-a-trusted-MID-certificate) for more info.

#### Using with custom ssl context

<!-- Do not change code samples here but instead copy from ReadmeTest.documentConfigureTheClientWithTrustSslContext() -->
```java
...
        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());
        
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        client = MidClient.newBuilder()
                .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
                .withRelyingPartyName("DEMO")
                .withHostUrl("https://tsp.demo.sk.ee/mid-api")
                .withTrustSslContext(sslContext)
                .build();
```

#### Specifying trusted certificates as string list

<!-- Do not change code samples here but instead copy from ReadmeTest.documentConfigureTheClientWithTrustedCertificatesList() -->
```java
        client = MidClient.newBuilder()
                .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
                .withRelyingPartyName("DEMO")
                .withHostUrl("https://tsp.demo.sk.ee/mid-api")
                .withTrustedCertificates("PEM encoded cert 1", "PEM encoded cert 2")
                .build();
```

### How to create a trust store

Download production (mid.sk.ee) certificate in PEM format from here: https://www.skidsolutions.eu/en/repository/certs/

Import it into Java keystore:
`keytool -import -file truststoreCert.pem -alias alias -keystore truststore.jks`

If you want you can then convert the Java keystore to a P12 key store and use it instead

`keytool -importkeystore -srckeystore production_server_trusted_ssl_certs.jks -destkeystore production_server_trusted_ssl_certs.p12 -srcstoretype JKS -deststoretype PKCS12`

Read next chapter how to obtain demo environment server SSL certificate.


#### Updating certs in tests of mid-rest-java-client

Integration tests (*_IT.java) that check the validity of server are configured not to run after server's certificate expiration.
When server (either production server or demo server) certificate has expired
then to make the tests run again one needs to replace certificate value in respective constant and import it into the trust store.
Here is the process that needs to be followed.

1. Obtain the new certificate. Production server (mid.sk.ee) certificate will be available here:  https://www.skidsolutions.eu/en/repository/certs/
Demo server (tsp.demo.sk.ee) certificate can be obtained by running:
 
`openssl s_client -showcerts -servername tsp.demo.sk.ee -connect tsp.demo.sk.ee:443 </dev/null`
(copy first certificate in chain and save to file new.tsp.demo.sk.ee.certificate.cer)

2. Replace the certificate value in LIVE_SERVER_CERT or in DEMO_SERVER_CERT constant.

3. Update the certificate expiration date in LIVE_SERVER_CERT_EXPIRATION_DATE or in DEMO_SERVER_CERT_EXPIRATION_DATE.

4. Import the new production (mid.sk.ee) certificate into production_server_trusted_ssl_certs.jks or the new demo (tsp.demo.sk.ee) certificate into demo_server_trusted_ssl_certs.jks like this:

Change into directory src/test/resources
DEMO:
`keytool -importcert -file new.tsp.demo.sk.ee.certificate.cer -keystore demo_server_trusted_ssl_certs.jks -alias "tsp.demo.sk.ee that expires YYYY-MM-DD" `
password: changeit
trust this certificate: yes

LIVE:
`keytool -importcert -file new.mid.sk.ee.certificate.cer -keystore production_server_trusted_ssl_certs.jks -alias "mid.sk.ee that expires YYYY-MM-DD" `
password: changeit
trust this certificate: yes

5. If it was production server's certificate that expired you also need to convert JKS keyestore to P12 keystore:

```
    cd src/test/resources
    keytool -importkeystore -srckeystore production_server_trusted_ssl_certs.jks -destkeystore production_server_trusted_ssl_certs.p12 -srcstoretype JKS -deststoretype PKCS12
```

After following this process the tests (that were ignored programmatically) should run again and a Pull Request could be submitted.

### Configuring a proxy
#### JBoss and WildFly


```java
        // import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
        // import org.jboss.resteasy.client.jaxrs.ResteasyClient
        ResteasyClient resteasyClient = new ResteasyClientBuilder()
            .defaultProxy("192.168.1.254", 8080, "http")
            .build();
        MidClient client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withConfiguredClient(resteasyClient)
            .withTrustStore(trustStore)
            .build();
```
#### Tomcat

```java
        // import org.glassfish.jersey.client.ClientConfig
        ClientConfig clientConfig = new ClientConfig()
        clientConfig.property(ClientProperties.PROXY_URI, "192.168.1.254:8080");
        MidClient client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withTrustStore(trustStore)
            .withwithNetworkConnectionConfig(clientConfig)
            .build();
```

### Long-polling configuration
Under the hood operations as signing and authentication consist of 2 request steps:

* Initiation request
* Session status request

Session status request by default is a long poll method, meaning it might not return until a timeout expires.
The caller can tune the request parameters inside the bounds set by a service operator by using the `withLongPollingTimeoutSeconds(int)`:

<!-- Do not change code samples here but instead copy from ReadmeTest.documentClientWithLongPollingTimeout() -->
```java
        MidClient client = MidClient.newBuilder()
            // set hostUrl, relyingPartyUUID, relyingPartyName and trustStore/trustSslContext
            .withLongPollingTimeoutSeconds(60)
            .build();
```

> Check [Long polling](https://github.com/SK-EID/MID#334-long-polling) documentation chapter for more information.

### Calling without long polling

If for some reason you cannot use long polling (which is recommended)
then you need to set `withPollingSleepTimeoutSeconds(int)` to a few seconds (between 1...5).
This makes a request to Application Provider, the response is returned immediately
and if the session is not completed the client performs a sleep for configured amount of seconds
before making a new request.

<!-- Do not change code samples here but instead copy from ReadmeTest.documentWithPollingSleepTimeoutSeconds() -->
```java
        MidClient client = MidClient.newBuilder()
            // set hostUrl, relyingPartyUUID, relyingPartyName and trustStore/trustSslContext
            .withPollingSleepTimeoutSeconds(2)
            .build();
```

If you don't set a positive value either to longPollingTimeoutSeconds or pollingSleepTimeoutSeconds
then pollingSleepTimeoutSeconds defaults to value 3 seconds.

## Retrieving signing certificate

In order to create signed container one needs to know the certificate of the user
which can be obtained with a separate request:
 
<!-- Do not change code samples here but instead copy from ReadmeTest.documentRetrieveCert() -->
```java
    MidCertificateRequest request = MidCertificateRequest.newBuilder()
        .withPhoneNumber("+37200000266")
        .withNationalIdentityNumber("60001019939")
        .build();

    MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);

    X509Certificate certificate = client.createMobileIdCertificate(response);
```

There are convenience methods to read and validate
phone number and national identity number entered by the user.
See chapter [Validating user input](#validating-user-input).

## Creating the signature

### Creating the signature from raw data file
You can pass raw data to builder of SignableHash and it creates the hash itself internally:

<!-- Do not change code samples here but instead copy from ReadmeTest.documentCreateFromExistingData() -->
```java
    byte[] data = "MY_DATA".getBytes(StandardCharsets.UTF_8);
    
    MidHashToSign hashToSign = MidHashToSign.newBuilder()
        .withDataToHash(data)
        .withHashType( MidHashType.SHA256)
        .build();

    String verificationCode = hashToSign.calculateVerificationCode();

    MidSignatureRequest request = MidSignatureRequest.newBuilder()
        .withPhoneNumber("+37200000766")
        .withNationalIdentityNumber("60001019906")
        .withHashToSign(hashToSign)
        .withLanguage( MidLanguage.ENG)
        .withDisplayText("Sign document?")
        .withDisplayTextFormat( MidDisplayTextFormat.GSM7)
        .build();

    MidSignatureResponse response = client.getMobileIdConnector().sign(request);

    MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
        "/signature/session/{sessionId}");

    MidSignature signature = client.createMobileIdSignature(sessionStatus);
  
```

> **Note** that `verificationCode` of the service should be displayed on the screen, so the person could verify if the verification code displayed on the screen and code sent him as a text message are identical.

Java demo application [mid-rest-java-demo](https://github.com/SK-EID/mid-rest-java-demo)
demonstrates how to create and sign a container with Mobile-ID and
[digidoc4j](https://github.com/open-eid/digidoc4j) library.

### Creating the signature from existing hash

<!-- Do not change code samples here but instead copy from ReadmeTest.documentCreateSignatureFromExistingHash() -->
```java
    MidHashToSign hashToSign = MidHashToSign.newBuilder()
        .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
        .withHashType( MidHashType.SHA256)
        .build();

```

## Authentication

#### Getting the authentication response

For security reasons, a new hash value must be created for each new authentication request.

<!-- Do not change code samples here but instead copy from ReadmeTest.documentGetAuthenticationResponse() -->
```java
    MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();

    String verificationCode = authenticationHash.calculateVerificationCode();

    MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
        .withPhoneNumber("+37200000766")
        .withNationalIdentityNumber("60001019906")
        .withHashToSign(authenticationHash)
        .withLanguage( MidLanguage.ENG)
        .withDisplayText("Log into self-service?")
        .withDisplayTextFormat( MidDisplayTextFormat.GSM7)
        .build();

    MidAuthenticationResponse response = client.getMobileIdConnector().authenticate(request);

    MidSessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
        "/authentication/session/{sessionId}");

    MidAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash);
```

> **Note** that `verificationCode` of the service should be displayed on the screen,
so the person could verify if the verification code displayed on the screen and code sent him as a text message are identical.

Java demo application [mid-rest-java-demo](https://github.com/SK-EID/mid-rest-java-demo)
and PHP demo application [mid-rest-php-demo](https://github.com/SK-EID/mid-rest-php-demo)
demonstrate how to perform authentication and verify the response.

### Verifying the authentication response

<!-- Do not change code samples here but instead copy from ReadmeTest.documentHowToVerifyAuthenticationResult() -->
```java
        InputStream is = TestData.class.getResourceAsStream("/path/to/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, "changeit".toCharArray());
        
        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(trustStore);
        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        assertThat(authenticationResult.isValid(), is(true));
        assertThat(authenticationResult.getErrors().isEmpty(), is(true));
```

When the authentication result is valid a session could be created now within the e-service or application.

#### Validate returned certificate is a trusted MID certificate

To avoid man-in-the-middle attacks you need to make sure that the authentication certificate returned by MID API is issued by Application Provider (SK ID Solutions AS).
You can read more about this requirement from [MID API documentation](https://github.com/SK-EID/MID#336-verifying-the-authentication-response).

You need to keep a Trust Store that trusts certificates taken from [SK Certificate Repository](https://www.skidsolutions.eu/en/repository/certs/).

For testing you need to import certificates for testing:  https://www.skidsolutions.eu/en/repository/certs/certificates-for-testing

You can use the same Trust Store file that you keep trusted SSL server certificates (see chapter 
[Verifying the SSL connection to Application Provider (SK)](#verifying-the-ssl-connection-to-application-provider-sk)).


When the authentication result is not valid or the returned certificate is not signed by a CA that we trust then the reasons for invalidity are obtainable like this:

<!-- Do not change code samples here but instead copy from ReadmeTest.documentGettingErrors() -->
```java
    List<String> errors = authenticationResult.getErrors();
```




`AuthenticationIdentity` could be helpful for obtaining information about the authenticated person when constructing the session.

<!-- Do not change code samples here but instead copy from ReadmeTest.documentAuthenticationIdentityUsage() -->
```java
    MidAuthenticationIdentity authenticationIdentity = authenticationResult.getAuthenticationIdentity();
    String givenName = authenticationIdentity.getGivenName();
    String surName = authenticationIdentity.getSurName();
    String identityCode = authenticationIdentity.getIdentityCode();
    String country = authenticationIdentity.getCountry();
```


## Handling negative scenarios

If user cancels the operation or the phone is unreachable then specific exceptions are thrown.
These can be caught and handled locally.

Following exceptions indicate problems with integration or configuration on Relying Party (integrator) side:
`MidSessionNotFoundException`, `MissingOrInvalidParameterException`, `UnauthorizedException`.

`MidInternalErrorException` is for MID internal errors that cannot be handled by clients.

### Handling authentication and signing exceptions

<!-- Do not change code samples here but instead copy from ReadmeTest.documentCatchingErrors() -->
```java
   try {
        // perform authentication or signing
    }
    catch (MidUserCancellationException e) {
        logger.info("User cancelled operation from his/her phone.");
        // display error
    }
    catch (MidNotMidClientException e) {
        logger.info("User is not a MID client or user's certificates are revoked.");
        // display error
    }
    catch (MidSessionTimeoutException e) {
        logger.info("User did not type in PIN code or communication error.");
        // display error
    }
    catch (MidPhoneNotAvailableException e) {
        logger.info("Unable to reach phone/SIM card. User needs to check if phone has coverage.");
        // display error
    }
    catch (MidDeliveryException e) {
        logger.info("Error communicating with the phone/SIM card.");
        // display error
    }
    catch (MidInvalidUserConfigurationException e) {
        logger.info("Mobile-ID configuration on user's SIM card differs from what is configured on service provider's side. User needs to contact his/her mobile operator.");
        logger.info("In case of DEMO the user needs to re-import MID certificate at https://demo.sk.ee/MIDCertsReg/");
        // display error
    }
    catch (MidSessionNotFoundException | MidMissingOrInvalidParameterException | MidUnauthorizedException e) {
        logger.error("Integrator-side error with MID integration or configuration", e);
        // navigate to error page
    }
    catch (MidServiceUnavailableException e) {
        logger.warn("MID service is currently unavailable. Please try again later.");
        // navigate to error page
    }
    catch (MidInternalErrorException e) {
        logger.warn("MID service returned internal error that cannot be handled locally.");
        // navigate to error page
    }
```

### Handling certificate retrieval exceptions

If you request signing certificate in a separate try block then you need to handle following exceptions separately:

<!-- Do not change code samples here but instead copy from ReadmeTest.documentCatchingCertificateRequestErrors() -->
```java
    try {
        // request user signing certificates
    }
    catch (MidNotMidClientException e) {
        logger.info("User is not a MID client or user's certificates are revoked");
    }
    catch (MidMissingOrInvalidParameterException | MidUnauthorizedException e) {
        logger.error("Integrator-side error with MID integration (including insufficient input validation) or configuration", e);
    }
    catch (MidInternalErrorException e) {
        logger.warn("MID service returned internal error that cannot be handled locally.");
    }
```

## Validating user input

This library comes with convenience methods to validate user input.
You can use the methods also to clean input from whitespaces.

<!-- Do not change code samples here but instead copy from ReadmeTest.documentValidateUserInput() -->
```java
    try {
        String nationalIdentityNumber = MidInputUtil.getValidatedNationalIdentityNumber("<national identity number entered by user>");
        String phoneNumber = MidInputUtil.getValidatedPhoneNumber("<phone number entered by user>");
    }
    catch (MidInvalidNationalIdentityNumberException e) {
        logger.info("User entered invalid national identity number");
        // display error
    }
    catch (MidInvalidPhoneNumberException e) {
        logger.info("User entered invalid phone number");
        // display error
    }
```

# Logging

This library uses [Logback](https://logback.qos.ch/) for logging.
To log incoming and outgoing requests made by the library set following class to log at 'trace' level:

```
    <logger name="ee.sk.mid.rest.MidLoggingFilter" level="trace" additivity="false">
        <appender-ref ref="Console" />
    </logger>
```

# License
This project is licensed under the terms of the [MIT license](LICENSE).
