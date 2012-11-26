/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;


import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static net.jadler.matchers.QueryStringRequestMatcher.requestQueryString;
import static org.hamcrest.Matchers.*;


@RunWith(MockitoJUnitRunner.class)
public class QueryStringRequestMatcherTest {

    private static final String QUERY = "id%5Bgt%5D=1800004238&dir=asc&limit=10";

    private MockHttpServletRequest request;
    
    @Mock
    private Matcher<String> mockMatcher;


    @Before
    public void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
    }


   @Test
    public void retrieveValue() throws Exception {
        request.setQueryString(QUERY);
        assertThat(requestQueryString(mockMatcher).retrieveValue(request), is(QUERY));
    }
   
   
    @Test
    public void retrieveValueNoQueryString() throws Exception {
        assertThat(requestQueryString(mockMatcher).retrieveValue(request), is(nullValue()));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestQueryString(mockMatcher).provideDescription(), is("query string is"));
    }

}
