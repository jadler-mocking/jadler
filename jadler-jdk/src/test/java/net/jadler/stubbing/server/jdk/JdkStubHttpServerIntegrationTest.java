/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests that in works without jadler-all.
 */
public class JdkStubHttpServerIntegrationTest {

    public static final String CONTENT = "Content";

    @Before
    public void initJadler() {
        initJadlerUsing(new JdkStubHttpServer());
    }

    @After
    public void shutdown() {
        closeJadler();
    }

    @Test
    public void shouldMock() throws IOException {
        onRequest().respond().withStatus(201);

        URL url = new URL("http://localhost:" + port());
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        assertThat(urlConnection.getResponseCode(), is(201));
        urlConnection.disconnect();
    }

}