package net.jadler.junit.rule;

import net.jadler.Jadler;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link net.jadler.junit.rule.JadlerRule#JadlerRule(int)} variant.
 *
 * @author Christian Galsterer
 */
public class JadlerFixedPortRuleTest {

    private static final int port = 12345;

    @Rule
    public JadlerRule fixedPortJadler = new JadlerRule(port);

    @Test
    public void testWithFixedPort() {
        assertTrue(Jadler.port() == port);
    }
}
