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

import static ee.sk.mid.mock.SessionStatusDummy.assertCompleteSessionStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createDeliveryErrorStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createInvalidCardResponseStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createMIDNotReadyStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createMSSPTransactionExpiredStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createNotMIDClientStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createResponseRetrievingErrorStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createRunningSessionStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createSignatureHashMismatchStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createSimNotAvailableStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createSuccessfulSessionStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createTimeoutSessionStatus;
import static ee.sk.mid.mock.SessionStatusDummy.createUserCancellationStatus;
import static ee.sk.mid.mock.TestData.AUTHENTICATION_SESSION_PATH;
import static ee.sk.mid.mock.TestData.SESSION_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import ee.sk.mid.exception.DeliveryException;
import ee.sk.mid.exception.InvalidUserConfigurationException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.NotMidClientException;
import ee.sk.mid.exception.PhoneNotAvailableException;
import ee.sk.mid.exception.UserCancellationException;
import ee.sk.mid.mock.MobileIdConnectorStub;
import ee.sk.mid.rest.dao.SessionStatus;
import org.junit.Before;
import org.junit.Test;

public class SessionStatusPollerTest {

    private MobileIdConnectorStub connector;
    private SessionStatusPoller poller;

    @Before
    public void setUp() {
        connector = new MobileIdConnectorStub();
        poller = SessionStatusPoller.newBuilder()
            .withConnector(connector)
            .withPollingSleepTimeoutSeconds(1)
            .withLongPollingTimeoutSeconds(0)
            .build();
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

        poller = SessionStatusPoller.newBuilder()
            .withConnector(connector)
            .withPollingSleepTimeoutSeconds(2)
            .withLongPollingTimeoutSeconds(0)
            .build();

        addMultipleRunningSessionResponses();
        connector.getResponses().add(createSuccessfulSessionStatus());
        long duration = measurePollingDuration();
        System.out.println(duration);

        assertThat(duration > 10000L, is(true));
        assertThat(duration < 10100L, is(true));
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void getUserTimeoutResponse_shouldThrowException() {
        connector.getResponses().add(createTimeoutSessionStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getResponseRetrievingErrorResponse_shouldThrowException() {
        connector.getResponses().add(createResponseRetrievingErrorStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = NotMidClientException.class)
    public void getNotMIDClientResponse_shouldThrowException() {
        connector.getResponses().add(createNotMIDClientStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = MidSessionTimeoutException.class)
    public void getMSSSPTransactionExpiredResponse_shouldThrowException() {
        connector.getResponses().add(createMSSPTransactionExpiredStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = UserCancellationException.class)
    public void getUserCancellationResponse_shouldThrowException() {
        connector.getResponses().add(createUserCancellationStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getMIDNotReadyResponse_shouldThrowException() {
        connector.getResponses().add(createMIDNotReadyStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = PhoneNotAvailableException.class)
    public void getSimNotAvailableResponse_shouldThrowException() {
        connector.getResponses().add(createSimNotAvailableStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = DeliveryException.class)
    public void getDeliveryErrorResponse_shouldThrowException() {
        connector.getResponses().add(createDeliveryErrorStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = DeliveryException.class)
    public void getInvalidCardResponse_shouldThrowException() {
        connector.getResponses().add(createInvalidCardResponseStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = InvalidUserConfigurationException.class)
    public void getSignatureHashMismatchResponse_shouldThrowException() {
        connector.getResponses().add(createSignatureHashMismatchStatus());
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = MidInternalErrorException.class)
    public void getUnknownResult_shouldThrowException() {
        SessionStatus sessionStatus = createSuccessfulSessionStatus();
        sessionStatus.setResult("HACKERMAN");
        connector.getResponses().add(sessionStatus);
        poller.fetchFinalSessionStatus(SESSION_ID, AUTHENTICATION_SESSION_PATH);
    }

    @Test(expected = MidInternalErrorException.class)
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
