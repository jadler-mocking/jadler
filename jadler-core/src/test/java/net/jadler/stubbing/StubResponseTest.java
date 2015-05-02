/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;


import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import net.jadler.KeyValues;
import org.junit.Test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;


public class StubResponseTest {
    
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] UTF_8_REPRESENTATION = 
            {(byte)0xC3, (byte)0xA1, (byte)0xC5, (byte)0x99, (byte)0xC5, (byte)0xBE};
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final String HEADERS_TO_STRING = "headers_to_string";
    private static final KeyValues DEFAULT_HEADERS = new KeyValues()
            .add("header_1", "value_1_1").add("header_2", "value_2_1");
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderStatusWrongParam() {
        StubResponse.builder().status(-1);
    }
    
    
    @Test
    public void builderDefaultResponse() {
        final StubResponse resp = StubResponse.builder().build();
        assertThat(resp.getBody(), is(new byte[0]));
        assertThat(resp.getDelay(), is(0L));
        assertThat(resp.getEncoding(), is(nullValue()));
        assertThat(resp.getHeaders(), is(notNullValue()));
        assertThat(resp.getHeaders(), is(new KeyValues()));
        assertThat(resp.getStatus(), is(200));
    }
    
    
    @Test
    public void builderStatus() {
        assertThat(StubResponse.builder().status(202).build().getStatus(), is(202));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderBodyArrayWrongParam() {
        StubResponse.builder().body(null);
    }
    
    
    @Test
    public void builderBodyArray() {
        final StubResponse resp =  StubResponse.builder().body(UTF_8_REPRESENTATION).build();
        
        assertThat(resp.getBody(), is(UTF_8_REPRESENTATION));
        assertThat(resp.getEncoding(), is(nullValue()));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderBodyStringWrongParam1() {
        StubResponse.builder().body(null, Charset.defaultCharset());
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderBodyStringWrongParam2() {
        StubResponse.builder().body("body", null);
    }
    
    
    @Test
    public void builderBodyString() {
        final StubResponse resp =  StubResponse.builder().body(STRING_WITH_DIACRITICS, UTF_8_CHARSET).build();
        assertThat(resp.getBody(), is(UTF_8_REPRESENTATION));
        assertThat(resp.getEncoding(), is(UTF_8_CHARSET));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderHeadersWrongParam() {
        StubResponse.builder().headers(null);
    }
    
    
    @Test
    public void builderHeaders() {
        final KeyValues newHeaders = new KeyValues()
                .add("header_3", "value_3_1")
                .add("header_4", "value_4_1");
        
          //default headers must be overwritten by the new ones
        final StubResponse resp = StubResponse.builder().headers(DEFAULT_HEADERS).headers(newHeaders).build();
        assertThat(resp.getHeaders(), is(newHeaders));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderHeaderWrongParam1() {
        StubResponse.builder().header(null, "value");
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderHeaderWrongParam2() {
        StubResponse.builder().header("", "value");
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void builderHeaderWrongParam3() {
        StubResponse.builder().header("header", null);
    }
    
    
    @Test
    public void builderHeaderEmptyValue() {
        StubResponse.builder().header("header", "");
    }
    
    
    @Test
    public void builderHeader() {
        final StubResponse resp = StubResponse.builder()
                .headers(DEFAULT_HEADERS)
                .header("header_2", "value_2_2")  //adds 2nd value to header_2
                .header("header_3", "value_3_1")  //adds new header
                .build();
        
        final KeyValues expected = new KeyValues()
                .add("header_1", "value_1_1")
                .add("header_2", "value_2_1")
                .add("header_2", "value_2_2")
                .add("header_3", "value_3_1");
        
        assertThat(resp.getHeaders(), is(expected));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void builderDelayWrongParam1() {
        StubResponse.builder().delay(-1, TimeUnit.DAYS);
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void builderDelayWrongParam2() {
        StubResponse.builder().delay(100, null);
    }
    
    
    @Test
    public void builderDelay() {
        assertThat(StubResponse.builder().delay(5, TimeUnit.SECONDS).build().getDelay(), is(5000L));
    }
    
    
    @Test
    public void testToStringBinaryBody() {
        final StubResponse resp = StubResponse.builder().body(UTF_8_REPRESENTATION).build();
        assertThat(resp.toString(), is("status=200, body=<binary>, headers=(), delay=0 milliseconds"));
    }
    
    
    @Test
    public void testToStringStringBody() {
        final StubResponse resp = StubResponse.builder().body(STRING_WITH_DIACRITICS, UTF_8_CHARSET).build();
        assertThat(resp.toString(), is(String.format(
                "status=200, body=%s, encoding=UTF-8, headers=(), delay=0 milliseconds", STRING_WITH_DIACRITICS)));
    }
    
    
    @Test
    public void testToStringBinaryEmptyBody() {
        final StubResponse resp = StubResponse.builder().body(new byte[0]).build();
        assertThat(resp.toString(), is("status=200, body=<empty>, headers=(), delay=0 milliseconds"));
    }
    
    
    @Test
    public void testToStringStringEmptyBody() {
        final StubResponse resp = StubResponse.builder().body("", UTF_8_CHARSET).build();
        assertThat(resp.toString(), is("status=200, body=<empty>, headers=(), delay=0 milliseconds"));
    }
    
    
    @Test
    public void testToStringHeaders() {
        final KeyValues headers = mock(KeyValues.class);
        when(headers.toString()).thenReturn(HEADERS_TO_STRING);
        final StubResponse resp = StubResponse.builder().headers(headers).build();
        assertThat(resp.toString(),
                is("status=200, body=<empty>, headers=(" + HEADERS_TO_STRING + "), delay=0 milliseconds"));
    }
    
    
    @Test
    public void testToStringDelay() {
        final StubResponse resp = StubResponse.builder().delay(4, TimeUnit.DAYS).build();
        assertThat(resp.toString(), is("status=200, body=<empty>, headers=(), delay=4 days"));
    }
}