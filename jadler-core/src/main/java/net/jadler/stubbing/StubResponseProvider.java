/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import javax.servlet.http.HttpServletRequest;


/**
 * A component which provides stub http response definitions for the given http request.
 */
public interface StubResponseProvider {
    
    /**
     * @param req http request to return a stub response for
     * @return definition of a stub response to be returned for the given request or null,
     * if no response is defined for this request
     */
    StubResponse provideStubResponseFor(HttpServletRequest req);
}
