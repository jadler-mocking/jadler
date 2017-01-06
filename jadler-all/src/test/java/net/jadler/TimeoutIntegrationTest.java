/*
 * Copyright (c) 2012 - 2016 Jadler contributors
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import net.jadler.parameters.StubHttpServerFactory;
import net.jadler.parameters.TestParameters;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;



/**
 * When a timeout value is defined in a jUnit test, the test is executed in a different thread than the thread
 * executing the setup and teardown methods thus a separated test for it. 
 */
@RunWith(Parameterized.class)
public class TimeoutIntegrationTest {
    
    private HttpClient client;
    
    private final StubHttpServerFactory serverFactory;

    
    public TimeoutIntegrationTest(final StubHttpServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }
        
    @Parameterized.Parameters
    public static Iterable<StubHttpServerFactory[]> parameters() {
        return new TestParameters().provide();
    }

    @Before
    public void setUp() {
        initJadlerUsing(this.serverFactory.createServer());
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
