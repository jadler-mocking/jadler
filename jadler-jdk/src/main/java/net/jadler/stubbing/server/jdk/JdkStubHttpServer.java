/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import com.sun.net.httpserver.HttpServer;
import net.jadler.RequestManager;
import net.jadler.stubbing.server.StubHttpServer;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Stub server implementation based on {@link HttpServer} which is part of JDK.
 */
public class JdkStubHttpServer implements StubHttpServer {
    private final HttpServer server;

    public JdkStubHttpServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 100);
        } catch (IOException e) {
            throw new IllegalStateException("Can not start server");
        }
    }

    public JdkStubHttpServer() {
        this(0);
    }


    @Override
    public void registerRequestManager(RequestManager ruleProvider) {
        Validate.notNull(ruleProvider, "ruleProvider cannot be null");
        server.createContext("/", new JdkHandler(ruleProvider));
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop(0);
    }

    @Override
    public int getPort() {
        return server.getAddress().getPort();
    }
}
