/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.Request;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;


public class RequestUtilsTest {
    
    @Test
    public void method() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        Request req = RequestUtils.convert(httpRequest);
        assertThat(req.getMethod(), is("POST"));
    }
    
    
    @Test
    public void body() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        httpRequest.setContent("abcd".getBytes());

        final Request req = RequestUtils.convert(httpRequest);
        assertThat(req.getBodyAsString(), is("abcd"));
    }
    

    @Test
    public void headers() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        httpRequest.addHeader("header1", "value11");
        httpRequest.addHeader("header2", "value21");
        httpRequest.addHeader("header2", "value22");

        final Request req = RequestUtils.convert(httpRequest);
        
        assertThat(req.getHeaders().getKeys(), containsInAnyOrder("header1", "header2"));
        assertThat(req.getHeaders().getValues("header1"), contains("value11"));
        assertThat(req.getHeaders().getValues("header2"), contains("value21", "value22"));
    }

    
    @Test
    public void parameters() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        httpRequest.setQueryString("a=1&b=3");
        httpRequest.setContent("a=2".getBytes());
        httpRequest.addHeader("content-type", "application/x-www-form-urlencoded");

        final Request req = RequestUtils.convert(httpRequest);
        assertThat(req.getParameters().getKeys(), containsInAnyOrder("a", "b"));
        assertThat(req.getParameters().getValues("a"), contains("1", "2"));
        assertThat(req.getParameters().getValues("b"), contains("3"));
    }
    
    
    @Test
    public void parametersURLEncoded() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        httpRequest.setQueryString("param1%20name=param1%20value");
        httpRequest.setContent("param2%20name=param2%20value".getBytes());
        httpRequest.addHeader("content-type", "application/x-www-form-urlencoded");

        final Request req = RequestUtils.convert(httpRequest);
        assertThat(req.getParameters().getKeys(), containsInAnyOrder("param1%20name", "param2%20name"));
        assertThat(req.getParameters().getValues("param1%20name"), contains("param1%20value"));
        assertThat(req.getParameters().getValues("param2%20name"), contains("param2%20value"));
    }
    

    @Test
    public void uri() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        httpRequest.setScheme("https");
        httpRequest.setServerName("example.com");
        httpRequest.setServerPort(1234);
        httpRequest.setRequestURI("/test/a/b?a=1");

        Request req = RequestUtils.convert(httpRequest);
        assertThat(req.getURI(), is(URI.create("https://example.com:1234/test/a/b?a=1")));
    }
    
    
    @Test
    public void uriURLEncoded() throws IOException {
        final MockHttpServletRequest httpRequest = prepareEmptyRequest();
        httpRequest.setScheme("https");
        httpRequest.setServerName("example.com");
        httpRequest.setServerPort(1234);
        httpRequest.setRequestURI("/te%20st/a/%20/b?a=1&param%20name=param%20value");

        Request req = RequestUtils.convert(httpRequest);
        assertThat(req.getURI(), is(URI.create("https://example.com:1234/te%20st/a/%20/b?a=1&param%20name=param%20value")));
    }
    
    
    private MockHttpServletRequest prepareEmptyRequest() {
        final MockHttpServletRequest res = new MockHttpServletRequest();
        res.setMethod("POST");
        res.setContent(new byte[0]);
        
        return res;
    }
}
