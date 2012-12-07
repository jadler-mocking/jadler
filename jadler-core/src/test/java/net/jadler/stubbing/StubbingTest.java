package net.jadler.stubbing;

import net.jadler.exception.JadlerException;
import net.jadler.matchers.BodyRequestMatcher;
import net.jadler.matchers.HeaderRequestMatcher;
import net.jadler.matchers.MethodRequestMatcher;
import net.jadler.matchers.ParameterRequestMatcher;
import net.jadler.matchers.QueryStringRequestMatcher;
import net.jadler.matchers.URIRequestMatcher;
import net.jadler.rule.HttpMockResponse;
import net.jadler.rule.HttpMockRule;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class StubbingTest {

    private static final MultiValueMap DEFAULT_HEADERS = new MultiValueMap();
    private static final int DEFAULT_STATUS = 200;
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

    private Stubbing stubbing;

    @Mock
    private Matcher<Object> matcher;


    @Before
    public void setUp() {
        this.stubbing = new Stubbing(DEFAULT_ENCODING, DEFAULT_STATUS, DEFAULT_HEADERS);
    }


    @Test
    public void that() {
        this.stubbing.that(matcher);

          //Fuck Java generics. This thing is sick.
        this.assertOneMatcher(Matchers.<Matcher<? super HttpServletRequest>>equalTo(matcher));
    }


    @Test
    public void havingMethodEqualTo() {
        this.stubbing.havingMethodEqualTo("GET");
        this.assertOneMatcher(is(instanceOf(MethodRequestMatcher.class)));
    }


    @Test
    public void havingMethod() {
        this.stubbing.havingMethod(matcher);
        this.assertOneMatcher(is(instanceOf(MethodRequestMatcher.class)));
    }


    @Test
    public void havingBodyEqualTo() {
        this.stubbing.havingBodyEqualTo("body");
        this.assertOneMatcher(is(instanceOf(BodyRequestMatcher.class)));
    }


    @Test
    public void havingBody() {
        this.stubbing.havingBody(matcher);
        this.assertOneMatcher(is(instanceOf(BodyRequestMatcher.class)));
    }


    @Test
    public void havingURIMatching() {
        this.stubbing.havingURIEqualTo("/**");
        this.assertOneMatcher(is(instanceOf(URIRequestMatcher.class)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void havingURIMatchingWrongValue() {
        this.stubbing.havingURIEqualTo("/a/b?param");
    }


    @Test
    public void havingURI() {
        this.stubbing.havingURI(matcher);
        this.assertOneMatcher(is(instanceOf(URIRequestMatcher.class)));
    }


    @Test
    public void havingQueryStringEqualTo() {
        this.stubbing.havingQueryStringEqualTo("a=b");
        this.assertOneMatcher(is(instanceOf(QueryStringRequestMatcher.class)));
    }


    @Test
    public void havingQueryString() {
        this.stubbing.havingQueryString(matcher);
        this.assertOneMatcher(is(instanceOf(QueryStringRequestMatcher.class)));
    }


    @Test
    public void havingParameterEqualTo() {
        this.stubbing.havingParameterEqualTo("name", "value");
        this.assertOneMatcher(is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingParameter() {
        this.stubbing.havingParameter("name", matcher);
        this.assertOneMatcher(is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingParameterWithoutValue() {
        this.stubbing.havingParameter("name");
        this.assertOneMatcher(is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingParameters() {
        this.stubbing.havingParameters("name1", "name2");
        assertThat(this.stubbing.getMatchers(), is(notNullValue()));
        assertThat(this.stubbing.getMatchers(), hasSize(2));

        assertThat(this.stubbing.getMatchers().get(0), is(instanceOf(ParameterRequestMatcher.class)));
        assertThat(this.stubbing.getMatchers().get(1), is(instanceOf(ParameterRequestMatcher.class)));
    }


    @Test
    public void havingHeaderEqualTo() {
        this.stubbing.havingHeaderEqualTo("name", "value");
        this.assertOneMatcher(is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void havingHeader() {
        this.stubbing.havingHeader("name", hasItem("value"));
        this.assertOneMatcher(is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void havingHeaderWithoutValue() {
        this.stubbing.havingHeader("name");
        this.assertOneMatcher(is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void havingHeaders() {
        this.stubbing.havingHeaders("name1", "name2");
        assertThat(this.stubbing.getMatchers(), is(notNullValue()));
        assertThat(this.stubbing.getMatchers(), hasSize(2));

        assertThat(this.stubbing.getMatchers().get(0), is(instanceOf(HeaderRequestMatcher.class)));
        assertThat(this.stubbing.getMatchers().get(1), is(instanceOf(HeaderRequestMatcher.class)));
    }


    @Test
    public void respond() {
        this.stubbing.respond();
        assertOneDefaultResponse();
    }


    @Test
    public void thenRespond() {
        this.stubbing.thenRespond();
        assertOneDefaultResponse();
    }


    @Test
    public void withBodyString() {
        final String body = "body";
        this.stubbing.respond().withBody(body);

        final HttpMockResponse response = assertAndGetOneResponse();
        assertThat(response.getBody(), equalTo(body));
    }


    @Test
    public void withBody() throws Exception {
        final String body = "body";
        final Reader reader = spy(new StringReader(body));
        this.stubbing.respond().withBody(reader);

        final HttpMockResponse response = assertAndGetOneResponse();
        assertThat(response.getBody(), equalTo(body));
        verify(reader).close();
    }


    @Test(expected = JadlerException.class)
    public void withBodyReaderThrowingIOE() throws Exception {
        final String body = "body";
        final Reader reader = spy(new StringReader(body));
        doThrow(IOException.class).when(reader).read(any(char[].class));

        try {
            this.stubbing.respond().withBody(reader);
        } finally {
            verify(reader).close();
        }
    }


    @Test
    public void withHeader() {
        final String name = "name";
        final String value = "value";
        this.stubbing.respond().withHeader(name, value);

        final HttpMockResponse response = assertAndGetOneResponse();
        assertThat(response.getHeaders(), is(notNullValue()));
        assertThat(response.getHeaders().size(), is(1));
        assertThat(response.getHeaders().containsKey(name), is(true));
        assertThat(response.getHeaders().get(name), is(notNullValue()));
        assertThat(((List) response.getHeaders().get(name)).size(), is(1));
        assertThat(((List) response.getHeaders().get(name)).get(0), is(equalTo((Object) value)));
    }


    @Test
    public void withStatus() {
        final int status = 2;
        this.stubbing.respond().withStatus(status);

        final HttpMockResponse response = assertAndGetOneResponse();
        assertThat(response.getStatus(), is(status));
    }


    @Test
    public void withTimeout() {
        final long timeout = 2;
        this.stubbing.respond().withTimeout(timeout, TimeUnit.MILLISECONDS);

        final HttpMockResponse response = assertAndGetOneResponse();
        assertThat(response.getTimeout(), is(timeout));
    }


    @Test
    public void createRule() {
        this.stubbing.thenRespond();
        final HttpMockRule rule = this.stubbing.createRule();

        assertThat(rule, is(notNullValue()));
    }


    // helper methods

    private void assertOneDefaultResponse() {
        assertThat(this.stubbing.getResponses(), is(notNullValue()));
        assertThat(this.stubbing.getResponses(), hasSize(1));

        assertThat(this.stubbing.getResponses().get(0), is(instanceOf(HttpMockResponse.class)));
        assertThat(this.stubbing.getResponses().get(0).getHeaders(), equalTo((MultiMap) DEFAULT_HEADERS));
        assertThat(this.stubbing.getResponses().get(0).getStatus(), equalTo(DEFAULT_STATUS));
    }


    private HttpMockResponse assertAndGetOneResponse() {
        assertThat(this.stubbing.getResponses(), is(notNullValue()));
        assertThat(this.stubbing.getResponses(), hasSize(1));

        assertThat(this.stubbing.getResponses().get(0), is(instanceOf(HttpMockResponse.class)));
        return this.stubbing.getResponses().get(0);
    }


    private void assertOneMatcher(final Matcher<? super Matcher<? super HttpServletRequest>> matcher) {
        assertThat(this.stubbing.getMatchers(), is(notNullValue()));
        assertThat(this.stubbing.getMatchers(), hasSize(1));

        assertThat(this.stubbing.getMatchers().get(0), matcher);
    }
}
