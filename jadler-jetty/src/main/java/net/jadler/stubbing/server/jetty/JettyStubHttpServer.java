/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.RequestManager;
import net.jadler.stubbing.server.StubHttpServer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.Validate;
import org.eclipse.jetty.server.Connector;


/**
 * Default stub http server implementation using <a href="http://jetty.codehaus.org/jetty/" target="_blank">Jetty</a>
 * as an http server.
 */
public class JettyStubHttpServer implements StubHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyStubHttpServer.class);
    private final Server server;
    private final Connector selectChannelConnector;

    public JettyStubHttpServer() {
        this(0);
    }

    public JettyStubHttpServer(final int port) {
        this.server = new Server();
        this.selectChannelConnector = new SelectChannelConnector();
        selectChannelConnector.setPort(port);
        server.addConnector(selectChannelConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerRequestManager(final RequestManager ruleProvider) {
        Validate.notNull(ruleProvider, "ruleProvider cannot be null");

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {new StubHandler(ruleProvider), new DefaultHandler() });
        server.setHandler(handlers);
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
        return selectChannelConnector.getLocalPort();
    }
}