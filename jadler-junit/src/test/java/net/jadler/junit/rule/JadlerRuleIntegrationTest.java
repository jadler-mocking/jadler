/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.junit.rule;

import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.Header;

import static net.jadler.Jadler.verifyThatRequest;
import static net.jadler.Jadler.port;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Tests the {@link net.jadler.junit.rule.JadlerRule#JadlerRule()} variant.
 *
 * @author Christian Galsterer
 */
public class JadlerRuleIntegrationTest {
    
    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final int DEFAULT_STATUS = 201;
    private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-2");
    private static final String HEADER_NAME1 = "name1";
    private static final String HEADER_NAME2 = "name2";
    private static final String HEADER_VALUE1_1 = "value11";
    private static final String HEADER_VALUE1_2 = "value12";
    private static final String HEADER_VALUE2 = "value2";
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};

    @Rule
    public JadlerRule defaultJadler = new JadlerRule()
            .withRequestsRecordingDisabled()
            .withDefaultResponseContentType(DEFAULT_CONTENT_TYPE)
            .withDefaultResponseStatus(DEFAULT_STATUS)
            .withDefaultResponseEncoding(DEFAULT_ENCODING)
            .withDefaultResponseHeader(HEADER_NAME1, HEADER_VALUE1_1)
            .withDefaultResponseHeader(HEADER_NAME1, HEADER_VALUE1_2)
            .withDefaultResponseHeader(HEADER_NAME2, HEADER_VALUE2);
    
    private HttpClient client;
    
    @Before
    public void setUp() {
          //send a default response on any request
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);
        
        this.client = new HttpClient();
    }
    

    @Test
    public void testWithDefaultPort() {
         assertThat(port(), is(greaterThanOrEqualTo(0)));
    }
    
    @Test(expected = IllegalStateException.class)
    public void withRequestsRecordingDisabled() {
        verifyThatRequest();
    }
    
    @Test
    public void withDefaultResponseContentType() throws IOException {
        assertThat(this.createAndExecute().getResponseHeader("Content-Type").getValue(), is(DEFAULT_CONTENT_TYPE));
    }
    
    @Test
    public void withDefaultResponseStatus() throws IOException {
        assertThat(this.createAndExecute().getStatusCode(), is(DEFAULT_STATUS));
    }
    
    @Test
    public void withDefaultResponseEncoding() throws IOException {
        final byte[] body = this.createAndExecute().getResponseBody();
        assertThat(body, is(ISO_8859_2_REPRESENTATION));
    }

    @Test
    public void withDefaultResponseHeader() throws IOException {
        final GetMethod req = this.createAndExecute();
        
        assertThat(req.getResponseHeaders(HEADER_NAME1), is(arrayContainingInAnyOrder(
                new Header(HEADER_NAME1, HEADER_VALUE1_1), new Header(HEADER_NAME1, HEADER_VALUE1_2))));
        
        assertThat(req.getResponseHeaders(HEADER_NAME2), is(arrayContaining(new Header(HEADER_NAME2, HEADER_VALUE2))));
    }
    
    private GetMethod createAndExecute() throws IOException {
        final GetMethod req = new GetMethod("http://localhost:" + port());
        this.client.executeMethod(req);
        
        return req;
    }
}
