/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.mocking;

import net.jadler.RequestManager;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.fail;


@RunWith(MockitoJUnitRunner.class)
public class VerifyingTest {
    
    @Mock
    public RequestManager requestManager;
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructorIllegalArg() {
        new Verifying(null);
        fail("requestManager cannot be null");
    }
    
    
    @Test
    public void constructor() {
        new Verifying(mock(RequestManager.class));
    }
    

    @Test(expected=IllegalArgumentException.class)
    public void receivedTimesMatcherIllegalArg() {
        new Verifying(requestManager).receivedTimes(null);
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void receivedTimesMatcher() {
        
        final Matcher<Integer> pred = mock(Matcher.class);
        when(pred.matches(eq(42))).thenReturn(true);
        
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(42);
        
        new Verifying(requestManager).receivedTimes(pred);
    }
    
    
    @Test(expected=VerificationException.class)
    public void receivedTimesMatcherNegative() {
        
        @SuppressWarnings("unchecked")
        final Matcher<Integer> pred = mock(Matcher.class);
        when(pred.matches(anyObject())).thenReturn(false);
        
        new Verifying(requestManager).receivedTimes(pred);
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void receivedTimesIntInvalidArg() {
        new Verifying(requestManager).receivedTimes(-1);
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void receivedTimesInt() {
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(42);
        
        new Verifying(requestManager).receivedTimes(42);
    }
    
    
    @Test(expected=VerificationException.class)
    @SuppressWarnings("unchecked")
    public void receivedTimesIntNegative() {
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(41);
        
        new Verifying(requestManager).receivedTimes(42);
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void receivedOnce() {
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(1);
        
        new Verifying(requestManager).receivedOnce();
    }
    
    
    @Test(expected=VerificationException.class)
    @SuppressWarnings("unchecked")
    public void receivedOnceNegative() {
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(2);
        
        new Verifying(requestManager).receivedOnce();
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void receivedNever() {
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(0);
        
        new Verifying(requestManager).receivedNever();
    }
    
    
    @Test(expected=VerificationException.class)
    @SuppressWarnings("unchecked")
    public void receivedNeverNegative() {
        when(requestManager.numberOfRequestsMatching(anyCollection())).thenReturn(1);
        
        new Verifying(requestManager).receivedNever();
    }
}