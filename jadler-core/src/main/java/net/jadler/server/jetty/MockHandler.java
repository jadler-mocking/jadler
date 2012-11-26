package net.jadler.server.jetty;

import net.jadler.rule.HttpMockResponse;
import net.jadler.httpmocker.ResponseProvider;
import net.jadler.server.MultipleReadsHttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.commons.collections.MultiMap;


public class MockHandler extends AbstractHandler {

    private static final String RESPONSE_ENCODING = "UTF-8";
    private final ResponseProvider ruleProvider;

    public MockHandler(final ResponseProvider ruleProvider) {
        this.ruleProvider = ruleProvider;
    }

    /**
     * This method handles incoming HTTP request, then consults list of
     * registered rules and generates response.
     *
     * @throws IllegalArgumentException when no suitable rule found
     */
    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        final MultipleReadsHttpServletRequest multiReadsRequest = new MultipleReadsHttpServletRequest(request);
        final HttpMockResponse mockResponse = this.ruleProvider.provideResponseFor(multiReadsRequest);
        if (mockResponse != null) {
            setResponseHeaders(mockResponse.getHeaders(), response);
            setStatus(mockResponse.getStatus(), response);
            baseRequest.setHandled(true);
            response.setCharacterEncoding(RESPONSE_ENCODING);
            processTimeout(mockResponse.getTimeout());
            writeResponseBody(mockResponse.getBody(), response);
        } else {
            final String queryString;
            if (StringUtils.isNotBlank(request.getQueryString())) {
                queryString = "?" + request.getQueryString();
            } else {
                queryString = "";
            }

            throw new IllegalArgumentException("No suitable rule found for request: " + request.getMethod()
                    + " " + request.getRequestURI() + queryString);
        }
    }

    private void writeResponseBody(final String body, final HttpServletResponse response) throws IOException {
        if (StringUtils.isNotBlank(body)) {
            response.getWriter().print(body);
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