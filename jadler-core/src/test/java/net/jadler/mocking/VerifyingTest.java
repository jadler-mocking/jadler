/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;

import net.jadler.Request;
import net.jadler.RequestManager;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;


@RunWith(MockitoJUnitRunner.class)
public class VerifyingTest {

    @Mock
    public RequestManager requestManager;


    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        doThrow(new VerificationException(""))
                .when(this.requestManager)
                .evaluateVerification(Mockito.<Matcher<? super Request>>anyCollection(), any(Matcher.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArg() {
        new Verifying(null);
        fail("requestManager cannot be null");
    }


    @Test
    public void constructor() {
        new Verifying(mock(RequestManager.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void receivedTimesMatcherIllegalArg() {
        new Verifying(requestManager).receivedTimes(null);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void receivedTimesMatcher_positive() {

        final Matcher<Request> m1 = mock(Matcher.class);
        final Matcher<Request> m2 = mock(Matcher.class);

        final Verifying v = new Verifying(requestManager).that(m1).that(m2);

        final Matcher<Integer> pred = mock(Matcher.class);
        final Collection<Matcher<? super Request>> matchers = Arrays.<Matcher<? super Request>>asList(m1, m2);

        doNothing().when(this.requestManager).evaluateVerification(eq(matchers), eq(pred));

        v.receivedTimes(pred);
    }


    @Test(expected = VerificationException.class)
    @SuppressWarnings("unchecked")
    public void receivedTimesMatcher_negative() {
        new Verifying(requestManager).receivedTimes(mock(Matcher.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void receivedTimesIntInvalidArg() {
        new Verifying(requestManager).receivedTimes(-1);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void receivedTimesInt() {

        final Matcher<Request> m1 = mock(Matcher.class);
        final Matcher<Request> m2 = mock(Matcher.class);
        final Collection<Matcher<? super Request>> matchers = Arrays.<Matcher<? super Request>>asList(m1, m2);

        final Verifying v = new Verifying(requestManager).that(m1).that(m2);

        doNothing().when(this.requestManager).evaluateVerification(eq(matchers), any(IsEqual.class));

        v.receivedTimes(42);
    }


    @Test(expected = VerificationException.class)
    public void receivedTimesInt_negative() {
        new Verifying(requestManager).receivedTimes(42);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void receivedOnce() {
        final Matcher<Request> m1 = mock(Matcher.class);
        final Matcher<Request> m2 = mock(Matcher.class);
        final Collection<Matcher<? super Request>> matchers = Arrays.<Matcher<? super Request>>asList(m1, m2);

        final Verifying v = new Verifying(requestManager).that(m1).that(m2);

        doNothing().when(this.requestManager).evaluateVerification(eq(matchers), any(IsEqual.class));

        v.receivedOnce();
    }


    @Test(expected = VerificationException.class)
    public void receivedOnce_negative() {
        new Verifying(requestManager).receivedOnce();
    }


    @Test
    @SuppressWarnings("unchecked")
    public void receivedNever() {
        final Matcher<Request> m1 = mock(Matcher.class);
        final Matcher<Request> m2 = mock(Matcher.class);
        final Collection<Matcher<? super Request>> matchers = Arrays.<Matcher<? super Request>>asList(m1, m2);

        final Verifying v = new Verifying(requestManager).that(m1).that(m2);

        doNothing().when(this.requestManager).evaluateVerification(eq(matchers), any(IsEqual.class));

        v.receivedNever();
    }


    @Test(expected = VerificationException.class)
    public void receivedNever_negative() {
        new Verifying(requestManager).receivedNever();
    }
}
