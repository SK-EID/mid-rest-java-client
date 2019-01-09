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

import ee.sk.mid.Language;
import ee.sk.mid.SignableData;
import ee.sk.mid.SignableHash;
import ee.sk.mid.exception.MobileIdException;
import ee.sk.mid.exception.ParameterMissingException;

public class SignatureRequestBuilder extends AbstractAuthSignRequestBuilder {

    public SignatureRequestBuilder withRelyingPartyUUID(String relyingPartyUUID) {
        super.withRelyingPartyUUID(relyingPartyUUID);
        return this;
    }

    public SignatureRequestBuilder withRelyingPartyName(String relyingPartyName) {
        super.withRelyingPartyName(relyingPartyName);
        return this;
    }

    public SignatureRequestBuilder withPhoneNumber(String phoneNumber) {
        super.withPhoneNumber(phoneNumber);
        return this;
    }

    public SignatureRequestBuilder withNationalIdentityNumber(String nationalIdentityNumber) {
        super.withNationalIdentityNumber(nationalIdentityNumber);
        return this;
    }

    public SignatureRequestBuilder withSignableData(SignableData dataToSign) {
        super.withSignableData(dataToSign);
        return this;
    }

    public SignatureRequestBuilder withSignableHash(SignableHash hashToSign) {
        super.withSignableHash(hashToSign);
        return this;
    }

    public SignatureRequestBuilder withLanguage(Language language) {
        super.withLanguage(language);
        return this;
    }

    public SignatureRequestBuilder withDisplayText(String displayText) {
        super.withDisplayText(displayText);
        return this;
    }

    public SignatureRequest build() throws MobileIdException {
        validateParameters();
        return createSignatureRequest();
    }

    private SignatureRequest createSignatureRequest() {
        SignatureRequest request = new SignatureRequest();
        request.setRelyingPartyUUID(getRelyingPartyUUID());
        request.setRelyingPartyName(getRelyingPartyName());
        request.setPhoneNumber(getPhoneNumber());
        request.setNationalIdentityNumber(getNationalIdentityNumber());
        request.setHash(getHashInBase64());
        request.setHashType(getHashType());
        request.setLanguage(getLanguage());
        request.setDisplayText(getDisplayText());
        return request;
    }

    protected void validateParameters() throws ParameterMissingException {
        super.validateParameters();
        super.validateExtraParameters();
    }
}
