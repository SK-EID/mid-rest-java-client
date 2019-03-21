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

import ee.sk.mid.rest.dao.MidSessionStatus;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SessionStatusDummy {

    public static MidSessionStatus createRunningSessionStatus() {
        MidSessionStatus sessionStatus = new MidSessionStatus();
        sessionStatus.setState("RUNNING");
        return sessionStatus;
    }

    public static MidSessionStatus createSuccessfulSessionStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("OK");
        return sessionStatus;
    }

    public static MidSessionStatus createTimeoutSessionStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("TIMEOUT");
        return sessionStatus;
    }

    public static MidSessionStatus createResponseRetrievingErrorStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("ERROR");
        return sessionStatus;
    }

    public static MidSessionStatus createNotMIDClientStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("NOT_MID_CLIENT");
        return sessionStatus;
    }

    public static MidSessionStatus createMSSPTransactionExpiredStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("EXPIRED_TRANSACTION");
        return sessionStatus;
    }

    public static MidSessionStatus createUserCancellationStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("USER_CANCELLED");
        return sessionStatus;
    }

    public static MidSessionStatus createMIDNotReadyStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("MID_NOT_READY");
        return sessionStatus;
    }

    public static MidSessionStatus createSimNotAvailableStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("PHONE_ABSENT");
        return sessionStatus;
    }

    public static MidSessionStatus createDeliveryErrorStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("DELIVERY_ERROR");
        return sessionStatus;
    }

    public static MidSessionStatus createInvalidCardResponseStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("SIM_ERROR");
        return sessionStatus;
    }

    public static MidSessionStatus createSignatureHashMismatchStatus() {
        MidSessionStatus sessionStatus = createCompleteSessionStatus();
        sessionStatus.setResult("SIGNATURE_HASH_MISMATCH");
        return sessionStatus;
    }

    private static MidSessionStatus createCompleteSessionStatus() {
        MidSessionStatus sessionStatus = new MidSessionStatus();
        sessionStatus.setState("COMPLETE");
        return sessionStatus;
    }

    public static void assertCompleteSessionStatus(MidSessionStatus sessionStatus) {
        assertThat(sessionStatus, is(notNullValue()));
        assertThat(sessionStatus.getState(), is("COMPLETE"));
    }

    public static void assertSuccessfulSessionStatus(MidSessionStatus sessionStatus) {
        assertThat(sessionStatus.getState(), is("COMPLETE"));
        assertThat(sessionStatus.getResult(), is("OK"));
    }

    public static void assertErrorSessionStatus(MidSessionStatus sessionStatus, String result) {
        assertThat(sessionStatus.getState(), is("COMPLETE"));
        assertThat(sessionStatus.getResult(), is(result));
    }
}
