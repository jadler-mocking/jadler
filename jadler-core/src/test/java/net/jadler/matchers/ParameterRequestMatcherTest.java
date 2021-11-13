/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.KeyValues;
import net.jadler.Request;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static net.jadler.KeyValues.EMPTY;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
    public void retrieveValue() {
        final KeyValues params = EMPTY.add(PARAMETER_NAME, PARAMETER_VALUE1).add(PARAMETER_NAME, PARAMETER_VALUE2);
        final Request req = when(mock(Request.class).getParameters()).thenReturn(params).getMock();

        assertThat(requestParameter(PARAMETER_NAME, mockMatcher).retrieveValue(req),
                is(allOf(notNullValue(), contains(PARAMETER_VALUE1, PARAMETER_VALUE2))));
    }


    @Test
    public void retrieveValueEmpty() {
        final KeyValues param = EMPTY.add(NO_VALUE_PARAMETER_NAME, "");

        final Request req = when(mock(Request.class).getParameters()).thenReturn(param).getMock();
        assertThat(requestParameter(NO_VALUE_PARAMETER_NAME, mockMatcher).retrieveValue(req),
                is(allOf(notNullValue(), contains(""))));
    }


    @Test
    public void retrieveValueNoParameter() {
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
