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

import ee.sk.mid.DisplayTextFormat;
import ee.sk.mid.HashToSign;
import ee.sk.mid.HashType;
import ee.sk.mid.Language;
import ee.sk.mid.exception.MissingOrInvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAuthSignRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthSignRequestBuilder.class);

    String relyingPartyName;
    String relyingPartyUUID;
    String phoneNumber;
    String nationalIdentityNumber;
    HashToSign hashToSign;
    Language language;
    String displayText;
    DisplayTextFormat displayTextFormat;

    protected AbstractAuthSignRequestBuilder withRelyingPartyUUID(String relyingPartyUUID) {
        this.relyingPartyUUID = relyingPartyUUID;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withRelyingPartyName(String relyingPartyName) {
        this.relyingPartyName = relyingPartyName;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withNationalIdentityNumber(String nationalIdentityNumber) {
        this.nationalIdentityNumber = nationalIdentityNumber;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withHashToSign(HashToSign hashToSign) {
        this.hashToSign = hashToSign;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withLanguage(Language language) {
        this.language = language;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withDisplayText(String displayText) {
        this.displayText = displayText;
        return this;
    }

    public AbstractAuthSignRequestBuilder withDisplayTextFormat(DisplayTextFormat displayTextFormat) {
        this.displayTextFormat = displayTextFormat;
        return this;
    }

    protected HashType getHashType() {
        return hashToSign.getHashType();
    }

    protected String getHashInBase64() {
        return hashToSign.getHashInBase64();
    }

    protected void validateParameters() throws MissingOrInvalidParameterException {
        if (isBlank(phoneNumber) || isBlank(nationalIdentityNumber)) {
            logger.error("Phone number and national identity must be set");
            throw new MissingOrInvalidParameterException("Phone number and national identity must be set");
        }
    }

    protected void validateExtraParameters() throws MissingOrInvalidParameterException {
        if (hashToSign == null) {
            throw new MissingOrInvalidParameterException("hashToSign must be set");
        }
        if (language == null) {
            throw new MissingOrInvalidParameterException("Language for user dialog in mobile phone must be set");
        }
    }
}
