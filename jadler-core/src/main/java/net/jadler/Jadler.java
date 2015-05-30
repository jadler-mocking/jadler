/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.nio.charset.Charset;
import net.jadler.exception.JadlerException;
import net.jadler.mocking.VerificationException;
import net.jadler.mocking.Verifying;
import net.jadler.stubbing.RequestStubbing;
import net.jadler.stubbing.Responder;
import net.jadler.stubbing.server.StubHttpServerManager;
import net.jadler.stubbing.server.StubHttpServer;
import net.jadler.stubbing.ResponseStubbing;


/**
 * <p>This class is a gateway to the whole Jadler library. Jadler is a powerful yet simple to use 
 * http mocking library for writing integration tests in the http environment. It provides a convenient way
 * to create a stub http server which serves all http requests sent during a test execution
 * by returning stub responses according to defined rules.</p>
 * 
 * <h3>Jadler Usage Basics</h3>
 * 
 * <p>Let's have a simple component with one operation: </p>
 * 
 * <pre>
 * public interface AccountManager {
 *   Account getAccount(String id);
 * }
 * </pre>
 * 
 * <p>An implementation of the {@code getAccount} operation is supposed to send a GET http request to
 * {@code /accounts/{id}} where {@code {id}} stands for the method {@code id} parameter, deserialize the http response
 * to an {@code Account} instance and return it. If there is no such account (the GET request returned 404),
 * {@code null} must be returned. If some problem occurs (50x http response), a runtime exception must be thrown.</p>
 * 
 * <p>For the integration testing of this component it would be great to have a way to start a stub http server
 * which would return predefined stub responses. This is where Jadler comes to help.</p>
 * 
 * <p>Let's write such an integration test using <a href="http://junit.org" target="_blank">jUnit</a>:</p>
 * 
 * <pre> 
 * ...
 * import static net.jadler.Jadler.*;
 * ...
 * 
 * public class AccountManagerImplTest {
 * 
 *     private static final String ID = "123";
 *     private static final String ACCOUNT_JSON = "{\"account\":{\"id\": \"123\"}}";
 * 
 *
 *     {@literal @}Before
 *     public void setUp() {
 *         initJadler();
 *     }
 *  
 *     {@literal @}After
 *     public void tearDown() {
 *         closeJadler();
 *     }
 *   
 *     {@literal @}Test
 *     public void getAccount() {
 *         onRequest()
 *             .havingMethodEqualTo("GET")
 *             .havingPathEqualTo("/accounts/" + ID)
 *         .respond()
 *             .withBody(ACCOUNT_JSON)
 *             .withStatus(200);
 * 
 *         final AccountManager am = new AccountManagerImpl("http", "localhost", port());
 * 
 *         final Account account = ag.getAccount(ID);
 *       
 *         assertThat(account, is(notNullValue()));
 *         assertThat(account.getId(), is(ID));
 *     }
 * }
 * </pre>
 * 
 * <p>There are three main parts of this test. The <em>setUp</em> phase just initializes Jadler (which includes
 * starting a stub http server), while the <em>tearDown</em> phase just closes all resources. Nothing
 * interesting so far.</p>
 * 
 * <p>All the magic happens in the test method. New http stub is defined, in the <em>THEN</em> part
 * the http stub server is instructed to return a specific http response
 * (200 http status with a body defined in the {@code ACCOUNT_JSON} constant) if the incoming http request
 * fits the given conditions defined in the <em>WHEN</em> part (must be a GET request to {@code /accounts/123}).</p>
 * 
 * <p>In order to communicate with the http stub server instead of the real web service, the tested instance
 * must be configured to access {@code localhost} using the http protocol (https will be supported
 * in a latter version of Jadler) connecting to a port which can be retrieved using the {@link Jadler#port()} method.</p>
 * 
 * <p>The rest of the test method is business as usual. The {@code getAccount(String)} is executed and some
 * assertions are evaluated.</p>
 * 
 * <p>Now lets write two more test methods to test the 404 and 500 scenarios:</p>
 * 
 * <pre>
 * {@literal @}Test
 * public void getAccountNotFound() {
 *     onRequest()
 *         .havingMethodEqualTo("GET")
 *         .havingPathEqualTo("/accounts/" + ID)
 *     .respond()
 *         .withStatus(404);
 * 
 *     final AccountManager am = new AccountManagerImpl("http", "localhost", port());
 * 
 *     Account account = am.getAccount(ID);
 * 
 *     assertThat(account, is(nullValue()));
 * }
 * 
 * 
 * {@literal @}Test(expected=RuntimeException.class)
 * public void getAccountError() {
 *     onRequest()
 *         .havingMethodEqualTo("GET")
 *         .havingPathEqualTo("/accounts/" + ID)
 *     .respond()
 *         .withStatus(500);
 * 
 *     final AccountManager am = new AccountManagerImpl("http", "localhost", port());
 * 
 *     ag.getAccount(ID);
 * }
 * </pre>
 * 
 * <p>The first test method checks the {@code getAccount(String)} method returns {@code null} if 404 is returned
 * from the server. The second one tests a runtime exception is thrown upon 500 http response.</p>
 * 
 * 
 * <h3>Multiple responses definition</h3> 
 * <p>Sometimes you need to define more subsequent messages in your testing scenario. Let's test here
 * your code can recover from an unexpected 500 response and retry the POST receiving 201 this time:</p>
 * 
 * <pre>
 * onRequest()
 *     .havingPathEqualTo("/accounts")
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
 *     .havingPathEqualTo("/accounts")
 * .respond()
 *     .withStatus(201);
 * 
 * onRequest()
 *     .havingMethodEqualTo("POST")
 * .respond()
 *     .withStatus(202);
 * </pre>
 * 
 * <p>If a POST http request was sent to {@code /accounts} both rules would be applicable. However, the latter stub
 * gets priority over the former one. In this example, an http response with {@code 202} status code would be
 * returned.</p>
 * 
 * <h3>Advanced http stubbing</h3>
 * <h4 id="stubbing">The <em>WHEN</em> part</h4>
 * <p>So far two {@code having*} methods have been introduced,
 * {@link RequestStubbing#havingMethodEqualTo(java.lang.String)} to check the http method equality and
 * {@link RequestStubbing#havingPathEqualTo(java.lang.String)} to check the path equality. But there's more!</p>
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
 *     .havingPathEqualTo("/accounts")
 *     .havingBodyEqualTo("{\"account\":{}}")
 *     .havingHeaderEqualTo("Content-Type", "application/json")
 *     .havingParameterEqualTo("force", "1")
 * .respond()
 *     .withStatus(201);
 * </pre>
 * 
 * <p>The 201 stub response will be returned if the incoming request was a {@code POST} request to {@code /accounts}
 * with the specified body, {@code application/json} content type header and a {@code force} http parameter set to
 * {@code 1}.</p>
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
 * set such header. For setting the {@code Content-Type} header you can use specially tailored
 * {@link net.jadler.stubbing.ResponseStubbing#withContentType(java.lang.String)} method.</p>
 * 
 * <p>And finally sometimes you would like to simulate a network latency. To do so just call the
 * {@link net.jadler.stubbing.ResponseStubbing#withDelay(long, java.util.concurrent.TimeUnit)} method.
 * The stub response will be returned after the specified amount of time or later.</p>
 * 
 * <p>Let's define the <em>THEN</em> part precisely:</p>
 * 
 * <pre>
 * onRequest()
 *     .havingMethodEqualTo("POST")
 *     .havingPathEqualTo("/accounts")
 *     .havingBodyEqualTo("{\"account\":{}}")
 *     .havingHeaderEqualTo("Content-Type", "application/json")
 *     .havingParameterEqualTo("force", "1")
 * .respond()
 *     .withDelay(2, SECONDS)
 *     .withStatus(201)
 *     .withBody("{\"account\":{\"id\" : 1}}")
 *     .withEncoding(Charset.forName("UTF-8"))
 *     .withContentType("application/json; charset=UTF-8")
 *     .withHeader("Location", "/accounts/1");
 * </pre>
 * 
 * <p>If the incoming http request fulfills the <em>WHEN</em> part, a stub response will be returned after at least 
 * 2 seconds. The response will have 201 status code, defined json body encoded using UTF-8 and both
 * {@code Content-Type} and {@code Location} headers set to proper values.</p>
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
 * is not PUT and the path value starts with <em>/accounts</em> then return an empty response
 * with the 200 http status:</p>
 * 
 * <pre>
 * onRequest()
 *     .havingBody(not(isEmptyOrNullString()))
 *     .havingPath(startsWith("/accounts"))
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
 *   <li>{@link RequestStubbing#havingPath(org.hamcrest.Matcher)}</li>
 * </ul>
 * 
 * <p>For adding predicates about request parameters and headers use
 * {@link RequestStubbing#havingHeader(java.lang.String, org.hamcrest.Matcher)} and
 * {@link RequestStubbing#havingParameter(java.lang.String, org.hamcrest.Matcher)} methods. Since a request header or 
 * parameter can have more than one value, these methods accept a list of strings predicates.</p>
 * 
 * <p>All introduced methods allow user to add a predicate about a part of an http request (body, method, ...).
 * If you need to add a predicate about the whole request object (of type {@link Request}),
 * you can use the {@link RequestStubbing#that(org.hamcrest.Matcher)} method: </p>
 * 
 * <pre>
 *   //meetsCriteria() is some factory method returning a Matcher&lt;Request&gt; instance
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
 * {@literal @}Before
 * public void setUp() {
 *     initJadler().that()
 *         .respondsWithDefaultStatus(200);
 * }
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
 * <p>If not redefined in the particular stubbing, every stub response will have 200 http status, {@code Content-Type}
 * header set to {@code text/plain}, response body encoded using {@code ISO-8859-1} and a header named
 * {@code X-DEFAULT-HEADER} set to {@code default_value}.</p>
 * 
 * <p>If no default status code is defined 200 will be used. And if no default response body encoding is defined,
 * {@code UTF-8} will be used by default.</p>
 * 
 * 
 * <h3>Generating a stub response dynamically</h3>
 * 
 * <p>In some integration testing scenarios it's necessary to generate a stub http response dynamically. This
 * is a case where the {@code with*} methods aren't sufficient. However Jadler comes to help here with with
 * the {@link net.jadler.stubbing.Responder} interface which allows to define the stub response dynamically
 * according to the received request: </p>
 * 
 * <pre>
 * onRequest()
 *     .havingMethodEqualTo("POST")
 *     .havingPathEqualTo("/accounts")
 *     .respondUsing(new Responder() {
 *
 *         private final AtomicInteger cnt = new AtomicInteger(1);
 * 
 *         {@literal @}Override
 *         public StubResponse nextResponse(final Request request) {
 *              final int current = cnt.getAndIncrement();
 *              final String headerValue = request.getHeaders().getValue("x-custom-request-header");
 *              return StubResponse.builder()
 *                      .status(current % 2 == 0 ? 200 : 500)
 *                      .header("x-custom-response-header", headerValue)
 *                      .build();
 *         }
 *     });
 * </pre>
 * 
 * <p>The intention to define the stub response dynamically is expressed by using
 * {@link net.jadler.stubbing.RequestStubbing#respondUsing(net.jadler.stubbing.Responder)}. This method takes
 * a {@link net.jadler.stubbing.Responder} implementation as a parameter, Jadler subsequently uses the
 * {@link net.jadler.stubbing.Responder#nextResponse(net.jadler.Request)} method to generate stub responses for
 * all requests fitting the given <em>WHEN</em> part.</p>
 * 
 * <p>In the previous example the http status of a stub response is {@code 200} for even requests and {@code 500}
 * for odd requests. And the value of the {@code x-custom-request-header} request header is used as
 * a response header.</p>
 * 
 * <p>As you can see in the example this {@link net.jadler.stubbing.Responder} implementation is thread-safe 
 * (by using {@link java.util.concurrent.atomic.AtomicInteger} here). This is important for tests of parallel nature
 * (more than one client can send requests fitting the <em>WHEN</em> part in parallel). Of course if requests are sent
 * in a serial way (which is the most common case) there is no need for the thread-safety of the implementation.</p>
 * 
 * <p>Please note this dynamic way of defining stub responses should be used as rarely as possible as
 * it very often signalizes a problem either with test granularity or somewhere in the tested code. However there
 * could be very specific testing scenarios where this functionality might be handy.</p>
 * 
 * 
 * <h3>Request Receipt Verification</h3>
 * 
 * <p>While the Jadler library is invaluable in supporting your test scenarios by providing a stub http server,
 * it has even more to offer.</p>
 * 
 * <p>Very often it's necessary not only to provide a stub http response but also to verify that a specific
 * http request was received during a test execution. Let's add a removal operation to the already introduced
 * {@code AccountManager} interface: </p>
 * 
 * <pre>
 * public interface AccountManager {
 *   Account getAccount(String id);
 * 
 *   void deleteAccount(String id);
 * }
 * </pre>
 * 
 * <p>The {@code deleteAccount} operation is supposed to delete an account by sending a {@code DELETE} http request
 * to {@code /accounts/{id}} where {@code {id}} stands for the operation {@code id} parameter. If the response status is
 * 204 the removal is considered successful and the execution is finished successfully. Let's write an integration
 * test for this scenario:</p>
 * 
 * <pre>
 * ...
 * import static net.jadler.Jadler.*;
 * ...
 * 
 * public class AccountManagerImplTest {
 * 
 *     private static final String ID = "123";
 *
 *     {@literal @}Before
 *     public void setUp() {
 *         initJadler();
 *     }
 *  
 *     {@literal @}After
 *         public void tearDown() {
 *         closeJadler();
 *     }
 *   
 *     {@literal @}Test
 *     public void deleteAccount() {
 *         onRequest()
 *             .havingMethodEqualTo("DELETE")
 *             .havingPathEqualTo("/accounts/" + ID)
 *         .respond()
 *             .withStatus(204);
 * 
 *         final AccountManager am = new AccountManagerImpl("http", "localhost", port());
 * 
 *         final Account account = am.deleteAccount(ID);
 *       
 *         verifyThatRequest()
 *             .havingMethodEqualTo("DELETE")
 *             .havingPathEqualTo("/accounts/" + ID)
           .receivedOnce();
 *     }
 * }
 * </pre>
 * 
 * <p>The first part of this test is business as usual. An http stub is created and the tested method
 * {@code deleteAccount} is invoked. However in this test case we would like to test whether the {@code DELETE} http
 * request was really sent during the execution of the method.</p>
 * 
 * <p>This is where Jadler comes again to help. Calling {@link #verifyThatRequest()} signalizes an intention to
 * verify a number of requests received so far meeting the given criteria. The criteria is defined using exactly
 * the same {@code having*} methods which has been already described in the <a href="#stubbing">stubbing section</a>
 * (the methods are defined in the {@link RequestMatching} interface).</p>
 * 
 * <p>The request definition must be followed by calling one of the {@code received*} methods. The already
 * introduced {@link Verifying#receivedOnce()} method verifies there has been received exactly one request meeting
 * the given criteria so far. If the verification fails a {@link VerificationException} instance is thrown.</p>
 * 
 * <p>There are three more verification methods. {@link Verifying#receivedNever()} verifies there has not been
 * received any request meeting the given criteria so far. {@link Verifying#receivedTimes(int)} allows to define
 * the exact number of requests meeting the given criteria. And finally
 * {@link Verifying#receivedTimes(org.hamcrest.Matcher)} allows to apply a Hamcrest matcher on the number of
 * requests meeting the given criteria. The following example shows how to verify there have been at most
 * 3 DELETE requests sent so far:</p>
 * 
 * <pre>
 * verifyThatRequest()
 *     .havingMethodEqualTo("DELETE")
 * .receivedTimes(lessThan(4));
 * </pre>
 * 
 * <p>This verification feature is implemented by recording all incoming http requests (including their bodies). In
 * some very specific corner cases this implementation can cause troubles. For example imagine a long running
 * performance test using Jadler for stubbing some remote http service. Since such a test can issue thousands
 * or even millions of requests the memory consumption probably would affect the test results (either
 * by a performance slowdown or even crashes). In this specific scenarios you should consider disabling
 * the incoming requests recording:</p>
 * 
 * <pre>
 * public void setUp() {
 *     initJadler().that()
 *             .skipsRequestsRecording();
 * }
 * </pre>
 * 
 * <p>Please note you should ignore this option almost every time you use Jadler unless you are really
 * convinced about it. Because premature optimization is the root of all evil, you know.</p>
 * 
 * <h3>Simplified Jadler Lifecycle Management</h3>
 * 
 * <p>In all previous examples the jUnit {@literal @}Before and {@literal @}After sections were used to manage
 * the Jadler lifecycle. If jUnit 4.11 (or newer) is on the classpath a simple Jadler 
 * <a href="https://github.com/junit-team/junit/wiki/Rules">rule</a> {@link net.jadler.junit.rule.JadlerRule}
 * can be used instead:</p>
 * 
 * <pre>
 * public class AccountManagerImplTest {
 *
 *     {@literal @}Rule
 *     public JadlerRule jadlerRule = new JadlerRule();
 * 
 *     ...
 * }
 * </pre>
 * 
 * <p>This piece of code starts Jadler on a random port at the beginning of each test and closes it at the end.
 * A specific port can be defined as well: {@code new JadlerRule(12345);}.</p>
 * 
 * <p>To use this rule please note the {@code jadler-junit} artifact must be on the classpath.</p>
 */
public class Jadler {
    
      //since jUnit might execute tests in a thread other than the thread executing the setup and teardown methods
      //when specific conditions are met (a timeout value is specified for the given
      //test, see http://junit.org/apidocs/org/junit/Test.html for details), the thread local container
      //is inheritable so the content is copied automatically to the child thread
    private static ThreadLocal<JadlerMocker> jadlerMockerContainer = new InheritableThreadLocal<JadlerMocker>();
    private static String JETTY_SERVER_CLASS = "net.jadler.stubbing.server.jetty.JettyStubHttpServer";

    private Jadler() {
        //gtfo
    }
    
    
    /**
     * Initializes Jadler and starts a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on any free port. The port number can be retrieved using {@link #port()}.
     * <br /><br />
     * This should be preferably called in the {@code setUp} method of the test suite
     * @return if additional tweaking needed on the initialized Jadler, call {@link AdditionalConfiguration#that()}
     * to add more configuration
     */
    public static AdditionalConfiguration initJadler() {
        return initInternal(new JadlerMocker(getJettyServer()));
    }
    

    /**
     * Initializes Jadler and starts a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on the given port.
     * <br /><br />
     * This should be preferably called in the {@code setUp} method of the test suite
     * @param port port the stub server will be listening on
     * @return if additional tweaking needed on the initialized Jadler, call {@link AdditionalConfiguration#that()}
     * to add more configuration
     */
    public static AdditionalConfiguration initJadlerListeningOn(final int port) {
        return initInternal(new JadlerMocker(getJettyServer(port)));
    }
    

    /**
     * Initializes Jadler and starts the given {@link StubHttpServer}.
     * <br /><br />
     * This should be preferably called in the {@code setUp} method of the test suite
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
     * This should be preferably called in the {@code tearDown} method of a test suite.
     */
    public static void closeJadler() {
        final StubHttpServerManager serverManager = jadlerMockerContainer.get();
        if (serverManager != null && serverManager.isStarted()) {
            serverManager.close();
        }
        
        jadlerMockerContainer.set(null);
    }


    /**
     * Use this method to retrieve the port the underlying http stub server is listening on
     * @return the port the underlying http stub server is listening on
     * @throws IllegalStateException if Jadler has not been initialized yet
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
    
    
    /**
     * Starts new verification (checking that an http request with given properties was or was not received)
     * @return verifying object for ongoing verifying 
     */
    public static Verifying verifyThatRequest() {
        checkInitialized();
        return jadlerMockerContainer.get().verifyThatRequest();
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
    
    
    private static StubHttpServer getJettyServer() {
        final Class<?> clazz = getJettyStubHttpServerClass();
        try {
            return (StubHttpServer) clazz.newInstance();
        }
        catch (final Exception e) {
            throw new JadlerException("Cannot instantiate default Jetty stub server", e);
        }
    }
    
    private static StubHttpServer getJettyServer(final int port) {
        final Class<?> clazz = getJettyStubHttpServerClass();
        try {
            return (StubHttpServer) clazz.getConstructor(int.class).newInstance(port);
        }
        catch (final Exception e) {
            throw new JadlerException("Cannot instantiate default Jetty stub server with the given port", e);
        }
    }
    
    
    private static Class<?> getJettyStubHttpServerClass() {
        try {
            return Class.forName(JETTY_SERVER_CLASS);
        }
        catch (final ClassNotFoundException e) {
            throw new JadlerException("Class " + JETTY_SERVER_CLASS + " cannot be found. "
                    + "Either add jadler-jetty to your classpath or use the initJadlerUsing method to specify the "
                    + "stub server explicitly.", e);
        }
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
         * <p>Disables incoming http requests recording.</p>
         * 
         * <p>Jadler mocking (verification) capabilities are implemented by storing all incoming requests (including their
         * bodies). This could cause troubles in some very specific testing scenarios, for further explanation jump
         * straight to {@link JadlerMocker#setRecordRequests(boolean)}.</p>
         * 
         * <p>Please note this method should be used very rarely and definitely should not be treated as a default.</p>
         * 
         * @see JadlerMocker#setRecordRequests(boolean)
         * @return this ongoing configuration 
         */
        public OngoingConfiguration skipsRequestsRecording() {
            jadlerMockerContainer.get().setRecordRequests(false);
            return this;
        }
        
        
        /**
         * Defines a default content type of every stub http response. This value will be used for all stub responses
         * with no specific content type defined. (see {@link ResponseStubbing#withContentType(java.lang.String)})
         * @param defaultContentType default {@code Content-Type} header of every http stub response
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
