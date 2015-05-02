/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;


/**
 * <p>This class represents a key-value data structure with following features:</p>
 * 
 * <ul>
 *   <li>multiple values for one key</li>
 *   <li>key case insensitivity</li>
 *   <li>immutability</li>
 * </ul>
 * 
 * <p>This structure is used in Jadler for modeling request/response headers and request parameters.</p>
 * 
 * <p>Please note this class is immutable and therefore thread safe. All addition operations
 * ({@link #add(java.lang.String, java.lang.String)}, {@link #addAll(net.jadler.KeyValues)} create new instances
 * rather than modifying the instance.</p>
 * 
 * @see Request
 * @see net.jadler.stubbing.StubResponse
 */
public class KeyValues {
    
    private final MultiMap values;
    

    /**
     * An empty instance.
     */
    public static final KeyValues EMPTY = new KeyValues();

    
    /**
     * Creates new empty instance.
     */
    public KeyValues() {
        this.values = new MultiValueMap();
    }
    

    /**
     * Adds new key-value pair. Supports multi-values for one key (if there has already been added
     * some value with this key, additional value is added instead of rewriting). Please note this method
     * creates new instance containing all existing values plus the new one rather than modifying this instance.
     * @param key key (cannot be empty)
     * @param value value (cannot be {@code null}, however can be empty for valueless headers)
     * @return an exact copy of this instance containing all existing values plus the new one
     */
    @SuppressWarnings("unchecked")
    public KeyValues add(final String key, final String value) {
        Validate.notEmpty(key, "key cannot be empty");
        Validate.notNull(value, "value cannot be null, use an empty string instead");
        
        final KeyValues res = new KeyValues();
        res.values.putAll(this.values);
        res.values.put(key.toLowerCase(), value);
        
        return res;
    }
    
    
    /**
     * Adds all values from the given instance. Supports multi-values for one key (if there has already been added
     * some value with this key, additional value is added instead of rewriting). Please note this method
     * creates new instance containing all existing values plus the new ones rather than modifying this instance.
     * @param keyValues values to be added no(cannot be {@code null})
     * @return an exact copy of this instance containing all existing values plus the new ones
     */
    @SuppressWarnings("unchecked")
    public KeyValues addAll(final KeyValues keyValues) {
        Validate.notNull(keyValues, "keyValues cannot be null");
        
        final KeyValues res = new KeyValues();
        res.values.putAll(this.values);
        res.values.putAll(keyValues.values);
        return res;
    }

       
    /**
     * Returns the first value for the given key
     * @param key key (case insensitive)
     * @return single (first) value for the given key or {@code null}, if there is no such a key in this instance
     */
    public String getValue(final String key) {
        Validate.notEmpty(key, "key cannot be empty");
        
        final List<String> allValues = this.getValues(key);
        return allValues != null ? allValues.get(0) : null;
    }

    
    /**
     * Returns all values for the given key
     * @param key key (case insensitive)
     * @return all values of the given header or {@code null}, if there is no such a key in this instance
     */
    public List<String> getValues(final String key) {
        Validate.notEmpty(key, "name cannot be empty");
        
        @SuppressWarnings("unchecked")
        final List<String> result = (List<String>) values.get(key.toLowerCase());
        return result == null || result.isEmpty() ? null : new ArrayList<String>(result);
    }
    
    
    /**
     * @return all keys (lower-cased) from this instance (never returns {@code null})
     */
    public Set<String> getKeys() {
        @SuppressWarnings("unchecked")
        final Set<String> result = new HashSet<String>(this.values.keySet());
        return result;
    }

    

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (@SuppressWarnings("unchecked")final Iterator<Map.Entry<String, Collection<String>>> it
                = this.values.entrySet().iterator(); it.hasNext();) {
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
        hash = 43 * hash + (this.values != null ? this.values.hashCode() : 0);
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
        final KeyValues other = (KeyValues) obj;
        if (this.values != other.values && (this.values == null || !this.values.equals(other.values))) {
            return false;
        }
        return true;
    }
}