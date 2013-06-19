/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;

import net.jadler.exception.JadlerException;


/**
 * A runtime exception signalizing a failure of verification.
 * 
 * @see net.jadler.Jadler#verifyThatRequest() 
 * @see Mocker#verifyThatRequest() 
 */
public class VerificationException extends JadlerException {

    /**
     * @param message verification result text description
     */
    public VerificationException(String message) {
        super(message);
    }
    
}
