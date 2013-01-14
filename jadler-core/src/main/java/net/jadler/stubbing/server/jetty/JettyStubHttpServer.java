/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.StubResponseProvider;
import net.jadler.stubbing.server.StubHttpServer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.Validate;
import org.eclipse.jetty.server.Connector;


/**
 * This class wraps Jetty server, performs all necessary configuration and
 * register HTTP rules which will be handled by the server.
 * <p/>
 * Jetty server created by this class runs in embedded mode.
 */
public class JettyStubHttpServer implements StubHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyStubHttpServer.class);
    private static final String CERTIFICATE_PASSWORD = "changeit";
    private static final String KEYSTORE_FILE_NAME = "jadler.keystore";
    private final Server server;
    private final Connector selectChannelConnector;

    public JettyStubHttpServer() {
        this(0);
    }

    public JettyStubHttpServer(Protocol protocol) {
        this(0, protocol);
    }

    public JettyStubHttpServer(final int port) {
        this(port, Protocol.HTTP);
    }

    /**
     * Creates new jetty stub server listening on given {@code port} and using given {@code protocol}.
     *
     * Note: If you use "https" you have to import "jadler.crt" to your java keystore in a similar way:
     * <pre>
     *     sudo keytool -importcert -alias jadler -file jadler.crt -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass 'changeit'
     * </pre>
     *
     * @param port port to be used
     * @param protocol protocol to be used
     */
    public JettyStubHttpServer(int port, final Protocol protocol) {

        switch (protocol) {
            case HTTP:
                this.selectChannelConnector = createHttpConnector();
                break;
            case HTTPS:
                this.selectChannelConnector = createHttpsConnector();
                break;
            default:
                throw new IllegalStateException("Cannot occur - invalid protocol is catched by argument validation.");
        }

        this.server = new Server();
        selectChannelConnector.setPort(port);
        server.addConnector(selectChannelConnector);
    }

    private Connector createHttpsConnector() {
        final SslSocketConnector connector = new SslSocketConnector();
        connector.setPassword(CERTIFICATE_PASSWORD);
        connector.setKeyPassword(CERTIFICATE_PASSWORD);
        final String keystoreUrl = getClass().getResource(KEYSTORE_FILE_NAME).toString();
        connector.setKeystore(keystoreUrl);
        connector.setTrustPassword(CERTIFICATE_PASSWORD);

        return connector;
    }

    private Connector createHttpConnector() {
        return new SelectChannelConnector();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerResponseProvider(final StubResponseProvider ruleProvider) {
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


    public static enum Protocol {
        HTTP,
        HTTPS
    }
}