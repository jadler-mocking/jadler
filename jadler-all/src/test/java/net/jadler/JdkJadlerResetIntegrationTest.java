/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.junit.BeforeClass;
import static net.jadler.Jadler.initJadlerUsing;

/**
 * Tests that it is possible to reset JadlerMocker JDK implementation.
 */
public class JdkJadlerResetIntegrationTest extends AbstractJadlerResetIntegrationTest {
    
    @BeforeClass
    public static void configureMocker() {
        initJadlerUsing(new JdkStubHttpServer());
    }
}
