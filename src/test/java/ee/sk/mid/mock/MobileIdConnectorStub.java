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

import java.util.ArrayList;
import java.util.List;

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

public class MobileIdConnectorStub implements MidConnector {

    private String sessionIdUsed;
    private List<MidSessionStatus> responses = new ArrayList<>();
    private int responseNumber = 0;

    public String getSessionIdUsed() {
        return sessionIdUsed;
    }

    public List<MidSessionStatus> getResponses() {
        return responses;
    }

    public int getResponseNumber() {
        return responseNumber;
    }

    @Override
    public MidCertificateChoiceResponse getCertificate(MidCertificateRequest request) {
        return null;
    }

    @Override
    public MidSignatureResponse sign(MidSignatureRequest request) {
        return null;
    }

    @Override
    public MidAuthenticationResponse authenticate(MidAuthenticationRequest request) {
        return null;
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
        sessionIdUsed = request.getSessionID();
        return responses.get(responseNumber++);
    }
}
