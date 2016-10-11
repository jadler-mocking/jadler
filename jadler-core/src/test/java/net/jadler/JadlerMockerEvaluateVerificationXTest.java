package net.jadler;

import net.jadler.mocking.VerificationException;
import net.jadler.stubbing.server.StubHttpServer;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for all cases where JadlerMocker.evaluateVerificationAsync
 * and Verifying.evaluateVerificationAsync should behave the same.
 *
 * When your create test here, your should never call the evaluateVerification method
 * directly, but instead though the evaluateVerificationCaller field, as this makes
 * sure that the test case is run ones where calling evaluateVerification and once where
 * calling evaluateVerificationAsyn. The timeout parameter will need to be supplied, but
 * will just not be used in the case where evaluateVerification is used.
 */
@RunWith(Parameterized.class)
public class JadlerMockerEvaluateVerificationXTest extends JadlerMockerTestBase {

	private static interface EvaluateVerificationCaller {
		void callOn(
				final JadlerMocker instance,
				final Collection<Matcher<? super Request>> requestPredicates,
				final Matcher<Integer> nrRequestsPredicate,
				final Duration timeOut);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{new EvaluateVerificationCaller() {
					@Override
					public void callOn(
							JadlerMocker instance,
							Collection<Matcher<? super Request>> p,
							Matcher<Integer> nrP,
							Duration timeOut) {
						instance.evaluateVerification(p, nrP);
					}
				}},
				{new EvaluateVerificationCaller() {
					@Override
					public void callOn(
							JadlerMocker instance,
							Collection<Matcher<? super Request>> p,
							Matcher<Integer> nrP,
							Duration timeOut) {
						instance.evaluateVerificationAsync(p, nrP, timeOut);
					}
				}}});
	}

	private final EvaluateVerificationCaller evaluateVerificationCaller;

	public JadlerMockerEvaluateVerificationXTest(
			EvaluateVerificationCaller evaluateVerificationCaller) {
		this.evaluateVerificationCaller = evaluateVerificationCaller;
	}


	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unchecked")
	public void evaluateVerification_illegalArgument1() {
		final JadlerMocker instance = new JadlerMocker(mock(StubHttpServer.class));
		evaluateVerificationCaller.callOn(
				instance, null, mock(Matcher.class), Duration.zero());
	}


	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unchecked")
	public void evaluateVerification_illegalArgument2() {
		final JadlerMocker instance = new JadlerMocker(mock(StubHttpServer.class));
		evaluateVerificationCaller.callOn(
				instance, Collections.<Matcher<? super Request>>singleton(mock(Matcher.class)),
				null,
				Duration.zero());
	}

	@Test(expected = IllegalStateException.class)
	@SuppressWarnings("unchecked")
	public void evaluateVerification_recordingDisabled() {
		final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
		mocker.setRecordRequests(false);

		evaluateVerificationCaller.callOn(
				mocker,
				Collections.<Matcher<? super Request>>singleton(mock(Matcher.class)),
				mock(Matcher.class), Duration.zero());
	}

	@Test
	public void evaluateVerification_2_matching_positive() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//R0 is not matched
		when(m1.matches(eq(REQUESTS[0]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[0]))).thenReturn(false);

		//R1 is matched
		when(m1.matches(eq(REQUESTS[1]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[1]))).thenReturn(true);

		//R2 is not matched
		when(m1.matches(eq(REQUESTS[2]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[2]))).thenReturn(true);

		//R3 is not matched
		when(m1.matches(eq(REQUESTS[3]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[3]))).thenReturn(false);

		//R4 is matched
		when(m1.matches(eq(REQUESTS[4]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[4]))).thenReturn(true);

		//2 requests matching, 2 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(2, 2, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		evaluateVerificationCaller.callOn(
				mocker, collectionOf(m1, m2), countMatcher, Duration.zero());
	}

	@Test(expected = VerificationException.class)
	public void evaluateVerification_2_matching_expectind3_WithNoneZeroTimeout_negative() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//R0 is not matched
		when(m1.matches(eq(REQUESTS[0]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[0]))).thenReturn(false);

		//R1 is matched
		when(m1.matches(eq(REQUESTS[1]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[1]))).thenReturn(true);

		//R2 is not matched
		when(m1.matches(eq(REQUESTS[2]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[2]))).thenReturn(true);

		//R3 is not matched
		when(m1.matches(eq(REQUESTS[3]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[3]))).thenReturn(false);

		//R4 is matched
		when(m1.matches(eq(REQUESTS[4]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[4]))).thenReturn(true);

		//2 requests matching, 3 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(3, 2, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		evaluateVerificationCaller.callOn(
				mocker, collectionOf(m1, m2), countMatcher, Duration.ofNanos(1));
	}

	@Test
	public void evaluateVerification_0_matching_positive() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//R0 is not matched
		when(m1.matches(eq(REQUESTS[0]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[0]))).thenReturn(false);

		//R1 is not matched
		when(m1.matches(eq(REQUESTS[1]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[1]))).thenReturn(false);

		//R2 is not matched
		when(m1.matches(eq(REQUESTS[2]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[2]))).thenReturn(true);

		//R3 is not matched
		when(m1.matches(eq(REQUESTS[3]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[3]))).thenReturn(false);

		//R4 is not matched
		when(m1.matches(eq(REQUESTS[4]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[4]))).thenReturn(true);

		//0 requests matching, 0 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(0, 0, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		evaluateVerificationCaller.callOn(
				mocker, collectionOf(m1, m2), countMatcher, Duration.zero());
	}

	@Test
	public void evaluateVerification_0_predicates_5_matching_positive() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		//5 requests matching (=received in this case), 5 expected
		final int actualCount = 5;
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(5, actualCount, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		evaluateVerificationCaller.callOn(
				mocker,
				Collections.<Matcher<? super Request>>emptySet(),
				countMatcher,
				Duration.zero());
	}

	@Test
	public void evaluateVerification_0_requests_0_matching_positive() {
		final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//0 requests matching (=received in this case), 0 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(0, 0, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		evaluateVerificationCaller.callOn(
				mocker, collectionOf(m1, m2), countMatcher, Duration.zero());
	}

	@Test
	public void evaluateVerification_2_matching_negative() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//R0 is not matched
		when(m1.matches(eq(REQUESTS[0]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[0]))).thenReturn(false);

		//R1 is matched
		when(m1.matches(eq(REQUESTS[1]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[1]))).thenReturn(true);

		//R2 is not matched
		when(m1.matches(eq(REQUESTS[2]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[2]))).thenReturn(true);

		//R3 is not matched
		when(m1.matches(eq(REQUESTS[3]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[3]))).thenReturn(false);

		//R4 is matched
		when(m1.matches(eq(REQUESTS[4]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[4]))).thenReturn(true);

		//2 requests matching, 1 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(1, 2, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		final Writer w = this.createAppenderWriter();
		try {
			evaluateVerificationCaller.callOn(
					mocker, collectionOf(m1, m2), countMatcher, Duration.zero());
			fail("VerificationException is supposed to be thrown here");
		}
		catch (final VerificationException e) {
			assertThat(e.getMessage(), is(expectedMessage()));
			assertThat(w.toString(), is(format(
					"[INFO] Verification failed, here is a list of requests received so far:\n" +
							"Request #1: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"    %s\n" +
							"Request #2: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"    %s\n" +
							"  clashing predicates: <none>\n" +
							"Request #3: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"Request #4: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"Request #5: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"    %s\n" +
							"  clashing predicates: <none>",
					REQUESTS[0], MATCHER1_MISMATCH, MATCHER2_MISMATCH,
					REQUESTS[1], MATCHER1_DESCRIPTION, MATCHER2_DESCRIPTION,
					REQUESTS[2], MATCHER2_DESCRIPTION, MATCHER1_MISMATCH,
					REQUESTS[3], MATCHER1_DESCRIPTION, MATCHER2_MISMATCH,
					REQUESTS[4], MATCHER1_DESCRIPTION, MATCHER2_DESCRIPTION)));
		}
		finally {
			this.clearLog4jSetup();
		}
	}

	@Test
	public void evaluateVerification_0_matching_negative() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//R0 is not matched
		when(m1.matches(eq(REQUESTS[0]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[0]))).thenReturn(false);

		//R1 is not matched
		when(m1.matches(eq(REQUESTS[1]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[1]))).thenReturn(true);

		//R2 is not matched
		when(m1.matches(eq(REQUESTS[2]))).thenReturn(false);
		when(m2.matches(eq(REQUESTS[2]))).thenReturn(true);

		//R3 is not matched
		when(m1.matches(eq(REQUESTS[3]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[3]))).thenReturn(false);

		//R4 is not matched
		when(m1.matches(eq(REQUESTS[4]))).thenReturn(true);
		when(m2.matches(eq(REQUESTS[4]))).thenReturn(false);

		//0 requests matching, 1 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(1, 0, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		final Writer w = this.createAppenderWriter();
		try {
			evaluateVerificationCaller.callOn(
					mocker, collectionOf(m1, m2), countMatcher, Duration.zero());
			fail("VerificationException is supposed to be thrown here");
		}
		catch (final VerificationException e) {
			assertThat(e.getMessage(), is(expectedMessage()));
			assertThat(w.toString(), is(format(
					"[INFO] Verification failed, here is a list of requests received so far:\n" +
							"Request #1: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"    %s\n" +
							"Request #2: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"Request #3: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"Request #4: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"  clashing predicates:\n" +
							"    %s\n" +
							"Request #5: %s\n" +
							"  matching predicates:\n" +
							"    %s\n" +
							"  clashing predicates:\n" +
							"    %s",
					REQUESTS[0], MATCHER1_MISMATCH, MATCHER2_MISMATCH,
					REQUESTS[1], MATCHER2_DESCRIPTION, MATCHER1_MISMATCH,
					REQUESTS[2], MATCHER2_DESCRIPTION, MATCHER1_MISMATCH,
					REQUESTS[3], MATCHER1_DESCRIPTION, MATCHER2_MISMATCH,
					REQUESTS[4], MATCHER1_DESCRIPTION, MATCHER2_MISMATCH)));
		}
		finally {
			this.clearLog4jSetup();
		}
	}

	@Test
	public void evaluateVerification_0_predicates_5_matching_negative() {
		final JadlerMocker mocker = this.createMockerWithRequests();

		//5 requests matching (=received in this case), 4 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(4, 5, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		final Writer w = this.createAppenderWriter();
		try {
			evaluateVerificationCaller.callOn(
					mocker,
					Collections.<Matcher<? super Request>>emptySet(),
					countMatcher,
					Duration.zero());
			fail("VerificationException is supposed to be thrown here");
		}
		catch (final VerificationException e) {
			assertThat(e.getMessage(), is(format("The number of http requests was expected to be %s, but %s",
					COUNT_MATCHER_DESCRIPTION, COUNT_MATCHER_MISMATCH)));

			assertThat(w.toString(), is(format(
					"[INFO] Verification failed, here is a list of requests received so far:\n" +
							"Request #1: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates: <none>\n" +
							"Request #2: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates: <none>\n" +
							"Request #3: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates: <none>\n" +
							"Request #4: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates: <none>\n" +
							"Request #5: %s\n" +
							"  matching predicates: <none>\n" +
							"  clashing predicates: <none>",
					REQUESTS[0], REQUESTS[1], REQUESTS[2], REQUESTS[3], REQUESTS[4])));
		}
		finally {
			this.clearLog4jSetup();
		}
	}

	@Test
	public void evaluateVerification_0_requests_0_matching_negative() {
		final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
		final Matcher<Request> m1 = this.createRequestMatcher(MATCHER1_DESCRIPTION, MATCHER1_MISMATCH);
		final Matcher<Request> m2 = this.createRequestMatcher(MATCHER2_DESCRIPTION, MATCHER2_MISMATCH);

		//0 requests matching (=received in this case), 0 expected
		final Matcher<Integer> countMatcher = this.createCountMatcherFor(1, 0, COUNT_MATCHER_DESCRIPTION,
				COUNT_MATCHER_MISMATCH);

		final Writer w = this.createAppenderWriter();

		try {
			evaluateVerificationCaller.callOn(
					mocker, collectionOf(m1, m2), countMatcher, Duration.zero());
			fail("VerificationException is supposed to be thrown here");
		}
		catch (final VerificationException e) {
			assertThat(e.getMessage(), is(expectedMessage()));
			assertThat(w.toString(),
					is("[INFO] Verification failed, here is a list of requests received so far: <none>"));
		}
		finally {
			this.clearLog4jSetup();
		}
	}
}
