/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.Request;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.net.URI;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class RequestUtilsTest {

    @Test
    public void headers() throws IOException {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("header1", "value11");
        httpRequest.addHeader("header2", "value21");
        httpRequest.addHeader("header2", "value22");

        Request request = RequestUtils.convert(httpRequest);
        assertEquals(asList("value11"), request.getHeaders("header1"));
        assertEquals(asList("value21","value22"), request.getHeaders("header2"));
        assertEquals(null, request.getHeaders("header3"));
    }

    @Test
    public void uriParameters() throws IOException {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setQueryString("a=1&b=3&a=2");

        Request request = RequestUtils.convert(httpRequest);
        assertEquals(asList("1","2"), request.getParameters("a"));
        assertEquals(asList("3"), request.getParameters("b"));
        assertEquals(null, request.getParameters("c"));
    }

    @Test
    public void uri() throws IOException {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRequestURI("/test/a/b?a=1");

        Request request = RequestUtils.convert(httpRequest);
        assertEquals(URI.create("http://localhost:80/test/a/b?a=1"), request.getUri());
        assertEquals("a=1", request.getQueryString());
    }
}
