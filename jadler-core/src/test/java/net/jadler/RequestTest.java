/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import static org.junit.Assert.assertThat;
import static java.net.URI.create;
import static java.lang.String.format;
import static org.hamcrest.Matchers.*;


public class RequestTest {
    
    private static final String METHOD = "GET";
    private static final URI URI = create("http://localhost/");
    
    private static final String HEADER1_NAME = "header1";
    private static final String HEADER1_VALUE1 = "value1_1";
    private static final String HEADER1_VALUE2 = "value1_2";
    private static final String HEADER2_NAME = "header2";
    private static final String HEADER2_VALUE = "value2";
    
    private static final String PARAM1_NAME = "param1";
    private static final String PARAM1_VALUE1 = "value1_1";
    private static final String PARAM1_VALUE2 = "value1_2";
    private static final String PARAM2_NAME = "header2";
    private static final String PARAM2_VALUE = "value2";
    private static final String PARAM_NAME_URL_ENCODED = "param1%20name";
    private static final String PARAM_VALUE_URL_ENCODED = "param1%20value";
    
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final byte[] BINARY_BODY = {1, 2, 3};
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u00ed\u00e9";
    private static final byte[] UTF_8_REPRESENTATION = 
            {(byte)0xC3, (byte)0xA1, (byte)0xC3, (byte)0xAD, (byte)0xC3, (byte)0xA9};
    private static final byte[] ISO_8859_1_REPRESENTATION = {(byte)0xE1, (byte)0xED, (byte)0xE9};
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xED, (byte)0xE9};
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final Charset ISO_8859_2_CHARSET = Charset.forName("ISO-8859-2");
    
       
    @Test
    public void emptyDefaults() throws IOException {
        System.out.println(STRING_WITH_DIACRITICS);
        final Request request = Request.builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(request.getHeaders().getKeys(), is(notNullValue()));
        assertThat(request.getHeaders().getKeys(), is(empty()));
        
        assertThat(request.getParameters().getKeys(), is(notNullValue()));
        assertThat(request.getParameters().getKeys(), is(empty()));
        
        assertThat(request.getBodyAsString(), is(notNullValue()));
        assertThat(request.getBodyAsString(), is(""));
        
        assertThat(request.getBodyAsStream(), is(notNullValue()));
        assertThat(request.getBodyAsBytes().length, is(0));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void methodNotSet() {
        Request.builder()
                .requestURI(URI)
                .build();
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void blankMethod() {
        Request.builder()
                .method("")
                .requestURI(URI)
                .build();
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void uriNotSet() {
                Request.builder()
                .method(METHOD)
                .build();
    }
    
    
    @Test
    public void getMethod() {
        final Request request = Request.builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(request.getMethod(), is(METHOD));
    }
    
    
    @Test
    public void getURI() {
        final Request request = Request.builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(request.getURI(), is(URI));        
    }
    

    @Test(expected = IllegalArgumentException.class)
    public void headersWrongParam() {
        Request.builder().headers(null);
    }
    
    
    @Test
    public void headers() {
        final KeyValues headers = new KeyValues().add(HEADER1_NAME, HEADER1_VALUE1).add(HEADER2_NAME, HEADER2_VALUE);
        
        assertThat(Request.builder()
                .method(METHOD)
                .requestURI(URI)
                .headers(headers)
                .build()
                .getHeaders(), is(headers));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void headerWrongParam1() {
        Request.builder().header(null, "value");
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void headerWrongParam2() {
        Request.builder().header("", "value");
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void headerWrongParam3() {
        Request.builder().header("key", null);
    }
    
    
    @Test
    public void header() {
        final KeyValues headers = Request.builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .header(HEADER1_NAME, HEADER1_VALUE2)
                .header(HEADER2_NAME, HEADER2_VALUE)
                .build()
                .getHeaders();
        
        assertThat(headers, is(new KeyValues()
                .add(HEADER1_NAME, HEADER1_VALUE1)
                .add(HEADER1_NAME, HEADER1_VALUE2)
                .add(HEADER2_NAME, HEADER2_VALUE)));
    }
    
    
    @Test
    public void parametersInQuery() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create(format(
                    "http://localhost/?%s=%s&%s=%s&%s=%s",
                    PARAM1_NAME, PARAM1_VALUE1,
                    PARAM1_NAME, PARAM1_VALUE2,
                    PARAM2_NAME, PARAM2_VALUE)))
                .build();
        
        final KeyValues expected = new KeyValues()
                .add(PARAM1_NAME, PARAM1_VALUE1)
                .add(PARAM1_NAME, PARAM1_VALUE2)
                .add(PARAM2_NAME, PARAM2_VALUE);
                
                
        assertThat(req.getParameters(), is(expected));
    }
    
    
    @Test
    public void parameterInBodyWrongMethod() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s",PARAM1_NAME, PARAM1_VALUE1).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
                   
        assertThat(req.getParameters(), is(KeyValues.EMPTY));
    }
    
    
    @Test
    public void parameterInBodyWrongContentType() {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s",PARAM1_NAME, PARAM1_VALUE1).getBytes())
                .build();
                   
        assertThat(req.getParameters(), is(KeyValues.EMPTY));
    }
    
    
    @Test
    public void parametersInBody() {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s&%s=%s&%s=%s",
                    PARAM1_NAME, PARAM1_VALUE1,
                    PARAM1_NAME, PARAM1_VALUE2,
                    PARAM2_NAME, PARAM2_VALUE).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        final KeyValues expected = new KeyValues()
                .add(PARAM1_NAME, PARAM1_VALUE1)
                .add(PARAM1_NAME, PARAM1_VALUE2)
                .add(PARAM2_NAME, PARAM2_VALUE);
                   
        assertThat(req.getParameters(), is(expected));
    }
    
    
    @Test
    public void parametersInBothBodyAndQuery() {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create(format("http://localhost/?%s=%s&%s=%s",
                    PARAM1_NAME, PARAM1_VALUE1,
                    PARAM2_NAME, PARAM2_VALUE)))
                .body(format("%s=%s", PARAM1_NAME, PARAM1_VALUE2).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        final KeyValues expected = new KeyValues()
                .add(PARAM1_NAME, PARAM1_VALUE1)
                .add(PARAM1_NAME, PARAM1_VALUE2)
                .add(PARAM2_NAME, PARAM2_VALUE);
                   
        assertThat(req.getParameters(), is(expected));
    }
    
    
    @Test
    public void getParameterValueInQueryURLEncoded() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create(format("http://localhost/?%s=%s", PARAM_NAME_URL_ENCODED, PARAM_VALUE_URL_ENCODED)))
                .build();  
        
        assertThat(req.getParameters().getValue(PARAM_NAME_URL_ENCODED), is(PARAM_VALUE_URL_ENCODED));
    }
    
    
    @Test
    public void getParameterValueInBodyURLEncoded() {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s", PARAM_NAME_URL_ENCODED, PARAM_VALUE_URL_ENCODED).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameters().getValue(PARAM_NAME_URL_ENCODED), is(PARAM_VALUE_URL_ENCODED));
    }
    
    
    @Test
    public void getBodyAsStreamEmpty() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(new byte[0])
                .build();
        
        assertThat(IOUtils.toByteArray(req.getBodyAsStream()).length, is(0));
    }
    
    
    @Test
    public void getBodyAsStream() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(BINARY_BODY)
                .build();
        
        assertThat(IOUtils.toByteArray(req.getBodyAsStream()), is(BINARY_BODY));
    }
    

    @Test
    public void getBodyAsBytesEmpty() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(new byte[0])
                .build();
        
        assertThat(req.getBodyAsBytes().length, is(0));
    }
    
    
    @Test
    public void getBodyAsBytes() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(BINARY_BODY)
                .build();
        
        assertThat(req.getBodyAsBytes(), is(BINARY_BODY));
    }
    
    
    @Test
    public void getBodyAsStringEmpty() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(new byte[0])
                .build();
        
        assertThat(req.getBodyAsString(), is(""));
    }
    
    
    @Test
    public void getBodyAsStringUTF8() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .encoding(UTF_8_CHARSET)
                .body(UTF_8_REPRESENTATION)
                .build();
        
        assertThat(req.getBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test
    public void getBodyAsStringISO88592() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .encoding(ISO_8859_2_CHARSET)
                .body(ISO_8859_2_REPRESENTATION)
                .build();
        
        assertThat(req.getBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test
    public void getBodyAsStringDefaultEncoding() throws IOException {
        final Request req = Request.builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(ISO_8859_1_REPRESENTATION)  //ISO-8859-1 is the default encoding
                .build();
        
        assertThat(req.getBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test
    public void getContentTypeNone() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .build();
        
        assertThat(req.getContentType(), is(nullValue()));
    }
    
    
    @Test
    public void getContentType() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .header("Content-Type", CONTENT_TYPE)
                .build();
        
        assertThat(req.getContentType(), is(CONTENT_TYPE));
    }
    
    
    @Test
    public void getEncodingNone() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .build();
        
        assertThat(req.getEncoding(), is(nullValue()));        
    }
    
    
    @Test
    public void getEncoding() {
        final Request req = Request.builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .encoding(UTF_8_CHARSET)
                .build();
        
        assertThat(req.getEncoding(), is(UTF_8_CHARSET));        
    }
}
