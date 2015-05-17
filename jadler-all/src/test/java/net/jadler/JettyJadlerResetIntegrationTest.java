/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jetty.JettyStubHttpServer;
import org.junit.BeforeClass;

/**
 * Tests that its possible to reset JadlerMocker Jetty implementation.
 */
public class JettyJadlerResetIntegrationTest extends AbstractJadlerResetIntegrationTest {
    @BeforeClass
    public static void configureMocker() {
        mocker = new JadlerMocker(new JettyStubHttpServer());
        mocker.start();
    }
}
