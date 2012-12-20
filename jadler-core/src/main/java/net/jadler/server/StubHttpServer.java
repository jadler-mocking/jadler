package net.jadler.server;

import net.jadler.httpmocker.ResponseProvider;


/**
 * Interface for a stub http server component.
 * 
 * This component represents an http server which waits for http requests and returns stub http responses
 * according to a {@link ResponseProvider} instance.
 * 
 * Jadler provides a default implementation of this interface {@link JettyStubHttpServer} based on
 * an embedded jetty server.
 */
public interface StubHttpServer {

    /**
     * Registers a response provider. This component provides a response prescription (in form
     * of a {@link HttpMockResponse} instance) for a given http request.
     * @param responseProvider response provider to use to retrieve response prescriptions.
     */
    void registerResponseProvider(ResponseProvider responseProvider);
    
    /**
     * Starts the underlying http server. From now, the server must be able to respond
     * according to prescriptions returned from the registered {@link ResponseProvider} instance.
     * @throws Exception when ugh... something went wrong
     */
    void start() throws Exception;

    /**
     * Stops the underlying http server.
     * @throws Exception when an error occurred while stopping the server
     */
    void stop() throws Exception;
}
