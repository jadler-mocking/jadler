/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.Request;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.list;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Converts {@link javax.servlet.http.HttpServletRequest} to {@link net.jadler.stubbing.Request}.
 */
class RequestUtils {
    public static Request convert(HttpServletRequest source) throws IOException {
        String method = source.getMethod();
        URI requestUri = URI.create(source.getRequestURL() + getQueryString(source));
        InputStream body = source.getInputStream();
        String encoding = source.getCharacterEncoding();
        Map<String, List<String>> headers = convertHeaders(source);
        return new Request(method, requestUri, headers, body, encoding);
    }

    private static String getQueryString(HttpServletRequest source) {
        return isNotBlank(source.getQueryString()) ? ("?" + source.getQueryString()) : "";
    }

    private static Map<String, List<String>> convertHeaders(HttpServletRequest request){
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            result.put(headerName, list(request.getHeaders(headerName)));
        }

        return result;
    }
}
