/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;


/**
 * A runtime exception signalizing a failure of verification.
 * 
 * @see net.jadler.Jadler#verifyThatRequest() 
 * @see Mocker#verifyThatRequest() 
 */
public class VerificationException extends AssertionError {

    /**
     * @param message verification result text description
     */
    public VerificationException(final String message) {
        super(message);
    }
    
}
