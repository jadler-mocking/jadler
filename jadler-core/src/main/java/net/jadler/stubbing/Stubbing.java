/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.exception.JadlerException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import net.jadler.Jadler;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;

import static org.hamcrest.Matchers.*;
import static net.jadler.matchers.QueryStringRequestMatcher.requestQueryString;
import static net.jadler.matchers.MethodRequestMatcher.requestMethod;
import static net.jadler.matchers.BodyRequestMatcher.requestBody;
import static net.jadler.matchers.RawBodyRequestMatcher.requestRawBody;
import static net.jadler.matchers.URIRequestMatcher.requestURI;
import static net.jadler.matchers.HeaderRequestMatcher.requestHeader;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;


/**
 * Internal class for defining http stubs in a fluid fashion. You shouldn't create instances
 * of this class on your own, please see {@link Jadler#onRequest()}
 * for more information on creating instances of this class.
 */
public class Stubbing implements RequestStubbing, ResponseStubbing {
    
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final List<Matcher<? super Request>> predicates;
    private final List<StubResponse> stubResponses;
    private final MultiMap defaultHeaders;
    private final int defaultStatus;
    private final Charset defaultEncoding;
    
    
    /**
     * @param defaultHeaders default headers to be present in every http stub response
     * @param defaultStatus default http status of every http stub response 
     * (can be overridden in the particular stub)
     * @param defaultEncoding default encoding of every stub response body (can be overridden in the particular stub)
     */
    @SuppressWarnings("unchecked")
    Stubbing(final Charset defaultEncoding, final int defaultStatus, final MultiMap defaultHeaders) {
        
        this.predicates = new ArrayList<Matcher<? super Request>>();
        this.stubResponses = new ArrayList<StubResponse>();
        this.defaultHeaders = new MultiValueMap();
        this.defaultHeaders.putAll(defaultHeaders);
        this.defaultStatus = defaultStatus;
        this.defaultEncoding = defaultEncoding;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestStubbing that(final Matcher<? super Request> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        this.predicates.add(predicate);
        return this;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestStubbing havingMethodEqualTo(final String method) {
        return havingMethod(equalToIgnoringCase(method));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingMethod(final Matcher<? super String> predicate) {
        return that(requestMethod(predicate));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingBodyEqualTo(final String requestBody) {
        return havingBody(equalTo(requestBody));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingBody(final Matcher<? super String> predicate) {
        return that(requestBody(predicate));
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestStubbing havingRawBodyEqualTo(byte[] requestBody) {
        return that(requestRawBody(equalTo(requestBody)));
    }
    
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingURIEqualTo(final String uri) {
        Validate.isTrue(!uri.contains("?"), "URI must not contain query parameters.");
        return havingURI(equalTo(uri));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingURI(final Matcher<? super String> predicate) {
        return that(requestURI(predicate));
    }
    
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingQueryStringEqualTo(final String queryString) {
        return havingQueryString(equalTo(queryString));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingQueryString(final Matcher<? super String> predicate) {
        return that(requestQueryString(predicate));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameterEqualTo(final String name, final String value) {
        return havingParameter(name, hasItem(value));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameter(final String name, final Matcher<? super List<String>> predicate) {
        return that(requestParameter(name, predicate));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameter(final String name) {
        return havingParameter(name, notNullValue());
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameters(String... names) {
        
        for (final String name: names) {
            havingParameter(name);
        }
        
        return this;
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeaderEqualTo(final String name, final String value) {
        return havingHeader(name, hasItem(value));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeader(final String name, final Matcher<? super List<String>> predicate) {
        return that(requestHeader(name, predicate));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeader(final String name) {
        return havingHeader(name, notNullValue());
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeaders(String... names) {
        for (final String name: names) {
            havingHeader(name);
        }

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing respond() {
        return this.thenRespond();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing thenRespond() {
        final StubResponse response = new StubResponse();
        
        response.addHeaders(defaultHeaders);
        response.setStatus(defaultStatus);
        response.setEncoding(defaultEncoding);
        response.setBody("");
        
        stubResponses.add(response);
        return this;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withContentType(final String contentType) {
        currentResponse().setHeaderCaseInsensitive(CONTENT_TYPE_HEADER, contentType);
        return this;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withEncoding(final Charset encoding) {
        currentResponse().setEncoding(encoding);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withBody(final String responseBody) {
        currentResponse().setBody(responseBody);
        return this;
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public ResponseStubbing withBody(final Reader reader) {
        try {
            final String responseBody;
            
            try {
                responseBody = IOUtils.toString(reader);
            } catch (final IOException ex) {
                throw new JadlerException("An error ocurred while reading the response body from "
                        + "the given Reader instance.", ex);
            }
            
            return this.withBody(responseBody);
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }
    
    
    @Override
    public ResponseStubbing withBody(final InputStream is) {
        try {
            final byte[] responseBody;
        
            try {
                responseBody = IOUtils.toByteArray(is);
            }
            catch (final IOException e) {
                throw new JadlerException("ERROR");
            }
        
            return this.withBody(responseBody);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    
    @Override
    public ResponseStubbing withBody(final byte[] responseBody) {
        currentResponse().setBody(responseBody);
        return this;
    }

    
    /**
     * {@inheritDoc}
     */ 
    @Override
    public ResponseStubbing withHeader(final String name, final String value) {
        currentResponse().addHeader(name, value);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withStatus(final int status) {
        currentResponse().setStatus(status);
        return this;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withTimeout(long timeout, TimeUnit timeUnit) {
        currentResponse().setTimeout(java.util.concurrent.TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
        return this;
    }
    
    
    /**
     * Creates a {@link StubRule} instance from this Stubbing instance.
     * Must be called once this stubbing has been finished.
     * @return {@link StubRule} instance configured using values from this stubbing
     */
    public StubRule createRule() {
        return new StubRule(predicates, stubResponses);
    }

    
    /**
     * package private getter for testing purposes
     * @return all registered predicates
     */
    List<Matcher<? super Request>> getPredicates() {
        return new ArrayList<Matcher<? super Request>>(this.predicates);
    }
    
    
    /**
     * package private getter for testing purposes
     * @return all defined stub responses
     */
    List<StubResponse> getStubResponses() {
        return new ArrayList<StubResponse>(this.stubResponses);
    }
    

    private StubResponse currentResponse() {
        return stubResponses.get(stubResponses.size() - 1);
    }
}