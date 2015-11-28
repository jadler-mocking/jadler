/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.StubHttpServer;
import net.jadler.stubbing.server.jetty.JettyStubHttpServer;


/**
 * Jadler integration test for Jetty.
 */
public class JettyJadlerStubbingIntegrationTest extends AbstractJadlerStubbingIntegrationTest {

    @Override
    protected StubHttpServer createServer() {
        return new JettyStubHttpServer();
    }
}
