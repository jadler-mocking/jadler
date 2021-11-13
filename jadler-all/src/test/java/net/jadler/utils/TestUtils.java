/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

import static net.jadler.Jadler.port;


/**
 * Various handy functions and classes useful for testing.
 */
public class TestUtils {

    /**
     * An http response handler which retrieves just the response status code value.
     */
    public static final StatusRetriever STATUS_RETRIEVER = new StatusRetriever();

    public static class StatusRetriever implements ResponseHandler<Integer> {

        @Override
        public Integer handleResponse(final HttpResponse response) {
            return response.getStatusLine().getStatusCode();
        }

    }


    /**
     * @return URI of the Jadler stub server
     */
    public static String jadlerUri() {
        return "http://localhost:" + port();
    }


    /**
     * Reads the body of an http response. Please use this function if and only if the {@code Content-Type} header is
     * not empty and the {@code charset} parameter of the header is set.
     *
     * @param response an hhtp response to read the body from
     * @return
     * @throws IOException if some IO problems occurs
     */
    public static String stringBodyOf(final HttpResponse response) throws IOException {
        final HttpEntity body = response.getEntity();
        final String charset = body.getContentType().getElements()[0].getParameterByName("charset").getValue();

        return IOUtils.toString(body.getContent(), charset);
    }


    /**
     * @param response an http response to read the raw body from
     * @return raw body retrieved from the response
     * @throws IOException if some IO problems occurs
     */
    public static byte[] rawBodyOf(final HttpResponse response) throws IOException {
        return IOUtils.toByteArray(response.getEntity().getContent());
    }
}
