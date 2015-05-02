/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.junit.Test;
import org.hamcrest.Matcher;
import java.util.List;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;
import net.jadler.KeyValues;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static net.jadler.matchers.HeaderRequestMatcher.requestHeader;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static net.jadler.KeyValues.EMPTY;


@RunWith(MockitoJUnitRunner.class)
public class HeaderRequestMatcherTest {

    private static final String HEADER_NAME = "header1";
    private static final String HEADER_VALUE1 = "value1";
    private static final String HEADER_VALUE2 = "value2";
    private static final String NO_VALUE_HEADER = "header2";
    private static final String UNDEFINED_HEADER = "header3";
    
    @Mock
    Matcher<? super List<String>> mockMatcher;
    
    
    @Test
    public void retrieveValue() {
        final KeyValues headers = EMPTY.add(HEADER_NAME, HEADER_VALUE1).add(HEADER_NAME, HEADER_VALUE2);
        final Request req = when(mock(Request.class).getHeaders()).thenReturn(headers).getMock();
        
        assertThat(requestHeader(HEADER_NAME, mockMatcher).retrieveValue(req), 
                is(allOf(notNullValue(), contains(HEADER_VALUE1, HEADER_VALUE2))));
    }
    
    
    @Test
    public void retrieveValueEmpty() throws Exception {
        final KeyValues param = EMPTY.add(NO_VALUE_HEADER, "");
        
        final Request req = when(mock(Request.class).getHeaders()).thenReturn(param).getMock();
        assertThat(requestHeader(NO_VALUE_HEADER, mockMatcher).retrieveValue(req), 
                is(allOf(notNullValue(), contains(""))));
    }
    
    
    @Test
    public void retrieveValueNoHeader() {
        final Request req = when(mock(Request.class).getHeaders()).thenReturn(EMPTY).getMock();
        assertThat(requestHeader(UNDEFINED_HEADER, mockMatcher).retrieveValue(req), is(nullValue()));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestHeader(HEADER_NAME, mockMatcher).provideDescription(),
                is("header \"" + HEADER_NAME + "\" is"));
    }
}
