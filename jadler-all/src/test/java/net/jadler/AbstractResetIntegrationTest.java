/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import java.io.IOException;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.verifyThatRequest;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.resetJadler;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * <p>Tests the alternative reset scenario. The stub server is started and stopped only once (before and after the
 * whole suite) and reset after every test.</p>
 * 
 * <p>Please note this test class is abstract. The particular extensions must provide their own {@code BeforeClass}
 * method which initializes Jadler using the desired stub server and instructing it to use {@link #DEFAULT_STATUS} as
 * a default response status.</p>
 * 
 * @see JdkResetIntegrationTest
 * @see JettyResetIntegrationTest
 */
public abstract class AbstractResetIntegrationTest {
    
    protected static final int DEFAULT_STATUS = 409;

    /**
     * Shutdown Jadler after the whole test suite
     */
    @AfterClass
    public static void close() {
        closeJadler();
    }


    /**
     * Reset Jadler after each test
     * 
     * @see Jadler#resetJadler()
     */
    @After
    public void reset() {
        resetJadler();
    }


    /*
     * Tests this test is independent on the rest of the tests (e.g. Jadler has been correctly reset,
     * all previously recorded requests have been deleted as well as all previously created stubs)
     */
    @Test
    public void test200() throws IOException {
        onRequest().respond().withStatus(200);
        
        assertStatus(200);
        verifyThatRequest().receivedOnce();
    }

    
    /*
     * Tests this test is independent on the rest of the tests (e.g. Jadler has been correctly reset,
     * all previously recorded requests have been deleted as well as all previously created stubs)
     */
    @Test
    public void test201() throws IOException {
        onRequest().respond().withStatus(201);
        
        assertStatus(201);
        verifyThatRequest().receivedOnce();
    }

    
    /*
     * Checks this test is independent on the rest of the tests (e.g. Jadler has been correctly reset,
     * all previously recorded requests have been deleted as well as all previously created stubs)
     * and that the reset method does <strong>not</strong> delete the default status configuration.
     */
    @Test
    public void testDefault() throws IOException {
        onRequest().respond(); //no status set, the default one must be used
        
        assertStatus(DEFAULT_STATUS);
        verifyThatRequest().receivedOnce();
    }
    
    
    /* sends a GET request and asserts the response status code is as expected */
    private void assertStatus(int expected) throws IOException {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        assertThat(client.executeMethod(method), is(expected));
        method.releaseConnection();
    }
}
