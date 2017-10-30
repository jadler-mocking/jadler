package net.jadler;

import net.jadler.mocking.VerificationException;
import net.jadler.mocking.Verifying;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.port;
import static net.jadler.Jadler.verifyThatRequest;
import static net.jadler.Jadler.onRequest;



public class VerificationAsyncIntegrationTest {

	private StopWatch stopWatch;
	private ScheduledExecutorService exec;

	@Before
	public void init() {
		initJadler();
		this.stopWatch = new StopWatch();
		this.exec = Executors.newSingleThreadScheduledExecutor();
	}

	@After
	public void shutdown() {
		closeJadler();
		this.exec.shutdownNow();
	}

	@Test
	public void callHasAlredyBeenMade() throws IOException {
		stubOkOnExpectedRequest();
		performExpectedRequest();
		stopWatch.start();
		verifyingObjForExpected()
				.receivedOnce(Duration.ofSeconds(10));
		stopWatch.stop();
		assertThat(stopWatch.getTime(), lessThan(1000L));
		// 1 seconds is just to make sure that the test does not fail, because it runs on
		// very busy computer, it should actually just return without waiting at all.
	}

	@Test
	public void callIsMadeAfterOneSecond() throws ExecutionException, InterruptedException {
		stubOkOnExpectedRequest();
		stopWatch.start();
		final ScheduledFuture<Integer> performResult = this.exec.schedule(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return performExpectedRequest();
			}
		}, 1, TimeUnit.SECONDS);
		verifyingObjForExpected().receivedOnce(Duration.ofSeconds(2));
		stopWatch.stop();
		assertEquals(200, performResult.get().intValue());
		assertThat(stopWatch.getTime(), lessThan(2000L));
		assertThat(stopWatch.getTime(), greaterThanOrEqualTo(1000L));
	}

	@Test
	public void callIsNeverMadeShouldReturnTheExactSameExceptionAs() {
		VerificationException expectedExeption = null;
		try {
			verifyingObjForExpected().receivedOnce();
			fail("Should have catched an exception by now");
		} catch (VerificationException e) {
			expectedExeption = e;
		}
		Jadler.resetJadler();
		stopWatch.start();
		VerificationException result = null;
		try {
			verifyingObjForExpected().receivedOnce(Duration.ofMillis(200));
			fail("Should have catched an exception by now");
		} catch (VerificationException e) {
			result = e;
		}
		stopWatch.stop();
		assertThat(stopWatch.getTime(), greaterThanOrEqualTo(200L));
		assertEquals(expectedExeption.getMessage(), result.getMessage());
	}

	private void stubOkOnExpectedRequest() {
		addExpectedRequestToRequestMatching(onRequest())
				.respond()
				.withStatus(200);
	}

	private static Verifying verifyingObjForExpected() {
		return addExpectedRequestToRequestMatching(verifyThatRequest());
	}

	private static <T extends RequestMatching<T>> T addExpectedRequestToRequestMatching(
			final RequestMatching<T> r) {
		return r
				.havingMethodEqualTo("POST")
				.havingPathEqualTo("/expectedPath")
				.havingBodyEqualTo("expected body");
	}

	private int performExpectedRequest() throws IOException {
		return org.apache.http.client.fluent.Request
				.Post("http://localhost:"+port()+"/expectedPath")
				.bodyString("expected body", ContentType.DEFAULT_TEXT)
				.execute()
				.returnResponse().getStatusLine().getStatusCode();
	}
}
