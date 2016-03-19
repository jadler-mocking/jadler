/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.exception.JadlerException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import net.jadler.AbstractRequestMatching;
import net.jadler.Jadler;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;


/**
 * Internal class for defining http stubs in a fluid fashion. You shouldn't create instances
 * of this class on your own, please see {@link Jadler#onRequest()}
 * for more information on creating instances of this class.
 */
public class Stubbing extends AbstractRequestMatching<RequestStubbing> implements RequestStubbing, ResponseStubbing {
    
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    Responder responder;
    final List<MutableStubResponse> stubResponses;
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
        
        this.stubResponses = new ArrayList<MutableStubResponse>();
        this.defaultHeaders = new MultiValueMap();
        this.defaultHeaders.putAll(defaultHeaders);
        this.defaultStatus = defaultStatus;
        this.defaultEncoding = defaultEncoding;        
        this.responder = null;
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
        final MutableStubResponse response = new MutableStubResponse();
        
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
    public void respondUsing(final Responder responder) {
        Validate.notNull(responder, "responder cannot be null");
        
        this.responder = responder;
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
    
    
    /**
     * {@inheritDoc}
     */ 
    @Override
    public ResponseStubbing withBody(final InputStream is) {
        try {
            final byte[] responseBody;
        
            try {
                responseBody = IOUtils.toByteArray(is);
            }
            catch (final IOException e) {
                throw new JadlerException("A problem occurred while reading the given input stream", e);
            }
        
            return this.withBody(responseBody);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }


    /**
     * {@inheritDoc}
     */ 
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
    public ResponseStubbing withDelay(long delayValue, TimeUnit delayUnit) {
        currentResponse().setDelay(java.util.concurrent.TimeUnit.MILLISECONDS.convert(delayValue, delayUnit));
        return this;
    }
    
    
    /**
     * Creates a {@link HttpStub} instance from this Stubbing instance.
     * Must be called once this stubbing has been finished.
     * @return {@link HttpStub} instance configured using values from this stubbing
     */
    public HttpStub createRule() {
        if (this.responder != null) {
            return new HttpStub(predicates, this.responder);
        }
        
        final List<StubResponse> res = new ArrayList<StubResponse>(this.stubResponses.size());
        for(final MutableStubResponse msr: this.stubResponses) {
            res.add(msr.toStubResponse());
        }
        
        return new HttpStub(predicates, new StaticResponder(res));
    }
    

    private MutableStubResponse currentResponse() {
        return stubResponses.get(stubResponses.size() - 1);
    }
}