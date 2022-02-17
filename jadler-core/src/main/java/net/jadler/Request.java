/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;


/**
 * <p>Immutable http request abstraction. Provides request method, URI, body, parameters and headers.</p>
 *
 * <p>To create instances of this class use {@link #builder()}.</p>
 */
public class Request {

    private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-1");

    private final String method;

    private final URI requestURI;

    private final byte[] body;

    private final KeyValues parameters;

    private final KeyValues headers;

    private final Charset encoding;


    @SuppressWarnings("unchecked")
    private Request(final String method, final URI requestURI, final KeyValues headers, final byte[] body,
                    final Charset encoding) {

        Validate.notEmpty(method, "method cannot be empty");
        this.method = method;

        Validate.notNull(requestURI, "requestURI cannot be null");
        this.requestURI = requestURI;

        this.encoding = encoding;

        Validate.notNull(body, "body cannot be null, use an empty array instead");
        this.body = body;

        Validate.notNull(headers, "headers cannot be null");
        this.headers = headers;

        this.parameters = readParameters();
    }

    /**
     * @return new builder for creating {@link Request} instances
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return http method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return URI of the request. For example http://localhost:8080/test/file?a=4
     */
    public URI getURI() {
        return this.requestURI;
    }

    /**
     * @return all http parameters (read from both query string and request body) from this request.
     * Never returns {@code null}
     */
    public KeyValues getParameters() {
        return this.parameters;
    }

    /**
     * @return all http headers from this request. Never returns {@code null}
     */
    public KeyValues getHeaders() {
        return this.headers;
    }

    /**
     * Returns the body content as an {@link InputStream} instance. This method can be called multiple times
     * always returning valid, readable stream.
     *
     * @return request body as an {@link InputStream} instance
     */
    public InputStream getBodyAsStream() {
        return new ByteArrayInputStream(body);
    }

    /**
     * @return request body as an array of bytes
     */
    public byte[] getBodyAsBytes() {
        return this.body.clone();
    }

    /**
     * @return request body as a string (if the body is empty, returns an empty string). If no encoding was
     * set using the {@code Content-Type} header ISO-8859-1 will be used
     * (http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html).
     */
    public String getBodyAsString() {
        return new String(this.body, this.getEffectiveEncoding());
    }

    /**
     * @return value of the {@code Content-Type} header.
     */
    public String getContentType() {
        return this.headers.getValue("content-type");
    }

    /**
     * @return request body encoding set by the {@code Content-Type} header or {@code null} if not set
     */
    public Charset getEncoding() {
        return this.encoding;
    }

    @SuppressWarnings("unchecked")
    private KeyValues readParameters() {
        KeyValues params = readParametersFromQueryString();

        //TODO: shitty attempt to check whether the body contains html form data. Please refactor.
        if (!StringUtils.isBlank(this.getContentType())
                && this.getContentType().contains("application/x-www-form-urlencoded")) {

            if ("POST".equalsIgnoreCase(this.getMethod()) || "PUT".equalsIgnoreCase(this.getMethod())) {
                params = params.addAll(this.readParametersFromBody());
            }
        }

        return params;
    }


    private KeyValues readParametersFromQueryString() {
        return this.readParametersFromString(this.requestURI.getRawQuery());
    }


    private KeyValues readParametersFromBody() {
        return this.readParametersFromString(new String(this.body, this.getEffectiveEncoding()));
    }


    private KeyValues readParametersFromString(final String parametersString) {
        KeyValues res = new KeyValues();

        if (StringUtils.isBlank(parametersString)) {
            return res;
        }

        final String[] pairs = parametersString.split("&");

        for (final String pair : pairs) {
            final int idx = pair.indexOf('=');
            if (idx > -1) {
                final String name = StringUtils.substring(pair, 0, idx);
                final String value = StringUtils.substring(pair, idx + 1);
                res = res.add(name, value);

            } else {
                res = res.add(pair, "");
            }
        }

        return res;
    }


    private Charset getEffectiveEncoding() {
        return this.encoding == null ? DEFAULT_ENCODING : this.encoding;
    }


    @Override
    public String toString() {
        return new StringBuilder()
                .append("{")
                .append("method=")
                .append(method)
                .append(", URI=")
                .append(requestURI)
                .append(", parameters=[")
                .append(parameters)
                .append("], headers=[")
                .append(headers)
                .append("], encoding=")
                .append(encoding == null ? "<none>" : encoding)
                .append(", body=")
                .append(this.getBodyAsBytes().length > 1 ? "<nonempty>" : "<empty>")
                .append("}")
                .toString();
    }


    /**
     * A builder class for {@link Request} instances.
     */
    public static class Builder {

        private String method;
        private URI requestURI;
        private byte[] body = new byte[0];
        private KeyValues headers = new KeyValues();
        private Charset encoding = null;


        /**
         * Private constructor. Use {@link Request#builder()} instead.
         */
        private Builder() {
        }


        /**
         * Sets the request method. Must be called before {@link Builder#build()}.
         *
         * @param method request method
         * @return this builder
         */
        public Builder method(final String method) {
            this.method = method;
            return this;
        }


        /**
         * Sets the request URI. Must be called before {@link Builder#build()}.
         *
         * @param requestURI request URI
         * @return this builder
         */
        public Builder requestURI(final URI requestURI) {
            this.requestURI = requestURI;
            return this;
        }


        /**
         * Sets the request body. If not called, an empty body will be used.
         *
         * @param body request body (cannot be {@code null})
         * @return this builder
         */
        public Builder body(final byte[] body) {
            this.body = body;
            return this;
        }


        /**
         * Sets the request headers (all previously defined headers will be lost).
         *
         * @param headers request headers (cannot be {@code null})
         * @return this builder
         */
        public Builder headers(final KeyValues headers) {
            Validate.notNull(headers, "headers cannot be null");

            this.headers = headers;
            return this;
        }


        /**
         * Adds a request header to the constructed request instance.
         *
         * @param name  header name (cannot be empty)
         * @param value header value (cannot be {@code null})
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            Validate.notEmpty(name, "name cannot be blank");
            Validate.notNull(value, "value cannot be null");

            this.headers = this.headers.add(name.toLowerCase(), value);
            return this;
        }


        /**
         * Sets the request encoding. If not set {@code null} value will be used signalizing no encoding was set
         * in the incoming request (using the {@code Content-Type} header)
         *
         * @param encoding request encoding (can be {@code null})
         * @return this builder
         */
        public Builder encoding(final Charset encoding) {
            this.encoding = encoding;
            return this;
        }


        /**
         * @return new {@link Request} instance
         */
        public Request build() {
            return new Request(method, requestURI, headers, body, encoding);
        }
    }
}
