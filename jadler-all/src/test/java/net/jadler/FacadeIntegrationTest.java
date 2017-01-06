/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jetty.JettyStubHttpServer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.springframework.util.SocketUtils;
import java.io.IOException;
import java.nio.charset.Charset;

import static net.jadler.Jadler.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * Integration tests of the {@link Jadler} facade.
 */
public class FacadeIntegrationTest {
    
    private static final int EXPECTED_STATUS = 409;
    private static final String EXPECTED_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final Charset EXPECTED_ENCODING = Charset.forName("ISO-8859-2");
    private static final String EXPECTED_HEADER_NAME = "default_header";
    private static final String EXPECTED_HEADER_VALUE = "value";
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};
    
    /*
     * initialization cannot be called twice without closing jadler between the calls
     */
    @Test(expected=IllegalStateException.class)
    public void initJadler_doubleInitialization() {
        try {
            initJadler();
            initJadler();
        }
        finally {
            closeJadler();
        }
        
        fail("jadler cannot be initialized twice");
    }
    
    
    /*
     * <p>The {@link Jadler#closeJadler()} method doesn't fail even if Jadler hasn't been initialized
     * properly before.</p>
     *
     * <p>The reason is the {@link Jadler#closeJadler()} method will be called in a <em>tearDown</em> method
     * of a test suite most often so it mustn't fail if the initialization failed before (simulated here
     * by not calling the initialization at all).</p>
     */
    @Test
    public void closeJadler_beforeInitialization() {
        closeJadler();
    }


    /*
     * {@link Jadler#resetJadler()} doesn't fail even if Jadler hasn't been initialized
     * properly before.
     */
    @Test
    public void resetJadler_beforeInitialization() {
        resetJadler();
    }

    
    /*
     * {@link Jadler#port()} fails if Jadler hasn't been initialized before
     */
    @Test(expected=IllegalStateException.class)
    public void port_beforeInitialization() {
        port();
        fail("cannot get the port value now, Jadler hasn't been initialized yet");
    }
    
    
    /*
     * {@link Jadler#onRequest()} must be called after initialization
     */
    @Test(expected=IllegalStateException.class)
    public void onRequest_beforeInitialization() {
        onRequest();
        fail("cannot do stubbing, Jadler hasn't been initialized yet");
    }

    
    /*
     * {@link Jadler#verifyThatRequest()} must be called after initialization
     */
    @Test(expected=IllegalStateException.class)
    public void verifyThatRequest_beforeInitialization() {
        verifyThatRequest();
        fail("cannot do verification, Jadler hasn't been initialized yet");
    }
    
    
    /*
     * Just inits Jadler without any additional configuration and tests everything works fine.
     */
    @Test
    public void standardConfigurationScenario() throws IOException {
        initJadler();
        
        try {
            onRequest().respond().withStatus(EXPECTED_STATUS);
            assertExpectedStatus();
        }
        finally {
            closeJadler();
        }
    }

    
    /*
     * Inits Jadler to start the default stub server on a specific port and tests everything works fine.
     */
    @Test
    public void portConfigurationScenario() throws IOException {
        initJadlerListeningOn(SocketUtils.findAvailableTcpPort());
        
        try {
            onRequest().respond().withStatus(EXPECTED_STATUS);
            assertExpectedStatus();
        }
        finally {
            closeJadler();
        }
    }
    
    
    /*
     * Inits Jadler to use the given stub server and tests everything works fine.
     */
    @Test
    public void serverConfigurationScenario() throws IOException {
        initJadlerUsing(new JettyStubHttpServer());
        
        try {
            onRequest().respond().withStatus(EXPECTED_STATUS);
            assertExpectedStatus();
        }
        finally {
            closeJadler();
        }
    }
    
    
    /*
     * Tests the additional defaults (the response status, content type, encoding and headers) configuration options.
     */
    @Test
    public void responseDefaultsConfigurationScenario() throws IOException {
        initJadler()
                .withDefaultResponseStatus(EXPECTED_STATUS)
                .withDefaultResponseContentType(EXPECTED_CONTENT_TYPE)
                .withDefaultResponseEncoding(EXPECTED_ENCODING)
                .withDefaultResponseHeader(EXPECTED_HEADER_NAME, EXPECTED_HEADER_VALUE);
        
        try {
            onRequest().respond().withBody(STRING_WITH_DIACRITICS);
            
            final HttpClient client = new HttpClient();
            final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
            client.executeMethod(method);

            assertThat(method.getStatusCode(), is(EXPECTED_STATUS));
            assertThat(method.getResponseHeader("Content-Type").getValue(), is(EXPECTED_CONTENT_TYPE));
            assertThat(method.getResponseHeader(EXPECTED_HEADER_NAME).getValue(), is(EXPECTED_HEADER_VALUE));
            assertThat(method.getResponseBody(), is(ISO_8859_2_REPRESENTATION));
            
            method.releaseConnection();
        }
        finally {
            closeJadler();
        }
    }
    
    
    /*
     * Tests the request recording skipping works scenario.
     */
    @Test(expected=IllegalStateException.class)
    public void requestsRecordingConfigurationScenario() {
        initJadler().withRequestsRecordingDisabled();
        
        try {
            verifyThatRequest();
            fail("request recording disabled, verification must fail");
        }
        finally {
            closeJadler();
        }
    }


    /*
     * Resets Jadler and tests everything works fine.
     */
    @Test
    public void resetScenario() throws IOException {
        initJadler();

        try {
            onRequest().respond().withStatus(202);
            assertExpectedStatus(202);
            verifyThatRequest().receivedOnce();

            resetJadler();

            onRequest().respond().withStatus(201);
            assertExpectedStatus(201);
            verifyThatRequest().receivedOnce();
        }
        finally {
            closeJadler();
        }
    }


    private void assertExpectedStatus() throws IOException {
        assertExpectedStatus(EXPECTED_STATUS);
    }

    
    /*
     * Sends a GET request and asserts the response status is as expected
     */
    private void assertExpectedStatus(final int expectedStatus) throws IOException {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        assertThat(client.executeMethod(method), is(expectedStatus));
        method.releaseConnection();
    }
}
