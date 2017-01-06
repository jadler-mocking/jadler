/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.parameters;

import net.jadler.stubbing.server.StubHttpServer;

/**
 * An interface for factories creating new {@link StubHttpServer} instances.
 * For testing purposes only.
 */
public interface StubHttpServerFactory {
    
    /**
     * @return new server instance
     */
    StubHttpServer createServer();
}
