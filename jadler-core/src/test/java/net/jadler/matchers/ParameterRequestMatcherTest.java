/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.hamcrest.Matcher;
import java.util.List;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;

import static org.junit.Assert.assertThat;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;
import static org.hamcrest.Matchers.*;



@RunWith(MockitoJUnitRunner.class)
public class ParameterRequestMatcherTest {

    private static final String PARAMETER_NAME = "param1";
    private static final String PARAMETER_VALUE1 = "value1";
    private static final String PARAMETER_VALUE2 = "value2";
    private static final String NO_VALUE_PARAMETER_NAME = "param2";
    private static final String UNDEFINED_PARAMETER = "param3";
    
    private MockHttpServletRequest request;
    
    @Mock
    Matcher<? super List<String>> mockMatcher;


    @Before
    public void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.request.addParameter(PARAMETER_NAME, new String[] {PARAMETER_VALUE1, PARAMETER_VALUE2});
        this.request.addParameter(NO_VALUE_PARAMETER_NAME, new String[] {});
    }
    
    
    @Test
    public void retrieveValue() throws Exception {
        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).retrieveValue(request), 
                is(allOf(notNullValue(), contains(PARAMETER_VALUE1, PARAMETER_VALUE2))));
        
        assertThat(requestParameter(NO_VALUE_PARAMETER_NAME, mockMatcher).retrieveValue(request), 
                is(allOf(notNullValue(), empty())));
        
        assertThat(requestParameter(UNDEFINED_PARAMETER, mockMatcher).retrieveValue(request), 
                is(nullValue()));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).provideDescription(),
                is("parameter " + PARAMETER_NAME + " is"));
    }
}
