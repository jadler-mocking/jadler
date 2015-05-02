/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;


import net.jadler.Request;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static net.jadler.matchers.PathRequestMatcher.requestPath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PathRequestMatcherTest {

    private static final String PATH = "/a/%C5%99";

    @Mock
    private Matcher<String> mockMatcher;


    @Test
    public void retrieveValue() throws Exception {
        final Request req = when(mock(Request.class).getURI()).thenReturn(new URI("http://localhost" + PATH)).getMock();
        assertThat(requestPath(mockMatcher).retrieveValue(req), is(PATH));
    }


    @Test
    public void retrieveValueRootPath() throws Exception {
        final Request req = when(mock(Request.class).getURI()).thenReturn(new URI("http://localhost/")).getMock();
        assertThat(requestPath(mockMatcher).retrieveValue(req), is("/"));
    }


    @Test
    public void provideDescription() {
        assertThat(requestPath(mockMatcher).provideDescription(), is("Path is"));
    }

}
