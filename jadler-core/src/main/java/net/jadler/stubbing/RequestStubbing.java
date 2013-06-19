/*
 * Copyright (c) 2013 Jadler contributors
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
     * Finishes the <i>WHEN</i> part of this stubbing and starts the <i>THEN</i> part.
     * @return response stubbing instance to continue this stubbing
     */
    ResponseStubbing respond();
}
