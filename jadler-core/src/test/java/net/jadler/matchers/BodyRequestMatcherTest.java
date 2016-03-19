/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.junit.Test;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static net.jadler.matchers.BodyRequestMatcher.requestBody;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BodyRequestMatcherTest {

    private static final String BODY = "Sample body";
    
    @Mock
    private Matcher<String> mockMatcher;

    
    @Test
    public void retrieveValue() throws Exception {
        final Request req = when(mock(Request.class).getBodyAsString()).thenReturn(BODY).getMock();
        when(req.getBodyAsString()).thenReturn(BODY);
        assertThat(requestBody(mockMatcher).retrieveValue(req), is(BODY));
    }
    
    
    @Test
    public void retrieveValueEmptyBody() throws Exception {
        final Request req = when(mock(Request.class).getBodyAsString()).thenReturn("").getMock();
        when(req.getBodyAsString()).thenReturn("");
        assertThat(requestBody(mockMatcher).retrieveValue(req), is(""));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestBody(mockMatcher).provideDescription(), is("body is"));
    }
    
}
