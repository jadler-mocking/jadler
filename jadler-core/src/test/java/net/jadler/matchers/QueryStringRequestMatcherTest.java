/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;


import net.jadler.Request;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static net.jadler.matchers.QueryStringRequestMatcher.requestQueryString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
        assertThat(requestQueryString(mockMatcher).retrieveValue(req), is(emptyString()));
    }


    @Test
    public void provideDescription() {
        assertThat(requestQueryString(mockMatcher).provideDescription(), is("query string is"));
    }
}
