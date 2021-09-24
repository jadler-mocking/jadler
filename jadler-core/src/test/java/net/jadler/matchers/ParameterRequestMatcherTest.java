/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.junit.Test;
import org.hamcrest.Matcher;
import java.util.List;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mock;
import net.jadler.KeyValues;

import static org.junit.Assert.assertThat;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static net.jadler.KeyValues.EMPTY;


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
        final KeyValues params = EMPTY.add(PARAMETER_NAME, PARAMETER_VALUE1).add(PARAMETER_NAME, PARAMETER_VALUE2);
        final Request req = when(mock(Request.class).getParameters()).thenReturn(params).getMock();
        
        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).retrieveValue(req), 
                is(allOf(notNullValue(), contains(PARAMETER_VALUE1, PARAMETER_VALUE2))));
    }
    

    @Test
    public void retrieveValueEmpty() throws Exception {
        final KeyValues param = EMPTY.add(NO_VALUE_PARAMETER_NAME, "");
        
        final Request req = when(mock(Request.class).getParameters()).thenReturn(param).getMock();
        assertThat(requestParameter(NO_VALUE_PARAMETER_NAME, mockMatcher).retrieveValue(req), 
                is(allOf(notNullValue(), contains(""))));
    }
    
    
    @Test
    public void retrieveValueNoParameter() throws Exception {
        final Request req = when(mock(Request.class).getParameters()).thenReturn(EMPTY).getMock();
        assertThat(requestParameter(UNDEFINED_PARAMETER, mockMatcher).retrieveValue(req), 
                is(nullValue()));   
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).provideDescription(),
                is("parameter \"" + PARAMETER_NAME + "\" is"));
    }
}
