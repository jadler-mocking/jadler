/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.apache.commons.lang.Validate;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static net.jadler.matchers.BodyRequestMatcher.requestBody;
import static net.jadler.matchers.HeaderRequestMatcher.requestHeader;
import static net.jadler.matchers.MethodRequestMatcher.requestMethod;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;
import static net.jadler.matchers.PathRequestMatcher.requestPath;
import static net.jadler.matchers.QueryStringRequestMatcher.requestQueryString;
import static net.jadler.matchers.RawBodyRequestMatcher.requestRawBody;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;


/**
 * A base implementation of the {@link RequestMatching} interface. Collects all request predicates to a protected
 * collection available in extending classes.
 * 
 * @param <T> type (either class or interface) of the class extending this abstract class. This type will be returned
 * by all methods introduced in {@link RequestMatching} implemented by this class so fluid request matching
 * is possible.
 */
public abstract class AbstractRequestMatching<T extends RequestMatching<T>> implements RequestMatching<T> {
    
    protected final List<Matcher<? super Request>> predicates;

    
    protected AbstractRequestMatching() {
        this.predicates = new ArrayList<Matcher<? super Request>>();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T that(final Matcher<? super Request> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        this.predicates.add(predicate);
        return (T)this;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T havingMethodEqualTo(final String method) {
        Validate.notEmpty(method, "method cannot be empty");
        
        return havingMethod(equalToIgnoringCase(method));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingMethod(final Matcher<? super String> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        return that(requestMethod(predicate));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingBodyEqualTo(final String requestBody) {
        Validate.notNull(requestBody, "requestBody cannot be null, use an empty string instead");
        
        return havingBody(equalTo(requestBody));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingBody(final Matcher<? super String> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        return that(requestBody(predicate));
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T havingRawBodyEqualTo(final byte[] requestBody) {
        Validate.notNull(requestBody, "requestBody cannot be null, use an empty array instead");
        
        return that(requestRawBody(equalTo(requestBody)));
    }
    
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingPathEqualTo(final String path) {
        Validate.notEmpty(path, "path cannot be empty");
        
        return havingPath(equalTo(path));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingPath(final Matcher<? super String> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        return that(requestPath(predicate));
    }
    
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingQueryStringEqualTo(final String queryString) {
        return havingQueryString(equalTo(queryString));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingQueryString(final Matcher<? super String> predicate) {
        Validate.notNull(predicate, "predicate cannot be null");
        
        return that(requestQueryString(predicate));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingParameterEqualTo(final String name, final String value) {
        Validate.notNull(value, "value cannot be null");
        
        return havingParameter(name, hasItem(value));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingParameter(final String name, final Matcher<? super List<String>> predicate) {
        Validate.notEmpty(name, "name cannot be empty");
        Validate.notNull(predicate, "predicate cannot be null");
        
        return that(requestParameter(name, predicate));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingParameter(final String name) {
        return havingParameter(name, notNullValue());
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    @SuppressWarnings("unchecked")
    public T havingParameters(final String... names) {
        
        for (final String name: names) {
            havingParameter(name);
        }
        
        return (T)this;
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingHeaderEqualTo(final String name, final String value) {
        Validate.notNull(value, "value cannot be null");
        
        return havingHeader(name, hasItem(value));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingHeader(final String name, final Matcher<? super List<String>> predicate) {
        Validate.notEmpty(name, "name cannot be empty");
        Validate.notNull(predicate, "predicate cannot be null");
        
        return that(requestHeader(name, predicate));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public T havingHeader(final String name) {
        return havingHeader(name, notNullValue());
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    @SuppressWarnings("unchecked")
    public T havingHeaders(final String... names) {
        Validate.notNull(names, "names cannot be null");
        
        for (final String name: names) {
            havingHeader(name);
        }

        return (T)this;
    }
}
