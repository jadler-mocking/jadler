/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.Responder;
import net.jadler.stubbing.StubResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * Suite of several integration tests for the stubbing part of the Jadler library.
 * Each test configures the stub server and tests either the <i>WHEN<i/> or <i>THEN</i> part of http stubbing using
 * an http client. This class is shared by subclasses that each configure different Jadler implementation.
 */
public abstract class AbstractJadlerStubbingIntegrationTest {
    
    private static final int DEFAULT_STATUS = 409;
    private static final String DEFAULT_HEADER1_NAME = "default_header";
    private static final String DEFAULT_HEADER1_VALUE1 = "value1";
    private static final String DEFAULT_HEADER1_VALUE2 = "value2";
    
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] UTF_8_REPRESENTATION = 
            {(byte)0xC3, (byte)0xA1, (byte)0xC5, (byte)0x99, (byte)0xC5, (byte)0xBE};
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};
    
    private static final byte[] BINARY_BODY = {1, 2, 3};
    
    private static final String UTF_8_TYPE = "text/html; charset=UTF-8";
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final String ISO_8859_2_TYPE = "text/html; charset=ISO-8859-2";
    private static final Charset ISO_8859_2_CHARSET = Charset.forName("ISO-8859-2");
    
    
    private HttpClient client;
    
    @Before
    public void setUp() {
        
        doInitJadler().that()
                .respondsWithDefaultStatus(DEFAULT_STATUS)
                .respondsWithDefaultHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE1)
                .respondsWithDefaultHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE2)
                .respondsWithDefaultEncoding(UTF_8_CHARSET)
                .respondsWithDefaultContentType(UTF_8_TYPE);
        
        this.client = new HttpClient();
    }

    protected abstract Jadler.AdditionalConfiguration doInitJadler();


    @After
    public void tearDown() {
        closeJadler();
    }
    
    
    /*
     * Tests the <tt>havingBody</tt> methods.
     */
    @Test
    public void havingBody() throws Exception {
        onRequest()
            .havingBodyEqualTo("postbody")
            .havingBody(notNullValue())
            .havingBody(not(isEmptyOrNullString()))
        .respond()
            .withStatus(201);

        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("postbody", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * An empty string (not null) is matched for an empty http request.
     */
    @Test
    public void havingEmptyBody() throws Exception {
        onRequest()
            .havingBodyEqualTo("")
            .havingBody(notNullValue())
            .havingBody(isEmptyString())
        .respond()
            .withStatus(201);
                    
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingRawBody</tt> method
     */
    @Test
    public void havingRawBody() throws IOException {
        onRequest()
            .havingRawBodyEqualTo(BINARY_BODY)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new ByteArrayRequestEntity(BINARY_BODY));
        
        final int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * An empty array (not null) is matched for an empty http request.
     */
    @Test
    public void havingRawEmptyBody() throws IOException {
        onRequest()
            .havingRawBodyEqualTo(new byte[0])
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        
        final int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Matches a request with a text body encoded using UTF-8.
     */
    @Test
    public void havingUTF8Body() throws Exception {
        
        onRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(UTF_8_REPRESENTATION)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain", UTF_8_CHARSET.name()));

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Matches a request with a text body encoded using ISO-8859-2.
     */
    @Test
    public void havingISOBody() throws Exception {
        
        onRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(ISO_8859_2_REPRESENTATION)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain", ISO_8859_2_CHARSET.name()));

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests a combination of the <tt>havingHeader</tt> methods.
     */
    @Test
    public void havingHeader() throws Exception {
        
        onRequest()
            .havingHeader("hdr1")
            .havingHeader("hdr1", not(empty()))
            .havingHeader("hDR1", hasSize(1))
            .havingHeader("hdr1", everyItem(not(isEmptyOrNullString())))
            .havingHeader("hdr1", contains("h1v1"))
            .havingHeaderEqualTo("hdr1", "h1v1")
            .havingHeader("HDr2")
            .havingHeader("hdr2", hasSize(2))
            .havingHeader("hdr2", contains("h2v1", "h2v2"))
            .havingHeader("hdr2", hasItem("h2v1"))
            .havingHeader("hdr3", nullValue())
            .havingHeaderEqualTo("hdr2", "h2v1")
            .havingHeaderEqualTo("HDR2", "h2v2")
            .havingHeaders("hDR1", "hdr2")
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        method.addRequestHeader("hdr1", "h1v1");
        method.addRequestHeader("hdr2", "h2v1");
        method.addRequestHeader("hdr2", "h2v2");
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * I'm not sure whether a request header can be empty according to the RFC. However, it seems to work. 
     */
    @Test
    public void havingEmptyHeader() throws IOException {
        onRequest()
                .havingHeaderEqualTo("empty", "")
                .havingHeader("empty")
                .havingHeader("empty", everyItem(isEmptyString()))
            .respond()
                .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        method.addRequestHeader("empty", "");
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingMethod</tt> methods.
     */
    @Test
    public void havingMethod() throws Exception {
        onRequest()
            .havingMethodEqualTo("POST")
                //the comparison must be case insensitive
            .havingMethodEqualTo("poSt")
            .havingMethod(not(isEmptyOrNullString()))
            .havingMethod(anything())
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + port());

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingParameter</tt> methods for a GET http request. Only query string values
     * are considered http parameters for a GET http request.
     */
    @Test
    public void havingParameterGET() throws Exception {
        
        onRequest()
            .havingParameter("p1")
            .havingParameter("p1", hasSize(1))
            .havingParameter("p1", everyItem(not(isEmptyOrNullString())))
            .havingParameter("p1", contains("p1v1"))
            .havingParameterEqualTo("p1", "p1v1")
            .havingParameter("p2")
            .havingParameter("p2", hasSize(2))
            .havingParameter("p2", hasItems("p2v1", "p2v2"))
            .havingParameterEqualTo("p2", "p2v1")
            .havingParameterEqualTo("p2", "p2v2")
            .havingParameters("p1", "p2")
            .havingParameter("p3")
            .havingParameter("p3", contains(""))
            .havingParameterEqualTo("p3", "")
            .havingParameter("p4")
            .havingParameter("p4", contains(""))
            .havingParameterEqualTo("p4", "")
            .havingParameter("p5", nullValue())
            .havingParameter("url%20encoded", contains("url%20encoded"))
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port() + 
                "?p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&url%20encoded=url%20encoded");

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingParameter</tt> methods for a POST http request with the
     * <tt>application/x-www-form-urlencoded</tt> content type. Both query string and request body values
     * are considered http parameters for such an http request.
     * 
     * This test also tests a combination of <tt>havingParameter</tt> and <tt>havingBody</tt> methods
     * since both of these require an access to the request body (which causes troubles in the servlet specification
     * addressed by the {@link MultipleReadsHttpServletRequest} wrapper).
     */
    @Test
    public void havingParameterPOST() throws Exception {
        final String body = "p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&url%20encoded=url%20encoded";
        
        onRequest()
            .havingParameter("p1")
            .havingParameter("p1", hasSize(1))
            .havingParameter("p1", everyItem(not(isEmptyOrNullString())))
            .havingParameter("p1", contains("p1v1"))
            .havingParameterEqualTo("p1", "p1v1")
            .havingParameter("p2")
            .havingParameter("p2", hasSize(3))
            .havingParameter("p2", hasItems("p2v1", "p2v2", "p2v3"))
            .havingParameterEqualTo("p2", "p2v1")
            .havingParameterEqualTo("p2", "p2v2")
            .havingParameterEqualTo("p2", "p2v3")
            .havingParameters("p1", "p2")
            .havingParameter("p3")
            .havingParameter("p3", contains(""))
            .havingParameterEqualTo("p3", "")
            .havingParameter("p4")
            .havingParameter("p4", contains(""))
            .havingParameterEqualTo("p4", "")
            .havingParameter("p5", nullValue())
            .havingParameter("url%20encoded", contains("url%20encoded"))
            .havingBodyEqualTo(body)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + port() + "?p2=p2v3");
        method.setRequestEntity(new StringRequestEntity(body, "application/x-www-form-urlencoded", "UTF-8"));

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingQueryString</tt> methods. 
     */
    @Test
    public void havingQueryString() throws Exception {
        onRequest()
            .havingQueryStringEqualTo("p1=v1&p2=v2&name=%C5%99eho%C5%99")
            .havingQueryString(not(isEmptyOrNullString()))
            .havingQueryString(anything())
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port() + "?p1=v1&p2=v2&name=%C5%99eho%C5%99");

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingQueryString</tt> methods. 
     */
    @Test
    public void havingEmptyQueryString() throws Exception {
        onRequest()
            .havingQueryStringEqualTo("")
            .havingQueryString(isEmptyString())
        .respond()
            .withStatus(201);
        
          //it seems HttpClient cannot send a request with an empty query string ('?' as the last character)
          //let's test this in a more hardcore fashion
        final URL url = new URL("http://localhost:" + port() + "/?");
        final HttpURLConnection c = (HttpURLConnection) url.openConnection();

        assertThat(c.getResponseCode(), is(201));
    }
    
    
    /*
     * Null value is matched for a http request without a query string
     */
    @Test
    public void havingNoQueryString() throws Exception {
        onRequest()
            .havingQueryString(nullValue())
            .havingQueryString(not(equalTo("")))
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    /*
     * Tests the <tt>havingPath</tt> methods.
     */
    @Test
    public void havingPath() throws Exception {
        onRequest()
            .havingPathEqualTo("/a/b/c/d/%C5%99")
            .havingPath(notNullValue())
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/a/b/c/d/%C5%99");

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests the <tt>havingPath</tt> methods for a root path.
     */
    @Test
    public void havingRootPath() throws IOException {
        onRequest()
            .havingPath(equalTo("/"))
            .havingPath(not(isEmptyOrNullString()))
        .respond()
            .withStatus(201);
        
          //if there was no slash at the end, the GetMethod constructor would add it automatically
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");

        final int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    @Test
    public void respondUsingResponder() throws IOException {
        onRequest()
            .havingMethodEqualTo("POST")
        .respondUsing(new Responder() {

            @Override
            public StubResponse nextResponse(final Request request) {
                
                return StubResponse.builder()
                        .status(201)
                        .header("Content-Type", "text/plain; charset=" + request.getEncoding().name())
                        .body(request.getBodyAsBytes())
                        .build();
            }
        });
        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity(STRING_WITH_DIACRITICS, UTF_8_TYPE, null));
        final int status = client.executeMethod(method);
        assertThat(status, is(201));
        assertThat(method.getResponseBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    /*
     * Tests the <tt>withBody(String)</tt> method in connection with the default body encoding (UTF-8).
     * Retrieves the response body as an array of bytes and checks that it's exactly the same as
     * the UTF-8 representation of the string.
     */
    @Test
    public void withDefaultEncoding() throws IOException {
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
          //check that the body was really encoded in UTF-8
        final byte[] body = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(body, is(UTF_8_REPRESENTATION));
    }
    
    
    /*
     * Tests the <tt>withBody(String)</tt> method in connection with the <tt>withEncoding</tt> method.
     * Retrieves the response body as an array of bytes and checks that it's exactly the same as
     * the ISO-8859-2 representation of the string.
     */
    @Test
    public void withEncoding() throws IOException {
        onRequest().respond()
                .withEncoding(ISO_8859_2_CHARSET)
                .withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
          //check that the body was really encoded in UTF-8
        final byte[] body = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(body, is(ISO_8859_2_REPRESENTATION));
    }    
    

    /*
     * Tests the <tt>withBody(String)</tt> method in connection with the default content type
     * (which is set to text/html; charset=UTF-8). Checks the content type was set to the stub response
     * and the body is readable to the http client.
     */
    @Test
    public void withDefaultContentType() throws IOException {
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
          //the content type header set to the default value
        assertThat(method.getResponseHeader("Content-Type").getValue(), is(UTF_8_TYPE));

          //the http client was able to retrieve the charset portion of the content type header
        assertThat(method.getResponseCharSet(), is(UTF_8_CHARSET.name()));
        
          //since the body was encoded in UTF-8 and content type charset was set to UTF-8,
          //the http client should be able to read it correctly
        assertThat(method.getResponseBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    /*
     * Tests the <tt>withBody(String)</tt> method in connection with the <tt>withEncoding</tt> and
     * <tt>withContentType</tt> methods. Both body encoding and the content type header are set
     * to ISO-8859-2. Checks the content type was set to the stub response and the body is readable to the http client.
     */
    @Test
    public void withContentType() throws IOException {
        onRequest().respond()
                .withEncoding(ISO_8859_2_CHARSET)
                .withContentType(ISO_8859_2_TYPE)
                .withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
          //the content type header set to the specified value
        assertThat(method.getResponseHeader("Content-Type").getValue(), is(ISO_8859_2_TYPE));

          //the http client was able to retrieve the charset portion of the content type header
        assertThat(method.getResponseCharSet(), is(ISO_8859_2_CHARSET.name()));
          
          //since the body was encoded in "ISO-8859-2" and content type charset was set to "ISO-8859-2",
          //the client should be able to read it correctly
        assertThat(method.getResponseBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    

    /*
     * Tests a mismatch between body encoding and the encoding stated in the content type header.
     * Body is encoded using ISO-8859-2, however the content type header states it's encoded
     * using UTF-8.
     */
    @Test
    public void withContentTypeEncodingMismatch() throws IOException {
        onRequest().respond()
                .withEncoding(ISO_8859_2_CHARSET)
                .withContentType(UTF_8_TYPE)
                .withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
          //the content type header set to the specified value
        assertThat(method.getResponseHeader("Content-Type").getValue(), is(UTF_8_TYPE));

          //the http client was able to retrieve the charset portion of the content type header
        assertThat(method.getResponseCharSet(), is(UTF_8_CHARSET.name()));
        
          //however the applied encoding is ISO-8859-2
        final byte[] body = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(body, is(ISO_8859_2_REPRESENTATION));
    }
    
    
    /*
     * Tests the <tt>withBody(Reader)</tt> method. The reader content is encoded using the ISO-8859-2 encoding
     * and then retrieved.
     */
    @Test
    public void withBodyReader() throws IOException {
        final Reader r = new StringReader(STRING_WITH_DIACRITICS);
        
        onRequest().respond()
                .withBody(r)
                .withEncoding(ISO_8859_2_CHARSET)
                .withContentType(ISO_8859_2_TYPE);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        client.executeMethod(method);

        final byte[] resultBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(resultBody, is(ISO_8859_2_REPRESENTATION));
    }
    
    
    /*
     * Tests the <tt>withBody(InputStream)</tt> method. The stream content is used straight as the response body
     * and then retrieved.
     */
    @Test
    public void withBodyInputStream() throws IOException {
        final InputStream is = new ByteArrayInputStream(BINARY_BODY);
        
        onRequest().respond().withBody(is);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        client.executeMethod(method);

        final byte[] resultBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(resultBody, is(BINARY_BODY));
    }
    
    
    /*
     * Tests the <tt>withBody(byte[])</tt> method. The byte array is used straight as the response body
     * and then retrieved.
     */
    @Test
    public void withBodyArrayOfBytes() throws IOException { 
        onRequest().respond().withBody(BINARY_BODY);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        client.executeMethod(method);

        final byte[] resultBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(resultBody, is(BINARY_BODY));
    }
    
    
    /*
     * Tests that for more matching stub rules the latter is applied.
     */
    @Test
    public void rulesOrdering() throws IOException {
          //these 3 rules are always matched, the latter one must be applied
        onRequest().that(is(anything())).respond().withStatus(201);
        onRequest().that(is(anything())).respond().withStatus(202);
        onRequest().that(is(anything())).respond().withStatus(203);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        final int status = client.executeMethod(method);
        assertThat(status, is(203));
    }
    
    
    /*
     * Tests more stub responses defined during stubbing.
     */
    @Test
    public void multipleStubResponses() throws IOException {
        onRequest().respond().withStatus(200).thenRespond().withStatus(201);
        
        final GetMethod method1 = new GetMethod("http://localhost:" + port());
        final int status1 = client.executeMethod(method1);
        assertThat(status1, is(200));
        
        final GetMethod method2 = new GetMethod("http://localhost:" + port());
        final int status2 = client.executeMethod(method2);
        assertThat(status2, is(201));
        
          //the 201 status must be repeated for every subsequent request
        final GetMethod method3 = new GetMethod("http://localhost:" + port());
        final int status3 = client.executeMethod(method3);
        assertThat(status3, is(201));
    }
    
    
    /*
     * Tests that 500 status and an empty body is returned when no stub rule matches.
     */
    @Test
    public void noRuleApplicable() throws IOException {
        onRequest().that(is(not(anything()))).respond();
        final GetMethod method = new GetMethod("http://localhost:" + port());
        final int status = client.executeMethod(method);
        
        assertThat(status, is(404));
        assertThat(method.getResponseHeader("Content-Type").getValue(), is("text/plain; charset=utf-8"));
        assertThat(method.getResponseBodyAsString(), is("No stub response found for the incoming request"));
    }
    
    
    /*
     * Tests default status and response headers 
     */
    @Test
    public void defaults() throws Exception {
        onRequest().respond();
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        int status = client.executeMethod(method);
        assertThat(status, is(DEFAULT_STATUS));

        final Header[] responseHeaders = method.getResponseHeaders(DEFAULT_HEADER1_NAME);
        assertThat(responseHeaders.length, is(2));
        assertThat(responseHeaders[0].getName(), is(equalToIgnoringCase(DEFAULT_HEADER1_NAME)));
        assertThat(responseHeaders[0].getValue(), is(DEFAULT_HEADER1_VALUE1));

        assertThat(responseHeaders[1].getName(), is(equalToIgnoringCase(DEFAULT_HEADER1_NAME)));
        assertThat(responseHeaders[1].getValue(), is(DEFAULT_HEADER1_VALUE2));
    }
    
    
    /*
     * Tests overriding the default status during stubbing
     */
    @Test
    public void overriddenDefaultStatus() throws Exception {
        onRequest().respond().withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    /*
     * Tests overriding default headers. The header DEFAULT_HEADER1_NAME is already defined with two default values.
     * This particular stubbing adds a third value. This test checks that all three values
     * are sent in the stub response.
     */
    @Test
    public void overriddenDefaultHeader() throws Exception {
        onRequest().respond().withHeader(DEFAULT_HEADER1_NAME, "value3");
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        int status = client.executeMethod(method);
        assertThat(status, is(DEFAULT_STATUS));

        final Header[] responseHeaders = method.getResponseHeaders(DEFAULT_HEADER1_NAME);
        assertThat(responseHeaders.length, is(3));
        assertThat(responseHeaders[0].getName(), is(equalToIgnoringCase(DEFAULT_HEADER1_NAME)));
        assertThat(responseHeaders[0].getValue(), is(DEFAULT_HEADER1_VALUE1));

        assertThat(responseHeaders[1].getName(), is(equalToIgnoringCase(DEFAULT_HEADER1_NAME)));
        assertThat(responseHeaders[1].getValue(), is(DEFAULT_HEADER1_VALUE2));

        assertThat(responseHeaders[2].getName(), is(equalToIgnoringCase(DEFAULT_HEADER1_NAME)));
        assertThat(responseHeaders[2].getValue(), is("value3"));      
    }
    
    
    /*
     * Tests the stub response is returned after at least three seconds as set during the stubbing
     */
    @Test
    public void delay() throws IOException {
        onRequest().respond().withDelay(1, TimeUnit.SECONDS);
        
        final GetMethod method = new GetMethod("http://localhost:" + port());
        
        final long start = System.currentTimeMillis();
        client.executeMethod(method);
        final long end = System.currentTimeMillis();
        final long dur = end - start;
        assertThat(dur / 1000, is(greaterThanOrEqualTo(1L)));
    }
}
