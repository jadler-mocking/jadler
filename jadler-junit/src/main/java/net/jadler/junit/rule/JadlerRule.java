package net.jadler.junit.rule;

import net.jadler.Jadler;
import net.jadler.stubbing.server.StubHttpServer;
import org.junit.rules.ExternalResource;

/**
 * Junit rule which simplifies the creation of a Jadler instance.
 *
 * @author Christian Galsterer
 */
public class JadlerRule extends ExternalResource {

    public static final int DEFAULT_PORT = -1;
    private int port = DEFAULT_PORT;

    /**
     * Initializes Jadler and starts a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on any free port.
     *
     * See also {@link net.jadler.Jadler#initJadler()}
     */
    public JadlerRule() {
    }

    /**∂
     * Initializes Jadler and starts a default stub server {@link net.jadler.stubbing.server.jetty.JettyStubHttpServer}
     * serving the http protocol listening on the given port.
     *
     * See also {@link net.jadler.Jadler#initJadlerListeningOn(int)}
     */
    public JadlerRule(int port) {

        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
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
