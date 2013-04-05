/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.junit.Before;
import org.junit.Test;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static net.jadler.matchers.BodyRequestMatcher.requestBody;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BodyRequestMatcherTest {

    private static final String BODY = "Sample body";
    
    private Request request;
    
    @Mock
    private Matcher<String> mockMatcher;


    @Before
    public void setUp() {
        this.request = mock(Request.class);
        when(request.getBodyAsString()).thenReturn(BODY);
    }

    
    @Test
    public void retrieveValue() throws Exception {
        assertThat(requestBody(mockMatcher).retrieveValue(request), is(BODY));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestBody(mockMatcher).provideDescription(), is("body is"));
    }
    
}
