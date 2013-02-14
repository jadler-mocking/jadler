/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server;

import net.jadler.stubbing.StubResponseProvider;


/**
 * Interface for a stub http server component.
 * 
 * This component represents an http server which waits for http requests and returns stub http responses
 * according to a {@link StubResponseProvider} instance.
 * 
 * Jadler provides a default implementation of this interface
 * {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer} based on an embedded jetty server.
 */
public interface StubHttpServer {

    /**
     * Registers a response provider. This component provides a response prescription (in form
     * of a {@link net.jadler.stubbing.StubResponse} instance) for a given http request.
     * @param responseProvider response provider to use to retrieve response prescriptions.
     */
    void registerResponseProvider(StubResponseProvider responseProvider);
    
    /**
     * Starts the underlying http server. From now, the server must be able to respond
     * according to prescriptions returned from the registered {@link StubResponseProvider} instance.
     * @throws Exception when ugh... something went wrong
     */
    void start() throws Exception;

    /**
     * Stops the underlying http server.
     * @throws Exception when an error occurred while stopping the server
     */
    void stop() throws Exception;

    /**
     * @return HTTP server port
     */
    int getPort();
}
