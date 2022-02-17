/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.jadler.Request;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RequestUtilsTest {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_WITH_ENCODING = "text/plain; charset=UTF-8";
    private static final String CONTENT_TYPE_NO_ENCODING = "text/plain";
    private static final String CONTENT_TYPE_CRIPPLED = "text/plain; charset=crippled";


    private Headers requestHeaders;

    private HttpExchange mockHttpExchange;

    @Before
    public void setUp() {
        this.requestHeaders = new Headers();

        this.mockHttpExchange = mock(HttpExchange.class);
        when(mockHttpExchange.getRequestHeaders()).thenReturn(requestHeaders);
    }


    @Test
    public void addEncoding() {
        this.requestHeaders.add(CONTENT_TYPE_HEADER, CONTENT_TYPE_WITH_ENCODING);

        Request.Builder builder = Request.builder().method("GET").requestURI(URI.create("/"));
        RequestUtils.addEncoding(builder, mockHttpExchange);

        assertThat(builder.build().getEncoding(), is(Charset.forName("UTF-8")));
    }


    @Test
    public void addEncoding_noEncoding() {
        this.requestHeaders.add(CONTENT_TYPE_HEADER, CONTENT_TYPE_NO_ENCODING);

        Request.Builder builder = Request.builder().method("GET").requestURI(URI.create("/"));
        RequestUtils.addEncoding(builder, mockHttpExchange);

        assertThat(builder.build().getEncoding(), is(nullValue()));
    }


    @Test
    public void addEncoding_noContentTypeHeader() {
        Request.Builder builder = Request.builder().method("GET").requestURI(URI.create("/"));
        RequestUtils.addEncoding(builder, mockHttpExchange);

        assertThat(builder.build().getEncoding(), is(nullValue()));
    }


    @Test
    public void addEncoding_crippledCharset() {
        this.requestHeaders.add(CONTENT_TYPE_HEADER, CONTENT_TYPE_CRIPPLED);

        Request.Builder builder = Request.builder().method("GET").requestURI(URI.create("/"));
        RequestUtils.addEncoding(builder, mockHttpExchange);

        assertThat(builder.build().getEncoding(), is(nullValue()));
    }
}
