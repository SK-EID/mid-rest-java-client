package ee.sk.mid.rest;

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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;

import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.databind.JsonNode;
import ee.sk.mid.exception.MidException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidServiceUnavailableException;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.exception.MidSslException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAbstractRequest;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidCertificateRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidRestConnector implements MidConnector {

    private static final Logger logger = LoggerFactory.getLogger( MidRestConnector.class);
    private static final String CERTIFICATE_PATH = "/certificate";
    private static final String SIGNATURE_PATH = "/signature";
    private static final String AUTHENTICATION_PATH = "/authentication";

    private String endpointUrl;
    private Configuration clientConfig;
    private Client configuredClient;

    private String relyingPartyUUID;
    private String relyingPartyName;

    private SSLContext trustSslContext;

    public MidRestConnector(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public MidRestConnector(String endpointUrl, Configuration clientConfig) {
        this(endpointUrl);
        this.clientConfig = clientConfig;
    }

    public MidRestConnector(String endpointUrl, Configuration clientConfig, String relyingPartyUUID, String relyingPartyName) {
        this.endpointUrl = endpointUrl;
        this.clientConfig = clientConfig;
        this.relyingPartyName = relyingPartyName;
        this.relyingPartyUUID = relyingPartyUUID;
    }

    MidRestConnector(MidRestConnectorBuilder mobileIdRestConnectorBuilder) {
        this.endpointUrl = mobileIdRestConnectorBuilder.endpointUrl;
        this.clientConfig = mobileIdRestConnectorBuilder.clientConfig;
        this.configuredClient = mobileIdRestConnectorBuilder.configuredClient;
        this.relyingPartyName = mobileIdRestConnectorBuilder.relyingPartyName;
        this.relyingPartyUUID = mobileIdRestConnectorBuilder.relyingPartyUUID;
        this.trustSslContext = mobileIdRestConnectorBuilder.sslContext;
    }

    @Override
    public MidCertificateChoiceResponse getCertificate(MidCertificateRequest request) {
        setRequestRelyingPartyDetailsIfMissing(request);

        logger.debug("Getting certificate for phone number: " + request.getPhoneNumber());

        URI uri = UriBuilder
            .fromUri(endpointUrl)
            .path(CERTIFICATE_PATH)
            .build();
        return postCertificateRequest(uri, request);
    }

    @Override
    public MidSignatureResponse sign(MidSignatureRequest request) {
        setRequestRelyingPartyDetailsIfMissing(request);

        logger.debug("Signing for phone number: " + request.getPhoneNumber());

        URI uri = UriBuilder
            .fromUri(endpointUrl)
            .path(SIGNATURE_PATH)
            .build();
        return postSignatureRequest(uri, request);
    }

    @Override
    public MidAuthenticationResponse authenticate(MidAuthenticationRequest request) {
        setRequestRelyingPartyDetailsIfMissing(request);

        logger.debug("Authenticating for phone number " + request.getPhoneNumber());

        URI uri = UriBuilder
            .fromUri(endpointUrl)
            .path(AUTHENTICATION_PATH)
            .build();
        return postAuthenticationRequest(uri, request);
    }

    private void setRequestRelyingPartyDetailsIfMissing(MidAbstractRequest request) {
        if (request.getRelyingPartyUUID() == null) {
            request.setRelyingPartyUUID(this.relyingPartyUUID);
        }
        if (request.getRelyingPartyName() == null) {
            request.setRelyingPartyName(this.relyingPartyName);
        }

        if (isBlank(request.getRelyingPartyUUID())) {
            throw new MidMissingOrInvalidParameterException("Relying Party UUID parameter must be set in client or request");
        }
        if (isBlank(request.getRelyingPartyName())) {
            throw new MidMissingOrInvalidParameterException("Relying Party Name parameter must be set in client or request");
        }
    }

    @Override
    public MidSessionStatus getAuthenticationSessionStatus(MidSessionStatusRequest request) throws MidSessionNotFoundException {
        return getSessionStatus(request, MidSessionStatusPoller.AUTHENTICATION_SESSION_PATH);
    }

    @Override
    public MidSessionStatus getSignatureSessionStatus(MidSessionStatusRequest request) {
        return getSessionStatus(request, MidSessionStatusPoller.SIGNATURE_SESSION_PATH);
    }

    @Override
    public MidSessionStatus getSessionStatus(MidSessionStatusRequest request, String path) throws MidSessionNotFoundException {
        logger.debug("Getting session status for " + request.getSessionID());
        UriBuilder uriBuilder = UriBuilder
            .fromUri(endpointUrl)
            .path(path);

        if (request.getTimeoutMs() != 0) {
            uriBuilder.queryParam("timeoutMs", request.getTimeoutMs());
        }

        URI uri = uriBuilder.build(request.getSessionID());
        try {
            return prepareClient(uri).get( MidSessionStatus.class);
        } catch (NotFoundException e) {
            logger.error("Session " + request + " not found: " + e.getMessage());
            throw new MidSessionNotFoundException();
        }
    }

    private MidCertificateChoiceResponse postCertificateRequest(URI uri, MidCertificateRequest request) {
        return postRequest(uri, request, MidCertificateChoiceResponse.class);
    }

    private MidSignatureResponse postSignatureRequest(URI uri, MidSignatureRequest request) {
        return postRequest(uri, request, MidSignatureResponse.class);
    }

    private MidAuthenticationResponse postAuthenticationRequest(URI uri, MidAuthenticationRequest request) {
        return postRequest(uri, request, MidAuthenticationResponse.class);
    }

    private <T, V> T postRequest(URI uri, V request, Class<T> responseType) throws MidException {
        try {
            Entity<V> requestEntity = Entity.entity(request, MediaType.APPLICATION_JSON);
            return prepareClient(uri).post(requestEntity, responseType);
        }
        catch (InternalServerErrorException e) {
            logger.error("Error getting response from cert-store/MSSP for URI " + uri + ": " + e.getMessage());
            throw new MidInternalErrorException("Error getting response from cert-store/MSSP for URI " + uri + ": " + e.getMessage());
        }
        catch (NotFoundException e) {
            logger.error("Response not found for URI " + uri + ": " + e.getMessage());
            throw new MidInternalErrorException("MID internal error");
        }
        catch (BadRequestException e) {
            String errorMessage = readErrorMessageFromBody(e);
            logger.error("MID rejected our input with message: " + errorMessage);
            throw new MidMissingOrInvalidParameterException(errorMessage);
        }
        catch (NotAuthorizedException e) {
            logger.error("Request is unauthorized for URI " + uri + ": " + e.getMessage());
            throw new MidUnauthorizedException("Request is unauthorized for URI " + uri + ": " + e.getMessage());
        }
        catch (ServiceUnavailableException e) {
            logger.error("MID server returned 503 - service unavailable", e);
            throw new MidServiceUnavailableException("MID service is currently unavailable. Please try again later.");
        }
        catch (ProcessingException p) {
            if (p.getCause() != null &&
                 p.getCause().getMessage() != null
                 && p.getCause().getMessage().contains("unable to find valid certification path to requested target")) {
                logger.error("SSL certificate not trusted");
                throw new MidSslException("MID-REST client is not configured to trust SSL certificate of MID API host");
            }
            throw new MidInternalErrorException("Unknown error when connecting to Host", p);
        }
    }

    private String readErrorMessageFromBody(BadRequestException e) {
        try {
            return e.getResponse().readEntity(JsonNode.class).get("error").asText();
        } catch (Exception ex) {
            logger.info("Could not read error from body. Most likely it didn't contain any");
            return e.getMessage();
        }
    }

    private Invocation.Builder prepareClient(URI uri) {
        Client client;
        if (this.configuredClient == null) {
            ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            if (null != this.clientConfig) {
                clientBuilder.withConfig(this.clientConfig);
            }
            if (null != this.trustSslContext) {
                clientBuilder.sslContext(this.trustSslContext);
            }
            client = clientBuilder.build();
        }
        else {
            client = this.configuredClient;
        }
        return client
            .register(new MidLoggingFilter())
            .target(uri)
            .request()
            .accept(APPLICATION_JSON_TYPE);
    }

    public static MidRestConnectorBuilder newBuilder() {
        return new MidRestConnectorBuilder();
    }

    @Override
    public void setSslTrustContext(SSLContext trustSslContext) {
        this.trustSslContext = trustSslContext;
    }
}
