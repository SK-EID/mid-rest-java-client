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

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class MidSessionStatusRequest implements Serializable {

    private static final Long serialVersionUID = 1L;

    private String sessionID;

    private int timeoutMs = 0;

    public MidSessionStatusRequest(String sessionID) {
        this.sessionID = sessionID;
    }

    public MidSessionStatusRequest(String sessionID, int timeoutSeconds) {
        this.sessionID = sessionID;
        this.timeoutMs = timeoutSeconds * 1000;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("sessionID", sessionID)
            .append("timeoutMs", timeoutMs)
            .toString();
    }
}
