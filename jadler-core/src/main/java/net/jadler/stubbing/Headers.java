/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;


/**
 * A representation of http response headers used in {@link StubResponse}. This class is immutable and therefore thread
 * safe.
 * 
 * @see StubResponse
 */
public class Headers {
    
    private final MultiMap headers;

    
    public Headers() {
        this.headers = new MultiValueMap();
    }
    

    /**
     * Adds a new stub response header. Supports multi-value headers (if a header with the same name has already been
     * added before, adds another value to it). Please note this method creates new headers object containing all
     * existing headers plus the new one rather than modifying this instance.
     * @param name header name (cannot be empty)
     * @param value header value (cannot be {@code null}, however can be empty for valueless headers)
     * @return an exact copy of this headers containing all existing headers plus the new one
     */
    @SuppressWarnings("unchecked")
    public Headers add(final String name, final String value) {
        Validate.notEmpty(name, "name cannot be empty");
        Validate.notNull(value, "value cannot be null, use an empty string instead");
        
        final Headers res = new Headers();
        res.headers.putAll(this.headers);
        res.headers.put(name, value);
        
        return res;
    }
    
    
    /**
     * @return set of all header names (never returns {@code null})
     */
    @SuppressWarnings("unchecked")
    public Set<String> getNames() {
        return new HashSet<String>(this.headers.keySet());
    }
    
    
    /**
     * @param name header name
     * @return collection of header values (returns {@code null} if no such a header exists) 
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getValues(final String name) {
        return (Collection<String>) this.headers.get(name);
    }
    

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (@SuppressWarnings("unchecked")final Iterator<Map.Entry<String, Collection<String>>> it
                = this.headers.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<String, Collection<String>> e = it.next();
            
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
        
        return sb.toString();
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.headers != null ? this.headers.hashCode() : 0);
        return hash;
    }
    

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Headers other = (Headers) obj;
        if (this.headers != other.headers && (this.headers == null || !this.headers.equals(other.headers))) {
            return false;
        }
        return true;
    }
}
