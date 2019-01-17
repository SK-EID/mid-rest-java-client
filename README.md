
[![Build Status](https://api.travis-ci.org/SK-EID/mid-rest-java-client.svg?branch=master)](https://travis-ci.org/SK-EID/mid-rest-java-client)
[![Coverage Status](https://img.shields.io/codecov/c/github/SK-EID/mid-rest-java-client.svg)](https://codecov.io/gh/SK-EID/mid-rest-java-client)
[![License: MIT](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)

# Mobile-ID Java client
Mobile-ID Java client is a Java library that can be used for easy integration with MID REST interface (https://github.com/SK-EID/MID) of the [Mobile-ID](https://www.id.ee/index.php?id=36809).

* [Features](#features)
* [Requirements](#requirements)
* [Maven](#maven)
* [Usage](#usage)
* [License](#license)

## Features
* Simple interface for user authentication
* Simple interface for digital signature services

## Requirements
* Java 1.7
* Internet access to Mobile-ID demo environment (to run integration tests)

## Maven
You can use the library as a Maven dependency from the Maven Central (http://mvnrepository.com/artifact/ee.sk.mid/mid-rest-java-client)

```xml
<dependency>
    <groupId>ee.sk.mid</groupId>
    <artifactId>mid-rest-java-client</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage
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

### Running against Demo environment

SK ID Solutions AS hosts a public demo environment that you can run your tests against.
If you have a production Mobile ID then you can even add your own phone to this environment.
[More info and tests numbers](https://github.com/SK-EID/MID/wiki/Test-number-for-automated-testing-in-DEMO).

The [integration tests](https://github.com/SK-EID/mid-rest-java-client/tree/master/src/test/java/ee/sk/mid/integration)
in this library have been configured to run against this Demo environment.


### Configure the client
```java
MobileIdClient client = MobileIdClient.newBuilder()
        .withRelyingPartyUUID("00000000-0000-0000-0000-000000000000")
        .withRelyingPartyName("DEMO")
        .withHostUrl("https://tsp.demo.sk.ee")
        .build();
```

> **Note** that these values are demo environment specific. In production use the values provided by Application Provider.


### Configure client network connection
Under the hood operations as signing and authentication consist of 2 request steps:

* Initiation request
* Session status request

Session status request by default is a long poll method, meaning it might not return until a timeout expires.
The caller can tune the request parameters inside the bounds set by a service operator by using the `setPollingSleepTimeSeconds(int)`:

```java
client.setPollingSleepTimeSeconds(2);
```

> Check [Long polling](https://github.com/SK-EID/MID#334-long-polling) documentation chapter for more information.

### Retrieve signing certificate
```java
CertificateRequest request = CertificateRequest.newBuilder()
        .withRelyingPartyUUID(client.getRelyingPartyUUID())
        .withRelyingPartyName(client.getRelyingPartyName())
        .withPhoneNumber("+37060000666")
        .withNationalIdentityNumber("60001019906")
        .build();

CertificateChoiceResponse response = client.getMobileIdConnector().getCertificate(request);

X509Certificate certificate = client.createMobileIdCertificate(response);
```

> **Note** that the cert retrieving process (before the actual singing) is only necessary for the AdES-style
digital signatures which require knowledge of the cert beforehand.

### Create a signature

#### Create a signature from existing hash
```java
SignableHash hashToSign = new SignableHash();
hashToSign.setHashInBase64("AE7S1QxYjqtVv+Tgukv2bMMi9gDCbc9ca2vy/iIG6ug=");
hashToSign.setHashType(HashType.SHA256);

String verificationCode = hashToSign.calculateVerificationCode();

SignatureRequest request = SignatureRequest.newBuilder()
        .withRelyingPartyUUID(client.getRelyingPartyUUID())
        .withRelyingPartyName(client.getRelyingPartyName())
        .withPhoneNumber("+37200000766")
        .withNationalIdentityNumber("60001019906")
        .withSignableHash(hashToSign)
        .withLanguage(Language.ENG)
        .build();

SignatureResponse response = client.getMobileIdConnector().sign(request);

SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
"/mid-api/signature/session/{sessionId}");

MobileIdSignature signature = client.createMobileIdSignature(sessionStatus);
```

> **Note** that `verificationCode` of the service should be displayed on the screen, so the person could verify if the verification code displayed on the screen and code sent him as a text message are identical.

#### Create a signature from unhashed data
This is a good case when we have some data that we want to sign, but it isn't transformed into a hash yet. We can use `SignableData`, simply providing it with the data and the wanted hash algorithm and the client will deal with hashing for you.

```java
SignableData dataToSign = new SignableData("HACKERMAN".getBytes(StandardCharsets.UTF_8));
dataToSign.setHashType(HashType.SHA256);

String verificationCode = dataToSign.calculateVerificationCode();

SignatureRequest request = SignatureRequest.newBuilder()
        .withRelyingPartyUUID(client.getRelyingPartyUUID())
        .withRelyingPartyName(client.getRelyingPartyName())
        .withPhoneNumber("+37200000766")
        .withNationalIdentityNumber("60001019906")
        .withSignableData(dataToSign)
        .withLanguage(Language.EST)
        .build();

SignatureResponse response = client.getMobileIdConnector().sign(request);

SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
"/mid-api/signature/session/{sessionId}");

MobileIdSignature signature = client.createMobileIdSignature(sessionStatus);
```

> **Note** that `verificationCode` of the service should be displayed on the screen, so the person could verify if the verification code displayed on the screen and code sent him as a text message are identical.

### Authenticate

#### Get an authentication response
For security reasons, a new hash value must be created for each new authentication request.

```java
MobileIdAuthenticationHash authenticationHash = MobileIdAuthenticationHash.generateRandomHashOfDefaultType();

String verificationCode = authenticationHash.calculateVerificationCode();

AuthenticationRequest request = AuthenticationRequest.newBuilder()
        .withRelyingPartyUUID(client.getRelyingPartyUUID())
        .withRelyingPartyName(client.getRelyingPartyName())
        .withPhoneNumber("+37200000766")
        .withNationalIdentityNumber("60001019906")
        .withAuthenticationHash(authenticationHash)
        .withLanguage(Language.EST)
        .build();

AuthenticationResponse response = client.getMobileIdConnector().authenticate(request);

SessionStatus sessionStatus = client.getSessionStatusPoller().fetchFinalSessionStatus(response.getSessionID(),
"/mid-api/authentication/session/{sessionId}");

MobileIdAuthentication authentication = client.createMobileIdAuthentication(sessionStatus, authenticationHash.getHashInBase64(), authenticationHash.getHashType());
```

> **Note** that `verificationCode` of the service should be displayed on the screen, so the person could verify if the verification code displayed on the screen and code sent him as a text message are identical.

#### Verify an authentication response
```java
AuthenticationResponseValidator validator = new AuthenticationResponseValidator();
MobileIdAuthenticationResult authenticationResult = validator.validate(authentication);

assertThat(authenticationResult.isValid(), is(true));
assertThat(authenticationResult.getErrors().isEmpty(), is(true));
```

When the authentication result is valid a session could be created now within the e-service or application. As the session logic is dependent on the implementation and may vary from system to system, this is something integrator has to do himself.

When the authentication result is not valid then the reasons for invalidity are obtainable like this:

```java
List <String> errors = authenticationResult.getErrors();
```

`AuthenticationIdentity` could be helpful for obtaining information about the authenticated person when constructing the session.

```java
AuthenticationIdentity authenticationIdentity = authenticationResult.getAuthenticationIdentity();
String givenName = authenticationIdentity.getGivenName();
String surName = authenticationIdentity.getSurName();
String identityCode = authenticationIdentity.getIdentityCode();
String country = authenticationIdentity.getCountry();
```

## License
This project is licensed under the terms of the [MIT license](LICENSE).