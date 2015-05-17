/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import static net.jadler.Jadler.initJadler;


/**
 * Jadler integration test for Jetty.
 */
public class JettyJadlerStubbingIntegrationTest extends AbstractJadlerStubbingIntegrationTest {
    @Override
    protected Jadler.AdditionalConfiguration doInitJadler() {
        return initJadler();
    }
}
