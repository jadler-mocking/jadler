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
 * One should never create new instances of this class directly, see {@link Jadler} for explanation and tutorial.
 */
public class StubResponse {
    private Charset encoding;
    private final MultiMap headers;
    private String body;
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
     * @return stub response body
     */
    public String getBody() {
        return this.body;
    }
    
    
    /**
     * @param body stub response body (cannot be null)
     */
    public void setBody(final String body) {
        Validate.notNull(body, "body cannot be null, use an empty string instead.");
        this.body = body;
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
                .append(", body=")
                .append(isBlank(this.body) ? "<empty>" : abbreviate(body, 13))
                .append(", headers=(");
        
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