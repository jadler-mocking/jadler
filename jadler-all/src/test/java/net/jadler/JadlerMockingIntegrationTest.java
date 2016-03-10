/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.mocking.VerificationException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import net.jadler.junit.rule.JadlerRule;
import org.junit.Rule;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static net.jadler.Jadler.verifyThatRequest;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;


/**
 * Suite of several integration tests for the stubbing part of the Jadler library.
 * Each test configures the stub server and tests either the <i>WHEN</i> or <i>THEN</i> part of http stubbing using
 * an http client.
 */
public class JadlerMockingIntegrationTest {
        
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] UTF_8_REPRESENTATION = 
            {(byte)0xC3, (byte)0xA1, (byte)0xC5, (byte)0x99, (byte)0xC5, (byte)0xBE};
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};
    
    private static final byte[] BINARY_BODY = {1, 2, 3};
    
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final Charset ISO_8859_2_CHARSET = Charset.forName("ISO-8859-2");
    
    private HttpClient client;
    
    
    @Rule
    public JadlerRule jadlerRule = new JadlerRule();
    
    @Before
    public void setUp() {
        onRequest().respond().withStatus(200);
        
        this.client = new HttpClient();
    }
    
    
    /*
     * Tests the havingBody methods.
     */
    @Test
    public void havingBody() throws Exception {
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("postbody", null, null));
        
        this.client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo("postbody")
            .havingBody(notNullValue())
            .havingBody(not(isEmptyOrNullString()))
        .receivedOnce();
    }
    
    
    /*
     * An empty string (not null) is matched for an empty http request.
     */
    @Test
    public void havingEmptyBody() throws Exception {               
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("", null, null));
        
        this.client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo("")
            .havingBody(notNullValue())
            .havingBody(isEmptyString())
        .receivedTimes(1);
    }
    
    
    /*
     * Tests the havingRawBody method
     */
    @Test
    public void havingRawBody() throws IOException {
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new ByteArrayRequestEntity(BINARY_BODY));
        
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingRawBodyEqualTo(BINARY_BODY)
        .receivedTimes(equalTo(1));
    }
    
    
    /*
     * An empty array (not null) is matched for an empty http request.
     */
    @Test
    public void havingRawEmptyBody() throws IOException {        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingRawBodyEqualTo(new byte[0])
        .receivedTimes(both(lessThan(2)).and(not(lessThan(0))));
    }
    
    
    /*
     * Matches a request with a text body encoded using UTF-8.
     */
    @Test
    public void havingUTF8Body() throws Exception {        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain", UTF_8_CHARSET.name()));

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(UTF_8_REPRESENTATION)
        .receivedOnce();
    }
    
    
    /*
     * Matches a request with a text body encoded using ISO-8859-2.
     */
    @Test
    public void havingISOBody() throws Exception {
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain", ISO_8859_2_CHARSET.name()));

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(ISO_8859_2_REPRESENTATION)
        .receivedOnce();
    }
    
    
    /*
     * Tests a combination of the havingHeader methods.
     */
    @Test
    public void havingHeader() throws Exception {
        final GetMethod method = new GetMethod("http://localhost:" + port());
        method.addRequestHeader("hdr1", "h1v1");
        method.addRequestHeader("hdr2", "h2v1");
        method.addRequestHeader("hdr2", "h2v2");
        
        client.executeMethod(method);
        
        verifyThatRequest()
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
        .receivedOnce();
    }
    
    
    /*
     * I'm not sure whether a request header can be empty according to the RFC. However, it seems to work. 
     */
    @Test
    public void havingEmptyHeader() throws IOException {
        final GetMethod method = new GetMethod("http://localhost:" + port());
        method.addRequestHeader("empty", "");
        
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingHeaderEqualTo("empty", "")
            .havingHeader("empty")
            .havingHeader("empty", everyItem(isEmptyString()))
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingMethod methods.
     */
    @Test
    public void havingMethod() throws Exception {
        final PostMethod method = new PostMethod("http://localhost:" + port());

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingMethodEqualTo("POST")
                //the comparison must be case insensitive
            .havingMethodEqualTo("poSt")
            .havingMethod(not(isEmptyOrNullString()))
            .havingMethod(anything())
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingParameter methods for a GET http request. Only query string values
     * are considered http parameters for a GET http request.
     */
    @Test
    public void havingParameterGET() throws Exception {        
        final GetMethod method = new GetMethod("http://localhost:" + port() + 
                "?p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&url%20encoded=url%20encoded");

        client.executeMethod(method);
        
        verifyThatRequest()
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
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingParameter methods for a POST http request with the
     * application/x-www-form-urlencoded content type. Both query string and request body values
     * are considered http parameters for such an http request.
     * 
     * This test also tests a combination of havingParameter and havingBody methods
     * since both of these require an access to the request body (which causes troubles
     * in the servlet specification).
     */
    @Test
    public void havingParameterPOST() throws Exception {
        final String body = "p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&url%20encoded=url%20encoded";
        
        final PostMethod method = new PostMethod("http://localhost:" + port() + "?p2=p2v3");
        method.setRequestEntity(new StringRequestEntity(body, "application/x-www-form-urlencoded", "UTF-8"));

        client.executeMethod(method);
        
        verifyThatRequest()
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
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingQueryString methods. 
     */
    @Test
    public void havingQueryString() throws Exception {
        final GetMethod method = new GetMethod("http://localhost:" + port() + "?p1=v1&p2=v2&name=%C5%99eho%C5%99");

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingQueryStringEqualTo("p1=v1&p2=v2&name=%C5%99eho%C5%99")
            .havingQueryString(not(isEmptyOrNullString()))
            .havingQueryString(anything())
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingQueryString methods. 
     */
    @Test
    public void havingEmptyQueryString() throws Exception {        
          //it seems HttpClient cannot send a request with an empty query string ('?' as the last character)
          //let's test this in a more hardcore fashion
        final URL url = new URL("http://localhost:" + port() + "/?");
        final HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.getResponseCode();

        verifyThatRequest()
            .havingQueryStringEqualTo("")
            .havingQueryString(isEmptyString())
        .receivedOnce();
    }
    
    
    /*
     * Null value is matched for a http request without a query string
     */
    @Test
    public void havingNoQueryString() throws Exception {        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingQueryString(nullValue())
            .havingQueryString(not(equalTo("")))
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingPath methods.
     */
    @Test
    public void havingPath() throws Exception {
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/a/b/c/d/%C5%99");

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingPathEqualTo("/a/b/c/d/%C5%99")
            .havingPath(notNullValue())
        .receivedOnce();
    }
    
    
    /*
     * Tests the havingPath methods for a root path.
     */
    @Test
    public void havingRootPath() throws IOException {
          //if there was no slash at the end, the GetMethod constructor would add it automatically
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingPath(equalTo("/"))
            .havingPath(not(isEmptyOrNullString()))
        .receivedOnce();
    }
    
    
    /*
     * Tests verifying without any predicates (all requests matched in this case) 
     */
    @Test
    public void noPredicates() throws IOException {
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        client.executeMethod(method);
        
        verifyThatRequest().receivedOnce();
    }
    
    
    /*
     * No such a request received
     */
    @Test
    public void noSuchRequest() throws IOException {
        verifyThatRequest()
        .receivedNever();
        
        verifyThatRequest()
        .receivedTimes(0);
        
        verifyThatRequest()
        .receivedTimes(equalTo(0));
    }
    
    
    /*
     * No such a request received
     */
    @Test(expected=VerificationException.class)
    public void verificationFailed() {
        verifyThatRequest()
            .havingMethodEqualTo("POST")
        .receivedTimes(not(lessThan(1)));
    }
    
    
    /* tests https://github.com/jadler-mocking/jadler/issues/110 */
    @Test
    public void twoIdenticalRequests() throws IOException {
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        this.client.executeMethod(method);
        this.client.executeMethod(method);
        
        verifyThatRequest().receivedTimes(2);
    }
}
