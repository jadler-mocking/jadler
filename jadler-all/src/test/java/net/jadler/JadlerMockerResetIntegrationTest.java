/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.mocking.Verifying;
import net.jadler.stubbing.RequestStubbing;
import net.jadler.stubbing.server.jetty.JettyStubHttpServer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests that its possible to reset JadlerMocker.
 */
public class JadlerMockerResetIntegrationTest {
    private static final JadlerMocker mocker = new JadlerMocker(new JettyStubHttpServer());

    @BeforeClass
    public static void start() {
        mocker.start();
    }

    @AfterClass
    public static void close() {
        mocker.close();
    }

    @After
    public void reset() {
        mocker.reset();
    }

    @Test
    public void test200() throws IOException {
        onRequest().respond().withStatus(200);
        assertStatus(200);
        verifyThatRequest().havingMethodEqualTo("GET").receivedOnce();
        verifyThatRequest().receivedOnce();
    }

    @Test
    public void test201() throws IOException {
        onRequest().respond().withStatus(201);
        assertStatus(201);
        verifyThatRequest().havingMethodEqualTo("GET").receivedOnce();
        verifyThatRequest().receivedOnce();
    }

    private void assertStatus(int expected) throws IOException {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + mocker.getStubHttpServerPort() + "/");
        assertThat(client.executeMethod(method), is(expected));
        method.releaseConnection();
    }

    private RequestStubbing onRequest() {
        return mocker.onRequest();
    }

    private Verifying verifyThatRequest() {
        return mocker.verifyThatRequest();
    }
}
