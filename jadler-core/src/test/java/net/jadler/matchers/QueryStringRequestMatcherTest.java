/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;


import net.jadler.stubbing.Request;
import org.junit.Before;
import org.junit.Test;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static net.jadler.matchers.QueryStringRequestMatcher.requestQueryString;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class QueryStringRequestMatcherTest {

    private static final String QUERY = "id%5Bgt%5D=1800004238&dir=asc&limit=10";

    private Request request;
    
    @Mock
    private Matcher<String> mockMatcher;


    @Before
    public void setUp() throws Exception {
        this.request = mock(Request.class);
    }


   @Test
    public void retrieveValue() throws Exception {
        when(request.getQueryString()).thenReturn(QUERY);
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
