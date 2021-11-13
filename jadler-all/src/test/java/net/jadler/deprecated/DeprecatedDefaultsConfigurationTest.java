/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.deprecated;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.verifyThatRequest;
import static net.jadler.utils.TestUtils.jadlerUri;
import static net.jadler.utils.TestUtils.rawBodyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Tests that the deprecated way of configuring jadler defaults still works.
 */
public class DeprecatedDefaultsConfigurationTest {


    private static final int EXPECTED_STATUS = 409;
    private static final String EXPECTED_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final Charset EXPECTED_ENCODING = Charset.forName("ISO-8859-2");
    private static final String EXPECTED_HEADER_NAME = "default_header";
    private static final String EXPECTED_HEADER_VALUE = "value";
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte) 0xE1, (byte) 0xF8, (byte) 0xBE};


    @AfterClass
    public static void cleanup() {
        Executor.closeIdleConnections();
    }


    /*
     * Tests that the deprecated way of setting response defaults using the {@code that()} clause still works
     */
    @Test
    @SuppressWarnings("deprecation")
    public void ongoingConfiguration() throws IOException {
        initJadler()
                .that()
                .respondsWithDefaultStatus(EXPECTED_STATUS)
                .respondsWithDefaultContentType(EXPECTED_CONTENT_TYPE)
                .respondsWithDefaultEncoding(EXPECTED_ENCODING)
                .respondsWithDefaultHeader(EXPECTED_HEADER_NAME, EXPECTED_HEADER_VALUE);

        try {
            onRequest().respond().withBody(STRING_WITH_DIACRITICS);

            final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

            assertThat(response.getStatusLine().getStatusCode(), is(EXPECTED_STATUS));
            assertThat(response.getFirstHeader("Content-Type").getValue(), is(EXPECTED_CONTENT_TYPE));
            assertThat(response.getFirstHeader(EXPECTED_HEADER_NAME).getValue(), is(EXPECTED_HEADER_VALUE));
            assertThat(rawBodyOf(response), is(ISO_8859_2_REPRESENTATION));
        } finally {
            closeJadler();
        }
    }


    /*
     * Tests that the deprecated way of disabling requests recording using the {@code that()} clause still works
     */
    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("deprecation")
    public void ongoingConfiguration_skipsRequestRecording() throws IOException {
        initJadler().that().skipsRequestsRecording();

        try {
            verifyThatRequest();
            fail("request recording disabled, verification must fail");
        } finally {
            closeJadler();
        }
    }
}
