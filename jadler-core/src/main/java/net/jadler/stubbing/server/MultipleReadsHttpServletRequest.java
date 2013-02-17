/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


public class MultipleReadsHttpServletRequest extends HttpServletRequestWrapper {
    
    private byte[] body;
    
    private Map<String, String[]> parameters;

    
    public MultipleReadsHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        
        this.body = IOUtils.toByteArray(this.getRequest().getInputStream());

          //parameters must be initialized after the request body is read
          //(since params can be read from the body as well)
        this.parameters = this.readParameters();
    }
    

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final InputStream res = new ByteArrayInputStream(this.body);
        
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return res.read();
            }
        };
    }
    

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), this.getEncodingInternal()));
    }
    

    @Override
    public String getParameter(String name) {
        if (this.parameters.containsKey(name)) {
            return this.parameters.get(name)[0];
        }
        
        return null;
    }
    

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }
    

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }
    

    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }
    
    
    private String getEncodingInternal() {
        return this.getCharacterEncoding() == null ? Charset.defaultCharset().name() : this.getCharacterEncoding();
    }
    
    
    @SuppressWarnings("unchecked")
    private Map<String, String[]> readParameters() throws IOException {
        final MultiMap params = readParametersFromQueryString(); 
        
          //TODO: shitty attempt to check whether the body contains html form data. Please refactor.
        if (!StringUtils.isBlank(this.getContentType()) &&
            this.getContentType().contains("application/x-www-form-urlencoded")) {
            
            if ("POST".equalsIgnoreCase(this.getMethod()) || "PUT".equalsIgnoreCase(this.getMethod())) {
                params.putAll(this.readParametersFromBody());
            }
        }
        
        final Map<String, String[]> res = new HashMap<>();
        for(final Object o: params.entrySet()) {
            final Entry<String, Collection<String>> e = (Entry) o;
            res.put(e.getKey(), e.getValue().toArray(new String[0]));
        }
        
        return res;
    }
    
    
    private MultiMap readParametersFromQueryString() {
        return this.readParametersFromString(this.getQueryString());
    }
    

    private MultiMap readParametersFromBody() throws IOException {
        return this.readParametersFromString(new String(this.body, this.getEncodingInternal()));
    }
    
    
    private MultiMap readParametersFromString(final String parametersString) {
        final MultiMap res = new MultiValueMap();
        
        if (StringUtils.isBlank(parametersString)) {
            return res;
        }
        
        final String enc = this.getEncodingInternal();
        final String[] pairs = parametersString.split("&");
        
        for (final String pair : pairs) {
            final int idx = pair.indexOf('=');
            if (idx > -1) {

                try {
                    final String name = URLDecoder.decode(StringUtils.substring(pair, 0, idx), enc);
                    final String value = URLDecoder.decode(StringUtils.substring(pair, idx + 1), enc);
                    res.put(name, value);
                }
                catch (final UnsupportedEncodingException ex) {
                    //indeed
                }
            }
            else {
                try {
                    res.put(URLDecoder.decode(pair, enc), "");
                }
                catch (final UnsupportedEncodingException ex) {
                    //no way
                }
            }
        }
        
        return res;
    }
}
