/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.junit.rule;

import net.jadler.Jadler;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the {@link net.jadler.junit.rule.JadlerRule#JadlerRule(int)} variant.
 *
 * @author Christian Galsterer
 */
public class JadlerFixedPortRuleTest {

    private static final int PORT = SocketUtils.findAvailableTcpPort();

    @Rule
    public JadlerRule fixedPortJadler = new JadlerRule(PORT);

    @Test
    public void testWithFixedPort() {
        assertThat(Jadler.port(), is(PORT));
    }
}
