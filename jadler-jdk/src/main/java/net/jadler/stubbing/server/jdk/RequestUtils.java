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
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class RequestUtils {
    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i).*\\bcharset=\\s*\"?([^\\s;\"]*)");

    static Request convert(final HttpExchange httpExchange) throws IOException {
        final Request.Builder builder = Request.builder()
                .method(httpExchange.getRequestMethod())
                .requestURI(httpExchange.getRequestURI())
                .body(IOUtils.toByteArray(httpExchange.getRequestBody()));

        addEncoding(builder, httpExchange);
        addHeaders(builder, httpExchange);

        return builder.build();
    }

    //package protected for testing purposes
    static void addEncoding(final Request.Builder builder, final HttpExchange httpExchange) {
        final String contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null) {
            final Matcher matcher = CHARSET_PATTERN.matcher(contentType);
            if (matcher.matches()) {
                try {
                    builder.encoding(Charset.forName(matcher.group(1)));
                }
                catch (UnsupportedCharsetException e) {
                    //just ignore, fallback encoding will be used instead
                }
            }
        }
    }

    private static void addHeaders(final Request.Builder builder, final HttpExchange httpExchange) {
        final Headers requestHeaders = httpExchange.getRequestHeaders();
        for (final String key : requestHeaders.keySet()) {
            for (final String value : requestHeaders.get(key)) {
                builder.header(key, value);
            }
        }
    }
}
