package net.jadler.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
    
    private String body;
    
    private Map<String, String[]> parameters;

    
    public MultipleReadsHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        
        this.initBody();
          //initParameters must be called after initBody since it uses the request body
        this.initParameters();
    }
    

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final InputStream res = new ByteArrayInputStream(this.body.getBytes(this.getEncodingInternal()));
        
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return res.read();
            }
        };
    }
    

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(this.body));
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
    
    
    private void initBody() throws IOException {
        synchronized (this.getRequest()) {
            if (this.body == null) {
                this.body = IOUtils.toString(super.getReader());
            }
        }
    }
    
    
    private void initParameters() {
        synchronized (this.getRequest()) {
            if (this.parameters == null) {
                this.parameters = this.readParameters();
            }
        }
    }
    
    
    private String getEncodingInternal() {
        return this.getCharacterEncoding() == null ? "UTF-8" : this.getCharacterEncoding();
    }
    
    
    @SuppressWarnings("unchecked")
    private Map<String, String[]> readParameters() {
        final MultiMap params = readParametersFromQueryString(); 
        
          //shitty attempt to check whether the body contains html form data. Please refactor.
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
    

    private MultiMap readParametersFromBody() {
        return this.readParametersFromString(this.body);
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
