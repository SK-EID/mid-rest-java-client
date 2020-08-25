package ee.sk.mid;

import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static ee.sk.mid.mock.TestData.*;

public class MidClientTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void buildClient_withMissingSslContext_shouldThrowException() {
        expectedEx.expect( MidMissingOrInvalidParameterException.class);
        expectedEx.expectMessage("You need to provide 'sslContext' or 'sslKeyStore' or 'sslCertificates' parameter with certificates of servers that are trusted");

        MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(UNKNOWN_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .build();
    }
}
