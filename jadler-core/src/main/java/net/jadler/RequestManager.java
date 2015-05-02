/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.util.Collection;
import net.jadler.stubbing.StubResponse;
import org.hamcrest.Matcher;


/**
 * A stateful component which provides stub responses for given requests. These requests are recorded so further mocking
 * (verifying) is possible.
 */
public interface RequestManager {
    
    /**
     * Returns a stub response for the given request. The request is recorded for further mocking (verifying).
     * @param req http request to return a stub response for
     * @return definition of a stub response to be returned by the stub http server (never returns {@code null})
     */
    StubResponse provideStubResponseFor(Request req);
    
    
    /**
     * @param predicates predicates to be applied on all incoming http requests 
     * @return number of requests recorded by {@link #provideStubResponseFor(net.jadler.Request)} matching the
     * given matchers
     */
    int numberOfRequestsMatching(Collection<Matcher<? super Request>> predicates);
}
