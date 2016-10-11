package net.jadler;

import net.jadler.stubbing.server.StubHttpServer;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared base for all JadlerMocker test classes.
 */
abstract class JadlerMockerTestBase {
	protected static final int DEFAULT_STATUS = 204;
	protected static final String HEADER_NAME1 = "h1";
	protected static final String HEADER_VALUE1 = "v1";
	protected static final String HEADER_NAME2 = "h2";
	protected static final String HEADER_VALUE2 = "v2";
	protected static final Charset DEFAULT_ENCODING = Charset.forName("UTF-16");
	protected static final int PORT = 12345;
	protected static final String HTTP_STUB1_TO_STRING = "http stub 1 toString";
	protected static final String HTTP_STUB2_TO_STRING = "http stub 2 toString";
	protected static final String HTTP_STUB1_MISMATCH = "http stub 1 mismatch";
	protected static final String HTTP_STUB2_MISMATCH = "http stub 2 mismatch";
	protected static final String MATCHER1_DESCRIPTION = "M1 description";
	protected static final String MATCHER1_MISMATCH = "M1 mismatch";
	protected static final String MATCHER2_DESCRIPTION = "M2 description";
	protected static final String MATCHER2_MISMATCH = "M2 mismatch";
	protected static final String COUNT_MATCHER_DESCRIPTION = "cnt matcher description";
	protected static final String COUNT_MATCHER_MISMATCH = "cnt matcher mismatch";

	protected static final Request[] REQUESTS = new Request[] {
			Request.builder().method("GET").requestURI(URI.create("/r1")).build(),
			Request.builder().method("GET").requestURI(URI.create("/r2")).build(),
			Request.builder().method("GET").requestURI(URI.create("/r3")).build(),
			Request.builder().method("GET").requestURI(URI.create("/r4")).build(),
			Request.builder().method("GET").requestURI(URI.create("/r5")).build()};


	protected JadlerMocker createMockerWithRequests() {
		final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));

		//register all requests
		for (final Request r: REQUESTS) {
			mocker.provideStubResponseFor(r);
		}

		return mocker;
	}

	protected Matcher<Request> createRequestMatcher(final String desc, final String mismatch) {
		@SuppressWarnings("unchecked")
		final Matcher<Request> m = mock(Matcher.class);

		this.addDescriptionTo(m, desc);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Description desc = (Description) invocation.getArguments()[1];
				desc.appendText(mismatch);

				return null;
			}
		}).when(m).describeMismatch(any(Request.class), any(Description.class));

		return m;
	}

	protected void addDescriptionTo(final Matcher<?> m, final String desc) {
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Description d = (Description) invocation.getArguments()[0];

				d.appendText(desc);

				return null;
			}
		}).when(m).describeTo(any(Description.class));
	}

	/*
     * Creates a Matcher<Integer> instance to be used as a matcher of a number of requests during a verification
     * @param expectedCount expected number of requests received so far which suit the verification description
     * @param actualCount actual number of requests received so far which suit the verification description
     * @param desc description of the matcher
     * @param mismatch description of a mismatch
     * @return a configured matcher
     */
	protected Matcher<Integer> createCountMatcherFor(final int expectedCount, final int actualCount,
	                                               final String desc, final String mismatch) {

		@SuppressWarnings("unchecked")
		final Matcher<Integer> m = mock(Matcher.class);

		when(m.matches(eq(expectedCount))).thenReturn(true);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Description desc = (Description) invocation.getArguments()[1];
				desc.appendText(mismatch);

				return null;
			}
		}).when(m).describeMismatch(eq(actualCount), any(Description.class));

		this.addDescriptionTo(m, desc);
		return m;
	}

	@SuppressWarnings("unchecked")
	protected Collection<Matcher<? super Request>> collectionOf(
			Matcher<? super Request> m1,
	        Matcher<? super Request> m2) {
		return Arrays.<Matcher<? super Request>>asList(m1, m2);
	}

	protected Writer createAppenderWriter() {
		final Writer w = new StringBuilderWriter();

		final WriterAppender appender = new WriterAppender();
		appender.setLayout(new PatternLayout("[%p] %m"));
		appender.setWriter(w);

		Logger.getRootLogger().addAppender(appender);
		return w;
	}


	protected void clearLog4jSetup() {
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
	}

	protected String expectedMessage() {
		return format("The number of http requests having %s AND %s was expected to be %s, but %s",
				MATCHER1_DESCRIPTION, MATCHER2_DESCRIPTION, COUNT_MATCHER_DESCRIPTION, COUNT_MATCHER_MISMATCH);
	}
}
