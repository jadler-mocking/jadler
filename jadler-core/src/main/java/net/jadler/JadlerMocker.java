/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.Stubber;
import net.jadler.stubbing.server.StubHttpServerManager;
import java.nio.charset.Charset;
import net.jadler.stubbing.RequestStubbing;
import net.jadler.stubbing.StubbingFactory;
import net.jadler.stubbing.Stubbing;
import net.jadler.stubbing.StubResponse;
import net.jadler.stubbing.HttpStub;
import net.jadler.exception.JadlerException;
import net.jadler.stubbing.server.StubHttpServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.jadler.mocking.Mocker;
import net.jadler.mocking.VerificationException;
import net.jadler.mocking.Verifying;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.allOf;


/**
 * <p>This class represents the very hearth of the Jadler library. It acts as a great {@link Stubber} providing
 * a way to create new http stubs, {@link StubHttpServerManager} allowing the client to manage the state
 * of the underlying stub http server and {@link RequestManager} providing stub response definitions
 * according to a given http request.</p>
 * 
 * <p>An underlying stub http server instance is registered to an instance of this class during the instantiation.</p>
 * 
 * <p>Normally you shouldn't create instances of this on your own, use the {@link Jadler} facade instead.
 * However, if more http stub servers are needed in one execution thread (for example two http stub servers
 * listening on different ports) have no fear, go ahead and create two or more instances directly.</p>
 * 
 * <p>This class is stateful and thread-safe.</p>
 */
public class JadlerMocker implements StubHttpServerManager, Stubber, RequestManager, Mocker {

    private final StubHttpServer server;
    private final StubbingFactory stubbingFactory;
    private final List<Stubbing> stubbings;
    private Deque<HttpStub> httpStubs;
    private final List<Request> receivedRequests;
    private final Set<AsyncVerificator> asyncVerificators;

    private MultiMap defaultHeaders;
    private int defaultStatus;
    private Charset defaultEncoding;
    private boolean recordRequests = true;
    
    private boolean started = false;
    private boolean configurable = true;
    
    private static final StubResponse NO_RULE_FOUND_RESPONSE;
    static {
        NO_RULE_FOUND_RESPONSE = StubResponse.builder()
                .status(404)
                .body("No stub response found for the incoming request", Charset.forName("UTF-8"))
                .header("Content-Type", "text/plain; charset=utf-8")
                .build();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(JadlerMocker.class);

    /**
     * The meaning of this class, is to wrpap the BlockingQueue inside an object,
     * where the equals method only equals on exactly the same objects, so that each
     * queue can be added to a set, and later removed again.
     */
    private static final class AsyncVerificator {
        private final BlockingQueue<Request> requestqueue;

        public AsyncVerificator() {
            this.requestqueue = new LinkedBlockingQueue<Request>();
        }
    }

    
    /**
     * Creates new JadlerMocker instance bound to the given http stub server.
     * 
     * @param server stub http server instance this mocker should use
     */
    public JadlerMocker(final StubHttpServer server) {
        this(server, new StubbingFactory());
    }
    
    
    /**
     * Package private constructor, for testing purposes only! Allows to define a {@link StubbingFactory} instance
     * as well.
     * 
     * @param server stub http server instance this mocker should use
     * @param stubbingFactory a factory to create stubbing instances
     */
    JadlerMocker(final StubHttpServer server, final StubbingFactory stubbingFactory) {
        Validate.notNull(server, "server cannot be null");
        this.server = server;
        
        this.stubbings = new LinkedList<Stubbing>();
        this.defaultHeaders = new MultiValueMap();
        this.defaultStatus = 200;
        this.defaultEncoding =  Charset.forName("UTF-8");
        
        Validate.notNull(stubbingFactory, "stubbingFactory cannot be null");
        this.stubbingFactory = stubbingFactory;
        
        this.httpStubs = new LinkedList<HttpStub>();
        
        this.receivedRequests = new ArrayList<Request>();
        this.asyncVerificators = new HashSet<AsyncVerificator>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (this.started) {
            throw new IllegalStateException("The stub server has been started already.");
        }
        
        logger.debug("starting the underlying stub server...");
        
        this.server.registerRequestManager(this);

        try {
            server.start();
        } catch (final Exception ex) {
            throw new JadlerException("Stub http server start failure", ex);
        }
        this.started = true;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (!this.started) {
            throw new IllegalStateException("The stub server hasn't been started yet.");
        }
        
        logger.debug("stopping the underlying stub server...");
        
        try {
            server.stop();
        } catch (final Exception ex) {
            throw new JadlerException("Stub http server shutdown failure", ex);
        }
        this.started = false;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return this.started;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getStubHttpServerPort() {
        if (!this.started) {
            throw new IllegalStateException("The stub http server hasn't been started yet.");
        }
        return server.getPort();
    }
    
    
    /**
     * Adds a default header to be added to every stub http response.
     * @param name header name (cannot be empty)
     * @param value header value (cannot be <tt>null</tt>)
     */
    public void addDefaultHeader(final String name, final String value) {
        Validate.notEmpty(name, "header name cannot be empty");
        Validate.notNull(value, "header value cannot be null, use an empty string instead");
        this.checkConfigurable();
        this.defaultHeaders.put(name, value);
    }
    

    /**
     * Defines a default status to be returned in every stub http response (if not redefined in the
     * particular stub rule)
     * @param defaultStatus status to be returned in every stub http response. Must be at least 0.
     */
    public void setDefaultStatus(final int defaultStatus) {
        Validate.isTrue(defaultStatus >= 0, "defaultStatus mustn't be negative");
        this.checkConfigurable();
        this.defaultStatus = defaultStatus;
    }
    
    
    /**
     * Defines default charset of every stub http response (if not redefined in the particular stub)
     * @param defaultEncoding default encoding of every stub http response
     */
    public void setDefaultEncoding(final Charset defaultEncoding) {
        Validate.notNull(defaultEncoding, "defaultEncoding cannot be null");
        this.checkConfigurable();
        this.defaultEncoding = defaultEncoding;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public RequestStubbing onRequest() {
        logger.debug("adding new stubbing...");
        this.checkConfigurable();
        
        final Stubbing stubbing = this.stubbingFactory.createStubbing(defaultEncoding, defaultStatus, defaultHeaders);
        stubbings.add(stubbing);
        return stubbing;
    }
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public StubResponse provideStubResponseFor(final Request request) {
        synchronized(this) {
            if (this.configurable) {
                this.configurable = false;
                this.httpStubs = this.createHttpStubs();
            }

            if (this.recordRequests) {
                this.receivedRequests.add(request);
                for (AsyncVerificator asyncVerificator : this.asyncVerificators) {
                    asyncVerificator.requestqueue.offer(request);
                }
            }
        }
        
        for (final Iterator<HttpStub> it = this.httpStubs.descendingIterator(); it.hasNext(); ) {
            final HttpStub rule = it.next();
            if (rule.matches(request)) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Following rule will be applied:\n");
                sb.append(rule);
                logger.debug(sb.toString());
                
                return rule.nextResponse(request);
            }
        }
        
        final StringBuilder sb = new StringBuilder();
        sb.append("No suitable rule found. Reason:\n");
        for (final HttpStub rule: this.httpStubs) {
            sb.append("The rule '");
            sb.append(rule);
            sb.append("' cannot be applied. Mismatch:\n");
            sb.append(rule.describeMismatch(request));
            sb.append("\n");
        }
        logger.info(sb.toString());
        
        return NO_RULE_FOUND_RESPONSE;
    }
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public Verifying verifyThatRequest() {
        checkRequestRecording();
        return new Verifying(this);
    }  
    
    
    /**
     * {@inheritDoc} 
     */
    @Deprecated
    @Override
    public int numberOfRequestsMatching(final Collection<Matcher<? super Request>> predicates) {
        Validate.notNull(predicates, "predicates cannot be null");
        checkRequestRecording();

        final Matcher<Request> all = allOf(predicates);
        
        int cnt = 0;
        
        synchronized(this) {
            for (final Request req: this.receivedRequests) {
                if (all.matches(req)) {
                    cnt++;
                }
            }
        }
        
        return cnt;
    }

    
    @Override
    public void evaluateVerification(final Collection<Matcher<? super Request>> requestPredicates,
            final Matcher<Integer> nrRequestsPredicate) {

        validateEvaluateVerificationArgsAndState(requestPredicates, nrRequestsPredicate);

        synchronized(this) {
            final int cnt = this.numberOfRequestsMatching(requestPredicates);

            if (!nrRequestsPredicate.matches(cnt)) {
                this.logReceivedRequests(requestPredicates);
                throw new VerificationException(this.mismatchDescription(cnt, requestPredicates, nrRequestsPredicate));
            }
        }
    }

    @Override
    public void evaluateVerificationAsync(
            final Collection<Matcher<? super Request>> requestPredicates,
            final Matcher<Integer> nrRequestsPredicate,
            final Duration timeOut) {

        validateEvaluateVerificationArgsAndState(requestPredicates, nrRequestsPredicate);
        Validate.notNull(timeOut, "timeUnit cannot be null");

        final long startTime = System.nanoTime();
        final AsyncVerificator myQueue = new AsyncVerificator();
        int cnt = 0;

        cnt = this.numberOfRequestsMatching(requestPredicates);
        if (nrRequestsPredicate.matches(cnt)) {
            return;
        }
        synchronized(this) { this.asyncVerificators.add(myQueue); }

        Duration timeLeft = calculateTimeLeft(startTime, timeOut);

        while (!timeLeft.isZero()) {
            try {
                myQueue.requestqueue.poll(timeLeft.getValue(), timeLeft.getTimeUnit());
                cnt = this.numberOfRequestsMatching(requestPredicates);
                if (nrRequestsPredicate.matches(cnt)) {
                    return;
                } else {
                    timeLeft = calculateTimeLeft(startTime, timeOut);
                }
            } catch (InterruptedException e) {
                timeLeft = calculateTimeLeft(startTime, timeOut);
            }
        }

        // If it reach here, then the time is up, and we should fail.
        failAsyncVerification(myQueue, requestPredicates, cnt, nrRequestsPredicate);
    }

    private void validateEvaluateVerificationArgsAndState(
            final Collection<Matcher<? super Request>> requestPredicates,
            final Matcher<Integer> nrRequestsPredicate) {
        Validate.notNull(requestPredicates, "requestPredicates cannot be null");
        Validate.notNull(nrRequestsPredicate, "nrRequestsPredicate cannot be null");
        this.checkRequestRecording();
    }

    private void failAsyncVerification(
            final AsyncVerificator a,
            final Collection<Matcher<? super Request>> requestPredicates,
            final int cnt,
            final Matcher<Integer> nrRequestsPredicate) {
        removeFromAsyncVerificators(a);
        this.logReceivedRequests(requestPredicates);
        throw new VerificationException(this.mismatchDescription(cnt, requestPredicates, nrRequestsPredicate));
    }

    private synchronized void removeFromAsyncVerificators(final AsyncVerificator a) {
        this.asyncVerificators.remove(a);
    }

    private Duration calculateTimeLeft(
            final long startTimeInNanos,
            final Duration dur) {
        final long now = System.nanoTime();
        if (startTimeInNanos + dur.toNanos() < now)
            return Duration.zero();
        else {
            final long left = (startTimeInNanos + dur.toNanos()) - now;
            return Duration.ofNanos(left);
        }
    }

    /**
     * <p>Resets this mocker instance so it can be reused. This method clears all previously created stubs as well as
     * stored received requests (for mocking purpose,
     * see {@link RequestManager#numberOfRequestsMatching(java.util.Collection)}). Once this method has been called
     * new stubs can be created again using {@link #onRequest()}.</p>
     * 
     * <p>Please note that calling this method in a test body <strong>always</strong> signalizes a poorly written test
     * with a problem with the granularity. In this case consider writing more fine grained tests instead of using this
     * method.</p>
     * 
     * <p>While the standard Jadler lifecycle consists of creating new instance of this class and starting the
     * underlying stub server (using {@link #start()}) in the <em>before</em> section of a test and stopping
     * the server (using {@link #close()}) in the <em>after</em> section, in some specific scenarios it could be useful
     * to reuse one instance of this class in all tests instead.</p>
     * 
     * <p>When more than just one instance of this class is used in a test suite (for mocking more http servers) it
     * could take some time to start all underlying stub servers before and stop these after every test method. This is
     * a typical use case this method might come to help.</p>
     * 
     * <p>Here's an example code using jUnit which demonstrates usage of this method in a test lifecycle:</p>
     * 
     * <pre>
     * public class JadlerResetIntegrationTest {
     *     private static final JadlerMocker mocker = new JadlerMocker(new JettyStubHttpServer());
     * 
     *     {@literal @}BeforeClass
     *     public static void beforeTests() {
     *         mocker.start();
     *     }
     * 
     *     {@literal @}AfterClass
     *     public static void afterTests() {
     *         mocker.close();
     *     }
     *
     *     {@literal @}After
     *     public void reset() {
     *         mocker.reset();
     *     }
     * 
     *     {@literal @}Test
     *     public void test1() {
     *         mocker.onRequest().respond().withStatus(201);
     * 
     *         //do an http request here, 201 should be returned from the stub server 
     *
     *         verifyThatRequest().receivedOnce();
     *     }
     * 
     *     {@literal @}Test
     *     public void test2() {
     *         mocker.onRequest().respond().withStatus(400);
     * 
     *         //do an http request here, 400 should be returned from the stub server 
     *
     *         verifyThatRequest().receivedOnce(); 
     *     }
     * }
     * </pre>
     */
    public void reset() {
        synchronized(this) {
            this.stubbings.clear();
            this.httpStubs.clear();
            this.receivedRequests.clear();
            this.configurable = true;
        }
    }
    
    
    /**
     * <p>By default Jadler records all incoming requests (including their bodies) so it can provide mocking
     * (verification) features defined in {@link net.jadler.mocking.Mocker}.</p>
     * 
     * <p>In some very specific corner cases this implementation of mocking can cause troubles. For example imagine
     * a long running performance test using Jadler for stubbing some remote http service. Since such a test can issue
     * thousands or even millions of requests the memory consumption probably would affect the test results (either
     * by a performance slowdown or even crashes). In this specific scenarios you should consider disabling
     * the incoming requests recording using this method.</p>
     * 
     * <p>When disabled calling {@link net.jadler.mocking.Mocker#verifyThatRequest()} will result in
     * {@link java.lang.IllegalStateException}</p>
     * 
     * <p>Please note you should ignore this option almost every time you use Jadler unless you are really
     * convinced about it. Because premature optimization is the root of all evil, you know.</p>
     * 
     * @param recordRequests {@code true} for enabling http requests recording, {@code false} for disabling it
     */
    public void setRecordRequests(final boolean recordRequests) {
        this.checkConfigurable();
        this.recordRequests = recordRequests;
    }
    
    
    private Deque<HttpStub> createHttpStubs() {
        final Deque<HttpStub> stubs = new LinkedList<HttpStub>();
        for (final Stubbing stub : stubbings) {
            stubs.add(stub.createRule());
        }
        return stubs;
    }
    
    
    private void logReceivedRequests(final Collection<Matcher<? super Request>> requestPredicates) {
        final StringBuilder sb = new StringBuilder("Verification failed, here is a list of requests received so far:");
        
        int pos = 1;
        synchronized (this) {
            this.appendNoneIfEmpty(this.receivedRequests, sb);
            for (final Request req: this.receivedRequests) {
                sb.append("\n");
                final Collection<Matcher<? super Request>> matching = new ArrayList<Matcher<? super Request>>();
                final Collection<Matcher<? super Request>> clashing = new ArrayList<Matcher<? super Request>>();

                for (final Matcher<? super Request> pred: requestPredicates) {
                    if (pred.matches(req)) {
                        matching.add(pred);
                    }
                    else {
                        clashing.add(pred);
                    }
                }

                this.appendReason(sb, req, pos, matching, clashing);

                pos++;
            }
        }
        logger.info(sb.toString());
    }

    
    private void appendReason(final StringBuilder sb, final Request req, final int position,
            final Collection<Matcher<? super Request>> matching,
            final Collection<Matcher<? super Request>> clashing) {
            
        sb.append("Request #");
        sb.append(position);
        sb.append(": ");
        sb.append(req);
        sb.append("\n");
        sb.append("  matching predicates:");
        this.appendNoneIfEmpty(matching, sb);
        sb.append('\n');

        for (final Matcher<? super Request> pred: matching) {
            sb.append("    ");
            final Description desc = new StringDescription(sb);
            pred.describeTo(desc);
            sb.append('\n');
        }

        sb.append("  clashing predicates:");
        this.appendNoneIfEmpty(clashing, sb);

        for (final Matcher<? super Request> pred: clashing) {
            sb.append("\n    ");

            final Description desc = new StringDescription(sb);
            pred.describeMismatch(req, desc);
        }
    }
    
    
    private void appendNoneIfEmpty(final Collection<?> coll, final StringBuilder sb) {
        if (coll.isEmpty()) {
            sb.append(" <none>");
        }
    }
    

    private String mismatchDescription(final int cnt, final Collection<Matcher<? super Request>> predicates,
            final Matcher<Integer> nrRequestsMatcher) {
        final Description desc = new StringDescription();

        desc.appendText("The number of http requests");
        if (!predicates.isEmpty()) {
            desc.appendText(" having");
        }
        desc.appendText(" ");

        for (final Iterator<Matcher<? super Request>> it = predicates.iterator(); it.hasNext();) {
            desc.appendDescriptionOf(it.next());

            if (it.hasNext()) {
                desc.appendText(" AND");
            }

            desc.appendText(" ");
        }

        desc.appendText("was expected to be ");
        desc.appendDescriptionOf(nrRequestsMatcher);
        desc.appendText(", but ");
        nrRequestsMatcher.describeMismatch(cnt, desc);

        return desc.toString();
    }
    
    
    private synchronized void checkConfigurable() {
        if (!this.configurable) {
            throw new IllegalStateException("Once first http request has been served, "
                    + "you can't do any stubbing anymore.");
        }
    }

    
    private synchronized void checkRequestRecording() {
        if (!this.recordRequests) {
            throw new IllegalStateException("Request recording is switched off, cannot do any request verification");
        }
    }
}
