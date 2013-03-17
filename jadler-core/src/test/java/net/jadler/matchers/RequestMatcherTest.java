/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.exception.JadlerException;

import net.jadler.stubbing.Request;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;




@RunWith(MockitoJUnitRunner.class)
public class RequestMatcherTest {

    private static final String MATCHER_DESC = "retrieved value is";
    private static final String INNER_MATCHER_DESC = "matched by a mock matcher";
    private static final String INNER_MATCHER_MISMATCH_DESC = "was not matched";
    private static final Object RETRIEVED_VALUE = new Object();
    
    @Mock
    private Matcher<Object> mockInnerMatcher;
    
    
    @Before
    public void setUp() {

          //define what to do when describeTo is called on the mockMatcher instance
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) {
                final Description arg = (Description) invocation.getArguments()[0];
                arg.appendText(INNER_MATCHER_DESC);
                return null;
            }
        }).when(this.mockInnerMatcher).describeTo(any(Description.class));
        
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) {
                final Description arg = (Description) invocation.getArguments()[1];
                arg.appendText(INNER_MATCHER_MISMATCH_DESC);
                return null;
            }
        }).when(this.mockInnerMatcher).describeMismatch(eq(RETRIEVED_VALUE), any(Description.class));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor() {
        new TestRequestMatcher(mockInnerMatcher);
        new TestRequestMatcher(null);
        fail("inner matcher cannot be null");
    }


    @Test
    public void testDescribeMismatch() {
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher);
        final Request req = mock(Request.class);
        final Description desc = new StringDescription();
        
        rm.describeMismatch(req, desc);
        assertThat(desc.toString(), is(
                "REQUIRED: " +
                MATCHER_DESC +
                " " +
                INNER_MATCHER_DESC +
                " BUT " +
                INNER_MATCHER_MISMATCH_DESC));
    }


    @Test
    public void describeTo() {
        
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher);
        final Description desc = new StringDescription();
        rm.describeTo(desc);
        
        assertThat(desc.toString(), is(MATCHER_DESC + " " + INNER_MATCHER_DESC));
    }
    
    
    @Test
    public void matchesObject() {
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher);
          //it's not a HttpServletRequest instance, must be false
        assertThat(rm.matches(new Object()), is(false));
    }
    
    
    @Test
    public void matchesNull() {
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher);
          //it's not a HttpServletRequest instance, must be false
        assertThat(rm.matches(null), is(false));
    }
    
    
    @Test(expected=JadlerException.class)
    public void matchesException() {
        final Request request = mock(Request.class);
        
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher) {

            @Override
            public Object retrieveValue(Request req) throws Exception {
                throw new Exception();
            }
        };
        
          //since the value retrieval ended by throwing an exception, the result must be false
        rm.matches(request);
    }
    

    @Test
    public void matches() {
        final Request request = mock(Request.class);
        
        when(this.mockInnerMatcher.matches(eq(RETRIEVED_VALUE))).thenReturn(true);
        
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher);
        
          //since the inner matcher returns true, the result must be true as well
        assertThat(rm.matches(request), is(true));
        
          //verify the inner matcher was given the value retrieved from the http request
        verify(this.mockInnerMatcher, times(1)).matches(eq(RETRIEVED_VALUE));
        verifyNoMoreInteractions(this.mockInnerMatcher);
    }
    
    
    @Test
    public void notMatches() {
        final Request request = mock(Request.class);
        
        when(this.mockInnerMatcher.matches(eq(RETRIEVED_VALUE))).thenReturn(false);
        
        final Matcher<?> rm = new TestRequestMatcher(mockInnerMatcher);
        
          //since the inner matcher returns false, the result must be true as well
        assertThat(rm.matches(request), is(false));
        
          //verify the inner matcher was given the value retrieved from the http request
        verify(this.mockInnerMatcher, times(1)).matches(eq(RETRIEVED_VALUE));
        verifyNoMoreInteractions(this.mockInnerMatcher);
    }


    /**
     * Non-abstract extension of the tested RequestMatcher abstract class
     */
    private class TestRequestMatcher extends RequestMatcher<Object> {

        public TestRequestMatcher(final Matcher<Object> innerMatcher) {
            super(innerMatcher);
        }
        
        @Override
        public Object retrieveValue(final Request req) throws Exception {
            return RETRIEVED_VALUE;
        }
      
        @Override
        public String provideDescription() {
            return MATCHER_DESC;
        }
    }
}
