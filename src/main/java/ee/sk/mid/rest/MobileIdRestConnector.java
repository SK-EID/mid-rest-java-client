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

import ee.sk.mid.exception.*;
import ee.sk.mid.rest.dao.SessionStatus;
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
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class MobileIdRestConnector implements MobileIdConnector {

    private static final Logger logger = LoggerFactory.getLogger(MobileIdRestConnector.class);
    private static final String CERTIFICATE_PATH = "/mid-api/certificate";
    private static final String SIGNATURE_PATH = "/mid-api/signature";
    private static final String AUTHENTICATION_PATH = "/mid-api/authentication";

    private String endpointUrl;
    private ClientConfig clientConfig;

    public MobileIdRestConnector(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public MobileIdRestConnector(String endpointUrl, ClientConfig clientConfig) {
        this(endpointUrl);
        this.clientConfig = clientConfig;
    }

    @Override
    public CertificateChoiceResponse getCertificate(CertificateRequest request) {
        logger.debug("Getting certificate for phone number: " + request.getPhoneNumber());
        URI uri = UriBuilder
                .fromUri(endpointUrl)
                .path(CERTIFICATE_PATH)
                .build();
        return postCertificateRequest(uri, request);
    }

    @Override
    public SignatureResponse sign(SignatureRequest request) {
        logger.debug("Signing for phone number: " + request.getPhoneNumber());
        URI uri = UriBuilder
                .fromUri(endpointUrl)
                .path(SIGNATURE_PATH)
                .build();
        return postSignatureRequest(uri, request);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        logger.debug("Authenticating for phone number " + request.getPhoneNumber());
        URI uri = UriBuilder
                .fromUri(endpointUrl)
                .path(AUTHENTICATION_PATH)
                .build();
        return postAuthenticationRequest(uri, request);
    }

    @Override
    public SessionStatus getAuthenticationSessionStatus(SessionStatusRequest request) throws SessionNotFoundException {
        return getSessionStatus(request, SessionStatusPoller.AUTHENTICATION_SESSION_PATH);
    }

    @Override
    public SessionStatus getSignatureSessionStatus(SessionStatusRequest request) {
        return getSessionStatus(request, SessionStatusPoller.SIGNATURE_SESSION_PATH);
    }

    @Override
    public SessionStatus getSessionStatus(SessionStatusRequest request, String path) throws SessionNotFoundException {
        logger.debug("Getting session status for " + request.getSessionID());
        UriBuilder uriBuilder = UriBuilder
                .fromUri(endpointUrl)
                .path(path);
        URI uri = uriBuilder.build(request.getSessionID());
        try {
            return prepareClient(uri).get(SessionStatus.class);
        } catch (NotFoundException e) {
            logger.error("Session " + request + " not found: " + e.getMessage());
            throw new SessionNotFoundException();
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
            throw new ResponseRetrievingException();
        } catch (NotFoundException e) {
            logger.error("Response not found for URI " + uri + ": " + e.getMessage());
            throw new ResponseNotFoundException();
        } catch (BadRequestException e) {
            logger.error("Request is invalid for URI " + uri + ": " + e.getMessage());
            throw new ParameterMissingException();
        } catch (NotAuthorizedException e) {
            logger.error("Request is unauthorized for URI " + uri + ": " + e.getMessage());
            throw new UnauthorizedException();
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
}
