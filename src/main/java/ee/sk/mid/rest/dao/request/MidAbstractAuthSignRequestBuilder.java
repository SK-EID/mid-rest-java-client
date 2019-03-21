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

import static org.apache.commons.lang3.StringUtils.isBlank;

import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidHashToSign;
import ee.sk.mid.MidHashType;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MidAbstractAuthSignRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger( MidAbstractAuthSignRequestBuilder.class);

    String relyingPartyName;
    String relyingPartyUUID;
    String phoneNumber;
    String nationalIdentityNumber;
    MidHashToSign hashToSign;
    MidLanguage language;
    String displayText;
    MidDisplayTextFormat displayTextFormat;

    protected MidAbstractAuthSignRequestBuilder withRelyingPartyUUID(String relyingPartyUUID) {
        this.relyingPartyUUID = relyingPartyUUID;
        return this;
    }

    protected MidAbstractAuthSignRequestBuilder withRelyingPartyName(String relyingPartyName) {
        this.relyingPartyName = relyingPartyName;
        return this;
    }

    protected MidAbstractAuthSignRequestBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    protected MidAbstractAuthSignRequestBuilder withNationalIdentityNumber(String nationalIdentityNumber) {
        this.nationalIdentityNumber = nationalIdentityNumber;
        return this;
    }

    protected MidAbstractAuthSignRequestBuilder withHashToSign(MidHashToSign hashToSign) {
        this.hashToSign = hashToSign;
        return this;
    }

    protected MidAbstractAuthSignRequestBuilder withLanguage(MidLanguage language) {
        this.language = language;
        return this;
    }

    protected MidAbstractAuthSignRequestBuilder withDisplayText(String displayText) {
        this.displayText = displayText;
        return this;
    }

    public MidAbstractAuthSignRequestBuilder withDisplayTextFormat(MidDisplayTextFormat displayTextFormat) {
        this.displayTextFormat = displayTextFormat;
        return this;
    }

    protected MidHashType getHashType() {
        return hashToSign.getHashType();
    }

    protected String getHashInBase64() {
        return hashToSign.getHashInBase64();
    }

    protected void validateParameters() throws MidMissingOrInvalidParameterException {
        if (isBlank(phoneNumber) || isBlank(nationalIdentityNumber)) {
            logger.error("Phone number and national identity must be set");
            throw new MidMissingOrInvalidParameterException("Phone number and national identity must be set");
        }
    }

    protected void validateExtraParameters() throws MidMissingOrInvalidParameterException {
        if (hashToSign == null) {
            throw new MidMissingOrInvalidParameterException("hashToSign must be set");
        }
        if (language == null) {
            throw new MidMissingOrInvalidParameterException("MidLanguage for user dialog in mobile phone must be set");
        }
    }
}
