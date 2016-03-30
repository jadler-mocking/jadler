/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.exception;

/**
 * Runtime exception used in jadler on various places. Because checked exceptions suck, you know.
 */
public class JadlerException extends RuntimeException {
 
    /**
     * @param cause the cause of this exception
     * @see RuntimeException#RuntimeException(java.lang.Throwable) 
     */
    public JadlerException(final Throwable cause) {
        super(cause);
    }

    /**
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     * @see RuntimeException#RuntimeException(java.lang.String, java.lang.Throwable) 
     */
    public JadlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message the detail message
     * @see RuntimeException#RuntimeException(java.lang.String) 
     */
    public JadlerException(final String message) {
        super(message);
    }

    /**
     * @see RuntimeException#RuntimeException() 
     */
    public JadlerException() {
    }
}
