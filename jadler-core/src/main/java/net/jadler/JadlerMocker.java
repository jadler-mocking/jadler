/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.Request;
import net.jadler.stubbing.Stubber;
import net.jadler.stubbing.server.StubHttpServerManager;
import net.jadler.stubbing.StubResponseProvider;
import java.nio.charset.Charset;
import net.jadler.stubbing.RequestStubbing;
import net.jadler.stubbing.StubbingFactory;
import net.jadler.stubbing.Stubbing;
import net.jadler.stubbing.StubResponse;
import net.jadler.stubbing.StubRule;
import net.jadler.exception.JadlerException;
import net.jadler.stubbing.server.StubHttpServer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>This class represents the very hearth of the Jadler library. It acts as a great {@link Stubber} providing
 * a way to create new http stubs, {@link StubHttpServerManager} allowing the client to manage the state
 * of the underlying stub http server and {@link StubResponseProvider} providing stub response definitions
 * according to a given http request.</p>
 * 
 * <p>An underlying stub http server instance is registered to an instance of this class during the instantiation.</p>
 * 
 * <p>Normally you shouldn't create instances of this on your own, use the {@link Jadler} facade instead.
 * However, if more http stub servers are needed in one execution thread (for example two http stub servers
 * listening on different ports) have no fear, go ahead and create more two or more instances directly.</p>
 * 
 * <p>This class is stateful and thread-safe.</p>
 */
public class JadlerMocker implements StubHttpServerManager, Stubber, StubResponseProvider {

    private final StubHttpServer server;
    private final StubbingFactory stubbingFactory;
    private final List<Stubbing> stubbings;
    private Deque<StubRule> httpStubRules;

    private MultiMap defaultHeaders;
    private int defaultStatus;
    private Charset defaultEncoding;
    
    private boolean started = false;
    private boolean configurable = true;
    
    private static final StubResponse NO_RULE_FOUND_RESPONSE;
    static {
        NO_RULE_FOUND_RESPONSE = new StubResponse();
        NO_RULE_FOUND_RESPONSE.setStatus(404);
        NO_RULE_FOUND_RESPONSE.setBody("No stub response found for the incoming request");
        NO_RULE_FOUND_RESPONSE.setEncoding(Charset.forName("UTF-8"));
        NO_RULE_FOUND_RESPONSE.setHeaderCaseInsensitive("Content-Type", "text/plain; charset=utf-8");
    }
    
    private static final Logger logger = LoggerFactory.getLogger(JadlerMocker.class);
    
    
    /**
     * Creates new JadlerMocker instance bound to the given http stub server.
     * Instances of this class should never be created directly, see {@link Jadler} for explanation and tutorial.
     * 
     * @param server stub http server instance this mocker should use
     */
    public JadlerMocker(final StubHttpServer server) {
        this(server, new StubbingFactory());
    }
    
    
    /**
     * Package private constructor, for testing purposes only! Allows to define a StubbingFactory instance
     * as well.
     * @param server stub http server instance this mocker should use
     * @param stubbingFactory a factory to create stubbing instances
     */
    JadlerMocker(final StubHttpServer server, final StubbingFactory stubbingFactory) {
        Validate.notNull(server, "server cannot be null");
        this.server = server;
        
        this.stubbings = new ArrayList<Stubbing>();
        this.defaultHeaders = new MultiValueMap();
        this.defaultStatus = 200; //OK
        this.defaultEncoding =  Charset.forName("UTF-8");
        
        Validate.notNull(stubbingFactory, "stubbingFactory cannot be null");
        this.stubbingFactory = stubbingFactory;
        
        this.httpStubRules = new LinkedList<StubRule>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (this.started){
            throw new IllegalStateException("The stub server has been started already.");
        }
        
        logger.debug("starting the underlying stub server...");
        
        this.server.registerResponseProvider(this);

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
    public void stop() {
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
     * Defines default headers to be added to every stub http response
     * @param defaultHeaders default headers to be added to every stub http response 
     */
    @SuppressWarnings("unchecked")
    public void setDefaultHeaders(final MultiMap defaultHeaders) {
        Validate.notNull(defaultHeaders, "defaultHeaders cannot be null, use an empty map instead");
        this.checkConfigurable();
        this.defaultHeaders = new MultiValueMap();
        this.defaultHeaders.putAll(defaultHeaders);
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
                this.httpStubRules = this.createRules();
            }
        }
        
        for (final Iterator<StubRule> it = this.httpStubRules.descendingIterator(); it.hasNext(); ) {
            final StubRule rule = it.next();
            if (rule.matchedBy(request)) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Following rule will be applied:\n");
                sb.append(rule);
                logger.debug(sb.toString());
                
                return rule.nextResponse();
            }
        }
        
        final StringBuilder sb = new StringBuilder();
        sb.append("No suitable rule found. Reason:\n");
        for (final StubRule rule: this.httpStubRules) {
            sb.append("The rule '");
            sb.append(rule);
            sb.append("' cannot be applied. Mismatch:\n");
            sb.append(rule.describeMismatch(request));
            sb.append("\n");
        }
        logger.info(sb.toString());
        
        return NO_RULE_FOUND_RESPONSE;
    }
    
    
    private Deque<StubRule> createRules() {
        final Deque<StubRule> rules = new LinkedList<StubRule>();
        for (final Stubbing stub : stubbings) {
            rules.add(stub.createRule());
        }
        return rules;
    }
    
    
    private synchronized void checkConfigurable() {
        if (!this.configurable) {
            throw new IllegalStateException("Once first http request has been served, "
                    + "you can't do any stubbing anymore.");
        }
    }
}