/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jetty.JettyStubHttpServer;
import org.junit.BeforeClass;

import static net.jadler.Jadler.initJadlerUsing;


/**
 * Tests that it is possible to reset the {@link JettyStubHttpServer} implementation.
 */
public class ResetJettyIntegrationTest extends AbstractResetIntegrationTest {

    @BeforeClass
    public static void configureMocker() {
        initJadlerUsing(new JettyStubHttpServer())
                .withDefaultResponseStatus(DEFAULT_STATUS);
    }
}
