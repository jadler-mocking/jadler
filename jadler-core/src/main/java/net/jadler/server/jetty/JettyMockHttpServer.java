package net.jadler.server.jetty;

import net.jadler.httpmocker.ResponseProvider;
import net.jadler.server.MockHttpServer;
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
 * This class wraps Jetty server, performs all necessary configuration and
 * register HTTP rules which will be handled by the server.
 *
 * Jetty server created by this class runs in embedded mode.
 */
public class JettyMockHttpServer implements MockHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyMockHttpServer.class);
    private final Server server;


    public JettyMockHttpServer(final int port) {
        this.server = new Server();
        final Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.addConnector(connector);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void registerResponseProvider(final ResponseProvider ruleProvider) {
        Validate.notNull(ruleProvider, "ruleProvider cannot be null");
        
        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {new MockHandler(ruleProvider), new DefaultHandler() });
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
}