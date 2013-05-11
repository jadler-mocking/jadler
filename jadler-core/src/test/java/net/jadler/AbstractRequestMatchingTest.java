/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.util.List;
import net.jadler.matchers.BodyRequestMatcher;
import net.jadler.matchers.HeaderRequestMatcher;
import net.jadler.matchers.MethodRequestMatcher;
import net.jadler.matchers.ParameterRequestMatcher;
import net.jadler.matchers.QueryStringRequestMatcher;
import net.jadler.matchers.RawBodyRequestMatcher;
import net.jadler.matchers.URIRequestMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class AbstractRequestMatchingTest {
    
    private TestRequestMatchingImpl stubbing;
    
    @Mock
    private Matcher<Object> matcher;
    
    
    @Before
    public void setUp() {
        this.stubbing = new TestRequestMatchingImpl();
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void thatWrongParam() {
        this.stubbing.that(null);
    }


    @Test
    public void that() {
        this.stubbing.that(matcher);

          //Fuck Java generics. This thing is sick.
        this.assertOneMatcher(Matchers.<Matcher<? super Request>>equalTo(matcher));
    }


    @Test
    public void havingMethodEqualTo() {
        this.stubbing.havingMethodEqualTo("GET");
        this.assertOneMatcher(is(instanceOf(MethodRequestMatcher.class)));
    }


    @Test
    public void havingMethod() {
        this.stubbing.havingMethod(matcher);
        this.assertOneMatcher(is(instanceOf(MethodRequestMatcher.class)));
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void havingBodyEqualToWrongParam() {
        this.stubbing.havingBodyEqualTo(null);
    }
    

    @Test
    public void havingBodyEqualTo() {
        this.stubbing.havingBodyEqualTo("body");
        this.assertOneMatcher(is(instanceOf(BodyRequestMatcher.class)));
    }


    @Test
    public void havingBody() {
        this.stubbing.havingBody(matcher);
        this.assertOneMatcher(is(instanceOf(BodyRequestMatcher.class)));
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void havingRawBodyEqualToWrongParam() {
        this.stubbing.havingRawBodyEqualTo(null);
    }
    
    
    @Test
    public void havingRawBodyEqualTo() {
        this.stubbing.havingRawBodyEqualTo(new byte[0]);
        this.assertOneMatcher(is(instanceOf(RawBodyRequestMatcher.class)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void havingURIMatchingWrongParam() {
        this.stubbing.havingURIEqualTo("");
    } 

    
    @Test
    public void havingURIMatching() {
        this.stubbing.havingURIEqualTo("/");
        this.assertOneMatcher(is(instanceOf(URIRequestMatcher.class)));
    }


    @Test
    public void havingURI() {
        this.stubbing.havingURI(matcher);
        this.assertOneMatcher(is(instanceOf(URIRequestMatcher.class)));
    }


    @Test
    public void havingQueryStringEqualTo() {
        this.stubbing.havingQueryStringEqualTo("a=b");
        this.assertOneMatcher(is(instanceOf(QueryStringRequestMatcher.class)));
    }


    @Test
    public void havingQueryString() {
        this.stubbing.havingQueryString(matcher);
        this.assertOneMatcher(is(instanceOf(QueryStringRequestMatcher.class)));
    }


    @Test
    public void havingParameterEqualTo() {
        this.stubbing.havingParameterEqualTo("name", "value");
        this.assertOneMatcher(is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingParameter() {
        this.stubbing.havingParameter("name", matcher);
        this.assertOneMatcher(is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingParameterWithoutValue() {
        this.stubbing.havingParameter("name");
        this.assertOneMatcher(is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingParameters() {
        this.stubbing.havingParameters("name1", "name2");
        assertThat(this.stubbing.getPredicates(), is(notNullValue()));
        assertThat(this.stubbing.getPredicates(), hasSize(2));

        assertThat(this.stubbing.getPredicates().get(0), is(instanceOf(ParameterRequestMatcher.class)));
        assertThat(this.stubbing.getPredicates().get(1), is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingHeaderEqualTo() {
        this.stubbing.havingHeaderEqualTo("name", "value");
        this.assertOneMatcher(is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void havingHeader() {
        this.stubbing.havingHeader("name", hasItem("value"));
        this.assertOneMatcher(is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void havingHeaderWithoutValue() {
        this.stubbing.havingHeader("name");
        this.assertOneMatcher(is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void havingHeaders() {
        this.stubbing.havingHeaders("name1", "name2");
        assertThat(this.stubbing.getPredicates(), is(notNullValue()));
        assertThat(this.stubbing.getPredicates(), hasSize(2));

        assertThat(this.stubbing.getPredicates().get(0), is(instanceOf(HeaderRequestMatcher.class)));
        assertThat(this.stubbing.getPredicates().get(1), is(instanceOf(HeaderRequestMatcher.class)));
    }
    
    
    private void assertOneMatcher(final Matcher<? super Matcher<? super Request>> matcher) {
        assertThat(this.stubbing.getPredicates(), is(notNullValue()));
        assertThat(this.stubbing.getPredicates(), hasSize(1));

        assertThat(this.stubbing.getPredicates().get(0), matcher);
    }

    
    private class TestRequestMatchingImpl extends AbstractRequestMatching<TestRequestMatchingImpl> {

        public List<Matcher<? super Request>> getPredicates() {
            return this.predicates;
        }
    }
}