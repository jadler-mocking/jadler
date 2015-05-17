/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.jdk.JdkStubHttpServer;

import static net.jadler.Jadler.initJadlerUsing;


/**
 * Jadler integration test for Jetty for JDK HTTP server.
 */
public class JdkJadlerStubbingIntegrationTest extends AbstractJadlerStubbingIntegrationTest {
    @Override
    protected Jadler.AdditionalConfiguration doInitJadler() {
        return initJadlerUsing(new JdkStubHttpServer());
    }
}
