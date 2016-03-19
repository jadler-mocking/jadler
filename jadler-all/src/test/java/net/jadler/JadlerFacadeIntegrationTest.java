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
 * Tests the {@link Jadler} facade.
 */
public class JadlerFacadeIntegrationTest {
    
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
    public void doubleInitialization() {
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
     * The closeJadler() method will be most often called in a tearDown method of a test suite so it doesn't
     * fail if the initialization failed (simulated here by not calling the init at all).
     */
    @Test
    public void closeWithoutInitialization() {
        closeJadler();
    }


    /*
     * The resetJadler() method will be most often called in a @After method of a test suite so it doesn't
     * fail if the initialization failed (simulated here by not calling the init at all).
     */
    @Test
    public void resetWithoutInitialization() {
        resetJadler();
    }

    
    /**
     * port() must be called after initialization
     */
    @Test(expected=IllegalStateException.class)
    public void portBeforeInitialization() {
        port();
        fail("cannot get the port value now, Jadler hasn't been initialized yet");
    }
    
    
    /*
     * onRequest() must be called after initialization
     */
    @Test(expected=IllegalStateException.class)
    public void onRequestBeforeInitialization() {
        onRequest();
        fail("cannot do stubbing, Jadler hasn't been initialized yet");
    }

    
    /*
     * onRequest() must be called after initialization
     */
    @Test(expected=IllegalStateException.class)
    public void verifyThatRequestBeforeInitialization() {
        verifyThatRequest();
        fail("cannot do verification, Jadler hasn't been initialized yet");
    }
    
    
    /*
     * Just inits Jadler without any additional configuration and tests everything works fine.
     */
    @Test
    public void noConfiguration() throws IOException {
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
    public void portConfiguration() throws IOException {
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
    public void serverConfiguration() throws IOException {
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
     * Tests the additional defaults configuration option.
     */
    @Test
    public void ongoingConfiguration() throws IOException {
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
     * Tests the request recording skipping set via the facade
     */
    @Test(expected=IllegalStateException.class)
    public void ongoingConfiguration_withRequestsRecordingDisabled() {
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
    public void testResetJadler() throws IOException {
        initJadler();

        try {
            onRequest().respond().withStatus(EXPECTED_STATUS);
            assertExpectedStatus();

            resetJadler();

            onRequest().respond().withStatus(201);
            assertExpectedStatus(201);
        }
        finally {
            closeJadler();
        }
    }


    private void assertExpectedStatus() throws IOException {
        assertExpectedStatus(EXPECTED_STATUS);
    }

    private void assertExpectedStatus(final int expectedStatus) throws IOException {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        assertThat(client.executeMethod(method), is(expectedStatus));
        method.releaseConnection();
    }
}
