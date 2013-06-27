/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>Http request abstraction. It insulates the code from an implementation and serves as immutable copy to keep
 * values after the original instance has been recycled.</p>
 * 
 * <p>To create instances of this class use a {@link Request.Builder} instance.</p>
 */
public class Request {

    private final String method;

    private final URI requestURI;

    private final byte[] body;

    private final MultiMap parameters;

    private final MultiMap headers;

    private final Charset encoding;

    
    @SuppressWarnings("unchecked")
    private Request(final String method, final URI requestURI, final MultiMap headers, final byte[] body, 
            final Charset encoding) {
        
        Validate.notEmpty(method, "method cannot be empty");
        this.method = method;
        
        Validate.notNull(requestURI, "requestURI cannot be null");
        this.requestURI = requestURI;
        
        Validate.notNull(encoding, "encoding cannot be null");
        this.encoding = encoding;
        
        Validate.notNull(body, "body cannot be null, use an empty array instead");
        this.body = body;
        
        Validate.notNull(headers, "headers cannot be null, use an empty map instead");
        this.headers = new MultiValueMap();
        this.headers.putAll(headers);
        
        this.parameters = readParameters();
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
     * Returns the first value of the given request header.
     * @param name header name (case insensitive)
     * @return single (first) value of the given header or null, if there is no such a header in this request
     */
    public String getHeaderValue(final String name) {
        final List<String> headerValues = this.getHeaderValues(name);
        return headerValues != null ? headerValues.get(0) : null;
    }

    
    /**
     * Returns all values of the given request header.
     * @param name header name (case insensitive)
     * @return all values of the given header or null, if there is no such a header in this request 
     */
    public List<String> getHeaderValues(final String name) {
        Validate.notEmpty(name, "name cannot be empty");
        
        @SuppressWarnings("unchecked")
        final List<String> result = (List<String>) headers.get(name.toLowerCase());
        return result == null || result.isEmpty() ? null : new ArrayList<String>(result);
    }
    
    
    /**
     * @return all header names (lower-cased) of this request (never returns {@code null})
     */
    public Set<String> getHeaderNames() {
        @SuppressWarnings("unchecked")
        final Set<String> result = new HashSet<String>(this.headers.keySet());
        return result;
    }
    
    
    /**
     * Returns the first value of the given request parameter.
     * @param name parameter name
     * @return single (first) value of the given parameter or null, if there is no such a parameter in this request
     */
    public String getParameterValue(final String name) {
        final List<String> parameterValues = getParameterValues(name);
        return parameterValues != null ? parameterValues.get(0) : null;
    }    

    
    /**
     * Returns all values of the given request parameter.
     * @param name parameter name
     * @return all values of the given parameter or null, if there is no such a parameter in this request 
     */
    public List<String> getParameterValues(String name) {
        Validate.notEmpty(name, "name cannot be empty");
        
        @SuppressWarnings("unchecked")
        final List<String> result = (List<String>) parameters.get(name);
        return result == null || result.isEmpty() ? null : new ArrayList<String>(result);
    }
    
    
    /**
     * @return all parameter names of this request (never returns <tt>null</tt>)
     */
    public Set<String> getParameterNames() {
        @SuppressWarnings("unchecked")
        final Set<String> result = new HashSet<String>(this.parameters.keySet());
        return result;
    }

    
    /**
     * Returns the body content as an {@link InputStream} instance. This method can be called multiple times
     * always returning valid, readable stream.
     * @return request body as an {@link InputStream} instance
     */
    public InputStream getBodyAsStream() {
        return new ByteArrayInputStream(body);
    }

    
    /**
     * @return request body as a string (if the body is empty, returns an empty string)
     */
    public String getBodyAsString() {
        return new String(body, this.encoding);
    }


    /**
     * @return value of the <tt>content-type</tt> header.
     */
    public String getContentType() {
        return this.getHeaderValue("content-type");
    }
      

    @SuppressWarnings("unchecked")
    private MultiMap readParameters() {
        final MultiMap params = readParametersFromQueryString();

        //TODO: shitty attempt to check whether the body contains html form data. Please refactor.
        if (!StringUtils.isBlank(this.getContentType())
                && this.getContentType().contains("application/x-www-form-urlencoded")) {

            if ("POST".equalsIgnoreCase(this.getMethod()) || "PUT".equalsIgnoreCase(this.getMethod())) {
                params.putAll(this.readParametersFromBody());
            }
        }

        return params;
    }
    

    private MultiMap readParametersFromQueryString() {
        return this.readParametersFromString(this.requestURI.getRawQuery());
    }

    
    private MultiMap readParametersFromBody() {
        return this.readParametersFromString(new String(this.body, this.encoding));
    }

    
    private MultiMap readParametersFromString(final String parametersString) {
        final MultiMap res = new MultiValueMap();

        if (StringUtils.isBlank(parametersString)) {
            return res;
        }

        final String enc = this.encoding.name();
        final String[] pairs = parametersString.split("&");

        for (final String pair : pairs) {
            final int idx = pair.indexOf('=');
            if (idx > -1) {
                final String name = StringUtils.substring(pair, 0, idx);
                final String value = StringUtils.substring(pair, idx + 1);
                res.put(name, value);

            }
            else {
                res.put(pair, "");
            }
        }

        return res;
    }
    

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", requestURI=" + requestURI +
                ", parameters=" + parameters +
                ", headers=" + headers +
                '}';
    }
    
    
    /**
     * Builder class for {@link Request} instances.
     */
    public static class Builder {
        
        private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-1");
        
        private String method;
        private URI requestURI;
        private byte[] body = new byte[0];
        private MultiMap headers = new MultiValueMap();
        private Charset encoding = DEFAULT_ENCODING;
        
        
        /**
         * Sets the request method. Must be called before {@link Builder#build()}.
         * @param method request method
         * @return this builder
         */
        public Builder method(final String method) {
            this.method = method;
            return this;
        }
        
        
        /**
         * Sets the request URI. Must be called before {@link Builder#build()}.
         * @param requestURI request URI
         * @return this builder
         */
        public Builder requestURI(final URI requestURI) {
            this.requestURI = requestURI;
            return this;
        }
        

        /**
         * Sets the request body. If not called, an empty body will be used.
         * @param body request body (cannot be null)
         * @return this builder
         */
        public Builder body(final byte[] body) {
            this.body = body;
            return this;
        }
        
        
        /**
         * Sets the request headers. If not called, no headers will be set.
         * @param name header name (cannot be empty)
         * @param value header value (cannot be {@code null})
         * @return this builder 
         */
        public Builder header(final String name, final String value) {
            Validate.notEmpty(name, "name cannot be blank");
            Validate.notNull(value, "value cannot be null");
            
            this.headers.put(name.toLowerCase(), value);
            return this;
        }
        
        
        /**
         * Sets the request encoding. If not called ISO-8859-1 will be used as a default encoding
         * (http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html).
         * @param encoding request encoding
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
