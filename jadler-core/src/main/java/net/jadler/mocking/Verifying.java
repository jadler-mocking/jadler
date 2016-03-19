/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;

import net.jadler.AbstractRequestMatching;
import net.jadler.RequestManager;
import org.apache.commons.lang.Validate;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;


/**
 * Allows defining new verification in a fluid fashion. You shouldn't create instances
 * of this class on your own, please see {@link net.jadler.Jadler#verifyThatRequest()} for more information
 * on creating instances of this class.
 */
public class Verifying extends AbstractRequestMatching<Verifying> {
    
    private final RequestManager requestManager;

    
    /**
     * @param requestManager request manager instance to assist the verification
     */
    public Verifying(final RequestManager requestManager) {
        Validate.notNull(requestManager, "requestManager cannot be null");
        
        this.requestManager = requestManager;
    }
    
    
    /**
     * Checks whether the number of requests described in this verifying object received so far matches the given predicate.
     * @param nrRequestsPredicate to be applied on the number of requests
     * @throws VerificationException if the number of requests described by this verifying is not matched by the given
     * predicate
     */
    public void receivedTimes(final Matcher<Integer> nrRequestsPredicate) {
        Validate.notNull(nrRequestsPredicate, "predicate cannot be null");
        
        this.requestManager.evaluateVerification(predicates, nrRequestsPredicate);
    }
    
    
    /**
     * Checks whether the number of requests described in this verifying object received so far matches the exact value.
     * @param count expected number of requests described by this verifying object
     * @throws VerificationException if the number of requests described in this verifying object received so far
     * is not equal to the expected value
     */
    public void receivedTimes(final int count) {
        Validate.isTrue(count >= 0, "count cannot be negative");
        this.receivedTimes(equalTo(count));
    }
    
    
    /**
     * Checks that exactly one request described in this verifying object has been received so far.
     * @throws VerificationException if the number of expected requests is not equal to one
     */
    public void receivedOnce() {
        this.receivedTimes(1);
    }
    
    
    /**
     * Checks that no request described in this verifying object has been received so far.
     * @throws VerificationException if at least one request described in this object has already been received
     */
    public void receivedNever() {
        this.receivedTimes(0);
    }
}