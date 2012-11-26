package net.jadler.server;

import net.jadler.rule.HttpMockResponse;
import net.jadler.httpmocker.ResponseProvider;
import net.jadler.server.jetty.JettyMockHttpServer;


/**
 * Interface for a mock http server component. This component represents a server (usually listening on
 * some port) which can respond to various http requests according to rules provided by a {@link ResponseProvider}
 * instance.
 * 
 * Jadler provides a default implementation of this interface {@link JettyMockHttpServer}, which
 * is based on a embedded jetty server.
 */
public interface MockHttpServer {

    /**
     * Registers a response provider. This component provides a response prescription (in form
     * of a {@link HttpMockResponse} instance for a given http request.
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
