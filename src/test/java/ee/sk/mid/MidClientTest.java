package ee.sk.mid;

import static ee.sk.mid.mock.TestData.DEMO_HOST_URL;
import static ee.sk.mid.mock.TestData.DEMO_RELYING_PARTY_UUID;
import static ee.sk.mid.mock.TestData.UNKNOWN_RELYING_PARTY_NAME;

import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MidClientTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void buildClient_withMissingSslContext_shouldThrowException() {
        expectedEx.expect( MidMissingOrInvalidParameterException.class);
        expectedEx.expectMessage("Provide certificates of servers that are trusted by calling exactly one of 'withTrustSslContext()', 'withTrustStore()' or 'withTrustedCertificates()'");

        MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();
    }

    @Test
    public void buildClient_negativeLongPollingTimeoutSeconds_shouldThrowException() {
        expectedEx.expect( MidMissingOrInvalidParameterException.class);
        expectedEx.expectMessage("longPollingTimeoutSeconds must be non-negative number");

        MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withLongPollingTimeoutSeconds(-1)
                .build();
    }

    @Test
    public void buildClient_negativePollingSleepTimeoutSeconds_shouldThrowException() {
        expectedEx.expect( MidMissingOrInvalidParameterException.class);
        expectedEx.expectMessage("pollingSleepTimeoutSeconds must be non-negative number");

        MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withPollingSleepTimeoutSeconds(-100)
                .build();
    }
}
