/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anything;


public class StubRuleTest {
    
    private static final List<StubResponse> DUMB_RESPONSE = Arrays.asList(new StubResponse());
    

    @Test(expected=IllegalArgumentException.class)
    public void constructor1() {
        new StubRule(null, DUMB_RESPONSE);
        fail("predicates cannot be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor2() {
        new StubRule(Collections.<Matcher<? super Request>>emptyList(),
                Collections.<StubResponse>emptyList());
        fail("stubResponses cannot be empty");
    }
    
    
    @Test
    public void constructor3() {
        new StubRule(Collections.<Matcher<? super Request>>emptyList(), DUMB_RESPONSE);
    }
    
    
    @Test
    public void matchedBy() {
        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m1 = mock(Matcher.class);
        
        when(m1.matches(any())).thenReturn(false);
        
        final StubRule rule = new StubRule(
                Arrays.<Matcher<? super Request>>asList(anything(), m1, anything()), DUMB_RESPONSE);
        
          //one matcher returns false, this rule is not applicable
        assertThat(rule.matchedBy(mock(Request.class)), is(false));
    }
    
    
    @Test
    public void nextResponse() {
        final StubResponse r1 = new StubResponse();
        final StubResponse r2 = new StubResponse();
        
        final StubRule rule = new StubRule(Collections.<Matcher<? super Request>>emptyList(),
                Arrays.asList(r1, r2));
        
        assertThat(rule.nextResponse(), is(r1));
        assertThat(rule.nextResponse(), is(r2));
          //no other response defined, r2 must be returned again
        assertThat(rule.nextResponse(), is(r2));
    }
}
