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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import ee.sk.mid.exception.MobileIdException;
import ee.sk.mid.exception.UnauthorizedException;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AbstractRequest;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.request.SessionStatusRequest;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.SignatureResponse;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobileIdRestConnector implements MobileIdConnector {

    private static final Logger logger = LoggerFactory.getLogger(MobileIdRestConnector.class);
    private static final String CERTIFICATE_PATH = "/certificate";
    private static final String SIGNATURE_PATH = "/signature";
    private static final String AUTHENTICATION_PATH = "/authentication";

    private String endpointUrl;
    private ClientConfig clientConfig;

    private String relyingPartyUUID;
    private String relyingPartyName;

    public MobileIdRestConnector(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public MobileIdRestConnector(String endpointUrl, ClientConfig clientConfig) {
        this(endpointUrl);
        this.clientConfig = clientConfig;
    }

    public MobileIdRestConnector(String endpointUrl, ClientConfig clientConfig, String relyingPartyUUID, String relyingPartyName) {
        this.endpointUrl = endpointUrl;
        this.clientConfig = clientConfig;
        this.relyingPartyName = relyingPartyName;
        this.relyingPartyUUID = relyingPartyUUID;
    }

    MobileIdRestConnector(MobileIdRestConnectorBuilder mobileIdRestConnectorBuilder) {
        this.endpointUrl = mobileIdRestConnectorBuilder.endpointUrl;
        this.clientConfig = mobileIdRestConnectorBuilder.clientConfig;
        this.relyingPartyName = mobileIdRestConnectorBuilder.relyingPartyName;
        this.relyingPartyUUID = mobileIdRestConnectorBuilder.relyingPartyUUID;
    }

    @Override
    public CertificateChoiceResponse getCertificate(CertificateRequest request) {
        setRequestRelyingPartyDetailsIfMissing(request);

        logger.debug("Getting certificate for phone number: " + request.getPhoneNumber());

        URI uri = UriBuilder
            .fromUri(endpointUrl)
            .path(CERTIFICATE_PATH)
            .build();
        return postCertificateRequest(uri, request);
    }

    @Override
    public SignatureResponse sign(SignatureRequest request) {
        setRequestRelyingPartyDetailsIfMissing(request);

        logger.debug("Signing for phone number: " + request.getPhoneNumber());

        URI uri = UriBuilder
            .fromUri(endpointUrl)
            .path(SIGNATURE_PATH)
            .build();
        return postSignatureRequest(uri, request);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        setRequestRelyingPartyDetailsIfMissing(request);

        logger.debug("Authenticating for phone number " + request.getPhoneNumber());

        URI uri = UriBuilder
            .fromUri(endpointUrl)
            .path(AUTHENTICATION_PATH)
            .build();
        return postAuthenticationRequest(uri, request);
    }

    private void setRequestRelyingPartyDetailsIfMissing(AbstractRequest request) {
        if (request.getRelyingPartyUUID() == null) {
            request.setRelyingPartyUUID(this.relyingPartyUUID);
        }
        if (request.getRelyingPartyName() == null) {
            request.setRelyingPartyName(this.relyingPartyName);
        }

        if (isBlank(request.getRelyingPartyUUID())) {
            throw new MissingOrInvalidParameterException("Relying Party UUID parameter must be set in client or request");
        }
        if (isBlank(request.getRelyingPartyName())) {
            throw new MissingOrInvalidParameterException("Relying Party Name parameter must be set in client or request");
        }
    }

    @Override
    public SessionStatus getAuthenticationSessionStatus(SessionStatusRequest request) throws MidSessionNotFoundException {
        return getSessionStatus(request, SessionStatusPoller.AUTHENTICATION_SESSION_PATH);
    }

    @Override
    public SessionStatus getSignatureSessionStatus(SessionStatusRequest request) {
        return getSessionStatus(request, SessionStatusPoller.SIGNATURE_SESSION_PATH);
    }

    @Override
    public SessionStatus getSessionStatus(SessionStatusRequest request, String path) throws MidSessionNotFoundException {
        logger.debug("Getting session status for " + request.getSessionID());
        UriBuilder uriBuilder = UriBuilder
            .fromUri(endpointUrl)
            .path(path);

        if (request.getTimeoutMs() != 0) {
            uriBuilder.queryParam("timeoutMs", request.getTimeoutMs());
        }

        URI uri = uriBuilder.build(request.getSessionID());
        try {
            return prepareClient(uri).get(SessionStatus.class);
        } catch (NotFoundException e) {
            logger.error("Session " + request + " not found: " + e.getMessage());
            throw new MidSessionNotFoundException();
        }
    }

    private CertificateChoiceResponse postCertificateRequest(URI uri, CertificateRequest request) {
        return postRequest(uri, request, CertificateChoiceResponse.class);
    }

    private SignatureResponse postSignatureRequest(URI uri, SignatureRequest request) {
        return postRequest(uri, request, SignatureResponse.class);
    }

    private AuthenticationResponse postAuthenticationRequest(URI uri, AuthenticationRequest request) {
        return postRequest(uri, request, AuthenticationResponse.class);
    }

    private <T, V> T postRequest(URI uri, V request, Class<T> responseType) throws MobileIdException {
        try {
            Entity<V> requestEntity = Entity.entity(request, MediaType.APPLICATION_JSON);
            return prepareClient(uri).post(requestEntity, responseType);
        } catch (InternalServerErrorException e) {
            logger.error("Error getting response from cert-store/MSSP for URI " + uri + ": " + e.getMessage());
            throw new MidInternalErrorException("Error getting response from cert-store/MSSP for URI " + uri + ": " + e.getMessage());
        } catch (NotFoundException e) {
            logger.error("Response not found for URI " + uri + ": " + e.getMessage());
            throw new MidInternalErrorException("MID internal error");
        } catch (BadRequestException e) {
            String errorMessage = readErrorMessageFromBody(e);
            logger.error("MID rejected our input with message: " + errorMessage);
            throw new MissingOrInvalidParameterException(errorMessage);
        } catch (NotAuthorizedException e) {
            logger.error("Request is unauthorized for URI " + uri + ": " + e.getMessage());
            throw new UnauthorizedException("Request is unauthorized for URI " + uri + ": " + e.getMessage());
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
        Client client = clientConfig == null ? ClientBuilder.newClient() : ClientBuilder.newClient(clientConfig);
        return client
            .register(new LoggingFilter())
            .target(uri)
            .request()
            .accept(APPLICATION_JSON_TYPE);
    }

    public static MobileIdRestConnectorBuilder newBuilder() {
        return new MobileIdRestConnectorBuilder();
    }


}
