package net.jadler.rule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anything;



public class HttpMockRuleTest {
    
    private static final List<HttpMockResponse> DUMB_RESPONSE = Arrays.asList(new HttpMockResponse());
    

    @Test(expected=IllegalArgumentException.class)
    public void constructor1() {
        new HttpMockRule(null, DUMB_RESPONSE);
        fail("matchers cannot be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor2() {
        new HttpMockRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(),
                Collections.<HttpMockResponse>emptyList());
        fail("responses cannot be empty");
    }
    
    
    @Test
    public void constructor3() {
        new HttpMockRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(), DUMB_RESPONSE);
    }
    
    
    @Test
    public void matches() {
        @SuppressWarnings("unchecked")
        final Matcher<? super HttpServletRequest> m1 = mock(Matcher.class);
        
        when(m1.matches(any())).thenReturn(false);
        
        final HttpMockRule rule = new HttpMockRule(
                Arrays.<Matcher<? super HttpServletRequest>>asList(anything(), m1, anything()), DUMB_RESPONSE);
        
          //one matcher returns false, this rule is not applicable
        assertThat(rule.matches(mock(HttpServletRequest.class)), is(false));
    }
    
    
    @Test
    public void nextResponse() {
        final HttpMockResponse r1 = new HttpMockResponse();
        r1.setTimeout(100);
        
        final HttpMockResponse r2 = new HttpMockResponse();
        r2.setTimeout(200);
        
        final HttpMockRule rule = new HttpMockRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(),
                Arrays.asList(r1, r2));
        
        assertThat(rule.nextResponse(), is(r1));
        assertThat(rule.nextResponse(), is(r2));
          //no other response defined, r2 must be returned again
        assertThat(rule.nextResponse(), is(r2));
    }
}
