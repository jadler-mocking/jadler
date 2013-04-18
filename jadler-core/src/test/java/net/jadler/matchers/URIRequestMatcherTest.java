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
import static net.jadler.matchers.URIRequestMatcher.requestURI;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class URIRequestMatcherTest {

    private static final String PATH = "/a/%C5%99";
    
    @Mock
    private Matcher<String> mockMatcher;


   @Test
    public void retrieveValue() throws Exception {
       final Request req = when(mock(Request.class).getURI()).thenReturn(new URI("http://localhost" + PATH)).getMock();
        assertThat(requestURI(mockMatcher).retrieveValue(req), is(PATH));
    }
   
   
   @Test
    public void retrieveValueRootPath() throws Exception {
       final Request req = when(mock(Request.class).getURI()).thenReturn(new URI("http://localhost/")).getMock();
        assertThat(requestURI(mockMatcher).retrieveValue(req), is("/"));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestURI(mockMatcher).provideDescription(), is("URI is"));
    }

}
