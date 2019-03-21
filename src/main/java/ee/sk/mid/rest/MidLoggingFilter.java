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

import org.glassfish.jersey.message.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;

public class MidLoggingFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    private static final Logger logger = LoggerFactory.getLogger( MidLoggingFilter.class);
    private static final String LOGGING_OUTPUT_STREAM_PROPERTY = "loggingOutputStream";

    @Override
    public void filter(ClientRequestContext requestContext) {
        if (logger.isDebugEnabled()) {
            logUrl(requestContext);
        }
        if (logger.isTraceEnabled()) {
            logHeaders(requestContext);
            if (requestContext.hasEntity()) {
                wrapEntityStreamWithLogger(requestContext);
            }
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response status: " + responseContext.getStatus() + " - " + responseContext.getStatusInfo());
        }
        if (logger.isTraceEnabled() && responseContext.hasEntity()) {
            logResponseBody(responseContext);
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        context.proceed();
        if (logger.isTraceEnabled()) {
            logRequestBody(context);
        }
    }

    private void logUrl(ClientRequestContext requestContext) {
        String method = requestContext.getMethod();
        URI uri = requestContext.getUri();
        logger.debug(method + " " + uri.toString());
    }

    private void logHeaders(ClientRequestContext requestContext) {
        MultivaluedMap<String, String> headers = requestContext.getStringHeaders();
        if (headers != null) {
            logger.trace("Request headers: " + headers.toString());
        }
    }

    private void wrapEntityStreamWithLogger(ClientRequestContext requestContext) {
        OutputStream entityStream = requestContext.getEntityStream();
        MidLoggingOutputStream loggingOutputStream = new MidLoggingOutputStream(entityStream);
        requestContext.setEntityStream(loggingOutputStream);
        requestContext.setProperty(LOGGING_OUTPUT_STREAM_PROPERTY, loggingOutputStream);
    }

    private void logResponseBody(ClientResponseContext responseContext) throws IOException {
        Charset charset = MessageUtils.getCharset(responseContext.getMediaType());
        InputStream entityStream = responseContext.getEntityStream();
        byte[] bodyBytes = readInputStreamBytes(entityStream);
        responseContext.setEntityStream(new ByteArrayInputStream(bodyBytes));
        logger.trace("Response body: " + new String(bodyBytes, charset));
    }

    private byte[] readInputStreamBytes(InputStream entityStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = entityStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toByteArray();
    }

    private void logRequestBody(WriterInterceptorContext context) {
        MidLoggingOutputStream loggingOutputStream = (MidLoggingOutputStream) context.getProperty(LOGGING_OUTPUT_STREAM_PROPERTY);
        if (loggingOutputStream != null) {
            Charset charset = MessageUtils.getCharset(context.getMediaType());
            byte[] bytes = loggingOutputStream.getBytes();
            logger.trace("Message body: " + new String(bytes, charset));
        }
    }
}
