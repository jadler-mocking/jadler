/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.junit.rule;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static net.jadler.Jadler.verifyThatRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;


/**
 * Tests the {@link net.jadler.junit.rule.JadlerRule#JadlerRule()} variant.
 *
 * @author Christian Galsterer
 */
public class JadlerRuleIntegrationTest {

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final int DEFAULT_STATUS = 201;
    private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-2");
    private static final String HEADER_NAME1 = "name1";
    private static final String HEADER_NAME2 = "name2";
    private static final String HEADER_VALUE1_1 = "value11";
    private static final String HEADER_VALUE1_2 = "value12";
    private static final String HEADER_VALUE2 = "value2";
    private static final String STRING_WITH_DIACRITICS = "\u00e1\u0159\u017e";
    private static final byte[] ISO_8859_2_REPRESENTATION = {(byte) 0xE1, (byte) 0xF8, (byte) 0xBE};

    @Rule
    public JadlerRule defaultJadler = new JadlerRule()
            .withRequestsRecordingDisabled()
            .withDefaultResponseContentType(DEFAULT_CONTENT_TYPE)
            .withDefaultResponseStatus(DEFAULT_STATUS)
            .withDefaultResponseEncoding(DEFAULT_ENCODING)
            .withDefaultResponseHeader(HEADER_NAME1, HEADER_VALUE1_1)
            .withDefaultResponseHeader(HEADER_NAME1, HEADER_VALUE1_2)
            .withDefaultResponseHeader(HEADER_NAME2, HEADER_VALUE2);

    @AfterClass
    public static void cleanup() {
        Executor.closeIdleConnections();
    }

    private static HeaderMatcher header(final String name, final String value) {
        return new HeaderMatcher(name, value);
    }

    @Before
    public void setUp() {
        //send a default response on any request
        onRequest().respond().withBody(STRING_WITH_DIACRITICS);
    }

    @Test
    public void testWithDefaultPort() {
        assertThat(port(), is(greaterThanOrEqualTo(0)));
    }

    @Test(expected = IllegalStateException.class)
    public void withRequestsRecordingDisabled() {
        verifyThatRequest();
    }

    @Test
    public void withDefaultResponseContentType() throws IOException {
        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(response.getFirstHeader("Content-Type").getValue(), is(DEFAULT_CONTENT_TYPE));
    }

    @Test
    public void withDefaultResponseStatus() throws IOException {
        final int status = Executor.newInstance().execute(Request.Get(jadlerUri()))
                .returnResponse().getStatusLine().getStatusCode();

        assertThat(status, is(DEFAULT_STATUS));
    }

    @Test
    public void withDefaultResponseEncoding() throws IOException {
        final byte[] body = Executor.newInstance().execute(Request.Get(jadlerUri())).returnContent().asBytes();
        assertThat(body, is(ISO_8859_2_REPRESENTATION));
    }

    @Test
    public void withDefaultResponseHeader() throws IOException {
        final HttpResponse response = Executor.newInstance().execute(Request.Get(jadlerUri())).returnResponse();

        assertThat(response.getHeaders(HEADER_NAME1), is(arrayContainingInAnyOrder(
                header(HEADER_NAME1, HEADER_VALUE1_1), header(HEADER_NAME1, HEADER_VALUE1_2))));

        assertThat(response.getHeaders(HEADER_NAME2), is(arrayContaining(header(HEADER_NAME2, HEADER_VALUE2))));
    }

    private String jadlerUri() {
        return "http://localhost:" + port();
    }

    private static class HeaderMatcher extends BaseMatcher<Header> {

        final String expectedName;
        final String expectedValue;


        public HeaderMatcher(final String expectedName, final String expectedValue) {
            this.expectedName = expectedName;
            this.expectedValue = expectedValue;
        }


        @Override
        public boolean matches(final Object item) {
            if (item == null) {
                return false;
            }

            final Header actual = (Header) item;

            return this.expectedName.equals(actual.getName()) && this.expectedValue.equals(actual.getValue());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText('<' + this.expectedName + ": " + this.expectedValue + '>');
        }

    }
}
 