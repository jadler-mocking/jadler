/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.Request;
import java.util.Arrays;
import java.util.Collections;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.junit.Test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;


public class HttpStubTest {
    
    private static final Responder DUMMY_RESPONSE_PRODUCER = new Responder() {
        
        @Override
        public StubResponse nextResponse(final Request request) {
            return StubResponse.EMPTY;
        }
    };
    

    @Test(expected=IllegalArgumentException.class)
    public void constructor1() {
        new HttpStub(null, DUMMY_RESPONSE_PRODUCER);
        fail("predicates cannot be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor2() {
        new HttpStub(Collections.<Matcher<? super Request>>emptyList(), null);
        fail("responder cannot be null");
    }
    
    
    @Test
    public void constructor3() {
        new HttpStub(Collections.<Matcher<? super Request>>emptyList(), DUMMY_RESPONSE_PRODUCER);
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void matches() {
        final HttpStub rule = new HttpStub(
                Arrays.<Matcher<? super Request>>asList(anything(), not(anything()), anything()), DUMMY_RESPONSE_PRODUCER);
        
          //one matcher returns false, this rule is not applicable
        assertThat(rule.matches(mock(Request.class)), is(false));
    }
    
    
    @Test
    public void describeMismatch() {
        final Request req = mock(Request.class);

        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m1 = mock(Matcher.class);
        when(m1.matches(any())).thenReturn(true);
        
        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m2 = mock(Matcher.class);
        when(m2.matches(any())).thenReturn(false);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Description desc = (Description)invocation.getArguments()[1];
                desc.appendText("mismatch_m1");
                return null;
            }
        }).when(m2).describeMismatch(eq(req), any(Description.class));
        
        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m3 = mock(Matcher.class);
        when(m3.matches(any())).thenReturn(false);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Description desc = (Description)invocation.getArguments()[1];
                desc.appendText("mismatch_m2");
                return null;
            }
        }).when(m3).describeMismatch(eq(req), any(Description.class));
        
        @SuppressWarnings("unchecked")
        final HttpStub stub =
                new HttpStub(Arrays.<Matcher<? super Request>>asList(m1, m2, m3), DUMMY_RESPONSE_PRODUCER);
        
        assertThat(stub.describeMismatch(req), is("  mismatch_m1 AND\n  mismatch_m2"));
    }
    
    
    @Test
    public void testToString() {
        
        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m1 = mock(Matcher.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Description desc = (Description) invocation.getArguments()[0];
                desc.appendText("matcher_m1");
                return null;
            }
        }).when(m1).describeTo(any(Description.class));

        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m2 = mock(Matcher.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Description desc = (Description) invocation.getArguments()[0];
                desc.appendText("matcher_m2");
                return null;
            }
        }).when(m2).describeTo(any(Description.class));
        
        final Responder responder = mock(Responder.class);
        when(responder.toString()).thenReturn("a responder");
        
        @SuppressWarnings("unchecked")
        final HttpStub stub = new HttpStub(Arrays.<Matcher<? super Request>>asList(m1, m2), responder);
        assertThat(stub.toString(), is("WHEN request (\n  matcher_m1 AND\n  matcher_m2)\nTHEN respond with a responder"));
    }
}