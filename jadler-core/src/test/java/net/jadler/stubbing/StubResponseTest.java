/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;


public class StubResponseTest {
    
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String STRING_BODY = "string_body";
    private static final byte[] BYTES_BODY = "bytes_body".getBytes(CHARSET);
    
    StubResponse sr = new StubResponse();
    
    
    @Before
    public void setUp() {
        this.sr = new StubResponse();
    }

    
    @Test
    public void testSetBodyString() {
        sr.setEncoding(CHARSET);
        sr.setBody(STRING_BODY);
        assertThat(sr.getBody(), is(STRING_BODY.getBytes(CHARSET)));
        
        sr.setBody(BYTES_BODY);
        sr.setBody(STRING_BODY);
          //bytes body must be overwritten by the string body
        assertThat(sr.getBody(), is(STRING_BODY.getBytes(CHARSET)));
    }


    @Test
    public void testSetBodyBytes() {
        sr.setBody(BYTES_BODY);
        assertThat(sr.getBody(), is(BYTES_BODY));
        
        sr.setBody(STRING_BODY);
        sr.setBody(BYTES_BODY);
          //string body must be overwritten by the bytes body
        assertThat(sr.getBody(), is(BYTES_BODY));
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void getBodyUndefined() {
        sr.getBody();
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void getBodyEncodingUndefined() {
        sr.setBody(STRING_BODY);
        sr.getBody();
    }
}
