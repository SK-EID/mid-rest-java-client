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

import ee.sk.mid.HashType;
import ee.sk.mid.Language;
import ee.sk.mid.SignableData;
import ee.sk.mid.SignableHash;
import ee.sk.mid.exception.ParameterMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class AbstractAuthSignRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthSignRequestBuilder.class);

    private String relyingPartyName;
    private String relyingPartyUUID;
    private String phoneNumber;
    private String nationalIdentityNumber;
    private SignableData dataToSign;
    private SignableHash hashToSign;
    private Language language;
    private String displayText;

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

    protected AbstractAuthSignRequestBuilder withSignableData(SignableData dataToSign) {
        this.dataToSign = dataToSign;
        return this;
    }

    protected AbstractAuthSignRequestBuilder withSignableHash(SignableHash hashToSign) {
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

    protected String getRelyingPartyUUID() {
        return relyingPartyUUID;
    }

    protected String getRelyingPartyName() {
        return relyingPartyName;
    }

    protected String getPhoneNumber() {
        return phoneNumber;
    }

    protected String getNationalIdentityNumber() {
        return nationalIdentityNumber;
    }

    protected HashType getHashType() {
        if (hashToSign != null) {
            return hashToSign.getHashType();
        }
        return dataToSign.getHashType();
    }

    protected String getHashInBase64() {
        if (hashToSign != null) {
            return hashToSign.getHashInBase64();
        }
        return dataToSign.calculateHashInBase64();
    }

    protected Language getLanguage() {
        return language;
    }

    protected String getDisplayText() {
        return displayText;
    }

    protected boolean isSignableHashSet() {
        return hashToSign == null || !hashToSign.areFieldsFilled();
    }

    protected boolean isSignableDataSet() {
        return dataToSign == null;
    }

    protected boolean isLanguageSet() {
        return language == null;
    }

    protected void validateParameters() throws ParameterMissingException {
        if (isBlank(relyingPartyUUID)) {
            logger.error("Relying Party UUID parameter must be set");
            throw new ParameterMissingException("Relying Party UUID parameter must be set");
        }
        if (isBlank(relyingPartyName)) {
            logger.error("Relying Party Name parameter must be set");
            throw new ParameterMissingException("Relying Party Name parameter must be set");
        }
        if (isBlank(phoneNumber) || isBlank(nationalIdentityNumber)) {
            logger.error("Phone number and national identity must be set");
            throw new ParameterMissingException("Phone number and national identity must be set");
        }
    }

    protected void validateExtraParameters() throws ParameterMissingException {
        if (isSignableHashSet() && isSignableDataSet()) {
            logger.error("Signable data or hash with hash type must be set");
            throw new ParameterMissingException("Signable data or hash with hash type must be set");
        }
        if (isLanguageSet()) {
            logger.error("Language for user dialog in mobile phone must be set");
            throw new ParameterMissingException("Language for user dialog in mobile phone must be set");
        }
    }
}
