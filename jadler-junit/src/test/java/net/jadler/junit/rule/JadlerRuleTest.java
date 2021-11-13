/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.junit.rule;

import net.jadler.stubbing.server.StubHttpServer;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class JadlerRuleTest {

    @Test
    public void constructor_valid() {
        new JadlerRule();
        new JadlerRule(12345);
        new JadlerRule(mock(StubHttpServer.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalid() {
        new JadlerRule(-1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalid2() {
        new JadlerRule(null);
    }
}