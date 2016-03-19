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

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static net.jadler.matchers.RawBodyRequestMatcher.requestRawBody;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RawBodyRequestMatcherTest {

    private static final String BODY = "Sample body";
    
    @Mock
    private Matcher<byte[]> mockMatcher;

    
    @Test
    public void retrieveValue() throws Exception {
        final Request req = 
                when(mock(Request.class).getBodyAsStream())
                .thenReturn(new ByteArrayInputStream(BODY.getBytes()))
                .getMock();
        
        assertThat(requestRawBody(mockMatcher).retrieveValue(req), is(BODY.getBytes()));
    }
    
    
    @Test
    public void retrieveValueEmptyBody() throws Exception {
        final Request req = 
                when(mock(Request.class).getBodyAsStream())
                .thenReturn(new ByteArrayInputStream(new byte[0]))
                .getMock();
        
        assertThat(requestRawBody(mockMatcher).retrieveValue(req), is(new byte[0]));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestRawBody(mockMatcher).provideDescription(), is("raw body is"));
    }
    
}
