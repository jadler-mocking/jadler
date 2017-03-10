/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.junit.After;
import org.junit.Test;
import java.io.IOException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import net.jadler.parameters.StubHttpServerFactory;
import net.jadler.parameters.TestParameters;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.junit.AfterClass;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.onRequest;
import static net.jadler.utils.TestUtils.STATUS_RETRIEVER;
import static net.jadler.utils.TestUtils.jadlerUri;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;



/**
 * When a timeout value is defined in a jUnit test, the test is executed in a different thread than the thread
 * executing the setup and teardown methods thus a separated test for it. 
 */
@RunWith(Parameterized.class)
public class TimeoutIntegrationTest {
    
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
    }
    
    
    @After
    public void tearDown() {
        closeJadler();
    }
    
    
    @AfterClass
    public static void cleanup() {
        Executor.closeIdleConnections();
    }
    
    
    @Test(timeout=10000L)
    public void timeout() throws IOException {
        onRequest().respond().withStatus(201);

        final int status = Executor.newInstance().execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);
        
        assertThat(status, is(201));
    }
}
