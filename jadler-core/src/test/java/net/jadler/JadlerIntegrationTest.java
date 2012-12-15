package net.jadler;

import java.nio.charset.Charset;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.Header;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static net.jadler.Jadler.*;


public class JadlerIntegrationTest {
    
    private static final int PORT = 44532;
    private static final int DEFAULT_STATUS = 409;
    private static final String DEFAULT_HEADER1_NAME = "default_header";
    private static final String DEFAULT_HEADER1_VALUE1 = "value1";
    private static final String DEFAULT_HEADER1_VALUE2 = "value2";
    
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] UTF_8_REPRESENTATION = 
            {(byte)0xC3, (byte)0xA1, (byte)0xC5, (byte)0x99, (byte)0xC5, (byte)0xBE};
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};
    
    private static final String UTF_8_TYPE = "text/html; charset=UTF-8";
    private static final String ISO_8859_2_TYPE = "text/html; charset=ISO-8859-2";
    
    private HttpClient client;
    
    @Before
    public void setUp() {
        
        initJadlerThat()
                .usesStandardServerListeningOn(PORT)
                .respondsWithDefaultStatus(DEFAULT_STATUS)
                .respondsWithDefaultHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE1)
                .respondsWithDefaultHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE2)
                .respondsWithDefaultEncoding(Charset.forName("UTF-8"))
                .respondsWithDefaultContentType(UTF_8_TYPE);
        
        startMockServer();
        
        this.client = new HttpClient();
    }
    
    
    @After
    public void tearDown() {
        stopMockServer();
    }
    
    
    @Test
    public void havingBody() throws Exception {
        onRequest()
            .havingBodyEqualTo("postbody")
            .havingBody(notNullValue())
            .havingBody(not(isEmptyOrNullString()))
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + PORT);
        method.setRequestEntity(new StringRequestEntity("postbody", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingEmptyBody() throws Exception {
        onRequest()
            .havingBodyEqualTo("")
            .havingBody(notNullValue())
            .havingBody(isEmptyString())
        .respond()
            .withStatus(201);
                    
        final PostMethod method = new PostMethod("http://localhost:" + PORT);
        method.setRequestEntity(new StringRequestEntity("", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingUTF8Body() throws Exception {
        
        onRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + PORT);
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain", Charset.forName("UTF-8").name()));

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingISOBody() throws Exception {
        
        onRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + PORT);
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain", Charset.forName("ISO-8859-2").name()));

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingHeader() throws Exception {
        
        onRequest()
            .havingHeader("hdr1")
            .havingHeader("hdr1", hasSize(1))
            .havingHeader("hdr1", everyItem(not(isEmptyOrNullString())))
            .havingHeader("hdr1", contains("h1v1"))
            .havingHeaderEqualTo("hdr1", "h1v1")
            .havingHeader("hdr2")
            .havingHeader("hdr2", hasSize(2))
            .havingHeader("hdr2", contains("h2v1", "h2v2"))
            .havingHeader("hdr3", nullValue())
            .havingHeaderEqualTo("hdr2", "h2v1")
            .havingHeaderEqualTo("hdr2", "h2v2")
            .havingHeaders("hdr1", "hdr2")
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        method.addRequestHeader("hdr1", "h1v1");
        method.addRequestHeader("hdr2", "h2v1");
        method.addRequestHeader("hdr2", "h2v2");
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingMethod() throws Exception {
        onRequest()
            .havingMethodEqualTo("POST")
            .havingMethodEqualTo("poSt")
            .havingMethod(not(isEmptyOrNullString()))
            .havingMethod(anything())
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + PORT);

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
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
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT + "?p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4");

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingParameterPOST() throws Exception {
        final String body = "p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4";
        
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
            .havingBodyEqualTo(body)
        .respond()
            .withStatus(201);
        
        final PostMethod method = new PostMethod("http://localhost:" + PORT + "?p2=p2v3");
        method.setRequestEntity(new StringRequestEntity(body, "application/x-www-form-urlencoded", "UTF-8"));

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingQueryString() throws Exception {
        onRequest()
            .havingQueryStringEqualTo("p1=v1&p2=v2")
            .havingQueryString(not(isEmptyOrNullString()))
            .havingQueryString(anything())
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT + "?p1=v1&p2=v2");

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingNoQueryString() throws Exception {
        onRequest()
            .havingQueryString(nullValue())
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    @Test
    public void havingURI() throws Exception {
        onRequest()
            .havingURIEqualTo("/a/b/c/d")
            .havingURI(notNullValue())
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT + "/a/b/c/d");

        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingEmptyURI() throws IOException {
        onRequest()
            .havingURI(equalTo("/"))
            .havingURI(not(isEmptyOrNullString()))
        .respond()
            .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        final int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    @Test
    public void withDefaultEncoding() throws IOException {
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        client.executeMethod(method);
        
          //check that the body was really encoded in UTF-8
        final byte[] body = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(body, is(UTF_8_REPRESENTATION));
    }
    
    
    @Test
    public void withEncoding() throws IOException {
        onRequest().respond()
                .withEncoding(Charset.forName("ISO-8859-2"))
                .withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        client.executeMethod(method);
        
          //check that the body was really encoded in UTF-8
        final byte[] body = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(body, is(ISO_8859_2_REPRESENTATION));
    }    
    

    @Test
    public void withDefaultContentType() throws IOException {
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        client.executeMethod(method);

          //the content-type header charset was set to UTF-8
        assertThat(method.getResponseCharSet(), is("UTF-8"));
          //since the body was encoded in UTF-8 and content type charset was set to UTF-8,
          //the client should be able to read it correctly
        assertThat(method.getResponseBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    
    
    @Test
    public void withContentType() throws IOException {
        onRequest().respond()
                .withEncoding(Charset.forName("ISO-8859-2"))
                .withContentType(ISO_8859_2_TYPE)
                .withBody(STRING_WITH_DIACRITICS)
                .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        final int status = client.executeMethod(method);
        assertThat(status, is(201));

          //the content-type header charset was set to "ISO-8859-2"
        assertThat(method.getResponseCharSet(), is("ISO-8859-2"));
          //since the body was encoded in "ISO-8859-2" and content type charset was set to "ISO-8859-2",
          //the client should be able to read it correctly
        assertThat(method.getResponseBodyAsString(), is(STRING_WITH_DIACRITICS));
    }
    

    @Test
    public void withContentTypeEncodingMismatch() throws IOException {
        onRequest().respond()
                .withEncoding(Charset.forName("ISO-8859-2"))
                .withContentType(UTF_8_TYPE)
                .withBody(STRING_WITH_DIACRITICS)
                .withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        final int status = client.executeMethod(method);
        assertThat(status, is(201));

          //the content-type header charset was set to "UTF-8"
        assertThat(method.getResponseCharSet(), is("UTF-8"));
          //however the applied encoding is ISO-8859-2
        final byte[] body = IOUtils.toByteArray(method.getResponseBodyAsStream());
        assertThat(body, is(ISO_8859_2_REPRESENTATION));
    }
    
    
    @Test @Ignore
    public void withEncodingSockets() throws IOException {
        onRequest().respond()
                .withEncoding(Charset.forName("ISO-8859-2"))
                .withContentType(ISO_8859_2_TYPE)
                .withBody(STRING_WITH_DIACRITICS)
                .withStatus(201);
        
        final Socket sock = new Socket("localhost", PORT);
        final OutputStream out = sock.getOutputStream();
        out.write("GET / HTTP/1.1\r\nHost:localhost\r\n\r\n".getBytes());
        
        InputStream in = sock.getInputStream();
        
        int b;
        while ((b = in.read()) != -1) {
            System.out.print((char) b);
        }
    }
    
    
    @Test
    public void rulesOrdering() throws IOException {
          //this rule is never matched
        onRequest().that(is(not(anything()))).respond().withStatus(200);
          //these 3 rules are always matched, the first one must be applied
        onRequest().that(is(anything())).respond().withStatus(201);
        onRequest().that(is(anything())).respond().withStatus(202);
        onRequest().that(is(anything())).respond().withStatus(203);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        final int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    @Test
    public void noRuleApplicable() throws IOException {
        onRequest().that(is(not(anything()))).respond();
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        final int status = client.executeMethod(method);
        
        assertThat(status, is(500));
        assertThat(method.getResponseBodyAsString(), is(""));
    }
    
    
    @Test
    public void defaults() throws Exception {
        onRequest().respond();
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        int status = client.executeMethod(method);
        assertThat(status, is(DEFAULT_STATUS));

        final Header[] responseHeaders = method.getResponseHeaders(DEFAULT_HEADER1_NAME);
        assertThat(responseHeaders.length, is(2));
        assertThat(responseHeaders[0].getName(), is(DEFAULT_HEADER1_NAME));
        assertThat(responseHeaders[0].getValue(), is(DEFAULT_HEADER1_VALUE1));

        assertThat(responseHeaders[1].getName(), is(DEFAULT_HEADER1_NAME));
        assertThat(responseHeaders[1].getValue(), is(DEFAULT_HEADER1_VALUE2));
    }
    
    
    @Test
    public void overridenDefaultStatus() throws Exception {
        onRequest().respond().withStatus(201);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void overridenDefaultHeader() throws Exception {
        onRequest().respond().withHeader(DEFAULT_HEADER1_NAME, "value3");
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        int status = client.executeMethod(method);
        assertThat(status, is(DEFAULT_STATUS));

        final Header[] responseHeaders = method.getResponseHeaders(DEFAULT_HEADER1_NAME);
        assertThat(responseHeaders.length, is(3));
        assertThat(responseHeaders[0].getName(), is(DEFAULT_HEADER1_NAME));
        assertThat(responseHeaders[0].getValue(), is(DEFAULT_HEADER1_VALUE1));

        assertThat(responseHeaders[1].getName(), is(DEFAULT_HEADER1_NAME));
        assertThat(responseHeaders[1].getValue(), is(DEFAULT_HEADER1_VALUE2));

        assertThat(responseHeaders[2].getName(), is(DEFAULT_HEADER1_NAME));
        assertThat(responseHeaders[2].getValue(), is("value3"));      
    }
    
    
    @Test
    public void timeout() throws IOException {
        onRequest().respond().withTimeout(3, TimeUnit.SECONDS);
        
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        
        final long start = System.currentTimeMillis();
        client.executeMethod(method);
        final long end = System.currentTimeMillis();
        final long dur = end - start;
        assertThat(dur / 1000, is(greaterThanOrEqualTo(3L)));
    }
}
