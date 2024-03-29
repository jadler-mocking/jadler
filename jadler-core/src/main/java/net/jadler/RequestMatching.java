/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.hamcrest.Matcher;

import java.util.List;


/**
 * This interface introduces methods for a fluent request matching. Classes implementing this interface usually
 * collect request predicates using these methods.
 *
 * @param <T> type (either class or interface) of the implementation. This type will be returned by all methods
 *            introduced by this interface so fluid request matching is possible.
 */
public interface RequestMatching<T extends RequestMatching<T>> {
    /**
     * Adds a request predicate to this request matching.
     *
     * @param predicate request predicate to be added to this request matching (cannot be {@code null})
     * @return this ongoing request matching
     */
    T that(Matcher<? super Request> predicate);


    /**
     * Adds a request method predicate. The request method must be equal (case insensitive) to the given value.
     *
     * @param method expected http method of the incoming http request
     * @return this ongoing request matching
     */
    T havingMethodEqualTo(String method);


    /**
     * Adds a request method predicate.
     *
     * @param predicate request method predicate (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingMethod(Matcher<? super String> predicate);


    /**
     * Adds a request body predicate. The request body must be equal to the given value. An empty request body is
     * represented by an empty string (not a {@code null} value) and therefore it can be matched by following
     * invocation: {@code havingBodyEqualTo("")}.
     *
     * @param requestBody expected body of the incoming http request
     * @return this ongoing request matching
     */
    T havingBodyEqualTo(String requestBody);


    /**
     * Adds a request body predicate. An empty request body is represented by an empty string (not a {@code null} value)
     * and therefore it can be matched for example by {@code havingBody(equalTo(""))}.
     *
     * @param predicate request body predicate (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingBody(Matcher<? super String> predicate);


    /**
     * Adds a request body predicate. The request body must be equal to the given value. An empty request body is
     * represented as an empty array (not a {@code null} value) and therefore it can be matched by
     * {@code havingRawBodyEqualTo(new byte[0])}.
     *
     * @param requestBody expected body of the incoming http request
     * @return this ongoing request matching
     */
    T havingRawBodyEqualTo(byte[] requestBody);


    /**
     * Adds a request path predicate. The request path must be equal to the given value. A root path can be matched
     * by {@code havingPathEqualTo("/")}. Please note the path value doesn't contain a query string portion and is
     * percent-encoded.
     *
     * @param path expected path of the incoming http request
     * @return this ongoing request matching
     */
    T havingPathEqualTo(String path);


    /**
     * Adds a request path predicate. A root path can be matched by {@code havingPath(equalTo("/"))} for example.
     * Please note the path value doesn't contain a query string portion and is percent-encoded.
     *
     * @param predicate request path predicate (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingPath(Matcher<? super String> predicate);


    /**
     * Adds a query string predicate. The query string must be equal to the given value. Examples:
     * <ul>
     *   <li>
     *     {@code havingQueryStringEqualTo(null)} matches a request without a query string
     *     (no <em>?</em> character in URI, {@code http://localhost/a/b}).
     *   </li>
     *   <li>
     *     {@code havingQueryStringEqualTo("")} matches a request with an empty query string
     *     ({@code http://localhost/?})
     *   </li>
     *   <li>
     *     {@code havingQueryStringEqualTo("a=b")} matches a request with the exact query string
     *     ({@code http://localhost/?a=b})
     *   </li>
     * </ul>
     * Please note the query string value is percent-encoded.
     *
     * @param queryString expected query string of the incoming http request
     * @return this ongoing request matching
     */
    T havingQueryStringEqualTo(String queryString);


    /**
     * Adds a query string predicate. Examples:
     *
     * <ul>
     *   <li>
     *     {@code havingQueryString(nullValue())} matches a request without a query string
     *     (no <em>?</em> character in URI, {@code http://localhost/a/b}).
     *   </li>
     *   <li>
     *     {@code havingQueryString(isEmptyString())} matches a request with an empty query string
     *     ({@code http://localhost/?})
     *   </li>
     *   <li>
     *     {@code havingQueryString(equalTo("a=b"))} matches a request with the exact query string
     *     ({@code http://localhost/?a=b})
     *   </li>
     * </ul>
     * <p>
     * Please note the query string is percent-encoded.
     *
     * @param predicate query string predicate (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingQueryString(Matcher<? super String> predicate);


    /**
     * <p>Adds a request parameter predicate. The given http parameter must be present
     * in the incoming request and at least one of its values must be equal to the given value. Both the name and
     * the value are percent-encoded.</p>
     *
     * <p>Parameters are <em>key=value</em> pairs contained in the query string or in the request body (the
     * {@code content-type} header must be {@code application/x-www-form-urlencoded} in this case)</p>
     *
     * @param name  case sensitive parameter name (cannot be empty)
     * @param value expected parameter value (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingParameterEqualTo(String name, String value);


    /**
     * <p>Adds a request parameter predicate. Parameters are <em>key=value</em> pairs contained in the query string
     * or in the request body (the {@code content-type} header must be {@code application/x-www-form-urlencoded}
     * in this case)</p>
     *
     * <p>Since a request parameter can have more than one value, the predicate is on a list of values. Examples:</p>
     *
     * <ul>
     *   <li>{@code havingParameter("unknown-param", nullValue())} matches a request
     *       without an {@code unknown-param}</li>
     *   <li>{@code havingParameter("existing-param", not(empty()))} matches a request which contains
     *       at least one {@code existing-param}</li>
     *   <li>{@code havingParameter("existing-param", hasItem("value"))} matches a request which contains
     *       at least one {@code existing-param} with the value {@code value}</li>
     * </ul>
     *
     * @param name      case sensitive parameter name (cannot be empty)
     * @param predicate parameter predicate (cannot be null)
     * @return this ongoing request matching
     */
    T havingParameter(String name, Matcher<? super List<String>> predicate);


    /**
     * <p>Adds a request parameter existence predicate. The given http parameter must be present
     * in the request body.</p>
     *
     * <p>Parameters are <em>key=value</em> pairs contained in the query string or in the request body (the
     * {@code content-type} header must be {@code application/x-www-form-urlencoded} in this case)</p>
     *
     * @param name case sensitive parameter name (cannot be empty)
     * @return this ongoing request matching
     */
    T havingParameter(String name);


    /**
     * <p>Adds a request parameters existence predicate. All of the given http parameters must be present
     * in the request body.</p>
     *
     * <p>Parameters are <em>key=value</em> pairs contained in the query string or in the request body (the
     * {@code content-type} header must be {@code application/x-www-form-urlencoded} in this case)</p>
     *
     * @param names case sensitive parameter names
     * @return this ongoing request matching
     */
    T havingParameters(String... names);


    /**
     * Adds a request header predicate. The given http header must be present in the request body and
     * at least one of its values must be equal to the given value.
     *
     * @param name  case insensitive header name (cannot be empty)
     * @param value expected header value (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingHeaderEqualTo(String name, String value);


    /**
     * Adds a request header predicate. Since a request header can have more than one value, the predicate is
     * on a list of values. Examples:
     * <ul>
     *   <li>{@code havingHeader("unknown-header", nullValue())} matches a request
     *       without an {@code unknown-header}</li>
     *   <li>{@code havingHeader("existing-header", not(empty()))} matches a request which contains
     *       at least one {@code existing-header}</li>
     *   <li>{@code havingHeader("existing-header", hasItem("value"))} matches a request which contains
     *       at least one {@code existing-header} with the value {@code value}</li>
     * </ul>
     *
     * @param name      case insensitive header name (cannot be empty)
     * @param predicate header predicate (cannot be {@code null})
     * @return this ongoing request matching
     */
    T havingHeader(String name, Matcher<? super List<String>> predicate);


    /**
     * Adds a request header existence predicate. The given http header must be present in the request body
     *
     * @param name case insensitive header name (cannot be empty)
     * @return this ongoing request matching
     */
    T havingHeader(final String name);


    /**
     * Adds a request headers existence predicate. All of the given http headers must be present in the request body.
     *
     * @param names case insensitive headers names
     * @return this ongoing request matching
     */
    T havingHeaders(String... names);

}
