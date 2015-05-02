/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class StaticResponderTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void constructorWrongParam1() {
        new StaticResponder(null);
        fail("stubResponses cannot be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructorWrongParam2() {
        new StaticResponder(Collections.<StubResponse>emptyList());
        fail("stubResponses cannot be empty");
    }
    
    
    @Test
    public void constructor() {
        new StaticResponder(Collections.singletonList(StubResponse.EMPTY));
    }
    

    @Test
    public void nextResponse() {
        final StubResponse r1 = StubResponse.builder().build();
        final StubResponse r2 = StubResponse.builder().build();
        
        final StaticResponder producer = new StaticResponder(Arrays.asList(r1, r2));        
        assertThat(producer.nextResponse(null), is(r1));
        assertThat(producer.nextResponse(null), is(r2));
          //no other response defined, r2 must be returned again
        assertThat(producer.nextResponse(null), is(r2));
    }
    
    
    @Test
    public void testToString() {
        final StubResponse r1 = mock(StubResponse.class);
        when(r1.toString()).thenReturn("r1");
        final StubResponse r2 = mock(StubResponse.class);
        when(r2.toString()).thenReturn("r2");
                
        assertThat(new StaticResponder(Arrays.asList(r1, r2)).toString(), is("r1\nfollowed by r2"));
    }
}