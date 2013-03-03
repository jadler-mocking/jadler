/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Request abstraction. It insulates the code from an implementation and serves as immutable copy to keep
 * values after the original instance has been recycled.
 */
public class Request {

    private final String method;

    private final URI requestUri;

    private final byte[] body;

    private final Map<String, List<String>> parameters;

    private final Map<String, List<String>> headers;

    private final String encoding;

    public Request(String method, URI requestUri, Map<String, List<String>> headers, InputStream body, String encoding) throws IOException {
        this.method = method;
        this.requestUri = requestUri;
        this.encoding = encoding != null ? encoding : "ISO-8859-1"; //HTTP default
        this.body = body != null ? IOUtils.toByteArray(body) : null;
        this.headers = copyHeaders(headers);
        this.parameters = readParameters();
    }

    /**
     * Makes immutable copy of headers. All header names are converted to lower-case.
     * @param headers
     * @return
     */
    private Map<String, List<String>> copyHeaders(Map<String, List<String>> headers) {
        if (headers==null) {
            return emptyMap();
        }
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for(Map.Entry<String, List<String>> header: headers.entrySet()) {
            result.put(header.getKey().toLowerCase(), unmodifiableList(new ArrayList<String>(header.getValue())));
        }
        return unmodifiableMap(result);
    }

    /**
     * Returns HTTP method
     * @return
     */
    public String getMethod() {
        return method;
    }

    /**
     * Uri of the request. For example http://localhost:8080/test/file?a=4
     * @return
     */
    public URI getUri() {
        return requestUri;
    }

    /**
     * Query part of the string. For example a=4&b=hallo.
     * @return
     */
    public String getQueryString() {
        return requestUri!=null?requestUri.getQuery():null;
    }

    /**
     * Returns list of headr values.
     * @param name
     * @return
     */
    public List<String> getHeaders(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * Returns first value for given header or null.
     * @param name
     * @return
     */
    public String getFirstHeader(String name) {
        List<String> headerValues = getHeaders(name);
        return headerValues!=null&&!headerValues.isEmpty()?headerValues.get(0):null;
    }

    /**
     * Returns body content as {@InputStream}. This method can be called multiple times always returning valid, readable
     * stream.
     * @return
     */
    public InputStream getBody() {
        return body!=null?new ByteArrayInputStream(body):null;
    }

    public String getBodyAsString() {
        return body!=null?new String(body, Charset.forName(getEncodingInternal())):null;
    }

    /**
     * Returns list of parameter values.
     * @param name
     * @return
     */
    public List<String> getParameters(String name) {
        return parameters.get(name);
    }

    /**
     * Returns first parameter with given name or null.
     * @param name
     * @return
     */
    public String getFirstParameter(String name) {
        List<String> parameterValues = getParameters(name);
        return parameterValues!=null&&!parameterValues.isEmpty()?parameterValues.get(0):null;
    }

    private String getEncodingInternal() {
        return encoding;
    }

    private String getContentType() {
        return getFirstHeader("content-type");
    }

    private Map<String, List<String>> readParameters() throws IOException {
           final MultiMap params = readParametersFromQueryString();

             //TODO: shitty attempt to check whether the body contains html form data. Please refactor.
           if (!StringUtils.isBlank(this.getContentType()) &&
               this.getContentType().contains("application/x-www-form-urlencoded")) {

               if ("POST".equalsIgnoreCase(this.getMethod()) || "PUT".equalsIgnoreCase(this.getMethod())) {
                   params.putAll(this.readParametersFromBody());
               }
           }

           final Map<String, List<String>> res = new HashMap<String, List<String>>();
           for(final Object o: params.entrySet()) {
               final Map.Entry<String, List<String>> e = (Map.Entry) o;
               res.put(e.getKey(), unmodifiableList(e.getValue()));
           }

           return unmodifiableMap(res);
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

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", requestUri=" + requestUri +
                ", parameters=" + parameters +
                ", headers=" + headers +
                '}';
    }
}
