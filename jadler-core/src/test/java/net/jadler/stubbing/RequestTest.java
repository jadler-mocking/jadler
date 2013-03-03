/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.URI.create;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class RequestTest {
    @Test
    public void testEmpty() throws IOException {
        Request request = new Request(null, null, null, null, null);
        assertEquals(null, request.getHeaders("test"));
        assertEquals(null, request.getParameters("test"));
        assertEquals(null, request.getBody());
        assertEquals(null, request.getBodyAsString());
        assertEquals(null, request.getMethod());
        assertEquals(null, request.getUri());
    }

    @Test
    public void testNormal() throws IOException {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("header1", asList("value11", "value12"));
        headers.put("header2", asList("value21"));
        URI uri = create("http://loaclhost:8080/a/b/c?param1=1&param1=2&param2=3");
        Request request = new Request("GET", uri, headers, new ByteArrayInputStream("body".getBytes()), "UTF-8");

        assertEquals("body", IOUtils.toString(request.getBody()));
        assertEquals("body", request.getBodyAsString());

        assertEquals("GET", request.getMethod());
        assertEquals(uri, request.getUri());

        assertEquals(asList("value11", "value12"), request.getHeaders("header1"));
        assertEquals(asList("value21"), request.getHeaders("header2"));
        assertEquals("value11", request.getFirstHeader("header1"));
        assertEquals("value21", request.getFirstHeader("header2"));
        assertEquals(null, request.getFirstHeader("header3"));

        assertEquals(asList("1", "2"), request.getParameters("param1"));
        assertEquals(asList("3"), request.getParameters("param2"));
        assertEquals(null, request.getParameters("param3"));
        assertEquals("1", request.getFirstParameter("param1"));
        assertEquals("3", request.getFirstParameter("param2"));
        assertEquals(null, request.getFirstParameter("param3"));
    }

    @Test
    public void testParamsInBody() throws IOException {
        URI uri = create("http://loaclhost:8080/a/b/c?param1=1&param1=2&param2=3");
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("content-Type", asList("application/x-www-form-urlencoded"));
        String body = "paramA=A1&paramA=A2&paramB=B1";
        Request request = new Request("POST", uri, headers, new ByteArrayInputStream(body.getBytes()), "UTF-8");

        assertEquals(asList("1", "2"), request.getParameters("param1"));
        assertEquals(asList("3"), request.getParameters("param2"));
        assertEquals(null, request.getParameters("param3"));
        assertEquals("1", request.getFirstParameter("param1"));
        assertEquals("3", request.getFirstParameter("param2"));
        assertEquals(null, request.getFirstParameter("param3"));

        assertEquals(asList("A1", "A2"), request.getParameters("paramA"));
        assertEquals(asList("B1"), request.getParameters("paramB"));

    }
}
