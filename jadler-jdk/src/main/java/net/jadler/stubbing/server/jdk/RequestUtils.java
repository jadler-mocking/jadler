/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.jadler.Request;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RequestUtils {
    private static final Pattern charsetPattern = Pattern.compile("(?i).*\\bcharset=\\s*\"?([^\\s;\"]*)");

    public static Request convert(HttpExchange httpExchange) throws IOException {
        final Request.Builder builder = Request.builder()
                .method(httpExchange.getRequestMethod())
                .requestURI(httpExchange.getRequestURI())
                .body(IOUtils.toByteArray(httpExchange.getRequestBody()));

        addEncoding(builder, httpExchange);
        addHeaders(builder, httpExchange);

        return builder.build();
    }

    private static void addEncoding(Request.Builder builder, HttpExchange httpExchange) {
        String contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null) {
            Matcher matcher = charsetPattern.matcher(contentType);
            if (matcher.matches()) {
                builder.encoding(Charset.forName(matcher.group(1)));
            }
        }
    }

    private static void addHeaders(Request.Builder builder, HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();
        for (String key : requestHeaders.keySet()) {
            for (String value : requestHeaders.get(key)) {
                builder.header(key, value);
            }
        }

    }
}
