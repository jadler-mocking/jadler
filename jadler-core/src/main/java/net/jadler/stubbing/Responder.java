/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.Request;

/**
 * <p>This interface provides a way to define a stub response in a dynamic way (instead of the static approach
 * triggered by the {@link RequestStubbing#respond()} method). Usually it's implemented as an anonymous inner class
 * provided as an argument to {@link RequestStubbing#respondUsing(net.jadler.stubbing.Responder)}.</p>
 */
public interface Responder {
    
    /**
     * <p>Generates dynamically a stub response for an http request fitting the <em>WHEN</em> part.</p>
     * 
     * <p>This method could be called multiple times (when more than one request fitting the <em>WHEN</em> arrives
     * to the stub server). It's up to the implementation whether it returns the same stub response every time or
     * generates it dynamically.</p>
     * 
     * <p>When implementing this interface please keep in mind the access to the instance is not synchronized. If the
     * tested code works in a parallel way (so this method could be called at the same time from more than
     * just one thread), make the class either immutable or synchronize the access to all shared inner states.</p>
     * 
     * @param request an incoming request this responder generates a stub response for
     * @return next stub response for the http request fitting the <em>WHEN</em> part
     * @see RequestStubbing#respondUsing(net.jadler.stubbing.Responder) 
     */
    StubResponse nextResponse(Request request);
}
