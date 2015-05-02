/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.RequestMatching;


/**
 * This interface defines methods for the http stubbing <i>WHEN</i> part. These methods provides
 * a way to define predicates (in form of Hamcrest matchers) the incoming http request must fulfill in order to
 * return a stub response (defined by methods of {@link ResponseStubbing}).
 */
public interface RequestStubbing extends RequestMatching<RequestStubbing> {

    /**
     * Finishes the <em>WHEN</em> part of this stubbing and starts the <em>THEN</em> part.
     * @return response stubbing instance to continue this stubbing
     */
    ResponseStubbing respond();
    
    
    /**
     * Finishes the <em>WHEN</em> part of this stubbing and allows to define the <em>THEN</em> part in
     * a dynamic way.
     * @param responder {@link Responder} instance (usually in a form of an anonymous inner class) which dynamically
     * creates an http response to be returned when an incoming http request matches the <em>WHEN</em> part.
     */
    void respondUsing(Responder responder);
}
