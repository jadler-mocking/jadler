/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jdk.JdkStubHttpServer;

import net.jadler.stubbing.server.StubHttpServer;


/**
 * Jadler integration test for Jetty for JDK HTTP server.
 */
public class JdkJadlerStubbingIntegrationTest extends AbstractJadlerStubbingIntegrationTest {

    @Override
    protected StubHttpServer createServer() {
        return new JdkStubHttpServer();
    }
}
