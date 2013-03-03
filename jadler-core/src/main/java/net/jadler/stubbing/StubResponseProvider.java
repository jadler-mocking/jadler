/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;


/**
 * A component which provides stub http response definitions for the given http request.
 */
public interface StubResponseProvider {
    
    /**
     * @param req http request to return a stub response for
     * @return definition of a stub response to be returned by the stub http server (never returns <tt>null</tt>)
     */
    StubResponse provideStubResponseFor(Request req);
}
