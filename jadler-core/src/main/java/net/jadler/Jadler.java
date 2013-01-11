/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.nio.charset.Charset;
import net.jadler.stubbing.RequestStubbing;
import net.jadler.stubbing.server.StubHttpServerManager;
import net.jadler.stubbing.server.StubHttpServer;
import net.jadler.stubbing.server.jetty.JettyStubHttpServer;
import net.jadler.stubbing.ResponseStubbing;


/**
 * <p>This class is a gateway to the whole Jadler library. Jadler is a powerful yet simple to use 
 * http mocking library for writing integration tests in the http environment. It provides a convenient way
 * to create a stub http server which serves all http requests sent during a test execution
 * by returning a stub response according to the defined rules.</p>
 * 
 * <h3>Jadler Usage Basics</h3>
 * 
 * <p>Let's have a simple component with one operation: </p>
 * 
 * <pre>
 * public interface AccountGetter {
 *   Account getAccount(String id);
 * }
 * </pre>
 * 
 * <p>An implementation of this component is supposed to send a GET http request to <tt>/accounts/{id}</tt>
 * where <tt>{id}</tt> stands for the method <tt>id</tt> parameter, deserialize the http response to
 * an <tt>Account</tt> instance and return it. If there is no such account (the GET request returned 404), <tt>null</tt>
 * must be returned. If some problem occurs (50x http response), a runtime exception must be thrown.</p>
 * 
 * <p>For the integration testing of this component it would be great to have an opportunity
 * to start a stub http server which would return predefined stub responses. This is where Jadler
 * comes to help.</p>
 * 
 * <p>Let's write such an integration test using <a href="http://junit.org" target="_blank">jUnit</a>:</p>
 * 
 * <pre> 
 * ...
 * import static net.jadler.Jadler.*;
 * ...
 * 
 * public class AccountGetterImplTest {
 * 
 *   private static final String ID = "123";
 *   private static final String ACCOUNT_JSON = "{\"account\":{\"id\": \"123\"}}";
 * 
 *
 *   {@literal @}Before
 *   public void setUp() {
 *     initJadler();
 *   }
 *  
 *   {@literal @}After
 *   public void tearDown() {
 *     closeJadler();
 *   }
 *   
 *   {@literal @}Test
 *   public void getAccount() {
 *     onRequest()
 *         .havingMethodEqualTo("GET")
 *         .havingURIEqualTo("/accounts/" + ID)
 *     .respond()
 *         .withBody(ACCOUNT_JSON)
 *         .withStatus(200);
 * 
 *     final AccountGetter ag = new AccountGetterImpl("http", "localhost", port());
 * 
 *     final Account account = ag.getAccount(ID);
 *       
 *     assertThat(account, is(notNullValue()));
 *     assertThat(account.getId(), is(ID));
 *   }
 * }
 * </pre>
 * 
 * <p>There are three main parts of this test. The <em>setUp</em> phase just initializes Jadler (this includes
 * starting a stub http server as well), while the <em>tearDown</em> phase just closes all resources. Nothing
 * interesting so far.</p>
 * 
 * <p>All the magic happens in the test method. New http stub is defined, in the <em>THEN</em> part
 * the http stub server is instructed to return a specific http response
 * (200 http status with a body defined in the <tt>ACCOUNT_JSON</tt> constant) if the incoming http request
 * fits the given conditions defined in the <em>WHEN</em> part (must be a GET request to <tt>/projects/123</tt>).</p>
 * 
 * <p>In order to communicate with the http stub server instead of the real web service, the tested instance
 * must be configured to access <tt>localhost</tt> using the http protocol (https will be supported
 * in Jadler 1.1) connecting to a port which can be retrieved using the {@link Jadler#port()} method.</p>
 * 
 * <p>The rest of the test method is business as usual. The <tt>getProject(String)</tt> is executed and some
 * assertions are evaluated.</p>
 * 
 * <p>Now lets write two more test methods to test the 404 and 500 scenarios:</p>
 * 
 * <pre>
 * {@literal @}Test
 * public void getAccountNonexisting() {
 *     onRequest()
 *         .havingMethodEqualTo("GET")
 *         .havingURIEqualTo("/projects/" + ID)
 *     .respond()
 *         .withStatus(404);
 * 
 *     final AccountGetter ag = new AccountGetterImpl("http", "localhost", port());
 * 
 *     Account account = ag.getAccount(ID);
 * 
 *     assertThat(account, is(nullValue()));
 * }
 * 
 * 
 * {@literal @}Test(expected=RuntimeException.class)
 * public void getAccountError() {
 *     onRequest()
 *         .havingMethodEqualTo("GET")
 *         .havingURIEqualTo("/projects/" + ID)
 *     .respond()
 *         .withStatus(500);
 * 
 *     final AccountGetter ag = new AccountGetterImpl("http", "localhost", port());
 * 
 *     ag.getAccount(ID);
 * }
 * </pre>
 * 
 * <p>The first test method checks the <tt>getProject(String)</tt> method returns <tt>null</tt> if 404 is returned
 * from the server. The second one tests a runtime exception is throws upon 500 http response.</p>
 * 
 * 
 * <h3>Multiple responses definition</h3> 
 * <p>Sometimes you need to define more subsequent messages in your testing scenario. Let's test here
 * your code can recover from an unexpected 500 response and retry the POST receiving 201 this time:</p>
 * 
 * <pre>
 * onRequest()
 *     .havingURIEqualTo("/projects")
 *     .havingMethodEqualTo("POST")
 * .respond()
 *     .withStatus(500)
 * .thenRespond()
 *     .withStatus(201);
 * </pre>
 * 
 * <p>The stub server will return a stub http response with 500 response status for the first request
 * which suits the stub rule. A stub response with 201 response status will be returned for the second request
 * (and all subsequent requests as well).</p>
 * 
 * <h3>More suitable stub rules</h3>
 * 
 * <p>It's not uncommon that more stub rules can be applied (the incoming request fits more than one <em>WHEN</em>
 * part). Let's have the following example: </p>
 * 
 * <pre>
 * onRequest()
 *     .havingURIEqualTo("/projects")
 * .respond()
 *     .withStatus(201);
 * 
 * onRequest()
 *     .havingMethodEqualTo("POST")
 * .respond()
 *     .withStatus(202);
 * </pre>
 * 
 * <p>If a POST http request was sent to <tt>/projects</tt>, both rules would be applicable. However, the latter stub
 * gets priority over the former one. In this example, an http response with <tt>202</tt> status code would be
 * returned.</p>
 * 
 * <h3>Advanced http stubbing</h3>
 * <h4>The <em>WHEN</em> part</h4>
 * <p>So far two <tt>having*</tt> methods have been introduced,
 * {@link RequestStubbing#havingMethodEqualTo(java.lang.String)} to check the http method equality and
 * {@link RequestStubbing#havingURIEqualTo(java.lang.String)} to check the URI equality. But there's more!</p>
 * 
 * <p>You can use {@link RequestStubbing#havingBodyEqualTo(java.lang.String)} and
 * {@link RequestStubbing#havingRawBodyEqualTo(byte[])}} to check the request body equality
 * (either as a string or as an array of bytes).</p>
 * 
 * <p>Feel free to to use {@link RequestStubbing#havingQueryStringEqualTo(java.lang.String)}
 * to test the query string value.</p>
 * 
 * <p>And finally don't hesitate to use {@link RequestStubbing#havingParameterEqualTo(java.lang.String, java.lang.String)}
 * or {@link RequestStubbing#havingHeaderEqualTo(java.lang.String, java.lang.String)}
 * for a check whether there is an http parameter / header in the incoming request with a given value.
 * If an existence check is sufficient you can use {@link RequestStubbing#havingParameter(java.lang.String)},
 * {@link RequestStubbing#havingParameters(java.lang.String[])} or
 * {@link RequestStubbing#havingHeader(java.lang.String)}, {@link RequestStubbing#havingHeaders(java.lang.String[])}
 * instead.</p>
 * 
 * <p>So let's write some advanced http stub here: </p>
 * 
 * <pre>
 * onRequest()
 *     .havingMethodEqualTo("POST")
 *     .havingURIEqualTo("/projects")
 *     .havingBodyEqualTo("{\"project\":{}}")
 *     .havingHeaderEqualTo("Content-Type", "application/json")
 *     .havingParameterEqualTo("force", "1")
 * .respond()
 *     .withStatus(201);
 * </pre>
 * 
 * <p>The 201 stub response will be returned if the incoming request was a <tt>POST</tt> request to <tt>/projects</tt>
 * with the specified body, <tt>application/json</tt> content type header and a <tt>force</tt> http parameter set to
 * <tt>1</tt>.</p>
 * 
 * <h4>The <em>THEN</em> part</h4>
 * <p>There are much more options than just setting the http response status using the
 * {@link net.jadler.stubbing.ResponseStubbing#withStatus(int)} in the <em>THEN</em> part of an http stub.</p>
 * 
 * <p>You will probably have to define the stub response body as a string very often. That's what the 
 * {@link net.jadler.stubbing.ResponseStubbing#withBody(java.lang.String)} and
 * {@link net.jadler.stubbing.ResponseStubbing#withBody(java.io.Reader)} methods are for. These
 * are very often used in conjunction of
 * {@link net.jadler.stubbing.ResponseStubbing#withEncoding(java.nio.charset.Charset)} to define the
 * encoding of the response body</p>
 * 
 * <p>If you'd like to define the stub response body binary, feel free to use either
 * {@link net.jadler.stubbing.ResponseStubbing#withBody(byte[])} or
 * {@link net.jadler.stubbing.ResponseStubbing#withBody(java.io.InputStream)}.</p>
 * 
 * <p>Setting a stub response header is another common http stubbing use case. Just call
 * {@link net.jadler.stubbing.ResponseStubbing#withHeader(java.lang.String, java.lang.String)} to 
 * set such header. For setting the <tt>Content-Type</tt> header you can use specially tailored
 * {@link net.jadler.stubbing.ResponseStubbing#withContentType(java.lang.String)} method.</p>
 * 
 * <p>And finally sometimes you would like to simulate a network latency. To do so just call the
 * {@link net.jadler.stubbing.ResponseStubbing#withTimeout(long, java.util.concurrent.TimeUnit)} method.
 * The stub response will be returned at least after the specified amount of time.</p>
 * 
 * <p>Let's define the <em>THEN</em> part precisely:</p>
 * 
 * <pre>
 * onRequest()
 *     .havingMethodEqualTo("POST")
 *     .havingURIEqualTo("/projects")
 *     .havingBodyEqualTo("{\"project\":{}}")
 *     .havingHeaderEqualTo("Content-Type", "application/json")
 *     .havingParameterEqualTo("force", "1")
 * .respond()
 *     .withTimeout(2, SECONDS)
 *     .withStatus(201)
 *     .withBody("{\"project\":{\"id\" : 1}}")
 *     .withEncoding(Charset.forName("UTF-8"))
 *     .withContentType("application/json; charset=UTF-8")
 *     .withHeader("Location", "/projects/1");
 * </pre>
 * 
 * <p>If the incoming http request fulfills the <em>WHEN</em> part, a stub response will be returned after at least 
 * 2 seconds. The response will have 201 status code, defined json body encoded using UTF-8 and both
 * <tt>Content-Type</tt> and <tt>Location</tt> headers set to proper values.</p>
 * 
 * 
 * <h3> Even more advanced http stubbing</h3>
 * 
 * <h4>Fine-tuning the <em>WHEN</em> part using predicates</h4>
 * <p>So far we have been using the equality check to define the <em>WHEN</em> part. However it's quite useful
 * to be able to use other predicates (<em>non empty string</em>, <em>contains string</em>, ...) then just
 * the request value equality.</p>
 * 
 * <p>Jadler uses <a href="http://hamcrest.org" target="_blank">Hamcrest</a> as a predicates library. Not only
 * it provides many already implemented predicates (called matchers) but also a simple way to implement
 * your own ones if necessary. More on Hamcrest usage to be found in this
 * <a href="http://code.google.com/p/hamcrest/wiki/Tutorial" target="_blank">tutorial</a>.</p>
 * 
 * <p>So let's write the following stub: if an incoming request has a non-empty body and the request method
 * is not PUT and the URI value starts with <em>/projects</em> then return an empty response
 * with the 200 http status:</p>
 * 
 * <pre>
 * onRequest()
 *     .havingBody(not(isEmptyOrNullString()))
 *     .havingURI(startsWith("/projects"))
 *     .havingMethod(not(equalToIgnoringCase("PUT")))
 * .respond()
 *     .withStatus(200);
 * </pre>
 * 
 * <p>You can use following <em>having*</em> methods for defining the <em>WHEN</em> part
 * using a Hamcrest string matcher: </p>
 * 
 * <ul>
 *   <li>{@link RequestStubbing#havingBody(org.hamcrest.Matcher)}</li>
 *   <li>{@link RequestStubbing#havingMethod(org.hamcrest.Matcher)}</li>
 *   <li>{@link RequestStubbing#havingQueryString(org.hamcrest.Matcher)}</li>
 *   <li>{@link RequestStubbing#havingURI(org.hamcrest.Matcher)}</li>
 * </ul>
 * 
 * <p>For adding predicates about request parameters and headers use
 * {@link RequestStubbing#havingHeader(java.lang.String, org.hamcrest.Matcher)} and
 * {@link RequestStubbing#havingParameter(java.lang.String, org.hamcrest.Matcher)} methods. Since a request header or 
 * parameter can have more than one value, these methods accept a list of strings predicate.</p>
 * 
 * <p>All introduced methods allows user to add a predicate about a part of an http request (body, method, ...).
 * If you need to add a predicate about the whole request object (of type {@link javax.servlet.http.HttpServletRequest}),
 * you can use the {@link RequestStubbing#that(org.hamcrest.Matcher)} method: </p>
 * 
 * <pre>
 *   //meetsCriteria() is some factory method returning a Matcher&lt;HttpServletRequest&gt; instance
 * 
 * onRequest()
 *     .that(meetsCriteria())
 * .respond()
 *     .withStatus(204);
 * </pre>
 * 
 * 
 * <h4>Fine-tuning the <em>THEN</em> part using defaults</h4>
 * 
 * <p>It's pretty common many <em>THEN</em> parts share similar settings. Let's have two or more stubs returning
 * an http response with 200 http status. Instead of calling {@link ResponseStubbing#withStatus(int)} during
 * every stubbing Jadler can be instructed to use 200 as a default http status: </p>
 * 
 * <pre>
 *   {@literal @}Before
 *   public void setUp() {
 *     initJadler().that()
 *         .respondsWithDefaultStatus(200);
 *   }
 * </pre>
 * 
 * <p>The {@link AdditionalConfiguration#that()} method here simply indicates Jadler will not only be initialized
 * but also configured. This particular test setup configures Jadler to return http stub responses with 200 http
 * status by default. This default can always be overwritten by calling the {@link ResponseStubbing#withStatus(int)} 
 * method in the particular stubbing.</p>
 * 
 * <p>The following example demonstrates all defaults options: </p>
 * 
 * <pre>
 *   {@literal @}Before
 *   public void setUp() {
 *       initJadler().that()
 *           .respondsWithDefaultStatus(200)
 *           .respondsWithDefaultContentType("text/plain")
 *           .respondsWithDefaultEncoding(Charset.forName("ISO-8859-1"))
 *           .respondsWithDefaultHeader("X-DEFAULT-HEADER", "default_value");
 *   }
 * </pre>
 * 
 * <p>If not redefined in the particular stubbing, every stub response will have 200 http status, <tt>Content-Type</tt>
 * header set to <tt>text/plain</tt>, response body encoded using <tt>ISO-8859-1</tt> and a header named
 * <tt>X-DEFAULT-HEADER</tt> set to <tt>default_value</tt>.</p>
 * 
 * <p>If no default status code is defined 200 will be used. And if no default response body encoding is defined,
 * <tt>UTF-8</tt> will be used by default.</p>
 * 
 */
public final class Jadler {
    
    private static ThreadLocal<JadlerMocker> jadlerMockerContainer = new ThreadLocal<>();

    private Jadler() {
        //gtfo
    }
    
    
    /**
     * Initializes Jadler and starts a default stub server {@link JettyStubHttpServer}
     * serving the http protocol listening on any free port. The port number can be
     * retrieved using {@link #port()}.
     * <br /><br />
     * This should be preferably called in the <tt>setUp</tt> method of the test suite
     * @return if additional tweaking needed on the initialized Jadler, call {@link AdditionalConfiguration#that()}
     * to add more configuration
     */
    public static AdditionalConfiguration initJadler() {
        return initInternal(new JadlerMocker());
    }
    

    /**
     * Initializes Jadler and starts a default stub server {@link JettyStubHttpServer}
     * serving the http protocol listening on the given port.
     * <br /><br />
     * This should be preferably called in the <tt>setUp</tt> method of the test suite
     * @param port port the stub server will be listening on
     * @return if additional tweaking needed on the initialized Jadler, call {@link AdditionalConfiguration#that()}
     * to add more configuration
     */
    public static AdditionalConfiguration initJadlerListeningOn(final int port) {
        return initInternal(new JadlerMocker(new JettyStubHttpServer(port)));
    }
    

    /**
     * Initializes Jadler and starts the given {@link StubHttpServer}.
     * <br /><br />
     * This should be preferably called in the <tt>setUp</tt> method of the test suite
     * @param server stub http server instance
     * @return if additional tweaking needed on the initialized Jadler, call {@link AdditionalConfiguration#that()}
     * to add more configuration
     */
    public static AdditionalConfiguration initJadlerUsing(final StubHttpServer server) {
        return initInternal(new JadlerMocker(server));
    }
    
    
    /**
     * Stops the underlying {@link StubHttpServer} and closes Jadler.
     * <br /><br />
     * This should be preferably called in the <tt>tearDown</tt> method of a test suite.
     */
    public static void closeJadler() {
        final StubHttpServerManager serverManager = jadlerMockerContainer.get();
        if (serverManager != null && serverManager.isStarted()) {
            serverManager.stop();
        }
        
        jadlerMockerContainer.set(null);
    }


    /**
     * Use this method to retrieve the port the underlying http stub server is listening on
     * @return the port the underlying http stub server is listening on
     * @throws IllegalStateException if Jadler was not initialized yet
     */
    public static int port() {
        checkInitialized();
        return jadlerMockerContainer.get().getStubHttpServerPort();
    }
    
    
    /**
     * Starts new http stubbing (defining new <i>WHEN</i>-<i>THEN</i> rule).
     * @return stubbing object for ongoing stubbing 
     */
    public static RequestStubbing onRequest() {
        checkInitialized();
        return jadlerMockerContainer.get().onRequest();
    }
    
    
    private static void checkInitialized() {
        if (jadlerMockerContainer.get() == null) {
            throw new IllegalStateException("Jadler has not been initialized yet.");
        }
    }
    
    
    private static AdditionalConfiguration initInternal(final JadlerMocker jadlerMocker) {
        if (jadlerMockerContainer.get() != null) {
            throw new IllegalStateException("Jadler seems to have been initialized already.");
        }
        
        jadlerMockerContainer.set(jadlerMocker);
        jadlerMocker.start();
        return AdditionalConfiguration.INSTANCE;        
    }
    
    
    /**
     * This class serves as a DSL support for additional Jadler configuration.
     */
    public static class OngoingConfiguration {
        private static final OngoingConfiguration INSTANCE = new OngoingConfiguration();
        
        
        private OngoingConfiguration() {
            //private constructor, instances of this class should never be created directly
        }
        
        /**
         * Sets the default http response status. This value will be used for all stub responses with no
         * specific http status defined. (see {@link ResponseStubbing#withStatus(int)})
         * @param defaultStatus default http response status
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultStatus(final int defaultStatus) {
            jadlerMockerContainer.get().setDefaultStatus(defaultStatus);
            return this;
        }
        
        
        /**
         * Defines a response header that will be sent in every http stub response.
         * Can be called repeatedly to define more headers.
         * @param name name of the header
         * @param value header value
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultHeader(final String name, final String value) {
            jadlerMockerContainer.get().addDefaultHeader(name, value);
            return this;
        }
        
        
        /**
         * Defines a default encoding of every stub http response. This value will be used for all stub responses
         * with no specific encoding defined. (see {@link ResponseStubbing#withEncoding(java.nio.charset.Charset)})
         * @param defaultEncoding default stub response encoding
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultEncoding(final Charset defaultEncoding) {
            jadlerMockerContainer.get().setDefaultEncoding(defaultEncoding);
            return this;
        }
        
        
        /**
         * Defines a default content type of every stub http response. This value will be used for all stub responses
         * with no specific content type defined. (see {@link ResponseStubbing#withContentType(java.lang.String)})
         * @param defaultContentType default <tt>Content-Type</tt> header of every http stub response
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultContentType(final String defaultContentType) {
            return this.respondsWithDefaultHeader("Content-Type", defaultContentType);
        }
    }
    
    
    /**
     * This class serves as a DSL support for initialization of an additional Jadler configuration.
     */
    public static class AdditionalConfiguration {
        private static final AdditionalConfiguration INSTANCE = new AdditionalConfiguration();
        
        private AdditionalConfiguration() {
            //private constructor, instances of this class should never be created directly
        }
        
        public OngoingConfiguration that() {
            return OngoingConfiguration.INSTANCE;
        }
    }
}
