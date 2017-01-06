/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.parameters;

import java.util.Arrays;
import net.jadler.stubbing.server.StubHttpServer;
import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import net.jadler.stubbing.server.jetty.JettyStubHttpServer;

/**
 * Provides test parameters for the acceptance/integration tests located in the {@code jadler-all} module.
 */
public class TestParameters {
    
    /**
     * @return parameters for acceptance/integration tests located in this module. The fugly return type
     * is required by the jUnit parameters mechanism. It basically returns two stub server factories as
     * test parameters.
     */
    public Iterable<StubHttpServerFactory[]> provide() {
        
        return Arrays.asList(
                singletonArray(new StubHttpServerFactory() {
                    @Override
                    public StubHttpServer createServer() {
                        return new JettyStubHttpServer();
                    }
                }),
                singletonArray(new StubHttpServerFactory() {
                    @Override
                    public StubHttpServer createServer() {
                        return new JdkStubHttpServer();
                    }
                })
        );
    }
    
    private StubHttpServerFactory[] singletonArray(final StubHttpServerFactory server) {
        return new StubHttpServerFactory[] {server};
    }
}
