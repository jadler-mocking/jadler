/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.exception;

/**
 * Runtime exception used in jadler on various places. Because checked exceptions suck, you know.
 */
public class JadlerException extends RuntimeException {
    
    /**
     * {@inheritDoc}
     */ 
    public JadlerException(final String message, final Throwable cause, final boolean enableSuppression, 
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * {@inheritDoc}
     */ 
    public JadlerException(final Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */     
    public JadlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */     
    public JadlerException(final String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */     
    public JadlerException() {
    }
}
