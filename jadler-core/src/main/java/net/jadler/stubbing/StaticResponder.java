/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.Request;
import org.apache.commons.lang.Validate;

import java.util.Iterator;
import java.util.List;


/**
 * Internal responder used as an envelope for responses statically defined through {@link RequestStubbing#respond()}.
 * This class is package private and used internally only.
 */
class StaticResponder implements Responder {

    private final List<StubResponse> stubResponses;
    private int responsePointer = 0;


    /**
     * Creates a {@link Responder} which returns stub responses from a predefined list.
     *
     * @param stubResponses list of predefined stub responses (cannot be empty)
     */
    StaticResponder(final List<StubResponse> stubResponses) {
        Validate.notEmpty(stubResponses, "stubResponses cannot be empty");
        this.stubResponses = stubResponses;
    }


    /**
     * @param request not used at all
     * @return next response from the list of stub responses provided by the constructor. If the last stub
     * response has already been reached, this response will be returned for all subsequent calls.
     */
    @Override
    public synchronized StubResponse nextResponse(final Request request) {
        if (this.responsePointer < this.stubResponses.size() - 1) {
            return this.stubResponses.get(this.responsePointer++);
        } else {
            return this.stubResponses.get(this.responsePointer);
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (final Iterator<StubResponse> it = this.stubResponses.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append("\nfollowed by ");
            }
        }

        return sb.toString();
    }
}
