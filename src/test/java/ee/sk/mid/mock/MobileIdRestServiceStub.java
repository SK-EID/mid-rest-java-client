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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;

public class MobileIdRestServiceStub {

    public static void stubNotFoundResponse(String url) {
        stubFor(get(urlEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Not found")));
    }

    public static void stubRequestWithResponse(String url, String responseFile, int delayInMillis) {
        stubFor(get(urlPathEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withFixedDelay(delayInMillis)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFileBody(responseFile))));
    }

    public static void stubRequestWithResponse(String url, String responseFile, int delayInMillis, String oldState, String newState) {
        stubFor(get(urlPathEqualTo(url))
                .inScenario("DEFAULT")
                .whenScenarioStateIs(oldState)
                .willSetStateTo(newState)
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withFixedDelay(delayInMillis)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFileBody(responseFile))));
    }

    public static void stubRequestWithResponse(String url, String responseFile) {
        stubRequestWithResponse(url, responseFile, 0);
    }

    public static void stubRequestWithResponse(String url, String requestFile, String responseFile) {
        stubRequestWithResponse(url, requestFile, responseFile, 0);
    }

    public static void stubRequestWithResponse(String url, String requestFile, String responseFile, int delayInMillies) {
        stubFor(post(urlEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .withRequestBody(equalToJson(readFileBody(requestFile)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayInMillies)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFileBody(responseFile))));
    }

    public static void stubSessionStatusWithState(String url, String responseFile, String startState, String endState, int delayMilliseconds) {
        stubFor(get(urlEqualTo(url))
                .inScenario("Session status")
                .whenScenarioStateIs(startState)
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayMilliseconds)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFileBody(responseFile)))
                .willSetStateTo(endState)
        );
    }

    public static void stubInternalServerErrorResponse(String url, String requestFile) {
        stubErrorResponse(url, requestFile, 500);
    }

    public static void stubNotFoundResponse(String url, String requestFile) {
        stubErrorResponse(url, requestFile, 404);
    }

    public static void stubUnauthorizedResponse(String url, String requestFile) {
        stubErrorResponse(url, requestFile, 401);
    }

    public static void stubBadRequestResponse(String url, String requestFile) {
        stubErrorResponse(url, requestFile, 400, "{\"error\": \"Invalid phoneNumber or nationalIdentityNumber\"}");
    }

    private static void stubErrorResponse(String url, String requestFile, int errorStatus) {
        stubFor(post(urlEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .withRequestBody(equalToJson(readFileBody(requestFile)))
                .willReturn(aResponse()
                        .withStatus(errorStatus)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Some body")));
    }

    private static void stubErrorResponse(String url, String requestFile, int errorStatus, String body) {
        stubFor(post(urlEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .withRequestBody(equalToJson(readFileBody(requestFile)))
                .willReturn(aResponse()
                        .withStatus(errorStatus)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static String readFileBody(String fileName) {
        ClassLoader classLoader = MobileIdRestServiceStub.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        assertNotNull("File not found: " + fileName, resource);
        File file = new File(resource.getFile());
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("Reading file failed");
        }
    }
}
