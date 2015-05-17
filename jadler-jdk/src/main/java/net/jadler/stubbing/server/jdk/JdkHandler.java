/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.jadler.KeyValues;
import net.jadler.Request;
import net.jadler.RequestManager;
import net.jadler.stubbing.StubResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Processes requests and sends them to the rest of Jadler library.
 */
class JdkHandler implements HttpHandler {
    private final RequestManager requestManager;

    public JdkHandler(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        final Request req = RequestUtils.convert(httpExchange);
        final StubResponse stubResponse = this.requestManager.provideStubResponseFor(req);

        byte[] body = stubResponse.getBody();

        this.processDelay(stubResponse.getDelay());

        KeyValues headers = stubResponse.getHeaders();
        for (final String key: headers.getKeys()) {
             for (final String value: headers.getValues(key)) {
                 httpExchange.getResponseHeaders().add(key, value);
             }
         }

        httpExchange.sendResponseHeaders(stubResponse.getStatus(), body.length > 0 ? body.length : -1);

        if (body.length > 0) {
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(body);
            outputStream.close();
        }

    }

    private void processDelay(long delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // ok
            }
        }
    }
}
