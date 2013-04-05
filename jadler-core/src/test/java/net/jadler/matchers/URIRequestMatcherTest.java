/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;


import java.net.URI;
import net.jadler.Request;
import org.junit.Before;
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

    private Request request;
    
    @Mock
    private Matcher<String> mockMatcher;


    @Before
    public void setUp() throws Exception {
        this.request = mock(Request.class);
    }


   @Test
    public void retrieveValue() throws Exception {
        when(request.getURI()).thenReturn(new URI("http://localhost" + PATH));
        assertThat(requestURI(mockMatcher).retrieveValue(request), is(PATH));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestURI(mockMatcher).provideDescription(), is("URI is"));
    }

}
