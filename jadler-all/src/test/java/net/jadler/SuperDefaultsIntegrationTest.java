/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.httpclient.HttpClient;
import org.junit.After;
import org.junit.Test;
import java.io.IOException;
import java.nio.charset.Charset;
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
 * Test suite for response super defaults (default response status and encoding values used when not defined at all).
 */
public class SuperDefaultsIntegrationTest {
    
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    
    private HttpClient client;
    
    @Before
    public void setUp() {
        initJadler(); //no defaults for the response status nor encoding set here
        this.client = new HttpClient();
    }
    
    
    @After
    public void tearDown() {
        closeJadler();
    }
    
    
    /*
    * When no defaults (response status and encoding) are set during Jadler initialization nor the status and encoding
    * values are provided during stubbing super-defaults (200, UTF-8) are used.
    */
    @Test
    public void superDefaults() throws IOException {
          //no values for the response status nor encoding set here
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);

        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("postbody", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(200));
        
          //the response body is decodable correctly using UTF-8
        assertThat(method.getResponseBody(), is(STRING_WITH_DIACRITICS.getBytes(Charset.forName("UTF-8"))));
    }
}
