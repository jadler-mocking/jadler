/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.io.IOException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.junit.After;
import org.junit.Before;
import net.jadler.parameters.TestParameters;
import net.jadler.parameters.StubHttpServerFactory;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.closeJadler;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.notNullValue;


/**
 * Integration test for default response headers.
 */
@RunWith(Parameterized.class)
public class StubbingResponseHeadersTest {
    
    private HttpClient client;
    
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
        this.client = new HttpClient();
        
        initJadlerUsing(serverFactory.createServer());
    }
    
    
    @After
    public void tearDown() {
        closeJadler();
    }
    
    
    /*
     * Checks that exactly two default headers (Date and Content-Lenght) are sent in a stub response. 
     */
    @Test
    public void allHeaders() throws IOException {
        onRequest().respond().withBody("13 chars long");
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        client.executeMethod(method);

        final Header[] responseHeaders = method.getResponseHeaders();
        
        assertThat(responseHeaders.length, is(2));
        assertThat(method.getResponseHeader("Date"), is(notNullValue()));
        assertThat(method.getResponseHeader("Content-Length").getValue(), is("13"));
    }
    
}
