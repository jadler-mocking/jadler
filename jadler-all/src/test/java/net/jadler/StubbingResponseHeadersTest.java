/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.parameters.StubHttpServerFactory;
import net.jadler.parameters.TestParameters;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.onRequest;
import static net.jadler.utils.TestUtils.jadlerUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


/**
 * Integration test for default response headers.
 */
@RunWith(Parameterized.class)
public class StubbingResponseHeadersTest {

    private final StubHttpServerFactory serverFactory;

    @Parameterized.Parameters
    public static Iterable<StubHttpServerFactory[]> parameters() {
        return new TestParameters().provide();
    }

    public StubbingResponseHeadersTest(final StubHttpServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }


    @Before
    public void setUp() {
        initJadlerUsing(serverFactory.createServer());
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
     * Checks that exactly two default headers (Date and Content-Lenght) are sent in a stub response.
     */
    @Test
    public void allHeaders() throws IOException {
        onRequest().respond().withBody("13 chars long");

        final HttpResponse resp = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(resp.getAllHeaders().length, is(2));
        assertThat(resp.getFirstHeader("Date"), is(notNullValue()));
        assertThat(resp.getFirstHeader("Content-Length").getValue(), is("13"));
    }
}
