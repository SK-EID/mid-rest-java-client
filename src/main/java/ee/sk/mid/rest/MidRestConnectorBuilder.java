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

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;


public class MidRestConnectorBuilder {
  String endpointUrl;
  Configuration clientConfig;
  Client configuredClient;
  String relyingPartyUUID;
  String relyingPartyName;
  SSLContext sslContext;

  public MidRestConnectorBuilder withEndpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
    return this;
  }

  public MidRestConnectorBuilder withClientConfig(Configuration clientConfig) {
    this.clientConfig = clientConfig;
    return this;
  }

  public MidRestConnectorBuilder withConfiguredClient(Client client) {
    this.configuredClient = client;
    return this;
  }

  public MidRestConnectorBuilder withRelyingPartyUUID(String relyingPartyUUID) {
    this.relyingPartyUUID = relyingPartyUUID;
    return this;
  }

  public MidRestConnectorBuilder withRelyingPartyName(String relyingPartyName) {
    this.relyingPartyName = relyingPartyName;
    return this;
  }

  public MidRestConnectorBuilder withSslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  public MidRestConnector build() {
    return new MidRestConnector(this);
  }
}
