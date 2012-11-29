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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static net.jadler.Jadler.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class JadlerIntegrationTest {
    
    private static final int PORT = 44532;
    private static final String UNICODE_BODY = "\u011b\u0161\u010d\u0159\u0159\u017e\u00fd\u00e1\u00e1\u00ed\u00e9";
    private static final int DEFAULT_STATUS = 409;
    private static final String DEFAULT_HEADER1_NAME = "default_header";
    private static final String DEFAULT_HEADER1_VALUE1 = "value1";
    private static final String DEFAULT_HEADER1_VALUE2 = "value2";
    
    @Before
    public void setUp() {
        
        initJadlerThat()
                .usesStandardServerListeningOn(PORT)
                .respondsWithDefaultStatus(DEFAULT_STATUS)
                .respondsWithDefaultHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE1)
                .respondsWithDefaultHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE2);
        
        startMockServer();
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
        
        final HttpClient client = new HttpClient();
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
                    
        final HttpClient client = new HttpClient();
        final PostMethod method = new PostMethod("http://localhost:" + PORT);
        method.setRequestEntity(new StringRequestEntity("", null, null));
        
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void havingUnicodeBody() throws Exception {
        
        onRequest()
            .havingBodyEqualTo(UNICODE_BODY)
        .respond()
            .withStatus(201);
        
        final HttpClient client = new HttpClient();
        final PostMethod method = new PostMethod("http://localhost:" + PORT);
        method.setRequestEntity(
                new StringRequestEntity(UNICODE_BODY, "text/plain", Charset.forName("UTF-8").name()));

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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + PORT);

        final int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    @Test
    public void rulesOrdering() throws IOException {
          //this rule is never matched
        onRequest().that(is(not(anything()))).respond().withStatus(200);
          //these 3 rules are always matched, the first one must be applied
        onRequest().that(is(anything())).respond().withStatus(201);
        onRequest().that(is(anything())).respond().withStatus(202);
        onRequest().that(is(anything())).respond().withStatus(203);
        
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        final int status = client.executeMethod(method);
        assertThat(status, is(201));        
    }
    
    
    @Test
    public void noRuleApplicable() throws IOException {
        onRequest().that(not(anything())).respond();
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        final int status = client.executeMethod(method);
        
        assertThat(status, is(500));
        assertThat(method.getResponseBodyAsString(), is(""));
    }
    
    
    @Test
    public void defaults() throws Exception {
        onRequest().respond();
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        int status = client.executeMethod(method);
        assertThat(status, is(201));
    }
    
    
    @Test
    public void overridenDefaultHeader() throws Exception {
        onRequest().respond().withHeader(DEFAULT_HEADER1_NAME, "value3");
        
        final HttpClient client = new HttpClient();
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
        
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod("http://localhost:" + PORT);
        
        final long start = System.currentTimeMillis();
        client.executeMethod(method);
        final long end = System.currentTimeMillis();
        final long dur = end - start;
        assertThat(dur / 1000, is(greaterThanOrEqualTo(3L)));
    }
}
