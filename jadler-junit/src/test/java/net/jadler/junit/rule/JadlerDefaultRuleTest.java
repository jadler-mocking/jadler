/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */

package net.jadler.junit.rule;

import net.jadler.Jadler;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link net.jadler.junit.rule.JadlerRule#JadlerRule()} variant.
 *
 * @author Christian Galsterer
 */
public class JadlerDefaultRuleTest {

    @Rule
    public JadlerRule defaultJadler = new JadlerRule();

    @Test
    public void testWithDefaultPort() {
         assertTrue(Jadler.port() >= 0);
    }
}
