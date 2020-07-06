[![Build Status](https://api.travis-ci.com/SK-EID/mid-rest-java-client.svg?branch=master)](https://travis-ci.com/SK-EID/mid-rest-java-client)
[![Coverage Status](https://img.shields.io/codecov/c/github/SK-EID/mid-rest-java-client.svg)](https://codecov.io/gh/SK-EID/mid-rest-java-client)
[![License: MIT](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ee.sk.mid/mid-rest-java-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ee.sk.mid/mid-rest-java-client)

# Mobile-ID (MID) Java client
Mobile-ID Java client is a Java library that can be used for easy integration with MID REST interface (https://github.com/SK-EID/MID) of the [Mobile-ID](https://www.id.ee/index.php?id=36809).

* [Features](#features)
* [Requirements](#requirements)
* [Maven](#maven)
* [Usage](#usage)
* [License](#license)

# Features
* Simple interface for user authentication
* Simple interface for digital signature services

# Requirements
* Java 1.8 
* Internet access to Mobile-ID demo environment (to run integration tests)

# Maven
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
compile group: 'ee.sk.mid', name: 'mid-rest-java-client', version: '1.2'
```

# Usage
* [Running Unit tests](#running-unit-tests)
* [Running against Demo environment](#running-against-demo-environment)
* [Configure the client](#configure-the-client)
* [Configure client network connection](#configure-client-network-connection)
* [Retrieve signing certificate](#retrieve-signing-certificate)
* [Create a signature](#create-a-signature)
  - [Create a signature from existing hash](#create-a-signature-from-existing-hash)
  - [Create a signature from unhashed data](#create-a-signature-from-unhashed-data)
* [Authenticate](#authenticate)
  - [Get an authentication response](#get-an-authentication-response)
  - [Verify an authentication response](#verify-an-authentication-response)
* [Handling negative scenarios](#handling-negative-scenarios)
* [Validating user input](#validating-user-input)

## Running unit tests

`mvn test`

## Running against Demo environment

SK ID Solutions AS hosts a public demo environment that you can run your tests against.
There are [test numbers](https://github.com/SK-EID/MID/wiki/Test-number-for-automated-testing-in-DEMO)
that can be used to simulate different scenarios.

The [integration tests](https://github.com/SK-EID/mid-rest-java-client/tree/master/src/test/java/ee/sk/mid/integration)
in this library have been configured to run against this Demo environment.

You can run only integration tests with:

`mvn failsafe:integration-test`


## How to forward requests to your phone

If you have Estonian or Lithuanian Mobile ID then you can run real-life tests with your
own phone if you register your Mobile ID certificates [SK Demo environment](https://demo.sk.ee/MIDCertsReg/).
It is also possible to change the status of the certificates from there.

You can run `MobileIdAuthenticationInteractive` main method to test it out 
(enter your own phone number and national identity code) and you should get a
request to enter your PIN to phone.

## Client configuration
```java
    MidClient client = MidClient.newBuilder()
        .withHostUrl("https://tsp.demo.sk.ee/mid-api")
        .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
        .withRelyingPartyName("DEMO")
        .build();
```

> **Note** that these values are demo environment specific. In production use the values provided by Application Provider.

### Configuring a proxy
#### JBoss and WildFly
```java
        // org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
        // org.jboss.resteasy.client.jaxrs.ResteasyClient
        ResteasyClient resteasyClient = new ResteasyClientBuilder()
            .defaultProxy("192.168.1.254", 8080, "http")
            .build();
        MidClient client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withConfiguredClient(resteasyClient)
            .build();
```
#### Tomcat
```java
        // org.glassfish.jersey.client.ClientConfig
        ClientConfig clientConfig = new ClientConfig()
        clientConfig.property(ClientProperties.PROXY_URI, "192.168.1.254:8080");
        MidClient client = MidClient.newBuilder()
            .withHostUrl("https://tsp.demo.sk.ee/mid-api")
            .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
            .withRelyingPartyName("DEMO")
            .withwithNetworkConnectionConfig(clientConfig)
            .build();
```

### Long-polling configuration
Under the hood operations as signing and authentication consist of 2 request steps:

* Initiation request
* Session status request

Session status request by default is a long poll method, meaning it might not return until a timeout expires.
The caller can tune the request parameters inside the bounds set by a service operator by using the `withLongPollingTimeoutSeconds(int)`:

```java
    MidClient client = MidClient.newBuilder()
        // set hostUrl, hRelyingParty UUID & Name
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

```java
    MidClient client = MidClient.newBuilder()
        // set hostUrl, hRelyingParty UUID & Name
        .withPollingSleepTimeoutSeconds(2)
        .build();
```

If you don't set a positive value either to longPollingTimeoutSeconds or pollingSleepTimeoutSeconds
then pollingSleepTimeoutSeconds defaults to value 3 seconds.

## Retrieving signing certificate

In order to create signed container one needs to know the certificate of the user
which can be obtained with a separate request:
 

```java
    MidCertificateRequest request = MidCertificateRequest.newBuilder()
        .withPhoneNumber("+37060000666")
        .withNationalIdentityNumber("60001019906")
        .build();
    
    MidCertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);
    
    X509Certificate certificate =   client.createMobileIdCertificate(response);
```

There are convenience methods to read and validate
phone number and national identity number entered by the user.
See chapter [Validating user input](#validating-user-input).

## Creating the signature

### Creating tje signature from raw data file.
You can pass raw data to builder of SignableHash and it creates the hash itself internally:

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
```java
    MidHashToSign hashToSign = MidHashToSign.newBuilder()
        .withHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=")
        .withHashType( MidHashType.SHA256)
        .build();

```

## Authentication

#### Getting an authentication response

For security reasons, a new hash value must be created for each new authentication request.

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
```java
    MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator();
    MidAuthenticationResult authenticationResult = validator.validate(authentication);

    assertThat(authenticationResult.isValid(), is(true));
    assertThat(authenticationResult.getErrors().isEmpty(), is(true));
```

When the authentication result is valid a session could be created now within the e-service or application. As the session logic is dependent on the implementation and may vary from system to system, this is something integrator has to do himself.

When the authentication result is not valid then the reasons for invalidity are obtainable like this:

```java
List<String> errors = authenticationResult.getErrors();
```

`AuthenticationIdentity` could be helpful for obtaining information about the authenticated person when constructing the session.

```java
MidAuthenticationIdentity authenticationIdentity = authenticationResult.getAuthenticationIdentity();
String givenName = authenticationIdentity.getGivenName();
String surName = authenticationIdentity.getSurName();
String identityCode = authenticationIdentity.getIdentityCode();
String country = authenticationIdentity.getCountry();
```

### Verifying the ssl connection to SK
By default the client is configured to trust both Live and Demo Environment ssl certificates
```java
    client = MidClient.newBuilder()
                 .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                 .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                 .withHostUrl(DEMO_HOST_URL)
                 .build();
```

Using with only demo environment certificates
```java
client = MidClient.newBuilder()
                 .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                 .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                 .withHostUrl(DEMO_HOST_URL)
                 .withDemoEnvCertificates()
                 .build();
```

Using with only live environment certificates
```java
client = MidClient.newBuilder()
                 .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                 .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                 .withHostUrl(DEMO_HOST_URL)
                 .withLiveEnvCertificates()
                 .build();
```

Using with custom certificates
```java
client = MidClient.newBuilder()
                 .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                 .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                 .withHostUrl(DEMO_HOST_URL)
                 .withSslCertificates("Pem encoded cert 1", "Pem encoded cert 2")
                 .build();
```

Using with custom keystore
```java
InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/pathToKeystore");
KeyStore keyStore = KeyStore.getInstance("JKS");
keyStore.load(is, "changeit".toCharArray());

client = MidClient.newBuilder()
                 .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                 .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                 .withHostUrl(DEMO_HOST_URL)
                 .withSslKeyStore(keyStore)
                 .build();
```

Using with custom ssl context
```java
...
SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
trustManagerFactory.init(keyStore);
sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

client = MidClient.newBuilder()
                 .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                 .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                 .withHostUrl(DEMO_HOST_URL)
                 .withSslContext(sslContext)
                 .build();
```

## Handling negative scenarios

If user cancels operation or the phone is unreachable then specific exceptions are thrown.
These can be caught and handled locally.

Following exceptions indicate problems with integration or configuration on Relying Party (integrator) side:
`MidSessionNotFoundException`, `MissingOrInvalidParameterException`, `UnauthorizedException`.

`MidInternalErrorException` is for MID internal errors that cannot be handled by clients.

### Handling authentication and signing exceptions

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
        // display error
    }
    catch (MidSessionNotFoundException | MidMissingOrInvalidParameterException | MidUnauthorizedException e) {
        logger.error("Integrator-side error with MID integration or configuration", e);
        // navigate to error page
    }
    catch (MidInternalErrorException e) {
        logger.warn("MID service returned internal error that cannot be handled locally.");
        // navigate to error page
    }
```

### Handling certificate retrieval exceptions

If you request signing certificate in a separate try block then you need to handle following exceptions separately:

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
