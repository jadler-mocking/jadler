/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.RequestManager;
import net.jadler.stubbing.server.StubHttpServer;
import org.apache.commons.lang.Validate;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default stub http server implementation using Jetty as an http server.
 */
public class JettyStubHttpServer implements StubHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyStubHttpServer.class);
    private final Server server;
    private final Connector httpConnector;

    public JettyStubHttpServer() {
        this(0);
    }


    public JettyStubHttpServer(final int port) {
        this.server = new Server();
        this.server.setSendServerVersion(false);
        this.server.setSendDateHeader(true);

        this.httpConnector = new SelectChannelConnector();
        this.httpConnector.setPort(port);
        server.addConnector(this.httpConnector);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerRequestManager(final RequestManager ruleProvider) {
        Validate.notNull(ruleProvider, "ruleProvider cannot be null");

        server.setHandler(new JadlerHandler(ruleProvider));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception {
        logger.debug("starting jetty");
        server.start();
        logger.debug("jetty started");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        logger.debug("stopping jetty");
        server.stop();
        logger.debug("jetty stopped");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return httpConnector.getLocalPort();
    }
}