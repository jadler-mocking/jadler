/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.jadler.exception.JadlerException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


public class StubbingTest {

    private static final MultiValueMap DEFAULT_HEADERS = new MultiValueMap();
    private static final int DEFAULT_STATUS = 200;
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

    private Stubbing stubbing;


    @Before
    public void setUp() {
        this.stubbing = new Stubbing(DEFAULT_ENCODING, DEFAULT_STATUS, DEFAULT_HEADERS);
    }


    @Test
    public void respond() {
        this.stubbing.respond();
        assertOneDefaultResponse();
    }


    @Test
    public void thenRespond() {
        this.stubbing.thenRespond();
        assertOneDefaultResponse();
    }


    @Test
    public void withBodyString() {
        final String body = "body";
        this.stubbing.respond().withBody(body);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getBody(), equalTo(body.getBytes(DEFAULT_ENCODING)));
    }


    @Test
    public void withBodyReader() throws Exception {
        final String body = "body";
        final Reader reader = spy(new StringReader(body));
        this.stubbing.respond().withBody(reader);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getBody(), equalTo(body.getBytes(DEFAULT_ENCODING)));
        verify(reader).close();
    }


    @Test(expected = JadlerException.class)
    public void withBodyReaderThrowingIOE() throws Exception {
        final Reader reader = mock(Reader.class);
        when(reader.read(any(char[].class))).thenThrow(new IOException());
                
        try {
            this.stubbing.respond().withBody(reader);
        } finally {
            verify(reader).close();
        }
    }
    
    
    @Test
    public void withBodyBytes() {
        final byte[] body = "body".getBytes(DEFAULT_ENCODING);
        this.stubbing.respond().withBody(body);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getBody(), equalTo(body));
    }
    
    
    @Test
    public void withBodyInputStream() throws Exception {
        final byte[] body = new byte[] {1, 2, 3};
        final InputStream is = spy(new ByteArrayInputStream(body));
        this.stubbing.respond().withBody(is);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getBody(), equalTo(body));
        verify(is).close();
    }
    
    
    @Test(expected = JadlerException.class)
    @SuppressWarnings("unchecked")
    public void withBodyInputStreamThrowingIOE() throws Exception {
        final InputStream is = mock(InputStream.class);

        when(is.read(any(byte[].class))).thenThrow(new IOException());
                
        try {
            this.stubbing.respond().withBody(is);
        } finally {
            verify(is).close();
        }
    }


    @Test
    public void withHeader() {
        final String name = "name";
        final String value = "value";
        this.stubbing.respond().withHeader(name, value);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getHeaders(), is(notNullValue()));
        assertThat(response.getHeaders().size(), is(1));
        assertThat(response.getHeaders().containsKey(name), is(true));
        assertThat(response.getHeaders().get(name), is(notNullValue()));
        assertThat(((List) response.getHeaders().get(name)).size(), is(1));
        assertThat(((List) response.getHeaders().get(name)).get(0), is(equalTo((Object) value)));
    }


    @Test
    public void withStatus() {
        final int status = 2;
        this.stubbing.respond().withStatus(status);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getStatus(), is(status));
    }


    @Test
    public void withTimeout() {
        final long timeout = 2;
        this.stubbing.respond().withTimeout(timeout, TimeUnit.MILLISECONDS);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getTimeout(), is(timeout));
    }


    @Test
    public void createRule() {
        this.stubbing.thenRespond();
        final HttpStub rule = this.stubbing.createRule();

        assertThat(rule, is(notNullValue()));
    }


    // helper methods

    private void assertOneDefaultResponse() {
        assertThat(this.stubbing.getStubResponses(), is(notNullValue()));
        assertThat(this.stubbing.getStubResponses(), hasSize(1));

        assertThat(this.stubbing.getStubResponses().get(0), is(instanceOf(StubResponse.class)));
        assertThat(this.stubbing.getStubResponses().get(0).getHeaders(), equalTo((MultiMap) DEFAULT_HEADERS));
        assertThat(this.stubbing.getStubResponses().get(0).getStatus(), equalTo(DEFAULT_STATUS));
    }


    private StubResponse assertAndGetOneResponse() {
        assertThat(this.stubbing.getStubResponses(), is(notNullValue()));
        assertThat(this.stubbing.getStubResponses(), hasSize(1));

        assertThat(this.stubbing.getStubResponses().get(0), is(instanceOf(StubResponse.class)));
        return this.stubbing.getStubResponses().get(0);
    }
}
