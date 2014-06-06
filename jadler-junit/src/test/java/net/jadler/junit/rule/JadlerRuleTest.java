/*
 * Copyright (c) 2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.junit.rule;

import org.junit.Test;


public class JadlerRuleTest {
    
    @Test
    public void constructor_valid() {
        new JadlerRule();
        new JadlerRule(12345);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalid() {
        new JadlerRule(-1);
    }
}