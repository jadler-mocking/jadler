/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */

package net.jadler.junit.rule;

import net.jadler.Jadler;
import org.junit.rules.ExternalResource;

import static org.apache.commons.lang.Validate.isTrue;


/**
 * JUnit rule which simplifies the creation of a Jadler instance.
 *
 * @author Christian Galsterer
 */
public class JadlerRule extends ExternalResource {

    private static final int DEFAULT_PORT = -1;
    
    private final int port;

    /**
     * Instructs Jadler to use a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on any free port.
     *
     * See also {@link net.jadler.Jadler#initJadler()}
     */
    public JadlerRule() {
        this.port = DEFAULT_PORT;
    }

    /**
     * Instructs Jadler to use a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on the given port.
     *
     * See also {@link net.jadler.Jadler#initJadlerListeningOn(int)}
     * 
     * @param port port the stub server should be listening on (must be bigger than 0)
     */
    public JadlerRule(final int port) {
        isTrue(port > 0, "port must be an integer bigger than 0");
        
        this.port = port;
    }

    @Override
    protected void before() {
        if (port == DEFAULT_PORT)
            Jadler.initJadler();
        else
            Jadler.initJadlerListeningOn(port);
    }

    @Override
    protected void after() {
        Jadler.closeJadler();
    }
}
