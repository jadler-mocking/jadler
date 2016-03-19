/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import net.jadler.KeyValues;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;


/**
 * An internal package private holder for stub response data. This class is mutable so the data can be set on the fly.
 */
class MutableStubResponse {
    private Charset encoding;
    private final MultiMap headers;
    private String stringBody;
    private byte[] rawBody;
    private int status;
    private long delay;

    
    /**
     * Creates new empty stub http response definition.
     */
    MutableStubResponse() {
        this.headers = new MultiValueMap();
    }
    
  
    /**
     * @return encoding of the stub response body
     */  
    Charset getEncoding() {
        return this.encoding;
    }

    
    /**
     * @param encoding encoding of the stub response body
     */
    void setEncoding(final Charset encoding) {
        this.encoding = encoding;
    }

    
    /**
     * @return http status of the stub response
     */
    int getStatus() {
        return status;
    }
    
    
    /**
     * @param status http status of the stub response
     */
    void setStatus(final int status) {
        this.status = status;
    }
    
    
    /**
     * @return body as set by {@link #setBody(java.lang.String)} or {@code null} if the body was not set as a string
     */
    String getStringBody() {
        return this.stringBody;
    }


    /**
     * @return body as set by {@link #setBody(byte[])} or {@code null} if the body was not set as an array of bytes
     */
    byte[] getRawBody() {
        return this.rawBody;
    }
    
    
    /**
     * Sets the stub response body as a string.
     * Calling this method also resets any previous calls of {@link #setBody(byte[]) }
     * @param body stub response body
     */
    void setBody(final String body) {
        this.stringBody = body;
        this.rawBody = null;
    }

    
    /**
     * Sets the stub response body as an array of bytes.
     * Calling this method also resets any previous calls of {@link #setBody(java.lang.String) }
     * @param body stub response body
     */
    void setBody(byte[] body) {
        this.rawBody = body;
        this.stringBody = null;
    }
    
    
    /**
     * @return stub response headers
     */
    MultiMap getHeaders() {
        return this.headers;
    }
        
    
    /**
     * Adds a new header to this stub response. If there already exists a header with this name
     * in this stub response, multiple values will be sent.
     * @param name header name
     * @param value header value
     */
    void addHeader(final String name, final String value) {
        this.headers.put(name, value);
    }
    
    
    /**
     * Adds headers to this stub response. If there already exists a header with a same name
     * in this stub response, multiple values will be sent.
     * @param headers response headers (both keys and values must be of type String)
     */
    @SuppressWarnings("unchecked")
    void addHeaders(final MultiMap headers) {
        this.headers.putAll(headers);
    }
    
    
    /**
     * Removes all occurrences of the given header in this stub response (using a case insensitive search)
     * and sets its single value.
     * @param name header name
     * @param value header value
     */
    @SuppressWarnings("unchecked")
    void setHeaderCaseInsensitive(final String name, final String value) {
        final MultiMap result = new MultiValueMap();
        
        for (final Object o: this.headers.keySet()) {
            final String key = (String) o; //fucking non-generics MultiMap
            if (!name.equalsIgnoreCase(key)) {
                  //copy all other headers to the result multimap
                for(final String s: (Collection<String>)this.headers.get(o)) {
                    result.put(o, s);
                }
            }
        }
        
        this.headers.clear();
        this.headers.putAll(result);
        this.addHeader(name, value);
    }
    
    
    /**
     * @return a delay (in millis) this stub response will be returned after
     */
    long getDelay() {
        return delay;
    }
    
    
    /**
     * @param delay a delay (in millis) this stub response will be returned after 
     */
    void setDelay(final long delay) {
        this.delay = delay;
    }
    
    
    /**
     * @return a {@link StubResponse} instance created from data stored in this object
     */
    StubResponse toStubResponse() {
        final StubResponse.Builder builder = StubResponse.builder()
                .status(status)
                .delay(delay, TimeUnit.MILLISECONDS)
                .headers(this.createHeaders());
        
        if (this.stringBody != null) {
            if (this.encoding == null) {
                throw new IllegalStateException("The response body encoding has not been set yet, "
                        + "cannot generate a StubResponse instance.");
            }
            else {
                builder.body(this.stringBody, this.encoding);
            }
        }
        else if (this.rawBody != null) {
            builder.body(rawBody);
        }
        else {
            throw new IllegalStateException("The response body has not been set yet, "
                    + "cannot generate a StubResponse instance.");
        }
        
        return builder.build();
    }
    
    
    private KeyValues createHeaders() {
        KeyValues res = new KeyValues();
        
        for (@SuppressWarnings("unchecked") final Iterator<String> itKey =
                (Iterator<String>) this.headers.keySet().iterator(); itKey.hasNext();) {
            final String key = itKey.next();
            
            for (@SuppressWarnings("unchecked") final Iterator<String> itValue =
                    ((Collection<String>)this.headers.get(key)).iterator(); itValue.hasNext();) {
                res = res.add(key, itValue.next());
            }
        }
        
        return res;
    }
}