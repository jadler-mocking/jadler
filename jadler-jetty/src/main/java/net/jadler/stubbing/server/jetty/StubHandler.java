/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.StubResponse;
import net.jadler.RequestManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.servlet.ServletOutputStream;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.Validate;


public class StubHandler extends AbstractHandler {

    private final RequestManager requestManager;

    public StubHandler(final RequestManager requestManager) {
        Validate.notNull(requestManager, "requestManager cannot be null");
        this.requestManager = requestManager;
    }


    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        net.jadler.Request req = RequestUtils.convert(request);

        final StubResponse stubResponse = this.requestManager.provideStubResponseFor(req);
        setResponseHeaders(stubResponse.getHeaders(), response);
        setStatus(stubResponse.getStatus(), response);
        processDelay(stubResponse.getDelay());
        writeResponseBody(stubResponse.getBody(), response);

        baseRequest.setHandled(true);
    }

    
    private void writeResponseBody(final byte[] body, final HttpServletResponse response) throws IOException {
        if (body.length > 0) {
            final ServletOutputStream os = response.getOutputStream();
            os.write(body);
        }
    }
    

    private void setStatus(final int status, final HttpServletResponse response) {
            response.setStatus(status);
    }
    
    
    private void setResponseHeaders(final MultiMap headers, final HttpServletResponse response) {
        for (@SuppressWarnings("unchecked") final Iterator<Entry<String, Collection<String>>> it 
                = headers.entrySet().iterator(); it.hasNext(); ) {
            
            final Entry<String, Collection<String>> e = it.next();
            
            for (final String value: e.getValue()) {
                response.addHeader(e.getKey(), value);
            }
        }
    }


    private void processDelay(long delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
        }
    }
}