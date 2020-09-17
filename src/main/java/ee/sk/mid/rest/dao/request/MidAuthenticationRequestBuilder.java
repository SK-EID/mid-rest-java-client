package ee.sk.mid.rest.dao.request;

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

import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidHashToSign;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.exception.MidException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;

public class MidAuthenticationRequestBuilder extends MidAbstractAuthSignRequestBuilder {

    @Override
    public MidAuthenticationRequestBuilder withRelyingPartyUUID(String relyingPartyUUID) {
        super.withRelyingPartyUUID(relyingPartyUUID);
        return this;
    }

    @Override
    public MidAuthenticationRequestBuilder withRelyingPartyName(String relyingPartyName) {
        super.withRelyingPartyName(relyingPartyName);
        return this;
    }

    @Override
    public MidAuthenticationRequestBuilder withPhoneNumber(String phoneNumber) {
        super.withPhoneNumber(phoneNumber);
        return this;
    }

    @Override
    public MidAuthenticationRequestBuilder withNationalIdentityNumber(String nationalIdentityNumber) {
        super.withNationalIdentityNumber(nationalIdentityNumber);
        return this;
    }

    public MidAuthenticationRequestBuilder withHashToSign(MidHashToSign mobileIdAuthenticationHash) {
        super.withHashToSign(mobileIdAuthenticationHash);
        return this;
    }

    @Override
    public MidAuthenticationRequestBuilder withLanguage(MidLanguage language) {
        super.withLanguage(language);
        return this;
    }

    @Override
    public MidAuthenticationRequestBuilder withDisplayText(String displayText) {
        super.withDisplayText(displayText);
        return this;
    }

    @Override
    public MidAuthenticationRequestBuilder withDisplayTextFormat(MidDisplayTextFormat displayTextFormat) {
        super.withDisplayTextFormat(displayTextFormat);
        return this;
    }

    public MidAuthenticationRequest build() throws MidException {
        validateParameters();
        return createAuthenticationRequest();
    }

    private MidAuthenticationRequest createAuthenticationRequest() {
        MidAuthenticationRequest request = new MidAuthenticationRequest();
        request.setRelyingPartyUUID(this.relyingPartyUUID);
        request.setRelyingPartyName(this.relyingPartyName);
        request.setPhoneNumber(this.phoneNumber);
        request.setNationalIdentityNumber(this.nationalIdentityNumber);
        request.setHash(getHashInBase64());
        request.setHashType(getHashType());
        request.setLanguage(this.language);
        request.setDisplayText(this.displayText);
        request.setDisplayTextFormat(this.displayTextFormat);
        return request;
    }

    protected void validateParameters() throws MidMissingOrInvalidParameterException {
        super.validateParameters();
        super.validateExtraParameters();
    }
}
