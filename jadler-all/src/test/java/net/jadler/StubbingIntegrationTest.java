/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.parameters.StubHttpServerFactory;
import net.jadler.parameters.TestParameters;
import net.jadler.stubbing.Responder;
import net.jadler.stubbing.StubResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static net.jadler.utils.TestUtils.STATUS_RETRIEVER;
import static net.jadler.utils.TestUtils.jadlerUri;
import static net.jadler.utils.TestUtils.rawBodyOf;
import static net.jadler.utils.TestUtils.stringBodyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;


/**
 * <p>Suite of several integration/acceptance tests for the stubbing part ofJadler.</p>
 *
 * <p>Each test configures the stub server and tests either the <i>WHEN</i> or <i>THEN</i> part of http stubbing using
 * an http client.</p>
 */
@RunWith(Parameterized.class)
public class StubbingIntegrationTest {

    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";

    private static final int DEFAULT_STATUS = 409;
    private static final String DEFAULT_HEADER1_NAME = "default_header";
    private static final String DEFAULT_HEADER1_VALUE1 = "value1";
    private static final String DEFAULT_HEADER1_VALUE2 = "value2";
    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-16";
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-16");
    private static final byte[] DEFAULT_ENCODING_BODY_REPRESENTATION = STRING_WITH_DIACRITICS.getBytes(DEFAULT_CHARSET);

    private static final String HEADER_NAME1 = "header1";
    private static final String HEADER_VALUE11 = "value11";
    private static final String HEADER_VALUE12 = "value12";

    private static final String HEADER_NAME2 = "header2";
    private static final String HEADER_VALUE2 = "value2";

    private static final byte[] BINARY_BODY = {1, 2, 3};

    private static final String UTF_8_TYPE = "text/html; charset=UTF-8";
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final byte[] UTF_8_BODY_REPRESENTATION = STRING_WITH_DIACRITICS.getBytes(UTF_8_CHARSET);

    private static final String ISO_8859_2_TYPE = "text/html; charset=ISO-8859-2";
    private static final Charset ISO_8859_2_CHARSET = Charset.forName("ISO-8859-2");
    private static final byte[] ISO_8859_2_BODY_REPRESENTATION = STRING_WITH_DIACRITICS.getBytes(ISO_8859_2_CHARSET);

    private final StubHttpServerFactory serverFactory;


    public StubbingIntegrationTest(final StubHttpServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    @Parameters
    public static Iterable<StubHttpServerFactory[]> parameters() {
        return new TestParameters().provide();
    }

    @AfterClass
    public static void cleanup() {
        Executor.closeIdleConnections();
    }

    @Before
    public void setUp() {

        initJadlerUsing(serverFactory.createServer())
                .withDefaultResponseStatus(DEFAULT_STATUS)
                .withDefaultResponseHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE1)
                .withDefaultResponseHeader(DEFAULT_HEADER1_NAME, DEFAULT_HEADER1_VALUE2)
                .withDefaultResponseEncoding(DEFAULT_CHARSET)
                .withDefaultResponseContentType(DEFAULT_CONTENT_TYPE);
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    /*
     * Nonempty body stubbing scenario
     */
    @Test
    public void havingBody() throws Exception {
        final String body = "postbody";

        onRequest()
                .havingBodyEqualTo(body)
                .havingBody(not(emptyOrNullString()))
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri()).bodyString(body, null))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Empty body stubbing scenario
     */
    @Test
    public void havingEmptyBody() throws Exception {
        onRequest()
                .havingBodyEqualTo("")
                .havingBody(notNullValue())
                .havingBody(is(emptyString()))
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri()))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Raw (binary) body stubbing scenario
     */
    @Test
    public void havingRawBody() throws IOException {
        onRequest()
                .havingRawBodyEqualTo(BINARY_BODY)
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri()).bodyByteArray(BINARY_BODY))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Empty raw body (represented as an empty array, not {@code null}) stubbing scenario
     */
    @Test
    public void havingRawEmptyBody() throws IOException {
        onRequest()
                .havingRawBodyEqualTo(new byte[0])
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri()))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * UTF-8 encoded body stubbing scenario
     */
    @Test
    public void havingUTF8Body() throws Exception {

        onRequest()
                .havingBodyEqualTo(STRING_WITH_DIACRITICS)
                .havingRawBodyEqualTo(UTF_8_BODY_REPRESENTATION)
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri())
                        .bodyString(STRING_WITH_DIACRITICS, ContentType.create("text/plain", UTF_8_CHARSET)))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * ISO-8859-2 encoded body stubbing scenario
     */
    @Test
    public void havingISOBody() throws Exception {

        onRequest()
                .havingBodyEqualTo(STRING_WITH_DIACRITICS)
                .havingRawBodyEqualTo(ISO_8859_2_BODY_REPRESENTATION)
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri())
                        .bodyString(STRING_WITH_DIACRITICS, ContentType.create("text/plain", ISO_8859_2_CHARSET)))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Request headers stubbing scenario
     */
    @Test
    public void havingHeader() throws Exception {

        onRequest()
                //hdr1 has exactly one value, h1v1
                .havingHeader("hdr1")
                .havingHeaderEqualTo("hdr1", "h1v1")
                .havingHeader("hdr1", not(empty()))
                .havingHeader("hDR1", hasSize(1))
                .havingHeader("hdr1", everyItem(not(emptyOrNullString())))
                .havingHeader("hdr1", contains("h1v1"))
                //hdr2 has two values, h2v1 and h2v2
                .havingHeader("HDr2")
                .havingHeaderEqualTo("hdr2", "h2v1")
                .havingHeaderEqualTo("HDR2", "h2v2")
                .havingHeader("hdr2", hasSize(2))
                .havingHeader("hdr2", contains("h2v1", "h2v2"))
                .havingHeader("hdr2", hasItem("h2v1"))
                .havingHeaders("hDR1", "hdr2")
                //there is no hdr3 header
                .havingHeader("hdr3", nullValue())
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri())
                        .addHeader("hdr1", "h1v1").addHeader("hdr2", "h2v1").addHeader("hdr2", "h2v2"))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * An empty header (an existing header without a value) stubbing scenario.
     *
     * I'm not sure whether a request header can be empty according to the RFC. However, it seems to work.
     */
    @Test
    public void havingEmptyHeader() throws IOException {
        onRequest()
                .havingHeaderEqualTo("empty", "")
                .havingHeader("empty")
                .havingHeader("empty", everyItem(emptyString()))
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri()).addHeader("empty", ""))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Method stubbing scenario.
     */
    @Test
    public void havingMethod() throws Exception {
        onRequest()
                .havingMethodEqualTo("POST")
                .havingMethodEqualTo("poSt")
                .havingMethod(not(emptyOrNullString()))
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri()))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Request parameters stubbing scenario.
     *
     * Only query string values are considered http parameters since it's a GET http request.
     */
    @Test
    public void havingParameterGET() throws Exception {

        onRequest()
                //p1 has exactly one value, p1v1
                .havingParameter("p1")
                .havingParameterEqualTo("p1", "p1v1")
                .havingParameter("p1", hasSize(1))
                .havingParameter("p1", everyItem(not(emptyOrNullString())))
                .havingParameter("p1", contains("p1v1"))
                //p2 has two values, p2v1 and p2v2
                .havingParameter("p2")
                .havingParameterEqualTo("p2", "p2v1")
                .havingParameterEqualTo("p2", "p2v2")
                .havingParameter("p2", hasSize(2))
                .havingParameter("p2", hasItems("p2v1", "p2v2"))
                //p3 is an existing param with no value, '=' character is used in the query string
                .havingParameter("p3")
                .havingParameter("p3", contains(""))
                .havingParameterEqualTo("p3", "")
                //p4 is an existing param with no value, '=' character is not used in the query string
                .havingParameter("p4")
                .havingParameter("p4", contains(""))
                .havingParameterEqualTo("p4", "")
                //p5 is not an existing param
                .havingParameter("p5", nullValue())
                //'p 6' has a percent-encoded name and value
                //both is available in the percent-encoded form for stubbing
                .havingParameter("p%206")
                .havingParameterEqualTo("p%206", "percent%20encoded")
                .havingParameter("p%206", contains("percent%20encoded"))
                .havingParameters("p1", "p2", "p%206")
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri() + "?p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&p%206=percent%20encoded"))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Request parameters stubbing scenario.
     *
     * Since it's a POST request with the application/x-www-form-urlencoded content type, both query string and
     * request body values are considered http parameters sources for such a http request.
     */
    @Test
    public void havingParameterPOST() throws Exception {
        final String body = "p1=p1v1&p2=p2v1&p2=p2v2&p3=&p4&p%206=percent%20encoded";

        onRequest()
                //p1 has exactly one value, p1v1
                .havingParameter("p1")
                .havingParameterEqualTo("p1", "p1v1")
                .havingParameter("p1", hasSize(1))
                .havingParameter("p1", everyItem(not(emptyOrNullString())))
                .havingParameter("p1", contains("p1v1"))
                //p2 has three values, two from the body (p2v1 and p2v2) and one (p2v3) from the query string
                .havingParameter("p2")
                .havingParameterEqualTo("p2", "p2v1")
                .havingParameterEqualTo("p2", "p2v2")
                .havingParameterEqualTo("p2", "p2v3")
                .havingParameter("p2", hasSize(3))
                .havingParameter("p2", hasItems("p2v1", "p2v2", "p2v3"))
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
                //both is available in the percent-encoded form for stubbing
                .havingParameter("p%206")
                .havingParameterEqualTo("p%206", "percent%20encoded")
                .havingParameter("p%206", contains("percent%20encoded"))
                //p7 is an existing parameter coming from the query string
                .havingParameter("p7")
                .havingParameterEqualTo("p7", "p7v1")
                .havingParameter("p7", hasSize(1))
                //p1, p2, 'p 6' and p7 are present among other params
                .havingParameters("p1", "p2", "p%206", "p7")
                //there was a bug when stubbing using both a body and params received from the body
                //so let's do a stubbing using even a body here
                .havingBodyEqualTo(body)
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Post(jadlerUri() + "?p2=p2v3&p7=p7v1")
                        .bodyString(body, ContentType.create("application/x-www-form-urlencoded", UTF_8_CHARSET)))
                .handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Query string stubbing scenario.
     */
    @Test
    public void havingQueryString() throws Exception {
        final String queryString = "p1=v1&p2=v2&name=%C5%99eho%C5%99";

        onRequest()
                .havingQueryStringEqualTo(queryString)
                .havingQueryString(not(startsWith("?"))) //no '?' character at the beginning
                .respond()
                .withStatus(201);

        int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri() + '?' + queryString)).handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Empty query string stubbing scenario.
     */
    @Test
    public void havingQueryString_empty() throws Exception {
        onRequest()
                .havingQueryStringEqualTo("")
                .havingQueryString(is(emptyString()))
                .respond()
                .withStatus(201);

        //it seems HttpClient cannot send a request with an empty query string ('?' as the last character)
        //let's test this in a more hardcore fashion
        final URL url = new URL("http://localhost:" + port() + "/?");
        final HttpURLConnection c = (HttpURLConnection) url.openConnection();

        assertThat(c.getResponseCode(), is(201));

        c.disconnect();
    }


    /*
     * Missing query string stubbing scenario.
     */
    @Test
    public void havingQueryString_none() throws Exception {
        onRequest()
                .havingQueryString(nullValue())
                .havingQueryString(not(equalTo("")))
                .respond()
                .withStatus(201);

        int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Path stubbing scenario
     */
    @Test
    public void havingPath() throws Exception {
        final String path = "/a/b/c/d/%C5%99";

        onRequest()
                .havingPathEqualTo(path) //query string is excluded
                .havingPath(notNullValue())
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri() + path + "?param=value")).handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Root path stubbing scenario.
     */
    @Test
    public void havingPath_root() throws IOException {
        onRequest()
                .havingPath(equalTo("/"))
                .havingPath(not(emptyOrNullString()))
                .respond()
                .withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri() + "/")).handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * A dynamic stubbing scenario.
     *
     * The response is created using a {@code Responder} instance, it just resends the request body in the response
     * using the same encoding and 201 status.
     */
    @Test
    public void respondUsingResponder() throws IOException {
        onRequest()
                .havingMethodEqualTo("POST")
                .respondUsing(new Responder() {

                    @Override
                    public StubResponse nextResponse(final net.jadler.Request request) {

                        return StubResponse.builder()
                                .status(201)
                                .header("Content-Type", "text/plain; charset=" + request.getEncoding().name())
                                .body(request.getBodyAsBytes())
                                .build();
                    }
                });

        final HttpResponse response = Executor.newInstance().execute(
                        Request.Post(jadlerUri())
                                .bodyString(STRING_WITH_DIACRITICS, ContentType.create("text/plain", ISO_8859_2_CHARSET)))
                .returnResponse();

        assertThat(response.getStatusLine().getStatusCode(), is(201));
        assertThat(stringBodyOf(response), is(STRING_WITH_DIACRITICS));
    }


    /*
     * Explicitly defined response status scenario. Must override the default status set in the Jadler initialization.
     */
    @Test
    public void withStatus() throws Exception {
        onRequest().respond().withStatus(201);

        final int status = Executor.newInstance()
                .execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(201));
    }


    /*
     * Response body scenario with an explicitly set (ISO-8859-2) encoding and an explicitly set default
     * content-type header (text/html; charset=ISO-8859-2)
     */
    @Test
    public void withEncoding() throws IOException {
        onRequest().respond()
                .withEncoding(ISO_8859_2_CHARSET)
                .withContentType(ISO_8859_2_TYPE)
                .withBody(STRING_WITH_DIACRITICS);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        //the content type header set to the specified value
        assertThat(response.getFirstHeader("Content-Type").getValue(), is(ISO_8859_2_TYPE));
        // try to read the body using the charset defined in the Content-Type header
        assertThat(stringBodyOf(response), is(STRING_WITH_DIACRITICS));

        //the body bytes correspond to a ISO-8859-2 representation of the string
        assertThat(rawBodyOf(response), is(ISO_8859_2_BODY_REPRESENTATION));
    }


    /*
     * Response body scenario with an explicitly set (ISO-8859-2) encoding and an explicitly set default
     * content-type header (text/html; charset=UTF-8).
     *
     * This scenario simulates a faulty server claiming the body is encoded in UTF-8 (via the content-type header),
     * but in fact it is encoded using ISO-8895-2. Because crappy servers like this exist.
     */
    @Test
    public void withEncoding_contentTypeMismatch() throws IOException {
        onRequest().respond()
                .withEncoding(ISO_8859_2_CHARSET) //body encoding set to ISO_8859_2
                .withContentType(UTF_8_TYPE)  //but the content-type header says it's UTF-8 incorrectly
                .withBody(STRING_WITH_DIACRITICS);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        //the content type header set to the specified value
        assertThat(response.getFirstHeader("Content-Type").getValue(), is(UTF_8_TYPE));

        //however, the applied encoding is ISO-8859-2
        assertThat(rawBodyOf(response), is(ISO_8859_2_BODY_REPRESENTATION));
    }


    /*
     * Response body scenario using a reader instance with an explicitly set (ISO-8859-2) encoding and an explicitly
     * set default content-type header (text/html; charset=ISO-8859-2).
     */
    @Test
    public void withBodyReader() throws IOException {
        final Reader r = new StringReader(STRING_WITH_DIACRITICS);

        onRequest().respond()
                .withBody(r)
                .withEncoding(ISO_8859_2_CHARSET)
                .withContentType(ISO_8859_2_TYPE);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(rawBodyOf(response), is(ISO_8859_2_BODY_REPRESENTATION));
    }


    /*
     * Response body scenario using an input stream instance.
     *
     * Tests the body retrieved in the response is exactly the same as the input stream content.
     */
    @Test
    public void withBodyInputStream() throws IOException {
        final InputStream is = new ByteArrayInputStream(BINARY_BODY);

        onRequest().respond().withBody(is);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(rawBodyOf(response), is(BINARY_BODY));
    }


    /*
     * Response body scenario using an array of bytes.
     *
     * Tests the body retrieved in the response is exactly the same as the array content.
     */
    @Test
    public void withBodyArrayOfBytes() throws IOException {
        onRequest().respond().withBody(BINARY_BODY);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(rawBodyOf(response), is(BINARY_BODY));
    }


    /*
     * Response headers scenario.
     */
    @Test
    public void withHeader() throws IOException {
        onRequest().respond()
                .withHeader(HEADER_NAME1, HEADER_VALUE11)
                .withHeader(HEADER_NAME1, HEADER_VALUE12)
                .withHeader(HEADER_NAME2, HEADER_VALUE2);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        final Header[] headers1 = response.getHeaders(HEADER_NAME1);

        assertThat(headers1.length, is(2));
        assertThat(headers1[0].getValue(), is(HEADER_VALUE11));
        assertThat(headers1[1].getValue(), is(HEADER_VALUE12));

        final Header[] headers2 = response.getHeaders(HEADER_NAME2);
        assertThat(headers2.length, is(1));
        assertThat(headers2[0].getValue(), is(HEADER_VALUE2));
    }


    /*
     * Scenario with more possible rules. The latter one must be used.
     */
    @Test
    public void rulesOrdering() throws IOException {
        //these 3 rules are always matched, the latter one must be applied
        onRequest().that(is(anything())).respond().withStatus(201);
        onRequest().that(is(anything())).respond().withStatus(202);
        onRequest().that(is(anything())).respond().withStatus(203);

        final int status = Executor.newInstance().execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);

        assertThat(status, is(203));
    }


    /*
     * Scenario with more subsequent responses. First response with 200 status, all subsequent responses with 201.
     */
    @Test
    public void multipleStubResponses() throws IOException {
        onRequest().respond().withStatus(200).thenRespond().withStatus(201);

        final int status1 = Executor.newInstance().execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);
        assertThat(status1, is(200));

        final int status2 = Executor.newInstance().execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);
        assertThat(status2, is(201));

        //the 201 status must be repeated for every subsequent request
        final int status3 = Executor.newInstance().execute(Request.Get(jadlerUri())).handleResponse(STATUS_RETRIEVER);
        assertThat(status3, is(201));
    }


    /*
     * No suitable stub rule scenario. 404 with a predefined text/plain body must be returned in case no
     * suitable response has been defined.
     */
    @Test
    public void noRuleApplicable() throws IOException {
        onRequest().that(is(not(anything()))).respond();

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(response.getStatusLine().getStatusCode(), is(404));
        assertThat(response.getFirstHeader("Content-Type").getValue(), is("text/plain; charset=utf-8"));
        assertThat(stringBodyOf(response), is("No stub response found for the incoming request"));
    }


    /*
     * Defaults scenario. In case no status is explicitly defined during the stubbing, the default status is used.
     * The same for the default encoding and default content-type header values. And a default header is added to
     * every stub response. All of these values are set in the setUp phase during jadler initialization.
     */
    @Test
    public void defaults() throws Exception {
        //encoding and content-type values set in the setUp phase
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(response.getStatusLine().getStatusCode(), is(DEFAULT_STATUS));

        final Header[] responseHeaders = response.getHeaders(DEFAULT_HEADER1_NAME);
        assertThat(responseHeaders.length, is(2));
        assertThat(responseHeaders[0].getValue(), is(DEFAULT_HEADER1_VALUE1));
        assertThat(responseHeaders[1].getValue(), is(DEFAULT_HEADER1_VALUE2));

        //the content type header set to the default value
        assertThat(response.getFirstHeader("Content-Type").getValue(), is(DEFAULT_CONTENT_TYPE));

        // try to read the body using the charset defined in the Content-Type header
        assertThat(stringBodyOf(response), is(STRING_WITH_DIACRITICS));

        //the body bytes correspond to the ISO-8859-2 representation of the string
        assertThat(rawBodyOf(response), is(DEFAULT_ENCODING_BODY_REPRESENTATION));
    }


    /*
     * Default headers overriding scenario. The header DEFAULT_HEADER1_NAME is already defined with two default values.
     * This particular stubbing adds a third value. This test checks that all three values are sent in
     * the stub response.
     */
    @Test
    public void overriddenDefaultHeader() throws Exception {
        onRequest().respond().withHeader(DEFAULT_HEADER1_NAME, "value3");

        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), is(DEFAULT_STATUS));

        final Header[] responseHeaders = response.getHeaders(DEFAULT_HEADER1_NAME);
        assertThat(responseHeaders.length, is(3));
        assertThat(responseHeaders[0].getValue(), is(DEFAULT_HEADER1_VALUE1));
        assertThat(responseHeaders[1].getValue(), is(DEFAULT_HEADER1_VALUE2));
        assertThat(responseHeaders[2].getValue(), is("value3"));
    }


    /*
     * Response delay scenario. Tests the stub response is returned after at least one second
     * as set during the stubbing.
     */
    @Test
    public void delay() throws IOException {
        onRequest().respond().withDelay(1, TimeUnit.SECONDS);

        final Executor executor = Executor.newInstance();

        final long start = System.currentTimeMillis();
        executor.execute(Request.Get(jadlerUri())).discardContent();
        final long end = System.currentTimeMillis();
        final long dur = end - start;
        assertThat(dur / 1000, is(greaterThanOrEqualTo(1L)));
    }
}
