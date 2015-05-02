/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import net.jadler.stubbing.server.jetty.JettyStubHttpServer;
import org.springframework.util.SocketUtils;

import static net.jadler.Jadler.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;


/**
 * Tests the {@link Jadler} facade.
 */
public class JadlerFacadeIntegrationTest {
    
    private static final int EXPECTED_STATUS = 204;
    
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
     * the closeJadler() method will be most often called in a tearDown method of a test suite so it doesn't
     * fail if the initialization failed (simulated here by not calling the init at all).
     */
    public void closeWithoutInitialization() {
        closeJadler();
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
    public void additionalConfiguration() throws IOException {
        initJadler()
                .that()
                .respondsWithDefaultStatus(200)
                .respondsWithDefaultContentType("text/plain")
                .respondsWithDefaultEncoding(Charset.forName("ISO-8859-1"))
                .respondsWithDefaultHeader("default_header", "value");
        
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
    @Test(expected=IllegalStateException.class)
    public void additionalConfiguration_skipRequestRecording() throws IOException {
        initJadler().that().skipsRequestsRecording();
        
        try {
            verifyThatRequest();
            fail("request recording disabled, verification must fail");
        }
        finally {
            closeJadler();
        }
    }

    
    private void assertExpectedStatus() throws IOException {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        assertThat(client.executeMethod(method), is(EXPECTED_STATUS));
        method.releaseConnection();
    }
}
