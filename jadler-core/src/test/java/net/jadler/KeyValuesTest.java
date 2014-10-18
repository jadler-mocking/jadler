/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;


public class KeyValuesTest {
    
    private KeyValues keyValues;
    
    private static final String HEADER1_NAME = "name_1";
    private static final String HEADER2_NAME = "name_2";
    private static final String HEADER3_NAME = "name_3";
    
    private static final String HEADER1_VALUE1 = "value_1_1";
    private static final String HEADER2_VALUE1 = "value_2_1";
    private static final String HEADER2_VALUE2 = "";
    private static final String HEADER3_VALUE1 = "value_3_1";
    
    @Before
    public void setUp() {
        this.keyValues = new KeyValues()
                .add(HEADER1_NAME, HEADER1_VALUE1)
                .add(HEADER2_NAME, HEADER2_VALUE1)
                .add(HEADER2_NAME, HEADER2_VALUE2)
                .add(HEADER3_NAME, HEADER3_VALUE1);
    }
    
    
    @Test
    public void constructor() {
        new KeyValues();
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void addWrongName1() {
        this.keyValues.add(null, "value");
    }
    

    @Test(expected = IllegalArgumentException.class)
    public void addWrongName2() {
        this.keyValues.add("", "value");
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void addWrongValue() {
        this.keyValues.add("name", null);
    }
    
    
    @Test
    public void addEmptyValue() {
        this.keyValues.add("name", "");
    }
    
    
    @Test
    public void addImmutability() {
        final KeyValues first = new KeyValues();
        assertThat(first, is(not(sameInstance(first.add("name", "value")))));
    }
    
    
    @Test
    public void add() {        
        assertThat(keyValues.getValues(HEADER1_NAME), containsInAnyOrder(HEADER1_VALUE1));
        assertThat(keyValues.getValues(HEADER2_NAME), containsInAnyOrder(HEADER2_VALUE1, HEADER2_VALUE2));
        assertThat(keyValues.getValues(HEADER3_NAME), containsInAnyOrder(HEADER3_VALUE1));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void addAllWrongParam() {
        this.keyValues.addAll(null);
    }
    
    
    @Test
    public void addAll() {
        final KeyValues additional = new KeyValues().add("name_4", "value_4_1");
        final KeyValues actual = this.keyValues.addAll(additional);
        
        assertThat(actual.getValues(HEADER1_NAME), containsInAnyOrder(HEADER1_VALUE1));
        assertThat(actual.getValues(HEADER2_NAME), containsInAnyOrder(HEADER2_VALUE1, HEADER2_VALUE2));
        assertThat(actual.getValues(HEADER3_NAME), containsInAnyOrder(HEADER3_VALUE1));
        assertThat(actual.getValues("name_4"), containsInAnyOrder("value_4_1"));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void getValueWrongParam() {
        this.keyValues.getValue("");
    }
    
    
    @Test
    public void getValueNonExistent() {
        assertThat(this.keyValues.getValue("unknown"), is(nullValue()));
    }
    
    
    @Test
    public void getValue() {
        assertThat(this.keyValues.getValue(HEADER1_NAME), is(HEADER1_VALUE1));
          //name_2 contains two values, the first one must be returned
        assertThat(this.keyValues.getValue(HEADER2_NAME), is(HEADER2_VALUE1));
    }
    
       
    @Test
    public void getValueCaseInsensitive1() {
        assertThat(this.keyValues.getValue(HEADER1_NAME.toUpperCase()), is(HEADER1_VALUE1));
    }
    
    
    @Test
    public void getValueCaseInsensitive2() {        
        assertThat(new KeyValues().add(HEADER1_NAME.toUpperCase(), HEADER1_VALUE1)
                .getValue(HEADER1_NAME), is(HEADER1_VALUE1));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void getValuesWrongParam() {
        this.keyValues.getValues("");
    }
    
    
    @Test
    public void getValuesNonExistent() {
        assertThat(this.keyValues.getValues("unknown"), is(nullValue()));
    }
    
    
    @Test
    public void getValues() {
        assertThat(this.keyValues.getValues(HEADER1_NAME), contains(HEADER1_VALUE1));
          //name_2 contains two values, both values must be returned
        assertThat(this.keyValues.getValues(HEADER2_NAME), contains(HEADER2_VALUE1, HEADER2_VALUE2));
    }
    
       
    @Test
    public void getValuesCaseInsensitive1() {
        assertThat(this.keyValues.getValues(HEADER2_NAME.toUpperCase()), contains(HEADER2_VALUE1, HEADER2_VALUE2));
    }
    
    
    @Test
    public void getValuesCaseInsensitive2() {
        final KeyValues kv = new KeyValues()
                .add(HEADER2_NAME.toUpperCase(), HEADER2_VALUE1)
                .add(HEADER2_NAME, HEADER2_VALUE2);
        
        assertThat(kv.getValues(HEADER2_NAME), contains(HEADER2_VALUE1, HEADER2_VALUE2));
    }
    
    
    @Test
    public void getKeys() {
        assertThat(keyValues.getKeys(), containsInAnyOrder("name_1", "name_2", "name_3"));
    }
    
    
    @Test
    public void getKeysEmpty() {
        assertThat(new KeyValues().getKeys(), is(not(nullValue())));
        assertThat(new KeyValues().getKeys(), is(empty()));
    }
    
    
    @Test
    public void testToStringHeaders() {
        assertThat(this.keyValues.toString().length(), is(65));
        assertThat(this.keyValues.toString(), containsString("name_1: value_1_1"));
        assertThat(this.keyValues.toString(), containsString("name_2: value_2_1"));
        assertThat(this.keyValues.toString(), containsString("name_2: "));
        assertThat(this.keyValues.toString(), containsString("name_3: value_3_1"));
    }
}