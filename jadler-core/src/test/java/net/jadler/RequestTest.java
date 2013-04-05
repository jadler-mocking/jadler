/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Set;

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
        final Request request = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(request.getHeaderNames(), is(notNullValue()));
        assertThat(request.getHeaderNames(), is(empty()));
        
        assertThat(request.getParameterNames(), is(notNullValue()));
        assertThat(request.getParameterNames(), is(empty()));
        
        assertThat(request.getBodyAsString(), is(notNullValue()));
        assertThat(request.getBodyAsString(), is(""));
        
        assertThat(request.getBodyAsStream(), is(notNullValue()));
        assertThat(IOUtils.toByteArray(request.getBodyAsStream()).length, is(0));
    }
    
    
    @Test
    public void emptyEncoding() throws IOException {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(ISO_8859_1_REPRESENTATION)  //ISO-8859-1 is the default encoding
                .build();
        
        assertThat(req.getBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void methodNotSet() {
        new Request.Builder()
                .requestURI(URI)
                .build();
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void blankMethod() throws URISyntaxException {
        new Request.Builder()
                .method("")
                .requestURI(URI)
                .build();
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void uriNotSet() {
                new Request.Builder()
                .method(METHOD)
                .build();
    }
    
    
    @Test
    public void getMethod() {
        final Request request = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(request.getMethod(), is(METHOD));
    }
    
    
    @Test
    public void getURI() {        
        final Request request = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(request.getURI(), is(URI));        
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void getHeaderValueEmptyName() {
        new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build()
                .getHeaderValue("");
    }
    
    
    @Test
    public void getHeaderValueNonExistent() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(req.getHeaderValue(HEADER1_NAME), is(nullValue()));
    }
    
    
    @Test
    public void getHeaderValueSingleValue() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .build();
        
        assertThat(req.getHeaderValue(HEADER1_NAME), is(HEADER1_VALUE1));
    }
      
    
    @Test
    public void getHeaderValueMultipleValues() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .header(HEADER1_NAME, HEADER1_VALUE2)
                .build();
        
        assertThat(req.getHeaderValue(HEADER1_NAME), is(HEADER1_VALUE1));
    }
    
    
    @Test
    public void getHeaderValueCaseInsensitive1() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .build();
        
        assertThat(req.getHeaderValue(HEADER1_NAME.toUpperCase()), is(HEADER1_VALUE1));
    }
    
    
    @Test
    public void getHeaderValueCaseInsensitive2() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME.toUpperCase(), HEADER1_VALUE1)
                .build();
        
        assertThat(req.getHeaderValue(HEADER1_NAME), is(HEADER1_VALUE1));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void getHeaderValuesEmptyName() {
        new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build()
                .getHeaderValues("");
    }
    
    
    @Test
    public void getHeaderValuesNonExistent() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(req.getHeaderValues(HEADER1_NAME), is(nullValue()));
    }
    
    
    @Test
    public void getHeaderValuesSingleValue() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .build();
        
        assertThat(req.getHeaderValues(HEADER1_NAME), contains(HEADER1_VALUE1));
    }
    
    
    @Test
    public void getHeaderValuesMultipleValues() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .header(HEADER1_NAME, HEADER1_VALUE2)
                .build();
        
        assertThat(req.getHeaderValues(HEADER1_NAME), contains(HEADER1_VALUE1, HEADER1_VALUE2));
    }
    
    
    @Test
    public void getHeaderValuesCaseInsensitive1() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME, HEADER1_VALUE1)
                .build();
        
        assertThat(req.getHeaderValues(HEADER1_NAME.toUpperCase()), contains(HEADER1_VALUE1));
    }
    
    
    @Test
    public void getHeaderValuesCaseInsensitive2() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .header(HEADER1_NAME.toUpperCase(), HEADER1_VALUE1)
                .build();
        
        assertThat(req.getHeaderValues(HEADER1_NAME), contains(HEADER1_VALUE1));
    }
    
    
    @Test
    public void getHeaderNamesEmpty() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        final Set<String> names = req.getHeaderNames();
        assertThat(names, is(notNullValue()));
        assertThat(names, is(empty()));
    }
    
    
    @Test
    public void getHeaderNames() {
        final Request req = new Request.Builder()
                        .method(METHOD)
                        .requestURI(URI)
                        .header(HEADER1_NAME, HEADER1_VALUE1)
                        .header(HEADER1_NAME, HEADER1_VALUE1)
                        .header(HEADER2_NAME, HEADER2_VALUE)
                        .build();
        
        final Set<String> names = req.getHeaderNames();
        assertThat(names, containsInAnyOrder(HEADER1_NAME, HEADER2_NAME));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void getParameterValueEmptyName() {
        new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build()
                .getParameterValue("");
    }
    
    
    @Test
    public void getParameterValueNonExistent() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME), is(nullValue()));
    }
    
    
    @Test
    public void getParameterValueSingleValueInQuery() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create(format("http://localhost/?%s=%s", PARAM1_NAME, PARAM1_VALUE1)))
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME), is(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValueSingleValueInBody() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s", PARAM1_NAME, PARAM1_VALUE1).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME), is(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValueMultipleValuesInQuery() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create(format(
                    "http://localhost/?%s=%s&%s=%s", PARAM1_NAME, PARAM1_VALUE1, PARAM1_NAME, PARAM1_VALUE2)))
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME), is(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValueMultipleValuesInBody() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s&%s=%s", PARAM1_NAME, PARAM1_VALUE1, PARAM1_NAME, PARAM1_VALUE2).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME), is(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValueMultipleValuesInBothQueryAndBody() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create(format("http://localhost/?%s=%s", PARAM1_NAME, PARAM1_VALUE1)))
                .body(format("%s=%s", PARAM1_NAME, PARAM1_VALUE2).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME), is(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValueCaseSensitive() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create(format("http://localhost/?%s=%s", PARAM1_NAME, PARAM1_VALUE1)))
                .build();
        
        assertThat(req.getParameterValue(PARAM1_NAME.toUpperCase()), is(nullValue()));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void getParameterValuesEmptyName() {
        new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build()
                .getParameterValues("");
    }
    
    
    @Test
    public void getParameterValuesNonExistent() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(URI)
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME), is(nullValue()));
    }
    
    
    @Test
    public void getParameterValuesSingleValueInQuery() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create(format("http://localhost/?%s=%s", PARAM1_NAME, PARAM1_VALUE1)))
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME), contains(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValuesSingleValueInBody() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s", PARAM1_NAME, PARAM1_VALUE1).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME), contains(PARAM1_VALUE1));
    }
    
    
    @Test
    public void getParameterValuesMultipleValuesInQuery() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create(
                    format("http://localhost/?%s=%s&%s=%s", PARAM1_NAME, PARAM1_VALUE1, PARAM1_NAME, PARAM1_VALUE2)))
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME), contains(PARAM1_VALUE1, PARAM1_VALUE2));
    }
    
    
    @Test
    public void getParameterValuesMultipleValuesInBody() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(format("%s=%s&%s=%s", PARAM1_NAME, PARAM1_VALUE1, PARAM1_NAME, PARAM1_VALUE2).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME), contains(PARAM1_VALUE1, PARAM1_VALUE2));
    }
    
    
    @Test
    public void getParameterValuesMultipleValuesInBothQueryAndBody() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create(format("http://localhost/?%s=%s", PARAM1_NAME, PARAM1_VALUE1)))
                .body(format("%s=%s", PARAM1_NAME, PARAM1_VALUE2).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME), contains(PARAM1_VALUE1, PARAM1_VALUE2));
    }
    
    
    @Test
    public void getParameterValuesCaseSensitive() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create(format("http://localhost/?%s=%s", PARAM1_NAME, PARAM1_VALUE1)))
                .build();
        
        assertThat(req.getParameterValues(PARAM1_NAME.toUpperCase()), is(nullValue()));
    }
    
    
    @Test
    public void getParameterNamesEmpty() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .build();
        
        assertThat(req.getParameterNames(), is(empty()));
    }
    
    
    @Test
    public void getParameterNames() {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create(format("http://localhost/?%s=%s&%s=%s", PARAM1_NAME, PARAM1_VALUE1, PARAM2_NAME, PARAM2_VALUE)))
                .body(format("%s=%s&%s=%s", PARAM1_NAME, PARAM1_VALUE2, PARAM2_NAME, PARAM2_VALUE).getBytes())
                .header("content-type", "application/x-www-form-urlencoded")
                .build();
        
        assertThat(req.getParameterNames(), containsInAnyOrder(PARAM1_NAME, PARAM2_NAME));
    }
    
    
    @Test
    public void getBodyAsStreamEmpty() throws IOException {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(new byte[0])
                .build();
        
        assertThat(IOUtils.toByteArray(req.getBodyAsStream()).length, is(0));
    }
    
    
    @Test
    public void getBodyAsStream() throws IOException {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(BINARY_BODY)
                .build();
        
        assertThat(IOUtils.toByteArray(req.getBodyAsStream()), is(BINARY_BODY));
    }
    
    
    @Test
    public void getBodyAsStringEmpty() throws IOException {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .body(new byte[0])
                .build();
        
        assertThat(req.getBodyAsString(), is(""));
    }
    
    
    @Test
    public void getBodyAsStringUTF8() throws IOException {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .encoding(UTF_8_CHARSET)
                .body(UTF_8_REPRESENTATION)
                .build();
        
        assertThat(req.getBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test
    public void getBodyAsStringISO88592() throws IOException {
        final Request req = new Request.Builder()
                .method("POST")
                .requestURI(create("http://localhost/"))
                .encoding(ISO_8859_2_CHARSET)
                .body(ISO_8859_2_REPRESENTATION)
                .build();
        
        assertThat(req.getBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test
    public void getContentTypeNone() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .build();
        
        assertThat(req.getContentType(), is(nullValue()));
    }
    
    
    @Test
    public void getContentType() {
        final Request req = new Request.Builder()
                .method(METHOD)
                .requestURI(create("http://localhost/"))
                .header("Content-Type", CONTENT_TYPE)
                .build();
        
        assertThat(req.getContentType(), is(CONTENT_TYPE));
        
    }
}
