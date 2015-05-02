/*
 * Copyright (c) 2012-2014 Jadler contributors
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.jadler.KeyValues;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.empty;
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
    private static final Responder RESPONDER = mock(Responder.class);

    private TestStubbing stubbing;


    @Before
    public void setUp() {
        this.stubbing = new TestStubbing(DEFAULT_ENCODING, DEFAULT_STATUS, DEFAULT_HEADERS);
    }


    @Test
    public void respond() {
        this.stubbing.respond();
        assertOneDefaultResponse();
        assertThat(this.stubbing.getResponder(), is(nullValue()));
    }


    @Test
    public void thenRespond() {
        this.stubbing.thenRespond();
        assertOneDefaultResponse();
        assertThat(this.stubbing.getResponder(), is(nullValue()));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void respondUsingWrongParam() {
        this.stubbing.respondUsing(null);
    }
    
    
    @Test
    public void respondUsing() {
        this.stubbing.respondUsing(RESPONDER);
        
        assertThat(this.stubbing.getStubResponses(), is(empty()));
        assertThat(this.stubbing.getResponder(), is(RESPONDER));
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
        
        final KeyValues expected = new KeyValues().add(name, value);
        assertThat(response.getHeaders(), is(expected));
    }


    @Test
    public void withStatus() {
        final int status = 2;
        this.stubbing.respond().withStatus(status);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getStatus(), is(status));
    }


    @Test
    public void withDelay() {
        this.stubbing.respond().withDelay(2, TimeUnit.SECONDS);

        final StubResponse response = assertAndGetOneResponse();
        assertThat(response.getDelay(), is(2000L));
    }

    
    @Test
    public void createRuleWithResponder() {
        this.stubbing.respondUsing(RESPONDER);
        final HttpStub rule = this.stubbing.createRule();
        
        assertThat(rule, is(notNullValue()));
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

        assertThat(this.stubbing.getStubResponses().get(0), is(instanceOf(MutableStubResponse.class)));
        assertThat(this.stubbing.getStubResponses().get(0).getHeaders(), equalTo((MultiMap) DEFAULT_HEADERS));
        assertThat(this.stubbing.getStubResponses().get(0).getStatus(), equalTo(DEFAULT_STATUS));
    }


    private StubResponse assertAndGetOneResponse() {
        assertThat(this.stubbing.getStubResponses(), is(notNullValue()));
        assertThat(this.stubbing.getStubResponses(), hasSize(1));
        
        assertThat(this.stubbing.getStubResponses().get(0), is(instanceOf(MutableStubResponse.class)));
        
        return this.stubbing.getStubResponses().get(0).toStubResponse();
    }
    
    
    /*
     * This is a test only extension of the Stubbing class which provides a getter to all StubResponses and to the
     * Responder
     */
    private static class TestStubbing extends Stubbing {
        TestStubbing(final Charset defaultEncoding, final int defaultStatus, final MultiMap defaultHeaders) {
            super(defaultEncoding, defaultStatus, defaultHeaders);
        }
        
        List<MutableStubResponse> getStubResponses() {
            return new ArrayList<MutableStubResponse>(this.stubResponses);
        }
        
        Responder getResponder() {
            return this.responder;
        }
    }
}
