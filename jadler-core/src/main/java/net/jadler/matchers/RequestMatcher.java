/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.exception.JadlerException;
import net.jadler.Request;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.apache.commons.lang.Validate.notNull;


/**
 * Convenient base class for all Jadler request matchers.
 * @param <T> type of the value retrieved from the given request to be matched
 */
public abstract class RequestMatcher<T> extends BaseMatcher<Request> {

    protected final Matcher<? super T> pred;


    /**
     * @param pred predicate to be applied on the value retrieved from the given request (cannot be {@code null}) 
     */
    protected RequestMatcher(final Matcher<? super T> pred) {
        notNull(pred, "pred cannot be null");
        this.pred = pred;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void describeMismatch(final Object item, final Description description) {
        final T value;
        try {
            value = this.retrieveValue((Request) item);
        } catch (Exception ex) {
            throw new JadlerException("An error occurred while retrieving a value from the http request "
                    + "for mismatch description", ex);
        }

        description.appendText("REQUIRED: ");
        description.appendDescriptionOf(this);
        description.appendText(" BUT ");
        this.pred.describeMismatch(value, description);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void describeTo(final Description description) {
        description.appendText(this.provideDescription());
        description.appendText(" ");
        description.appendDescriptionOf(this.pred);
    }


    /**
     * Checks whether the given {@link Request} object matches this matcher.
     * @param o {@link Request} object to be matched by this matcher. If this param is not of type {@link Request} this
     * method will always return {@code false}.
     * @return {@code true} if the value retrieved using {@link #retrieveValue(net.jadler.Request)} from the given
     * {@link Request} object matches the predicate registered by the {@link #RequestMatcher(org.hamcrest.Matcher)},
     * otherwise {@code false}.
     */
    @Override
    public boolean matches(final Object o) {
        if (!(o instanceof Request)) {
            return false;
        }

        T value;
        try {
            value = this.retrieveValue((Request) o);
        }
        catch (final Exception e) {
            throw new JadlerException("An error occurred while retrieving a value from the http request", e);
        }

        return this.pred.matches(value);
    }


    /**
     * Reads a value of the given request object (the value can be anything retrievable from the request
     * object: method, header, body,...).
     * @param req request object to read a value from
     * @return a value retrieved from the given request object.
     * @throws Exception when something goes wrong. This exception will be handler correctly by Jadler.
     */
    protected abstract T retrieveValue(final Request req) throws Exception;


    /**
     * <p>Provides a description of this matcher in form of a string consisting of "<em>noun</em> <em>verb</em>", where
     * noun describes the value retrieved using {@link #retrieveValue(net.jadler.Request)} and verb is usually a correct
     * form of <em>to be</em>.</p>
     * 
     * <p>If the {@link #retrieveValue(net.jadler.Request)} provided a request method, this method would return
     * <em>method is</em> string for example.</p>
     * @return matcher description
     */
    protected abstract String provideDescription();
}
