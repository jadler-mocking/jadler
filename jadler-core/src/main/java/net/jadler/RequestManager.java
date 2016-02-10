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
     * Verifies whether the number of received http requests fitting the given predicates is as expected. Basically
     * at first this operation computes the exact number of http requests received so far fitting the given predicates
     * and then verifies whether the number is as expected. If not a {@link net.jadler.mocking.VerificationException}
     * is thrown and the exact reason is logged on the {@code INFO} level.
     * 
     * @param requestPredicates predicates about the http requests received so far (cannot be {@code null}, can be
     * empty however)
     * @param nrRequestsPredicate a predicate about the number of http requests received so far which fit the given
     * request predicates (cannot be {@code null})
     * @throws net.jadler.mocking.VerificationException if the verification fails
     */
    void evaluateVerification(Collection<Matcher<? super Request>> requestPredicates,
            Matcher<Integer> nrRequestsPredicate);
    
    
    /**
     * @deprecated this (rather internal) method has been deprecated. Please use
     * {@link #evaluateVerification(java.util.Collection, org.hamcrest.Matcher)} instead
     * 
     * @param predicates predicates to be applied on all incoming http requests 
     * @return number of requests recorded by {@link #provideStubResponseFor(net.jadler.Request)} matching the
     * given matchers
     */
    @Deprecated
    int numberOfRequestsMatching(Collection<Matcher<? super Request>> predicates);
}