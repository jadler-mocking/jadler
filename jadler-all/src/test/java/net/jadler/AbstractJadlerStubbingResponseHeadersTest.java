/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.io.IOException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import net.jadler.stubbing.server.StubHttpServer;
import org.junit.After;
import org.junit.Before;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.closeJadler;
import static org.hamcrest.Matchers.notNullValue;


public abstract class AbstractJadlerStubbingResponseHeadersTest {
    
    private HttpClient client;
    
    /**
     * @return particular server implementation to execute this test with
     */
    protected abstract StubHttpServer createServer();
    
    @Before
    public void setUp() {
        this.client = new HttpClient();
        
        initJadlerUsing(this.createServer());
    }
    
    
    @After
    public void tearDown() {
        closeJadler();
    }
    
    
    @Test
    public void allHeaders() throws IOException {
        onRequest().respond().withBody("13 chars long");
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        client.executeMethod(method);

        final Header[] responseHeaders = method.getResponseHeaders();
        
                for (final Header h: responseHeaders) {
            System.out.println(h.getName() + ": " + h.getValue());
        }
        
        assertThat(responseHeaders.length, is(2));
        
        assertThat(method.getResponseHeader("Date"), is(notNullValue()));
        assertThat(method.getResponseHeader("Content-Length").getValue(), is("13"));
    }
    
}
