/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import net.jadler.stubbing.server.StubHttpServer;
import net.jadler.stubbing.server.jdk.JdkStubHttpServer;


public class JdkJadlerStubbingResponseHeadersTest extends AbstractJadlerStubbingResponseHeadersTest {

    @Override
    protected StubHttpServer createServer() {
        return new JdkStubHttpServer();
    }    
}
