/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server;


/**
 * An implementation of this interface can manage an underlying stub http server.
 */
public interface StubHttpServerManager {
        
    /**
     * Starts the underlying stub http server
     * @throws JadlerException if an error occurred while starting the stub http server.
     * @throws IllegalStateException if the stub server has been started already.
     */
    void start();
    
    
    /**
     * Stops the underlying stub http server.
     * @throws JadlerException if an error occurred while stopping the stub http server.
     * @throws IllegalStateException if the stub server hasn't been started yet or has been stopped already.
     */
    void close();
    
    
    /**
     * @return true, if the stub server has already been started, otherwise false
     */
    boolean isStarted();


    /**
     * @return port of HTTP server
     */
    public int getStubHttpServerPort();
}
