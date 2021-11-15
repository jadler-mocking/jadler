/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.utils.TestUtils.jadlerUri;
import static net.jadler.utils.TestUtils.rawBodyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * Test suite for response super defaults (default response status and encoding values used when not defined at all).
 */
public class SuperDefaultsIntegrationTest {

    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";

    @Before
    public void setUp() {
        initJadler(); //no defaults for the response status nor encoding set here
    }


    @After
    public void tearDown() {
        closeJadler();
    }


    @AfterClass
    public static void cleanup() {
        Executor.closeIdleConnections();
    }


    /*
     * When no defaults (response status and encoding) are set during Jadler initialization nor the status and encoding
     * values are provided during stubbing super-defaults (200, UTF-8) are used.
     */
    @Test
    public void superDefaults() throws IOException {
        //no values for the response status nor encoding set here
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);

        final HttpResponse response = Executor.newInstance()
                .execute(Request.Post(jadlerUri()).bodyString("postbody", null)).returnResponse();

        assertThat(response.getStatusLine().getStatusCode(), is(200));

        //the response body is decodable correctly using UTF-8
        assertThat(rawBodyOf(response), is(STRING_WITH_DIACRITICS.getBytes(Charset.forName("UTF-8"))));
    }
}
