package net.jadler.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.abbreviate;


/**
 * A definition of one http response. Contains a definition of the response body, status and headers as well as
 * a timeout the response will be returned after. Instances of this class are mutable so the response definition
 * can be constructed on the fly.
 * 
 * One should never create new instances of this class directly, see {@link HttpMockers} for explanation and tutorial.
 */
public class HttpMockResponse {
    private final MultiMap headers;
    private String body;
    private int status;
    private long timeout;

    
    /**
     * Creates new empty http response definition with http status code 200, no headers, empty body and no timeout.
     */
    public HttpMockResponse() {
        this.headers = new MultiValueMap();
        this.body = "";
        this.status = HttpServletResponse.SC_OK;
    }

    
    /**
     * @return http status of this response
     */
    public int getStatus() {
        return this.status;
    }
    
    /**
     * @param status http status of this response
     */
    public void setStatus(final int status) {
        this.status = status;
    }
    
    /**
     * @return body of this response
     */
    public String getBody() {
        return this.body;
    }
    
    /**
     * @param body body of this response (cannot be null)
     */
    public void setBody(final String body) {
        Validate.notNull(body, "body cannot be null, use an empty string instead.");
        this.body = body;
    }
    
    /**
     * @return response headers
     */
    @SuppressWarnings("unchecked")
    public MultiMap getHeaders() {
        final MultiMap res = new MultiValueMap();
        res.putAll(this.headers);
        
        return res;
    }
    
    /**
     * Adds new header to this response.
     * @param name name of the header
     * @param value header value
     */
    public void addHeader(final String name, final String value) {
        this.headers.put(name, value);
    }
    
    @SuppressWarnings("unchecked")
    public void addHeaders(final MultiMap headers) {
        this.headers.putAll(headers);
    }
    
    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append("status=")
                .append(this.status)
                .append(", body=")
                .append(isBlank(this.body) ? "<empty>" : abbreviate(body, 13))
                .append(", headers=(");
        
        for (@SuppressWarnings("unchecked")final Iterator<Entry<String, Collection<String>>> it
                = this.headers.entrySet().iterator(); it.hasNext();) {
            final Entry<String, Collection<String>> e = it.next();
            
            for (final Iterator<String> it2 = e.getValue().iterator(); it.hasNext();) {
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