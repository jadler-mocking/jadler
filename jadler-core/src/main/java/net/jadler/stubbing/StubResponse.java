/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.abbreviate;


/**
 * A definition of a stub http response. Defines the response status, encoding, body and headers as well as
 * a timeout the response will be returned after. Instances of this class are mutable so the stub response definition
 * can be constructed on the fly.
 * 
 * One should never create new instances of this class directly, see {@link net.jadler.Jadler} for explanation and tutorial.
 */
public class StubResponse {
    private Charset encoding;
    private final MultiMap headers;
    private String stringBody;
    private byte[] rawBody;
    private int status;
    private long timeout;

    
    /**
     * Creates new empty stub http response definition.
     */
    public StubResponse() {
        this.headers = new MultiValueMap();
    }
    
    
    /**
     * @return encoding of the stub response body
     */
    public Charset getEncoding() {
        return encoding;
    }

    
    /**
     * @param encoding encoding of the stub response body
     */
    public void setEncoding(final Charset encoding) {
        this.encoding = encoding;
    }

    
    /**
     * @return http status of the stub response
     */
    public int getStatus() {
        return this.status;
    }
    
    
    /**
     * @param status http status of the stub response
     */
    public void setStatus(final int status) {
        this.status = status;
    }
    
    
    /**
     * Sets the stub response body as a string.
     * Calling this method also resets any previous calls of {@link #setBody(byte[]) }
     * @param body stub response body (cannot be null)
     */
    public void setBody(final String body) {
        Validate.notNull(body, "body cannot be null, use an empty string instead.");
        this.stringBody = body;
        this.rawBody = null;
    }

    
    /**
     * Sets the stub response body as an array of bytes.
     * Calling this method also resets any previous calls of {@link #setBody(java.lang.String) }
     * @param body stub response body (cannot be null)
     */
    public void setBody(byte[] body) {
        Validate.notNull(body, "body cannot be null, use an empty array instead.");
        this.rawBody = body;
        this.stringBody = null;
    }
    
    
    public byte[] getBody() {
        if (this.rawBody != null) {
            return this.rawBody;
        }
        else if (stringBody != null) {
            if (this.encoding != null) {
                return this.stringBody.getBytes(this.encoding);
            }
            else {
                throw new IllegalStateException("The response body encoding has not been set yet, "
                        + "cannot return the response body as an array of bytes.");
            }
        }
        else {
            throw new IllegalStateException("The response body has not been set yet.");
        }
    }
    
    
    /**
     * @return stub response headers
     */
    @SuppressWarnings("unchecked")
    public MultiMap getHeaders() {
        final MultiMap res = new MultiValueMap();
        res.putAll(this.headers);
        
        return res;
    }
    
    
    /**
     * Adds a new header to this stub response. If there already exists a header with this name
     * in this stub response, multiple values will be sent.
     * @param name header name
     * @param value header value
     */
    public void addHeader(final String name, final String value) {
        this.headers.put(name, value);
    }
    
    
    /**
     * Adds headers to this stub response. If there already exists a header with a same name
     * in this stub response, multiple values will be sent.
     * @param headers response headers (both keys and values must be of type String)
     */
    @SuppressWarnings("unchecked")
    public void addHeaders(final MultiMap headers) {
        this.headers.putAll(headers);
    }
    
    
    /**
     * Removes all occurrences of the given header in this stub response (using a case insensitive search)
     * and sets its single value.
     * @param name header name
     * @param value header value
     */
    public void setHeaderCaseInsensitive(final String name, final String value) {
        
          //remove all occurrencies of the given header first
        for (final Object o: this.headers.keySet()) {
            final String key = (String) o; //fucking non-generics MultiMap
            if (name.equalsIgnoreCase(key)) {
                headers.remove(key);
            }
        }
        
        this.addHeader(name, value);
    }
    
    
    /**
     * @return a timeout (in millis) this stub response will be returned after
     */
    public long getTimeout() {
        return this.timeout;
    }
    
    
    /**
     * @param timeout a timeout (in millis) this stub response will be returned after 
     */
    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
    
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append("encoding=")
                .append(this.encoding)
                .append(", status=")
                .append(this.status)
                .append(", body=");
        if (!isBlank(this.stringBody)) {
            sb.append(abbreviate(this.stringBody, 13));
        }
        else if (this.rawBody != null && this.rawBody.length > 0) {
            sb.append("<binary>");
        }
        else {
            sb.append("<empty>");
        }

        sb.append(", headers=(");
        for (@SuppressWarnings("unchecked")final Iterator<Entry<String, Collection<String>>> it
                = this.headers.entrySet().iterator(); it.hasNext();) {
            final Entry<String, Collection<String>> e = it.next();
            
            for (final Iterator<String> it2 = e.getValue().iterator(); it2.hasNext();) {
                sb.append(e.getKey()).append(": ").append(it2.next());
                if (it2.hasNext()) {
                    sb.append(", ");
                }
            }
            
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
                
        sb.append("), timeout=").append(this.timeout).append("ms");
        return sb.toString();
    }
}