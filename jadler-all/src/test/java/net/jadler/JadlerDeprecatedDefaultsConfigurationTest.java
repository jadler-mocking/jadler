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

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static net.jadler.Jadler.verifyThatRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests the deprecated way of configuring jadler defaults still works.
 */
public class JadlerDeprecatedDefaultsConfigurationTest {
    

    private static final int EXPECTED_STATUS = 409;
    private static final String EXPECTED_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final Charset EXPECTED_ENCODING = Charset.forName("ISO-8859-2");
    private static final String EXPECTED_HEADER_NAME = "default_header";
    private static final String EXPECTED_HEADER_VALUE = "value";
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};
    
 
    /*
     * Tests the response defaults
     */
    @Test
    @SuppressWarnings("deprecation")
    public void ongoingConfiguration() throws IOException {
        initJadler()
                .that()
                .respondsWithDefaultStatus(EXPECTED_STATUS)
                .respondsWithDefaultContentType(EXPECTED_CONTENT_TYPE)
                .respondsWithDefaultEncoding(EXPECTED_ENCODING)
                .respondsWithDefaultHeader(EXPECTED_HEADER_NAME, EXPECTED_HEADER_VALUE);
        
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
     * Tests the requests recording settings
     */
    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("deprecation")
    public void ongoingConfiguration_skipsRequestRecording() throws IOException {
        initJadler().that().skipsRequestsRecording();
        
        try {
            verifyThatRequest();
            fail("request recording disabled, verification must fail");
        }
        finally {
            closeJadler();
        }
    }
}
