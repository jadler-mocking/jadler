/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.Request;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Enumeration;
import org.apache.commons.io.IOUtils;

import static org.apache.commons.lang.StringUtils.*;


/**
 * Converts {@link javax.servlet.http.HttpServletRequest} to {@link net.jadler.stubbing.Request}.
 */
class RequestUtils {
    
    static Request convert(final HttpServletRequest source) throws IOException {
        
        final Charset encoding = isNotBlank(source.getCharacterEncoding())
                ? Charset.forName(source.getCharacterEncoding())
                : null;
        
        final Request.Builder builder = new Request.Builder()
                .method(source.getMethod())
                .requestURI(URI.create(source.getRequestURL() + getQueryString(source)))
                .body(IOUtils.toByteArray(source.getInputStream()));
        
        if (encoding != null) {
            builder.encoding(encoding);
        }
        
        return addHeaders(builder, source).build();
    }
    

    private static String getQueryString(final HttpServletRequest source) {
        return source.getQueryString() != null ? ("?" + source.getQueryString()) : "";
    }

    
    private static Request.Builder addHeaders(final Request.Builder builder, final HttpServletRequest req) {
        @SuppressWarnings("unchecked")
        Enumeration<String> names = req.getHeaderNames();
        
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            
            @SuppressWarnings("unchecked")
            final Enumeration<String> values = req.getHeaders(name);
            
            while (values.hasMoreElements()) {
                builder.header(name, values.nextElement());
            }
        }        
        
        return builder;
    }
}