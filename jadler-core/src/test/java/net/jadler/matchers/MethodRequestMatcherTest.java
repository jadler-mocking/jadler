/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static net.jadler.matchers.MethodRequestMatcher.requestMethod;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MethodRequestMatcherTest {

    private static final String METHOD = "GET";
    @Mock
    Matcher<String> mockMatcher;
    private Request request;

    @Before
    public void setUp() throws Exception {
        this.request = mock(Request.class);
        when(request.getMethod()).thenReturn(METHOD);
    }

    @Test
    public void retrieveValue() {
        assertThat(requestMethod(mockMatcher).retrieveValue(request), is(METHOD));
    }


    @Test
    public void provideDescription() {
        assertThat(requestMethod(mockMatcher).provideDescription(), is("method is"));
    }
}
