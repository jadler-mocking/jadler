/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server;

import net.jadler.RequestManager;


/**
 * Interface for a stub http server component.
 * <p>
 * This component represents an http server which waits for http requests and returns stub http responses
 * according to a {@link StubHttpServerManager} instance.
 * <p>
 * Jadler provides a default implementation of this interface
 * {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer} based on an embedded jetty server.
 */
public interface StubHttpServer {

    /**
     * Registers a response provider. This component provides a response prescription (in form
     * of a {@link net.jadler.stubbing.StubResponse} instance) for a given http request.
     *
     * @param requestManager response provider to use to retrieve response prescriptions.
     */
    void registerRequestManager(RequestManager requestManager);


    /**
     * Starts the underlying http server. From now, the server must be able to respond
     * according to prescriptions returned from the registered {@link StubHttpServerManager} instance.
     *
     * @throws Exception when ugh... something went wrong
     */
    void start() throws Exception;


    /**
     * Stops the underlying http server.
     *
     * @throws Exception when an error occurred while stopping the server
     */
    void stop() throws Exception;


    /**
     * @return HTTP server port
     */
    int getPort();
}
