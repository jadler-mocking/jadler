/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.junit.Test;
import org.hamcrest.Matcher;
import java.util.Collections;
import java.util.List;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ParameterRequestMatcherTest {

    private static final String PARAMETER_NAME = "param%201";
    private static final String PARAMETER_VALUE1 = "value%201";
    private static final String PARAMETER_VALUE2 = "value2";
    private static final String NO_VALUE_PARAMETER_NAME = "param2";
    private static final String UNDEFINED_PARAMETER = "param3";
    
    @Mock
    Matcher<? super List<String>> mockMatcher;
    
    
    @Test
    public void retrieveValue() throws Exception {
        final Request req = when(mock(Request.class).getParameterValues(PARAMETER_NAME))
                .thenReturn(asList(PARAMETER_VALUE1, PARAMETER_VALUE2))
                .getMock();
        
        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).retrieveValue(req), 
                is(allOf(notNullValue(), contains(PARAMETER_VALUE1, PARAMETER_VALUE2))));
    }
    
    
    @Test
    public void retrieveValueEmpty() throws Exception {
        final Request req = when(mock(Request.class).getParameterValues(NO_VALUE_PARAMETER_NAME))
                .thenReturn(Collections.singletonList(""))
                .getMock();
        
        assertThat(requestParameter(NO_VALUE_PARAMETER_NAME, mockMatcher).retrieveValue(req), 
                is(allOf(notNullValue(), contains(""))));
    }
    
    
    @Test
    public void retrieveValueNoParameter() throws Exception {
        final Request req = when(mock(Request.class).getParameterValues(UNDEFINED_PARAMETER))
                .thenReturn(null)
                .getMock();
        
        
        assertThat(requestParameter(UNDEFINED_PARAMETER, mockMatcher).retrieveValue(req), 
                is(nullValue()));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).provideDescription(),
                is("parameter \"" + PARAMETER_NAME + "\" is"));
    }
}
