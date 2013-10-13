/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;

import static org.apache.commons.lang.StringUtils.abbreviate;


/**
 * Definition of a stub http response. For creating new instances use the {@link #builder()} static method.
 */
public class StubResponse {
    private final Headers headers;
    private final byte[] body;
    private final Charset encoding;
    private final int status;
    private final long delayValue;
    private final TimeUnit delayUnit;
    
    
    /**
     * An empty stub response containing nothing but defaults (empty body, http status 200, no headers and no delay)
     */
    public static final StubResponse EMPTY = new StubResponse.Builder().build();

    
    private StubResponse(final int status, final byte[] body, final Charset encoding,
            final Headers headers, final long delayValue, final TimeUnit delayUnit) {
        
        this.status = status;
        this.body = body;
        this.encoding = encoding;
        this.headers = headers;
        this.delayValue = delayValue;
        this.delayUnit = delayUnit;
    }

        
    /**
     * @return http status of the stub response
     */
    public int getStatus() {
        return this.status;
    }
    

    /**
     * @return response body as an array of bytes
     */
    public byte[] getBody() {
        return this.body.clone();
    }
    
    
    /**
     * @return encoding of the body ({@code null} if not set)
     */
    public Charset getEncoding() {
        return this.encoding;
    }
    
    
    /**
     * @return stub response headers
     */
    public Headers getHeaders() {
        return this.headers;
    }
    
    
    /**
     * @return a delay (in millis) this stub response will be returned after
     */
    public long getDelay() {
        return this.delayUnit.toMillis(this.delayValue);
    }
    
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append("status=")
                .append(this.status)
                .append(", body=");
        
        if (this.body.length > 0) {
            if (this.encoding != null) {
                sb.append(abbreviate(new String(this.body, this.encoding), 13));
                sb.append(", encoding=").append(this.encoding);
            }
            else {
                sb.append("<binary>");
            }
        }
        else {
            sb.append("<empty>");
        }

        sb.append(", headers=(").append(this.headers.toString());
        sb.append("), delay=").append(this.delayValue).append(" ").append(this.delayUnit.toString().toLowerCase());
        return sb.toString();
    }
    
    
    /**
     * @return new builder for creating {@link StubResponse} instances
     */
    public static Builder builder() {
        return new Builder();
    }
    
    
    /**
     * A builder class for creating new {@link StubResponse} instances.
     */
    public static class Builder {
        private int status;
        private byte[] body;
        private Charset encoding;
        private Headers headers;
        private long delayValue;
        private TimeUnit delayUnit;
        
        
        /**
         * Private package constructor. Use {@link StubResponse#builder()} instead.
         */
        Builder() {
            this.status = 200;
            this.body = new byte[0];
            this.encoding = null;
            this.headers = new Headers();
            this.delayValue = 0;
            this.delayUnit = TimeUnit.MILLISECONDS;
        }


        /**
         * Sets the stub response http status. If not called, {@code 200} will be used as a default.
         * @param status stub response status (cannot be negative)
         * @return this builder
         */
        public Builder status(final int status) {
            Validate.isTrue(status >= 0, "status cannot be negative");
            this.status = status;
            return this;
        }
        
        
        /**
         * Sets the response body as an array of bytes. Calling this method resets all data
         * previously provided by {@link #body(String, Charset)}. If the response body is not set at all, an empty
         * body is used.
         * @param body stub response body as an array of bytes (cannot be null).
         * @return this builder
         */
        public Builder body(final byte[] body) {
            Validate.notNull(body, "body cannot be null, use an empty array instead");
            this.body = body;
            this.encoding = null;
            return this;
        }
        
        
        /**
         * Sets the response body as a string. Calling this method resets all data previously provided
         * by {@link #body(byte[])}. If the response body is not set at all, an empty body is used.
         * @param body stub response body as a string (cannot be {@code null})
         * @param encoding encoding of the body (cannot be {@code null})
         * @return this builder
         */
        public Builder body(final String body, final Charset encoding) {
            Validate.notNull(body, "body cannot be null, use an empty string instead");
            Validate.notNull(encoding, "encoding cannot be null");
            this.body = body.getBytes(encoding);
            this.encoding = encoding;
            return this;
        }
        
        
        /**
         * Sets new stub response headers (all previously set headers are discarded).
         * @param headers stub response headers (cannot be {@code null})
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            Validate.notNull(headers, "headers cannot be null");
            this.headers = headers;
            return this;
        }
        
        
        /**
        * Adds a new stub response header. Supports multivalue headers (if a header with the same name has already been
        * added before, adds another value to it)
        * @param name header name (cannot be empty)
        * @param value header value (cannot be {@code null}, however can be empty for valueless headers)
        * @return this headers
        */
       public Builder header(final String name, final String value) {
           Validate.notEmpty(name, "name cannot be empty");
           Validate.notNull(value, "value cannot be null, use an empty string instead");

           this.headers = this.headers.add(name, value);
           return this;
       }
        
        
        /**
         * Sets the response delay. If not called {@code 0} will be used as a default.
         * @param delayValue a delay (in units defined by the {@code delayUnit} parameter)
         * this stub response will be returned after
         * @param delayUnit unit of the delay parameter
         * @return this builder
         */
        public Builder delay(long delayValue, TimeUnit delayUnit) {
            Validate.isTrue(delayValue >= 0, "delayValue cannot be negative");
            Validate.notNull(delayUnit, "delayUnitCannot be null");
            this.delayValue = delayValue;
            this.delayUnit = delayUnit;
            return this;
        }
        
        
        /**
         * @return a {@link StubResponse} instance built from values stored in this builder
         */
        public StubResponse build() {
            return new StubResponse(this.status, this.body, this.encoding, this.headers,
                    this.delayValue, this.delayUnit);
        }
    }
}