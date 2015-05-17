/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.junit.BeforeClass;

/**
 * Tests that its possible to reset JadlerMocker JDK implementation.
 */
public class JdkJadlerResetIntegrationTest extends AbstractJadlerResetIntegrationTest {
    @BeforeClass
    public static void configureMocker() {
        mocker = new JadlerMocker(new JdkStubHttpServer());
        mocker.start();
    }
}
