/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.StubResponse;
import net.jadler.stubbing.StubResponseProvider;
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


public class StubHandler extends AbstractHandler {

    private final StubResponseProvider ruleProvider;

    public StubHandler(final StubResponseProvider ruleProvider) {
        this.ruleProvider = ruleProvider;
    }


    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        final StubResponse stubResponse = this.ruleProvider.provideStubResponseFor(request);
        setResponseHeaders(stubResponse.getHeaders(), response);
        setStatus(stubResponse.getStatus(), response);
        processTimeout(stubResponse.getTimeout());
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


    private void processTimeout(long timeout) {
        if (timeout > 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignored) {
            }
        }
    }
}