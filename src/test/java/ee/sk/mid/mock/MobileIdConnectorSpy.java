package ee.sk.mid.mock;

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

import ee.sk.mid.exception.SessionNotFoundException;
import ee.sk.mid.rest.MobileIdConnector;
import ee.sk.mid.rest.SessionStatusPoller;
import ee.sk.mid.rest.dao.SessionStatus;
import ee.sk.mid.rest.dao.request.AuthenticationRequest;
import ee.sk.mid.rest.dao.request.CertificateRequest;
import ee.sk.mid.rest.dao.request.SessionStatusRequest;
import ee.sk.mid.rest.dao.request.SignatureRequest;
import ee.sk.mid.rest.dao.response.AuthenticationResponse;
import ee.sk.mid.rest.dao.response.CertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.SignatureResponse;

public class MobileIdConnectorSpy implements MobileIdConnector {

    private SessionStatus sessionStatusToRespond;
    private CertificateChoiceResponse certificateChoiceResponseToRespond;
    private AuthenticationResponse authenticationResponseToRespond;
    private SignatureResponse signatureResponseToRespond;

    private String sessionIdUsed;
    private CertificateRequest certificateRequestUsed;
    private AuthenticationRequest authenticationRequestUsed;
    private SignatureRequest signatureRequestUsed;

    public SessionStatus getSessionStatusToRespond() {
        return sessionStatusToRespond;
    }

    public void setSessionStatusToRespond(SessionStatus sessionStatusToRespond) {
        this.sessionStatusToRespond = sessionStatusToRespond;
    }

    public CertificateChoiceResponse getCertificateChoiceResponseToRespond() {
        return certificateChoiceResponseToRespond;
    }

    public void setCertificateChoiceResponseToRespond(CertificateChoiceResponse certificateChoiceResponseToRespond) {
        this.certificateChoiceResponseToRespond = certificateChoiceResponseToRespond;
    }

    public void setAuthenticationResponseToRespond(AuthenticationResponse authenticationResponseToRespond) {
        this.authenticationResponseToRespond = authenticationResponseToRespond;
    }

    public void setSignatureResponseToRespond(SignatureResponse signatureResponseToRespond) {
        this.signatureResponseToRespond = signatureResponseToRespond;
    }

    public String getSessionIdUsed() {
        return sessionIdUsed;
    }

    public CertificateRequest getCertificateRequestUsed() {
        return certificateRequestUsed;
    }

    public AuthenticationRequest getAuthenticationRequestUsed() {
        return authenticationRequestUsed;
    }

    public SignatureRequest getSignatureRequestUsed() {
        return signatureRequestUsed;
    }

    @Override
    public CertificateChoiceResponse getCertificate(CertificateRequest request) {
        certificateRequestUsed = request;
        return certificateChoiceResponseToRespond;
    }

    @Override
    public SignatureResponse sign(SignatureRequest request) {
        signatureRequestUsed = request;
        return signatureResponseToRespond;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationRequestUsed = request;
        return authenticationResponseToRespond;
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
        sessionIdUsed = request.getSessionID();
        return sessionStatusToRespond;
    }
}
