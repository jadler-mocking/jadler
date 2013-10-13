/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;



public class MutableStubResponseTest {
    
    private static final int STATUS = 215;
    private static final long DELAY = 1500;
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String STRING_BODY = "string_body";
    private static final byte[] BYTES_BODY = "bytes_body".getBytes(CHARSET);
    private static final MultiMap HEADERS_MAP;
    static {
        HEADERS_MAP = new MultiValueMap();
        HEADERS_MAP.put("header1", "value1_1");
        HEADERS_MAP.put("header2", "value2_1");
        HEADERS_MAP.put("header3", "value3_1");
    }
    private static final Headers HEADERS = new Headers()
            .add("header1", "value1_1")
            .add("header2", "value2_1")
            .add("header3", "value3_1");
    
    private MutableStubResponse msr;
    
    
    @Before
    public void setUp() {
        this.msr = new MutableStubResponse();
    }
    
    
    @Test
    public void setBodyString() {
        this.msr.setBody(BYTES_BODY);
        this.msr.setBody(STRING_BODY);  //this must reset the BYTES_BODY
        assertThat(this.msr.getRawBody(), is(nullValue()));
        assertThat(this.msr.getStringBody(), is(STRING_BODY));
    }
    
    
    @Test
    public void setBodyBytes() {
        this.msr.setBody(STRING_BODY);
        this.msr.setBody(BYTES_BODY);  //this must reset the STRING_BODY
        
        assertThat(this.msr.getStringBody(), is(nullValue()));
        assertThat(this.msr.getRawBody(), is(BYTES_BODY));
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void addHeader() {
        this.msr.addHeaders(HEADERS_MAP);
        this.msr.addHeader("header3", "value3_1");
        
        assertThat(this.msr.getHeaders().size(), is(3));
        assertThat((Set<String>)this.msr.getHeaders().keySet(), containsInAnyOrder("header1", "header2", "header3"));
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void addHeaderTwoValues() {
        this.msr.addHeaders(HEADERS_MAP);
        this.msr.addHeader("header2", "value2_2");
        
        assertThat(this.msr.getHeaders().size(), is(3));
        assertThat((Set<String>)this.msr.getHeaders().keySet(), containsInAnyOrder("header1", "header2", "header3"));
        assertThat((Collection<String>)this.msr.getHeaders().get("header2"),
                containsInAnyOrder("value2_1", "value2_2"));
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void addHeaders() {
        this.msr.addHeaders(HEADERS_MAP);
        
        final MultiMap additionalHeaders = new MultiValueMap();
        additionalHeaders.put("header3", "value3_1");
        this.msr.addHeaders(additionalHeaders);
        
        assertThat(this.msr.getHeaders().size(), is(3));
        assertThat((Set<String>)this.msr.getHeaders().keySet(), containsInAnyOrder("header1", "header2", "header3"));
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void addHeadersTwoValues() {
        this.msr.addHeaders(HEADERS_MAP);
        
        final MultiMap additionalHeaders = new MultiValueMap();
        additionalHeaders.put("header2", "value2_2");
        this.msr.addHeaders(additionalHeaders);
        
        assertThat(this.msr.getHeaders().size(), is(3));
        assertThat((Set<String>)this.msr.getHeaders().keySet(), containsInAnyOrder("header1", "header2", "header3"));
        assertThat((Collection<String>)this.msr.getHeaders().get("header2"),
                containsInAnyOrder("value2_1", "value2_2"));
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void setHeaderCaseInsensitive() {
        this.msr.addHeaders(HEADERS_MAP);
        this.msr.setHeaderCaseInsensitive("hEaDer2", "value2_2");
        
        final MultiMap expected = new MultiValueMap();
        expected.put("header1", "value1_1");
        expected.put("hEaDer2", "value2_2");
        expected.put("header3", "value3_1");
        assertThat(this.msr.getHeaders(), is(expected));
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void toStubResponseBodyNotSet() {
        this.msr.toStubResponse();
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void toStubResponseEncodingNotSet() {
        this.msr.setBody(STRING_BODY);
        this.msr.toStubResponse();
    }
    
    
    @Test
    public void toStubResponse() {
        this.msr.setStatus(STATUS);
        this.msr.setDelay(DELAY);
        this.msr.setEncoding(CHARSET);
        this.msr.addHeaders(HEADERS_MAP);
        this.msr.setBody(STRING_BODY);
        
        final StubResponse resp = this.msr.toStubResponse();
        assertThat(resp.getStatus(), is(STATUS));
        assertThat(resp.getDelay(), is(DELAY));
        assertThat(resp.getEncoding(), is(CHARSET));
        new Headers();
        assertThat(resp.getHeaders(), is(HEADERS));
        assertThat(resp.getBody(), is(STRING_BODY.getBytes(CHARSET)));
    }
    
    
    @Test
    public void toStubResponseByteBody() {
        this.msr.setStatus(STATUS);
        this.msr.setBody(BYTES_BODY);
        
        final StubResponse resp = this.msr.toStubResponse();
        assertThat(resp.getBody(), is(BYTES_BODY));
    }
}
