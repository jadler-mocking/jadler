/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.nio.charset.Charset;
import net.jadler.stubbing.RequestStubbing;
import net.jadler.httpmocker.HttpMocker;
import net.jadler.httpmocker.HttpMockerImpl;
import net.jadler.server.StubHttpServer;
import net.jadler.server.jetty.JettyStubHttpServer;
import net.jadler.stubbing.ResponseStubbing;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;


/**
 * This class is a gateway to the whole jadler library.
 * 
 * TODO huge doc/examples
 */
public final class Jadler {
    
    private static ThreadLocal<OngoingConfiguration> builderContainer = new ThreadLocal<>();
    private static ThreadLocal<HttpMocker> mockerContainer = new ThreadLocal<>();

    private Jadler() {
        //gtfo
    }
    
    
    /**
     * Starts configuring jadler. This method should be preferably called in
     * a <i>before<i> phase of every test.
     * @return ongoing configuration instance to continue configuring
     */
    public static OngoingConfiguration initJadlerThat() {
          //bind the current thread 
        builderContainer.set(new OngoingConfiguration());
        
          //clear the previously created http mocker instance if necessary
        final HttpMocker mocker = mockerContainer.get();
        if (mocker != null) {
            if (mocker.isStarted()) {
                mocker.stop();
            }
            mockerContainer.set(null);
        }
        
        return builderContainer.get();
    }
    
    
    /**
     * Starts the underlying http mock server. This should be preferably called
     * in the <i>before</i> phase of a test.
     */
    public static void startStubServer() {
        createMockerIfNotExists();
        
        final HttpMocker mocker =  mockerContainer.get();
        if (!mocker.isStarted()) {
            mocker.start();
        }
    }
    
    
    /**
     * Stops the underlying http mock server. This should be preferably called
     * in the <i>after</i> phase of a test.
     */
    public static void stopStubServer() {
        final HttpMocker mocker =  mockerContainer.get();
        if (mocker != null && mocker.isStarted()) {
            mocker.stop();
        }
    }
    
    
    /**
     * Starts new http stubbing (defining new <i>WHEN</i>-<i>THEN</i> rule).
     * @return stubbing object for ongoing stubbing 
     */
    public static RequestStubbing onRequest() {
        createMockerIfNotExists();
        return mockerContainer.get().onRequest();
    }
    
    
    private static void createMockerIfNotExists() {
        if (mockerContainer.get() == null) {
        
            if (builderContainer.get() == null) {
                throw new IllegalStateException("The HttpMocker instance has not been configured yet, "
                        + "call initHttpMocking first.");
            }
        
            mockerContainer.set(builderContainer.get().build());
        }
    }
    
    
    /**
     * Builder for constructing HttpMocker instances in a fluid way
     */
    public static class OngoingConfiguration {
        private StubHttpServer mockHttpServer;
        private Integer defaultStatus;
        private MultiMap defaultHeaders = new MultiValueMap();
        private Charset defaultEncoding;
        
        
        /**
         * Configures the new HttpMocker instance to use the default mock server implementation (jetty based).
         * This is the preferred way to use Jadler. The mock http server will be listening on the given port.
         * Use {@link #usesCustomServer(net.jadler.server.MockHttpServer)} if you want to use
         * a custom mock server implementation.
         * 
         * @param port port the http mock server will be listening on
         * @return this ongoing configuration
         */
        public OngoingConfiguration usesStandardServerListeningOn(final int port) {
            this.mockHttpServer = new JettyStubHttpServer(port);
            return this;
        }
        
        
        /**
         * Configures the new HttpMocker instance to use a custom mock server implementation. Godspeed you, brave developer!
         * 
         * Consider using {@link #usesStandardServerListeningOn(int)} if you want to use the default mock server
         * implementation instead.
         * @param mockHttpServer mock server implementation
         * @return this ongoing configuration
         */
        public OngoingConfiguration usesCustomServer(final StubHttpServer mockHttpServer) {
            Validate.notNull(mockHttpServer, "mockHttpServer cannot be null");
            
            this.mockHttpServer = mockHttpServer;
            return this;
        }
        
        
        /**
         * Sets the default http response status. This value will be used for all stub responses with no
         * specific http status defined. (see {@link ResponseStubbing#withStatus(int)})
         * @param defaultStatus default http response status
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultStatus(final int defaultStatus) {
            this.defaultStatus = defaultStatus;
            return this;
        }
        
        
        /**
         * Defines a response header that will be sent in every http mock response.
         * Can be called repeatedly to define more headers.
         * @param name name of the header
         * @param value header value
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultHeader(final String name, final String value) {
            Validate.notEmpty(name, "header name cannot be empty");
            this.defaultHeaders.put(name, value);
            return this;
        }
        
        
        /**
         * Defines a default encoding of every stub http response. This value will be used for all stub responses
         * with no specific encoding defined. (see {@link ResponseStubbing#withEncoding(java.nio.charset.Charset)})
         * @param defaultEncoding 
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultEncoding(final Charset defaultEncoding) {
            this.defaultEncoding = defaultEncoding;
            return this;
        }
        
        
        /**
         * Defines a default content type of every stub http response. This value will be used for all stub responses
         * with no specific content type defined. (see {@link ResponseStubbing#withContentType(java.lang.String)})
         * 
         * Calling this method is equivalent with calling
         * respondsWithDefaultHeader("Content-Type", defaultContentType)
         * @param defaultEncoding 
         * @return this ongoing configuration
         */
        public OngoingConfiguration respondsWithDefaultContentType(final String defaultContentType) {
            return this.respondsWithDefaultHeader("Content-Type", defaultContentType);
        }
        
        
        /**
         * @return a newly constructed HttpMocker instance.
         */
        private HttpMocker build() {
            final HttpMockerImpl res = new HttpMockerImpl(this.mockHttpServer);
            this.mockHttpServer.registerResponseProvider(res);

            if (this.defaultEncoding != null) {
                res.setDefaultEncoding(defaultEncoding);
            }
            if (this.defaultStatus != null) {
                res.setDefaultStatus(this.defaultStatus);
            }
            res.setDefaultHeaders(this.defaultHeaders);

            return res;
        }
    }
}
