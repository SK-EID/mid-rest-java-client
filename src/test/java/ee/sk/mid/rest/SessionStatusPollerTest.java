package ee.sk.mid.rest;

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

import ee.sk.mid.exception.*;
import ee.sk.mid.mock.MobileIdConnectorStub;
import ee.sk.mid.rest.dao.SessionStatus;
import org.junit.Before;
import org.junit.Test;

import static ee.sk.mid.mock.SessionStatusDummy.*;
import static ee.sk.mid.mock.TestData.AUTHENTICATION_SESSION_PATH;
import static ee.sk.mid.mock.TestData.SESSION_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SessionStatusPollerTest {

    private MobileIdConnectorStub connector;
    private SessionStatusPoller poller;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorStub();
        poller = new SessionStatusPoller(connector);
        poller.setPollingSleepTimeSeconds(1);
    }

    @Test
    public void getFirstCompleteResponse() {
        connector.getResponses().add(createSuccessfulSessionStatus());
        SessionStatus sessionStatus = poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);

        assertThat(connector.getSessionIdUsed(), is(SESSION_ID));
        assertThat(connector.getResponseNumber(), is(1));
        assertCompleteSessionStatus(sessionStatus);
    }

    @Test
    public void pollAndGetThirdCompleteResponse() {
        connector.getResponses().add(createRunningSessionStatus());
        connector.getResponses().add(createRunningSessionStatus());
        connector.getResponses().add(createSuccessfulSessionStatus());
        SessionStatus sessionStatus = poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);

        assertThat(connector.getResponseNumber(), is(3));
        assertCompleteSessionStatus(sessionStatus);
    }

    @Test
    public void setPollingSleepTime() {
        poller.setPollingSleepTimeSeconds(2);
        addMultipleRunningSessionResponses();
        connector.getResponses().add(createSuccessfulSessionStatus());
        long duration = measurePollingDuration();
        System.out.println(duration);

        assertThat(duration > 10000L, is(true));
        assertThat(duration < 10100L, is(true));
    }

    @Test(expected = SessionTimeoutException.class)
    public void getUserTimeoutResponse_shouldThrowException() {
        connector.getResponses().add(createTimeoutSessionStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = ResponseRetrievingException.class)
    public void getResponseRetrievingErrorResponse_shouldThrowException() {
        connector.getResponses().add(createResponseRetrievingErrorStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = NotMIDClientException.class)
    public void getNotMIDClientResponse_shouldThrowException() {
        connector.getResponses().add(createNotMIDClientStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = ExpiredException.class)
    public void getMSSSPTransactionExpiredResponse_shouldThrowException() {
        connector.getResponses().add(createMSSPTransactionExpiredStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = UserCancellationException.class)
    public void getUserCancellationResponse_shouldThrowException() {
        connector.getResponses().add(createUserCancellationStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = MIDNotReadyException.class)
    public void getMIDNotReadyResponse_shouldThrowException() {
        connector.getResponses().add(createMIDNotReadyStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = SimNotAvailableException.class)
    public void getSimNotAvailableResponse_shouldThrowException() {
        connector.getResponses().add(createSimNotAvailableStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = DeliveryException.class)
    public void getDeliveryErrorResponse_shouldThrowException() {
        connector.getResponses().add(createDeliveryErrorStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = InvalidCardResponseException.class)
    public void getInvalidCardResponse_shouldThrowException() {
        connector.getResponses().add(createInvalidCardResponseStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = SignatureHashMismatchException.class)
    public void getSignatureHashMismatchResponse_shouldThrowException() {
        connector.getResponses().add(createSignatureHashMismatchStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = TechnicalErrorException.class)
    public void getUnknownResult_shouldThrowException() {
        SessionStatus sessionStatus = createSuccessfulSessionStatus();
        sessionStatus.setResult("HACKERMAN");
        connector.getResponses().add(sessionStatus);
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = TechnicalErrorException.class)
    public void getMissingResult_shouldThrowException() {
        SessionStatus sessionStatus = createSuccessfulSessionStatus();
        sessionStatus.setResult(null);
        connector.getResponses().add(sessionStatus);
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    private long measurePollingDuration() {
        long startTime = System.currentTimeMillis();
        SessionStatus sessionStatus = poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
        assertCompleteSessionStatus(sessionStatus);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private void addMultipleRunningSessionResponses() {
        for (int i = 0; i < 5; i++)
            connector.getResponses().add(createRunningSessionStatus());
    }
}
