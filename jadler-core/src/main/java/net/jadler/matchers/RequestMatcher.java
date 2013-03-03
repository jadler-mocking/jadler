/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.exception.JadlerException;
import net.jadler.stubbing.Request;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;


import static org.apache.commons.lang.Validate.notNull;


/**
 * Convenient base class for all Jadler request matchers.
 * @param <T> type of the value retrieved from a request to be matched
 */
public abstract class RequestMatcher<T> extends BaseMatcher<Request> {

    protected final Matcher<? super T> pred;


    public RequestMatcher(final Matcher<? super T> pred) {
        notNull(pred, "pred cannot be null");
        this.pred = pred;
    }


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


    @Override
    public void describeTo(final Description description) {
        description.appendText(this.provideDescription());
        description.appendText(" ");
        description.appendDescriptionOf(this.pred);
    }


    @Override
    public boolean matches(final Object o) {
        if (!(o instanceof Request)) {
            return false;
        }

        T value;
        try {
            value = this.retrieveValue((Request) o);
        }
        catch (Exception e) {
            throw new JadlerException("An error occurred while retrieving a value from the http request", e);
        }

        return this.pred.matches(value);
    }


    /**
     * Reads a value of the given request object (the value can be anything readable
     * from the request object: method, header, body,...).
     * @param req request object to read a value from
     * @return a value read from the given request object.
     */
    protected abstract T retrieveValue(final Request req) throws Exception;


    protected abstract String provideDescription();
}
