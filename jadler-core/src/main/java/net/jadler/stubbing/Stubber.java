/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

/**
 * An implementation of this interface provides a way to create new stubbing process
 * (definition of a <i>WHEN</i>-<i>THEN</i> rule).
 */
public interface Stubber {
    /**
     * Starts new stubbing (definition of a <i>WHEN</i>-<i>THEN</i> rule).
     * @return stubbing object to continue the stubbing
     */
    RequestStubbing onRequest(); 
}
