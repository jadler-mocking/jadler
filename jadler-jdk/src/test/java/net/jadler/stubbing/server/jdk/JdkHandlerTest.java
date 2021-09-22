/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.jadler.Request;
import net.jadler.RequestManager;
import net.jadler.stubbing.StubResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.argThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;


public class JdkHandlerTest {
    
    private static final String METHOD = "GET";
    private static final URI REQUEST_URI = URI.create("/abcd");
    private static final byte[] REQUEST_BODY = "request body".getBytes();
    private static final String REQUEST_HEADER1 = "request-header1";
    private static final String REQUEST_HEADER1_VALUE1 = "request-value11";
    private static final String REQUEST_HEADER1_VALUE2 = "request-value12";
    private static final String REQUEST_HEADER2 = "request-header2";
    private static final String REQUEST_HEADER2_VALUE1 = "request-value21";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "text/plain; charset=UTF-8";
    
    private static final Request EXPECTED_REQUEST = Request.builder()
            .body(REQUEST_BODY)
            .encoding(Charset.forName("UTF-8"))
            .method(METHOD)
            .requestURI(REQUEST_URI)
            .header(REQUEST_HEADER1, REQUEST_HEADER1_VALUE1)
            .header(REQUEST_HEADER1, REQUEST_HEADER1_VALUE2)
            .header(REQUEST_HEADER2, REQUEST_HEADER2_VALUE1)
            .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE)
            .build();
    
    private static final int RESPONSE_STATUS = 200;
    private static final String RESPONSE_BODY = "response body";
    private static final int RESPONSE_DELAY = 1;
    private static final TimeUnit RESPONSE_DELAY_UNIT = TimeUnit.SECONDS;
    private static final String RESPONSE_HEADER1 = "response-header1";
    private static final String RESPONSE_HEADER1_VALUE1 = "response-value11";
    private static final String RESPONSE_HEADER1_VALUE2 = "response-value12";
    private static final String RESPONSE_HEADER2 = "response-header2";
    private static final String RESPONSE_HEADER2_VALUE1 = "response-value21";
            
    
    private HttpExchange httpExchange;
    
    private RequestManager mockManager;
    
    private Headers mockResponseHeaders;
    
    private OutputStream mockResponseStream;
    
    @Before
    public void setUp() {
        this.httpExchange = mock(HttpExchange.class);
        when(httpExchange.getRequestMethod()).thenReturn(METHOD);
        when(httpExchange.getRequestURI()).thenReturn(REQUEST_URI);
        when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(REQUEST_BODY));
        
        final Headers requestHeaders = new Headers();
        requestHeaders.add(REQUEST_HEADER1, REQUEST_HEADER1_VALUE1);
        requestHeaders.add(REQUEST_HEADER1, REQUEST_HEADER1_VALUE2);
        requestHeaders.add(REQUEST_HEADER2, REQUEST_HEADER2_VALUE1);
        requestHeaders.add(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE);
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);
        
        this.mockManager = mock(RequestManager.class);
        
        this.mockResponseHeaders = mock(Headers.class);
        when(httpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        
        this.mockResponseStream = mock(OutputStream.class);
        when(httpExchange.getResponseBody()).thenReturn(mockResponseStream);
    }
    
    
    @Test
    public void constructor() {
        new JdkHandler(mockManager);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void constructor_fail() {
        new JdkHandler(null);
    }
    

    @Test
    public void handle_responseBody() throws IOException {
        when(mockManager.provideStubResponseFor(reqEq(EXPECTED_REQUEST))).thenReturn(StubResponse.builder()
                .status(RESPONSE_STATUS)
                .body(RESPONSE_BODY.getBytes())
                .delay(RESPONSE_DELAY, RESPONSE_DELAY_UNIT)
                .header(RESPONSE_HEADER1, RESPONSE_HEADER1_VALUE1)
                .header(RESPONSE_HEADER1, RESPONSE_HEADER1_VALUE2)
                .header(RESPONSE_HEADER2, RESPONSE_HEADER2_VALUE1)
                .build());
        
        final long start = System.currentTimeMillis();
        new JdkHandler(mockManager).handle(httpExchange);
        assertThat(RESPONSE_DELAY_UNIT.toMillis(RESPONSE_DELAY),
                is(lessThanOrEqualTo(System.currentTimeMillis() - start)));
        
        verify(mockResponseHeaders).add(eq(RESPONSE_HEADER1), eq(RESPONSE_HEADER1_VALUE1));
        verify(mockResponseHeaders).add(eq(RESPONSE_HEADER1), eq(RESPONSE_HEADER1_VALUE2));
        verify(mockResponseHeaders).add(eq(RESPONSE_HEADER2), eq(RESPONSE_HEADER2_VALUE1));
        verifyNoMoreInteractions(mockResponseHeaders);
        
        verify(httpExchange).sendResponseHeaders(RESPONSE_STATUS, RESPONSE_BODY.length());
        verify(mockResponseStream).write(RESPONSE_BODY.getBytes());
    }
    
    
    @Test
    public void handle_noResponseBody() throws IOException {
        when(mockManager.provideStubResponseFor(reqEq(EXPECTED_REQUEST))).thenReturn(StubResponse.builder()
                .status(RESPONSE_STATUS)
                .build());
        
        new JdkHandler(mockManager).handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(RESPONSE_STATUS, -1);
        verifyNoInteractions(mockResponseStream);
    }
    
    
    private static Request reqEq(final Request req) {
        return argThat(new RequestMatcher(req));
    }
    
    
    private static class RequestMatcher implements ArgumentMatcher<Request> {

        private final Request expected;
        
        private RequestMatcher(final Request expected) {
            this.expected = expected;
        }
        
        @Override
        public boolean matches(final Request argument) {
            final Request arg = (Request) argument;

            if (!this.expected.getMethod().equals(arg.getMethod())) {
                return false;
            }

            if (!this.expected.getURI().equals(arg.getURI())) {
                return false;
            }
            
            if (!Arrays.equals(this.expected.getBodyAsBytes(), arg.getBodyAsBytes())) {
                return false;
            }
            
            if (!this.expected.getHeaders().equals(arg.getHeaders())) {
                return false;
            }

            return this.expected.getParameters().equals(arg.getParameters());
        }
    }               
}
