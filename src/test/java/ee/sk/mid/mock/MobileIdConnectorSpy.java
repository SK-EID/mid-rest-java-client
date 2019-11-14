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

import javax.net.ssl.SSLContext;

import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.MidSessionStatusPoller;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidCertificateRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import ee.sk.mid.rest.dao.response.MidCertificateChoiceResponse;
import ee.sk.mid.rest.dao.response.MidSignatureResponse;

public class MobileIdConnectorSpy implements MidConnector {

    private MidSessionStatus sessionStatusToRespond;
    private MidCertificateChoiceResponse certificateChoiceResponseToRespond;
    private MidAuthenticationResponse authenticationResponseToRespond;
    private MidSignatureResponse signatureResponseToRespond;


    public MidSessionStatus getSessionStatusToRespond() {
        return sessionStatusToRespond;
    }

    public void setSessionStatusToRespond(MidSessionStatus sessionStatusToRespond) {
        this.sessionStatusToRespond = sessionStatusToRespond;
    }

    public MidCertificateChoiceResponse getCertificateChoiceResponseToRespond() {
        return certificateChoiceResponseToRespond;
    }

    public void setCertificateChoiceResponseToRespond(MidCertificateChoiceResponse certificateChoiceResponseToRespond) {
        this.certificateChoiceResponseToRespond = certificateChoiceResponseToRespond;
    }

    public void setAuthenticationResponseToRespond(MidAuthenticationResponse authenticationResponseToRespond) {
        this.authenticationResponseToRespond = authenticationResponseToRespond;
    }

    public void setSignatureResponseToRespond(MidSignatureResponse signatureResponseToRespond) {
        this.signatureResponseToRespond = signatureResponseToRespond;
    }

    @Override
    public MidCertificateChoiceResponse getCertificate(MidCertificateRequest request) {
        return certificateChoiceResponseToRespond;
    }

    @Override
    public MidSignatureResponse sign(MidSignatureRequest request) {
        return signatureResponseToRespond;
    }

    @Override
    public MidAuthenticationResponse authenticate(MidAuthenticationRequest request) {
        return authenticationResponseToRespond;
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
        return sessionStatusToRespond;
    }

    @Override
    public void setSslContext(SSLContext sslContext) {

    }
}
