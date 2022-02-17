/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;


/**
 * An implementation of this interface provides a way to create new ongoing verification process.
 */
public interface Mocker {

    /**
     * Starts new verification process.
     *
     * @return verifying object to continue the ongoing verifying
     */
    Verifying verifyThatRequest();
}
