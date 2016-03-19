/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jdk;

import org.junit.Test;


public class JdkStubHttpServerTest {
    
    @Test
    public void constructor() {
        new JdkStubHttpServer();
    }
    
    @Test
    public void constructor_int() {
        new JdkStubHttpServer(0);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void constructor_fail() {
        new JdkStubHttpServer(-1);
    }
    

    @Test(expected = IllegalArgumentException.class)
    public void registerRequestManager_fail() {
        new JdkStubHttpServer().registerRequestManager(null);
    }
}