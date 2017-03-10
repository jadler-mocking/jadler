/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.mocking.VerificationException;
import org.junit.Test;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import net.jadler.junit.rule.JadlerRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import net.jadler.parameters.StubHttpServerFactory;
import net.jadler.parameters.TestParameters;
import org.junit.runners.Parameterized.Parameters;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.AfterClass;

import static net.jadler.Jadler.port;
import static net.jadler.Jadler.verifyThatRequest;
import static net.jadler.utils.TestUtils.jadlerUri;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;


/**
 * Integration/acceptance tests for the verification part of Jadler.
 */
@RunWith(Parameterized.class)
public class VerificationIntegrationTest {
        
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] UTF_8_REPRESENTATION = 
            {(byte)0xC3, (byte)0xA1, (byte)0xC5, (byte)0x99, (byte)0xC5, (byte)0xBE};
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte)0xE1, (byte)0xF8, (byte)0xBE};
    
    private static final byte[] BINARY_BODY = {1, 2, 3};
    
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final Charset ISO_8859_2_CHARSET = Charset.forName("ISO-8859-2");
    
    
    @Parameters
    public static Iterable<StubHttpServerFactory[]> parameters() {
        return new TestParameters().provide();
    }

    public VerificationIntegrationTest(final StubHttpServerFactory serverFactory) {
        this.jadlerRule = new JadlerRule(serverFactory.createServer());
    }
    
    
    @Rule
    public final JadlerRule jadlerRule;
    
    
    @AfterClass
    public static void cleanup() {
        Executor.closeIdleConnections();
    }
    
    
    /*
     * Request nonempty body verification
     */
    @Test
    public void havingBody() throws IOException {
        Executor.newInstance().execute(Request.Post(jadlerUri()).bodyString("postbody", null)).discardContent();
        
        verifyThatRequest()
            .havingBodyEqualTo("postbody")
            .havingBody(not(isEmptyOrNullString()))
        .receivedOnce();
    }
    
    
    /*
     * Request empty body verification
     */
    @Test
    public void havingEmptyBody() throws IOException {               
        Executor.newInstance().execute(Request.Post(jadlerUri())).discardContent();
        
        verifyThatRequest()
            .havingBodyEqualTo("")
            .havingBody(notNullValue())
            .havingBody(isEmptyString())
        .receivedTimes(1);
    }
    
    
    /*
     * Request raw body verification
     */
    @Test
    public void havingRawBody() throws IOException {
        Executor.newInstance().execute(Request.Post(jadlerUri()).bodyByteArray(BINARY_BODY)).discardContent();
        
        verifyThatRequest()
            .havingRawBodyEqualTo(BINARY_BODY)
        .receivedTimes(equalTo(1));
    }
    
    
    /*
     * Request raw empty body verification
     */
    @Test
    public void havingRawEmptyBody() throws IOException {
        Executor.newInstance().execute(Request.Post(jadlerUri())).discardContent();
        
        verifyThatRequest()
            .havingRawBodyEqualTo(new byte[0])
        .receivedTimes(1);
    }
    
    
    /*
     * Request body encoded in UTF-8 scenario
     */
    @Test
    public void havingUTF8Body() throws IOException {        
          //the request body contains a string with diacritics encoded using UTF-8
        Executor.newInstance().execute(Request.Post(jadlerUri())
                .bodyString(STRING_WITH_DIACRITICS, ContentType.create("text/plain", "UTF-8")))
                .discardContent();
        
        verifyThatRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(UTF_8_REPRESENTATION)
        .receivedOnce();
    }
    
    
    /*
     * Request body encoded in ISO-8859-2 scenario
     */
    @Test
    public void havingISOBody() throws IOException {
          //the request body contains a string with diacritics encoded using ISO-8859-2
        Executor.newInstance().execute(Request.Post(jadlerUri())
                .bodyString(STRING_WITH_DIACRITICS, ContentType.create("text/plain", "ISO-8859-2")))
                .discardContent();
                
        verifyThatRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(ISO_8859_2_REPRESENTATION)
        .receivedOnce();
    }
    
    
    /*
     * Request headers verification
     */
    @Test
    public void havingHeader() throws IOException {
        
        Executor.newInstance().execute(Request.Get(jadlerUri())
                .addHeader("hdr1", "h1v1") //hdr1 has one value
                .addHeader("hdr2", "h2v1")
                .addHeader("hdr2", "h2v2")) //two values for hdr2
                .discardContent();
        
        verifyThatRequest()
                //hdr1 has exactly one value, h1v1
            .havingHeader("hdr1")
            .havingHeaderEqualTo("hdr1", "h1v1")
            .havingHeader("hdr1", not(empty()))
            .havingHeader("hDR1", hasSize(1))
            .havingHeader("hdr1", contains("h1v1"))
                //hdr2 has two values, h2v1 a h2v2
            .havingHeader("HDr2")
            .havingHeaderEqualTo("hdr2", "h2v1")
            .havingHeaderEqualTo("HDR2", "h2v2")
            .havingHeader("hdr2", hasSize(2))
            .havingHeader("hdr2", contains("h2v1", "h2v2"))
            .havingHeader("hdr2", hasItem("h2v1"))
                //both hdr1 and hdr2 headers are present in the request
            .havingHeaders("hDR1", "hdr2")
                //there is no hdr3 in the request
            .havingHeader("hdr3", nullValue())
        .receivedOnce();
    }
    
    
    /*
     * An empty header (an existing header without a value) verification scenario.
     * 
     * I'm not sure whether a request header can be empty according to the RFC. However, it seems to work. 
     */
    @Test
    public void havingEmptyHeader() throws IOException {
        Executor.newInstance().execute(Request.Get(jadlerUri()).addHeader("empty", "")).discardContent();
        
        verifyThatRequest()
            .havingHeaderEqualTo("empty", "")
            .havingHeader("empty")
            .havingHeader("empty", everyItem(isEmptyString()))
        .receivedOnce();
    }
    
    
    /*
     * Request method verification
     */
    @Test
    public void havingMethod() throws IOException {
        Executor.newInstance().execute(Request.Post(jadlerUri())).discardContent();
        
        verifyThatRequest()
            .havingMethodEqualTo("POST")
            .havingMethodEqualTo("poSt") //the comparison must be case insensitive
        .receivedOnce();
    }
    
    
    /*
     * Request parameters verification
     *
     * Only query string values are considered http parameters since it's a GET http request.
     */
    @Test
    public void havingParameter_GET() throws IOException {
        Executor.newInstance().execute(
                Request.Get(jadlerUri() + "?p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&p%206=percent%20encoded")).discardContent();
        
        verifyThatRequest()
                //p1 has exactly one value, p1v1
            .havingParameter("p1")
            .havingParameterEqualTo("P1", "p1v1") //case insensitive
            .havingParameter("p1", hasSize(1))
            .havingParameter("p1", contains("p1v1"))
                //p2 has two values, p2v1 and p2v2
            .havingParameter("p2")
            .havingParameterEqualTo("p2", "p2v1")
            .havingParameterEqualTo("p2", "p2v2")
            .havingParameter("p2", hasSize(2))
            .havingParameter("p2", hasItems("p2v1", "p2v2"))
            .havingParameter("P2", contains("p2v1", "p2v2"))
            .havingParameter("p2", everyItem(not(isEmptyOrNullString())))
                //p3 is an existing param with no value, '=' character is used in the query string
            .havingParameter("p3")
            .havingParameterEqualTo("p3", "")
            .havingParameter("p3", contains(""))
                //p4 is an existing param with no value, '=' character is not used in the query string
            .havingParameter("p4")
            .havingParameterEqualTo("p4", "")
            .havingParameter("p4", contains(""))
                //p5 is not an existing param
            .havingParameter("p5", nullValue())
                //'p 6' has a percent-encoded name and value
                //both is available in the percent-encoded form for verification
            .havingParameter("p%206")
            .havingParameterEqualTo("p%206", "percent%20encoded")
            .havingParameter("p%206", contains("percent%20encoded"))
                //p1, p2 and 'p 6' are present among other params
            .havingParameters("p1", "p2", "p%206")
        .receivedOnce();
    }
    
    
    /*
     * Request parameters verification
     * 
     * Tests the havingParameter methods for a POST http request with the
     * application/x-www-form-urlencoded content type. Both query string and request body values
     * are considered http parameters sources for such an http request.
     */
    @Test
    public void havingParameterPOST() throws IOException {
          //url encoded body of the request containing several parameters
        final String body = "p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&p%206=percent%20encoded";
          //the query string contains additional parameters
        Executor.newInstance().execute(Request.Post(jadlerUri() + "?p2=p2v3&p7=p7v1")
                .bodyString(body, ContentType.create("application/x-www-form-urlencoded", "UTF-8")))
                .discardContent();
        
        verifyThatRequest()
                //p1 has exactly one value, p1v1
            .havingParameter("p1")
            .havingParameterEqualTo("p1", "p1v1")
            .havingParameter("p1", hasSize(1))
            .havingParameter("p1", everyItem(not(isEmptyOrNullString())))
            .havingParameter("p1", contains("p1v1"))
                //p2 has three values, two from the body (p2v1 and p2v2) and one (p2v3) from the query string
            .havingParameter("p2")
            .havingParameterEqualTo("p2", "p2v1")
            .havingParameterEqualTo("p2", "p2v2")
            .havingParameterEqualTo("p2", "p2v3")
            .havingParameter("p2", hasSize(3))
            .havingParameter("p2", hasItems("p2v1", "p2v2", "p2v3"))
            .havingParameter("p2", containsInAnyOrder("p2v1", "p2v2", "p2v3"))
                //p3 is an existing param with no value, '=' character is used in the body string
            .havingParameter("p3")
            .havingParameterEqualTo("p3", "")
            .havingParameter("p3", contains(""))
                //p4 is an existing param with no value, '=' character is not used in the body string
            .havingParameter("p4")
            .havingParameterEqualTo("p4", "")
            .havingParameter("p4", contains(""))
                //there is no p5 param in the request
            .havingParameter("p5", nullValue())
                //'p 6' has a percent-encoded name and value
                //both is available in the percent-encoded form for verification
            .havingParameter("p%206")
            .havingParameterEqualTo("p%206", "percent%20encoded")
            .havingParameter("p%206", contains("percent%20encoded"))
                //p7 is an existing parameter coming from the query string
            .havingParameter("p7")
            .havingParameterEqualTo("p7", "p7v1")
            .havingParameter("p7", hasSize(1))
                //p1, p2, 'p 6' and p7 are present among other params
            .havingParameters("p1", "p2", "p%206", "p7")
                //there was a bug when verifying both body and params received from the body
                //so let's verify even a body here as well
            .havingBodyEqualTo(body)
        .receivedOnce();
    }
    
    
    /*
     * Query string verification
     */
    @Test
    public void havingQueryString() throws IOException {
        final String queryString = "p1=v1&p2=v2&name=%C5%99eho%C5%99";
        
        Executor.newInstance().execute(Request.Get(jadlerUri() + '?' + queryString)).discardContent();
        
        verifyThatRequest()
            .havingQueryStringEqualTo(queryString)
            .havingQueryString(not(startsWith("?"))) //no '?' character at the beginning
        .receivedOnce();
    }
    
    
    /*
     * Empty query string verification
     */
    @Test
    public void havingQueryString_empty() throws IOException {        
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
     * Missing query string verification
     */
    @Test
    public void havingQueryString_none() throws IOException {
        Executor.newInstance().execute(Request.Get(jadlerUri())).discardContent();
        
        verifyThatRequest()
            .havingQueryString(nullValue())
            .havingQueryString(not(equalTo("")))
        .receivedOnce();
    }
    
    
    /*
     * Path verification
     */
    @Test
    public void havingPath() throws IOException {
        final String path = "/a/b/c/d/%C5%99";
        
        Executor.newInstance().execute(Request.Get(jadlerUri() + path + "?param=value")).discardContent();

        verifyThatRequest()
            .havingPathEqualTo(path) //the query string value is excluded
            .havingPath(notNullValue())
        .receivedOnce();
    }
    
    
    /*
     * Root path verification
     */
    @Test
    public void havingPath_root() throws IOException {
        Executor.newInstance().execute(Request.Get(jadlerUri() + '/')).discardContent();

        verifyThatRequest()
            .havingPath(equalTo("/"))
            .havingPath(not(isEmptyOrNullString()))
        .receivedOnce();
    }
    
    
    /*
     * Verification with an empty set of predicates (all requests matched in this case) 
     */
    @Test
    public void noPredicates() throws IOException {
        Executor.newInstance().execute(Request.Get(jadlerUri())).discardContent();
        
        verifyThatRequest().receivedOnce();
    }
    
    
    /*
     * No request received scenario
     */
    @Test
    public void noRequest() {
        verifyThatRequest()
        .receivedNever();
        
        verifyThatRequest()
        .receivedTimes(0);
        
        verifyThatRequest()
        .receivedTimes(equalTo(0));
    }
    
    
    /*
     * no request reveived negative scenario
     */
    @Test(expected=VerificationException.class)
    public void noRequest_negative() {
        verifyThatRequest().receivedTimes(1);
    }
    
    
    /*
     * Two identical requests scenario (bug https://github.com/jadler-mocking/jadler/issues/110)
     */
    @Test
    public void twoIdenticalRequests() throws IOException {
        final Executor exec = Executor.newInstance();
        final Request req = Request.Get(jadlerUri());
        
        exec.execute(req).discardContent();
        exec.execute(req).discardContent();
        
        verifyThatRequest().receivedTimes(2);
    }
}
