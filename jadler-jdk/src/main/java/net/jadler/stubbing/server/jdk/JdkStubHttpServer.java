/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import com.sun.net.httpserver.HttpServer;
import net.jadler.RequestManager;
import net.jadler.stubbing.server.StubHttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import net.jadler.exception.JadlerException;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;


/**
 * Stub server implementation based on {@link HttpServer} which is part of JDK.
 */
public class JdkStubHttpServer implements StubHttpServer {
    
    private final HttpServer server;

    public JdkStubHttpServer(final int port) {
        isTrue(port >= 0, "port cannot be a negative number");
        
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (final IOException e) {
            throw new JadlerException("Cannot create JDK server", e);
        }
    }

    public JdkStubHttpServer() {
        this(0);
    }

    @Override
    public void registerRequestManager(final RequestManager ruleProvider) {
        notNull(ruleProvider, "ruleProvider cannot be null");
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
