/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.junit.BeforeClass;
import static net.jadler.Jadler.initJadlerUsing;

/**
 * Tests that it is possible to reset the {@link JdkStubHttpServer} implementation.
 */
public class ResetJDKIntegrationTest extends AbstractResetIntegrationTest {
    
    @BeforeClass
    public static void configureMocker() {
        initJadlerUsing(new JdkStubHttpServer())
                .withDefaultResponseStatus(DEFAULT_STATUS);
    }
}
