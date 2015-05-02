/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.httpclient.HttpClient;
import org.junit.After;
import org.junit.Test;
import java.io.IOException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Before;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * When a timeout value is defined in an jUnit test, the test is executed in a different thread than the thread
 * executing the setup and teardown methods. 
 */
public class JadlerTimeoutIntegrationTest {
    
    private HttpClient client;
    
    @Before
    public void setUp() {
        initJadler();
        this.client = new HttpClient();
    }
    
    
    @After
    public void tearDown() {
        closeJadler();
    }
    
    
    @Test(timeout=10000L)
    public void timeout() throws IOException {
        onRequest().respond().withStatus(201);

        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("postbody", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
}
