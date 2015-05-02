/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;

import java.util.Iterator;
import net.jadler.AbstractRequestMatching;
import net.jadler.Request;
import net.jadler.RequestManager;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.hamcrest.Matchers.equalTo;


/**
 * Internal class for defining new verification in a fluid fashion. You shouldn't create instances
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
     * Checks whether the number of requests described in this verifying object matches the given predicate.
     * @param predicate to be applied on the number of requests
     * @throws VerificationException if the number of requests described by this verifying is not matched by the given
     * predicate
     */
    public void receivedTimes(final Matcher<Integer> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        final int cnt = this.requestManager.numberOfRequestsMatching(this.predicates);
        
        if (!predicate.matches(cnt)) {
            throw new VerificationException(this.mismatchDescription(cnt, predicate));
        }
    }
    
    
    /**
     * Checks the number of requests described in this verifying object.
     * @param count expected number of requests described in this verifying object.
     * @throws VerificationException if the number of requests described in this verifying object doesn't equal to
     * the given parameter
     */
    public void receivedTimes(final int count) {
        Validate.isTrue(count >= 0, "count cannot be negative");
        this.receivedTimes(equalTo(count));
    }
    
    
    /**
     * Checks only one request described in this verifying object has been received so far.
     * @throws VerificationException if the number of requests descri
     */
    public void receivedOnce() {
        this.receivedTimes(1);
    }
    
    
    public void receivedNever() {
        this.receivedTimes(0);
    }
    
    
    private String mismatchDescription(final int cnt, final Matcher<Integer> callsMatcher) {
        final Description desc = new StringDescription();
        
        desc.appendText("The number of http requests");
        if (!this.predicates.isEmpty()) {
            desc.appendText(" having");
        }
        desc.appendText("\n");
        
        for (final Iterator<Matcher<? super Request>> it = this.predicates.iterator(); it.hasNext();) {
            desc.appendText("  ");
            desc.appendDescriptionOf(it.next());
            
            if (it.hasNext()) {
                desc.appendText(" AND");
            }
            
            desc.appendText("\n");
        }
        
        desc.appendText("was expected to be ");
        desc.appendDescriptionOf(callsMatcher);
        desc.appendText(", but ");
        callsMatcher.describeMismatch(cnt, desc);
        
        return desc.toString();
    }   
}