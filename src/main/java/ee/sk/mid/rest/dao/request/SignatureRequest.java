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

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.sk.mid.HashType;
import ee.sk.mid.Language;

import javax.validation.constraints.NotNull;

public class SignatureRequest extends AbstractRequest {

    @NotNull
    private String phoneNumber;

    @NotNull
    private String nationalIdentityNumber;

    @NotNull
    private String hash;

    @NotNull
    private HashType hashType;

    @NotNull
    private Language language;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String displayText;

    @NotNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @NotNull
    public String getNationalIdentityNumber() {
        return nationalIdentityNumber;
    }

    public void setNationalIdentityNumber(@NotNull String nationalIdentityNumber) {
        this.nationalIdentityNumber = nationalIdentityNumber;
    }

    @NotNull
    public String getHash() {
        return hash;
    }

    public void setHash(@NotNull String hash) {
        this.hash = hash;
    }

    @NotNull
    public HashType getHashType() {
        return hashType;
    }

    public void setHashType(@NotNull HashType hashType) {
        this.hashType = hashType;
    }

    @NotNull
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(@NotNull Language language) {
        this.language = language;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    @Override
    public String toString() {
        return "SignatureRequest{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", nationalIdentityNumber='" + nationalIdentityNumber + '\'' +
                ", hash='" + hash + '\'' +
                ", hashType=" + hashType +
                ", language=" + language +
                ", displayText='" + displayText + '\'' +
                '}';
    }

    public static SignatureRequestBuilder newBuilder() {
        return new SignatureRequestBuilder();
    }

}
