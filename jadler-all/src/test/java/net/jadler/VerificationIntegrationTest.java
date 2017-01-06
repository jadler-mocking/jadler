/*
 * Copyright (c) 2012 - 2016 Jadler contributors
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import net.jadler.parameters.StubHttpServerFactory;
import net.jadler.parameters.TestParameters;
import org.junit.runners.Parameterized.Parameters;

import static net.jadler.Jadler.port;
import static net.jadler.Jadler.verifyThatRequest;
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
    
    private HttpClient client;
    
    @Parameters
    public static Iterable<StubHttpServerFactory[]> parameters() {
        return new TestParameters().provide();
    }

    public VerificationIntegrationTest(final StubHttpServerFactory serverFactory) {
        this.jadlerRule = new JadlerRule(serverFactory.createServer());
    }
    
    
    @Rule
    public final JadlerRule jadlerRule;
    
    
    @Before
    public void setUp() {
        this.client = new HttpClient();
    }
    
    
    /*
     * Request nonempty body verification
     */
    @Test
    public void havingBody() throws Exception {
        final PostMethod method = new PostMethod("http://localhost:" + port());
        method.setRequestEntity(new StringRequestEntity("postbody", null, null));
        
        this.client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo("postbody")
            .havingBody(not(isEmptyOrNullString()))
        .receivedOnce();
    }
    
    
    /*
     * Request empty body verification
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
     * Request raw body verification
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
     * Request raw empty body verification
     */
    @Test
    public void havingRawEmptyBody() throws IOException {        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingRawBodyEqualTo(new byte[0])
        .receivedTimes(1);
    }
    
    
    /*
     * Request body encoded in UTF-8 scenario
     */
    @Test
    public void havingUTF8Body() throws Exception {        
        final PostMethod method = new PostMethod("http://localhost:" + port());
        //the request body contains a string with diacritics encoded using UTF-8
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain; charset=UTF-8", UTF_8_CHARSET.name()));
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(UTF_8_REPRESENTATION)
        .receivedOnce();
    }
    
    
    /*
     * Request body encoded in ISO-8859-2 scenario
     */
    @Test
    public void havingISOBody() throws Exception {
        final PostMethod method = new PostMethod("http://localhost:" + port());
        //the request body contains a string with diacritics encoded using ISO-8859-2
        method.setRequestEntity(
                new StringRequestEntity(STRING_WITH_DIACRITICS, "text/plain; charset=ISO-8859-2",
                        ISO_8859_2_CHARSET.name()));
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingBodyEqualTo(STRING_WITH_DIACRITICS)
            .havingRawBodyEqualTo(ISO_8859_2_REPRESENTATION)
        .receivedOnce();
    }
    
    
    /*
     * Request headers verification
     */
    @Test
    public void havingHeader() throws Exception {
        final GetMethod method = new GetMethod("http://localhost:" + port());
        method.addRequestHeader("hdr1", "h1v1"); //hdr1 has one value
        method.addRequestHeader("hdr2", "h2v1");
        method.addRequestHeader("hdr2", "h2v2"); //two values for hdr2
        client.executeMethod(method);
        
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
     * Request method verification
     */
    @Test
    public void havingMethod() throws Exception {
        final PostMethod method = new PostMethod("http://localhost:" + port());
        client.executeMethod(method);
        
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
    public void havingParameter_GET() throws Exception {        
        final GetMethod method = new GetMethod("http://localhost:" + port() + 
                "?p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&p%206=percent%20encoded");

        client.executeMethod(method);
        
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
    public void havingParameterPOST() throws Exception {
        //url encoded body of the request containing several parameters
        final String body = "p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&p%206=percent%20encoded";
        //the query string contains additional parameters
        final PostMethod method = new PostMethod("http://localhost:" + port() + "?p2=p2v3&p7=p7v1");
        method.setRequestEntity(new StringRequestEntity(body, "application/x-www-form-urlencoded", "UTF-8"));
        client.executeMethod(method);
        
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
    public void havingQueryString() throws Exception {
        final String queryString = "p1=v1&p2=v2&name=%C5%99eho%C5%99";
        
        final GetMethod method = new GetMethod("http://localhost:" + port() + '?' + queryString);
        client.executeMethod(method);
        
        verifyThatRequest()
            .havingQueryStringEqualTo(queryString)
            .havingQueryString(not(startsWith("?"))) //no '?' character at the beginning
        .receivedOnce();
    }
    
    
    /*
     * Empty query string verification
     */
    @Test
    public void havingQueryString_empty() throws Exception {        
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
    public void havingQueryString_none() throws Exception {        
        final GetMethod method = new GetMethod("http://localhost:" + port());

        client.executeMethod(method);
        
        verifyThatRequest()
            .havingQueryString(nullValue())
            .havingQueryString(not(equalTo("")))
        .receivedOnce();
    }
    
    
    /*
     * Path verification
     */
    @Test
    public void havingPath() throws Exception {
        final String path = "/a/b/c/d/%C5%99";
        final GetMethod method = new GetMethod("http://localhost:" + port() + path + "?param=value");
        client.executeMethod(method);
        
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
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        client.executeMethod(method);
        
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
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        client.executeMethod(method);
        
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
        final GetMethod method = new GetMethod("http://localhost:" + port() + "/");
        this.client.executeMethod(method);
        this.client.executeMethod(method);
        
        verifyThatRequest().receivedTimes(2);
    }
}
