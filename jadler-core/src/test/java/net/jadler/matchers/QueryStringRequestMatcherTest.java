/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;


import java.net.URI;
import net.jadler.Request;
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

    private static final String QUERY = "name=%C5%99eho%C5%99";
    
    @Mock
    private Matcher<String> mockMatcher;


    @Test
     public void retrieveValue() throws Exception {
         final Request req = when(mock(Request.class).getURI())
                 .thenReturn(new URI("http://localhost?" + QUERY)).getMock();
         assertThat(requestQueryString(mockMatcher).retrieveValue(req), is(QUERY));
     }


     @Test
     public void retrieveValueNoQueryString() throws Exception {
         final Request req = when(mock(Request.class).getURI()).thenReturn(new URI("http://localhost")).getMock();
         assertThat(requestQueryString(mockMatcher).retrieveValue(req), is(nullValue()));
     }


     @Test
     public void retrieveValueEmptyQueryString() throws Exception {
         final Request req = when(mock(Request.class).getURI()).thenReturn(new URI("http://localhost/?")).getMock();
         assertThat(requestQueryString(mockMatcher).retrieveValue(req), isEmptyString());
     }


     @Test
     public void provideDescription() {
         assertThat(requestQueryString(mockMatcher).provideDescription(), is("query string is"));
     }
}
