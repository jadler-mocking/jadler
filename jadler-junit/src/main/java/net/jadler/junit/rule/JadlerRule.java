/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.junit.rule;

import net.jadler.Jadler;
import net.jadler.Jadler.OngoingConfiguration;
import net.jadler.JadlerConfiguration;
import net.jadler.KeyValues;
import net.jadler.stubbing.server.StubHttpServer;
import org.junit.rules.ExternalResource;

import java.nio.charset.Charset;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;


/**
 * JUnit rule which simplifies the creation of a Jadler instance.
 *
 * @author Christian Galsterer
 */
public class JadlerRule extends ExternalResource implements JadlerConfiguration {

    private static final int DEFAULT_PORT = -1;

    private final int port;
    private final StubHttpServer server;

    private String defaultContentType;
    private Charset defaultEncoding;
    private int defaultStatus = -1;
    private boolean skipsRequestsRecording = false;
    private KeyValues defaultHeaders = KeyValues.EMPTY;

    /**
     * Instructs Jadler to use a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on any free port.
     * <p>
     * See also {@link net.jadler.Jadler#initJadler()}
     */
    public JadlerRule() {
        this.port = DEFAULT_PORT;
        this.server = null;
    }

    /**
     * Instructs Jadler to use a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on the given port.
     * <p>
     * See also {@link net.jadler.Jadler#initJadlerListeningOn(int)}
     *
     * @param port port the stub server should be listening on (must be bigger than 0)
     */
    public JadlerRule(final int port) {
        isTrue(port > 0, "port must be an integer bigger than 0");

        this.port = port;
        this.server = null;
    }

    /**
     * Instructs Jadler to use use the given stub server instance.
     * <p>
     * See also {@link net.jadler.Jadler#initJadlerUsing(net.jadler.stubbing.server.StubHttpServer)}
     *
     * @param server stub server to use
     */
    public JadlerRule(final StubHttpServer server) {
        notNull(server, "server cannot be null");

        this.port = DEFAULT_PORT;
        this.server = server;
    }

    @Override
    protected void before() {
        final OngoingConfiguration conf;

        if (port == DEFAULT_PORT) {
            if (server == null) {
                conf = Jadler.initJadler();
            } else {
                conf = Jadler.initJadlerUsing(server);
            }
        } else {
            conf = Jadler.initJadlerListeningOn(port);
        }

        if (this.defaultContentType != null) {
            conf.withDefaultResponseContentType(defaultContentType);
        }

        if (this.defaultEncoding != null) {
            conf.withDefaultResponseEncoding(defaultEncoding);
        }

        if (this.defaultStatus > -1) {
            conf.withDefaultResponseStatus(defaultStatus);
        }

        for (final String name : this.defaultHeaders.getKeys()) {
            for (final String value : this.defaultHeaders.getValues(name)) {
                conf.withDefaultResponseHeader(name, value);
            }
        }

        if (this.skipsRequestsRecording) {
            conf.withRequestsRecordingDisabled();
        }
    }

    @Override
    protected void after() {
        Jadler.closeJadler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JadlerRule withDefaultResponseContentType(final String defaultContentType) {
        this.defaultContentType = defaultContentType;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JadlerRule withDefaultResponseEncoding(final Charset defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JadlerRule withDefaultResponseHeader(final String name, final String value) {
        this.defaultHeaders = this.defaultHeaders.add(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JadlerRule withDefaultResponseStatus(final int defaultStatus) {
        this.defaultStatus = defaultStatus;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JadlerRule withRequestsRecordingDisabled() {
        this.skipsRequestsRecording = true;
        return this;
    }
}
