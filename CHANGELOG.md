# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).



## [1.6] - upcoming

### Changed
- Added two examples (and unit tests) that show how to use a proxy to acces the server
- Update versions of dependencies

## [1.5] - March 2022

### Changed
- Update versions of dependencies 

## [1.4] - 2021-12-21

### Changed
- Fixed a flaw that could potentially cause errors on high load

## [1.3] - 2020-09-17

### Changed
- Relying Party must keep the list of trusted server certificates and supply them to mid-rest-java-client
    - recommended way is to create a trust store (ehiter JKS or P12 format) and load trusted certificates into it
    (and update this file when new certificates are published and eventually replaced by Application Provider (SK)
    - withLiveEnvCertificates() and withDemoEnvCertificates() methods are now removed (certificates are not longer hard coded into client library)
- To indicate that we have a trust store the following methods should be renamed (old methods are now marked as deprecated)
    - withSslKeyStore() -> withTrustStore()
    - withSslCertificates() -> withTrustedCertificates()
    - withSslContext() -> withTrustSslContext
- MidAuthenticationResponseValidator now takes trusted certificates info as constructor parameter.

### Added
- handling 503 (Service Unavailable) exceptions

### Removed
- Removed handling "NOT_ACTIVE" certificate status as it is never return by MID API (API always returns NOT_MID_CLIENT instead)

## [1.2.1] - 2020-09-11
- trusted_certificates renamed trusted_certificates_mid

## [1.2] - 2020-07-06

### Added
- Different options for ensuring secure connection with SK mobile id backend
    - `MidClient.MobileIdClientBuilder.withSslKeyStore(keyStore)`
    - `MidClient.MobileIdClientBuilder.withSslCertificates("Pem encoded cert 1", "Pem encoded cert 2")`
    - `MidClient.MobileIdClientBuilder.withSslContext(sslContext)`

## [1.1] - 2019-03-22

### Added
- Maven dependency check plugin for continuous security
