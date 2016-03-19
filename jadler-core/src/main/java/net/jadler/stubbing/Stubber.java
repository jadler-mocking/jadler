/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

/**
 * An implementation of this interface provides a way to create new stubbing process
 * (ongoing definition of a <em>WHEN</em>-<em>THEN</em> rule).
 */
public interface Stubber {
    /**
     * Starts new stubbing (definition of a <em>WHEN</em>-<em>THEN</em> rule).
     * @return stubbing object to continue the stubbing
     */
    RequestStubbing onRequest(); 
}
