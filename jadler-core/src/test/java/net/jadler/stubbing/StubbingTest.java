package net.jadler.stubbing;

import net.jadler.matchers.BodyRequestMatcher;
import net.jadler.matchers.MethodRequestMatcher;
import net.jadler.matchers.QueryStringRequestMatcher;
import net.jadler.matchers.URIRequestMatcher;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.map.MultiValueMap;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


@RunWith(MockitoJUnitRunner.class)
public class StubbingTest {
    
    private Stubbing stubbing;
    
    @Mock
    private Matcher<Object> m;
    
    
    @Before
    public void setUp() {
        this.stubbing = new Stubbing(new MultiValueMap(), 200);
    }
    
    
    @Test
    public void that() {               
        this.stubbing.that(m);
        
          //Fuck Java generics. This thing is sick.
        this.assertOneMatcher(Matchers.<Matcher<? super HttpServletRequest>>equalTo(m));
    }
    
    
    @Test
    public void havingMethodEqualTo() {
        this.stubbing.havingMethodEqualTo("GET");
        this.assertOneMatcher(is(instanceOf(MethodRequestMatcher.class)));
    }
    
    
    @Test
    public void havingMethod() {
        this.stubbing.havingMethod(m);
        this.assertOneMatcher(is(instanceOf(MethodRequestMatcher.class)));
    }
    
    
    @Test
    public void havingBodyEqualTo() {
        this.stubbing.havingBodyEqualTo("body");
        this.assertOneMatcher(is(instanceOf(BodyRequestMatcher.class)));
    }
    
    
    @Test
    public void havingBody() {
        this.stubbing.havingBody(m);
        this.assertOneMatcher(is(instanceOf(BodyRequestMatcher.class)));
    }
    
    
    @Test
    public void havingURIMatching() {
        this.stubbing.havingURIEqualTo("/**");
        this.assertOneMatcher(is(instanceOf(URIRequestMatcher.class)));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void havingURIMatchingWrongValue() {
        this.stubbing.havingURIEqualTo("/a/b?param");
    }
    
    
    @Test
    public void havingURI() {
        this.stubbing.havingURI(m);
        this.assertOneMatcher(is(instanceOf(URIRequestMatcher.class)));
    }
    
    
    @Test
    public void havingQueryStringEqualTo() {
        this.stubbing.havingQueryStringEqualTo("a=b");
        this.assertOneMatcher(is(instanceOf(QueryStringRequestMatcher.class)));
    }
    
    
    @Test
    public void havingQueryString() {
        this.stubbing.havingQueryString(m);
        this.assertOneMatcher(is(instanceOf(QueryStringRequestMatcher.class)));
    }
    
    
    private void assertOneMatcher(final Matcher<? super Matcher<? super HttpServletRequest>> m) {
        assertThat(this.stubbing.getMatchers(), is(notNullValue()));
        assertThat(this.stubbing.getMatchers(), is(not(empty())));
        assertThat(this.stubbing.getMatchers(), hasSize(1));
        
        assertThat(this.stubbing.getMatchers().get(0), m);
    }
}