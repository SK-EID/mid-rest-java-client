# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.2] - 2020-07-06

### Added
- Different options for ensuring secure connection with SK mobile id backend
    - `MidClient.MobileIdClientBuilder.withSslKeyStore(keyStore)`
    - `MidClient.MobileIdClientBuilder.withSslCertificates("Pem encoded cert 1", "Pem encoded cert 2")`
    - `MidClient.MobileIdClientBuilder.withLiveEnvCertificates()`
    - `MidClient.MobileIdClientBuilder.withDemoEnvCertificates()`
    - `MidClient.MobileIdClientBuilder.withSslContext(sslContext)`

## [1.1] - 2019-03-22

### Added
- Maven dependency check plugin for continuous security
