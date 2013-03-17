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
import static net.jadler.matchers.MethodRequestMatcher.requestMethod;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MethodRequestMatcherTest {
    
    private static final String METHOD = "GET";

    private Request request;
    
    @Mock
    Matcher<String> mockMatcher;

    @Before
    public void setUp() throws Exception {
        this.request = mock(Request.class);
        when(request.getMethod()).thenReturn(METHOD);
    }

    @Test
    public void retrieveValue() throws Exception {
        assertThat(requestMethod(mockMatcher).retrieveValue(request), is(METHOD));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestMethod(mockMatcher).provideDescription(), is("method is"));
    }
}
