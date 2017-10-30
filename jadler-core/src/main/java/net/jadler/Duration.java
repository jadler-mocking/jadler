package net.jadler;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import java.util.concurrent.TimeUnit;

/**
 * Simple class to represent a Duration in some timeunit. It does this be
 * holding the value as a long, and the type of timeunit as a TimeUnit enum value.
 *
 * When the library one day requires java8, this class should be removed, and replaced
 * by java.time.Duration.
 *
 * This class is Immutable.
 */
public final class Duration {

	private static class ZeroHolder {
		public static final Duration instance = Duration.ofNanos(0L);
	}

	private final long value;
	private final TimeUnit timeUnit;

	public static Duration zero() {
		return ZeroHolder.instance;
	}

	public static Duration ofNanos(final long value) {
		return new Duration(value, TimeUnit.NANOSECONDS);
	}

	public static Duration ofMillis(final long value) {
		return new Duration(value, TimeUnit.MILLISECONDS);
	}

	public static Duration ofSeconds(final long value) {
		return new Duration(value, TimeUnit.SECONDS);
	}

	public static Duration ofMinutes(final long value) {
		return new Duration(value, TimeUnit.MINUTES);
	}

	private Duration(final long value, final TimeUnit timeUnit) {
		if (value < 0)
			throw new IllegalArgumentException("Value of a Duratino cannot be negative");
		this.value = value;
		this.timeUnit = timeUnit;
	}

	public long getValue() {
		return value;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public long toNanos() {
		return this.timeUnit.toNanos(this.value);
	}

	public boolean isZero() {
		return this.value == 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof Duration)) return false;

		Duration duration = (Duration) o;

		return new EqualsBuilder()
				.append(value, duration.value)
				.append(timeUnit, duration.timeUnit)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(value)
				.append(timeUnit)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("value", value)
				.append("timeUnit", timeUnit)
				.toString();
	}
}
