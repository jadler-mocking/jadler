/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;


public class HeadersTest {
    
    private Headers headers;
    
    @Before
    public void setUp() {
        this.headers = new Headers()
                .add("name_1", "value_1_1")
                .add("name_2", "value_2_1")
                .add("name_2", "")
                .add("name_3", "value_3_1");
    }
    
    
    @Test
    public void constructor() {
        new Headers();
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void addWrongName1() {
        new Headers().add(null, "value");
    }
    

    @Test(expected = IllegalArgumentException.class)
    public void addWrongName2() {
        new Headers().add("", "value");
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void addWrongValue() {
        new Headers().add("name", null);
    }
    
    
    @Test
    public void addEmptyValue() {
        new Headers().add("name", "");
    }
    
    
    @Test
    public void addImmutability() {
        final Headers first = new Headers();
        assertThat(first, is(not(sameInstance(first.add("name", "value")))));
    }
    
    
    @Test
    public void addAndGetValues() {        
        assertThat(headers.getValues("name_1"), containsInAnyOrder("value_1_1"));
        assertThat(headers.getValues("name_2"), containsInAnyOrder("value_2_1", ""));
        assertThat(headers.getValues("name_3"), containsInAnyOrder("value_3_1"));
        assertThat(headers.getValues("unknown"), is(nullValue()));
    }
    
    
    @Test
    public void getNames() {
        assertThat(headers.getNames(), containsInAnyOrder("name_1", "name_2", "name_3"));
    }
    
    
    @Test
    public void getNamesEmpty() {
        assertThat(new Headers().getNames(), is(not(nullValue())));
        assertThat(new Headers().getNames(), is(empty()));
    }
    
    
    @Test
    public void testToStringHeaders() {
        assertThat(this.headers.toString().length(), is(65));
        assertThat(this.headers.toString(), containsString("name_1: value_1_1"));
        assertThat(this.headers.toString(), containsString("name_2: value_2_1"));
        assertThat(this.headers.toString(), containsString("name_2: "));
        assertThat(this.headers.toString(), containsString("name_3: value_3_1"));
    }
}