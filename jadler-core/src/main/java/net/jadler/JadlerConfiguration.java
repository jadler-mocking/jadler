/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.nio.charset.Charset;


/**
 * An interface for classes capable of providing defaults for the Jadler configuration.
 */
public interface JadlerConfiguration {

    /**
     * Defines a default content type of every stub http response. This value will be used for all stub responses
     * with no specific content type defined. (see {@link ResponseStubbing#withContentType(java.lang.String)})
     * @param defaultContentType default {@code Content-Type} header of every http stub response
     * @return this ongoing configuration
     */
    JadlerConfiguration withDefaultResponseContentType(final String defaultContentType);

    /**
     * Defines a default encoding of every stub http response. This value will be used for all stub responses
     * with no specific encoding defined. (see {@link ResponseStubbing#withEncoding(java.nio.charset.Charset)})
     * @param defaultEncoding default stub response encoding
     * @return this ongoing configuration
     */
    JadlerConfiguration withDefaultResponseEncoding(final Charset defaultEncoding);

    /**
     * Defines a response header that will be sent in every http stub response.
     * Can be called repeatedly to define more headers.
     * @param name name of the header
     * @param value header value
     * @return this ongoing configuration
     */
    JadlerConfiguration withDefaultResponseHeader(final String name, final String value);

    /**
     * Sets the default http response status. This value will be used for all stub responses with no
     * specific http status defined. (see {@link ResponseStubbing#withStatus(int)})
     * @param defaultStatus default http response status
     * @return this ongoing configuration
     */
    JadlerConfiguration withDefaultResponseStatus(final int defaultStatus);

    /**
     * <p>Disables incoming http requests recording.</p>
     *
     * <p>Jadler mocking (verification) capabilities are implemented by storing all incoming requests (including their
     * bodies). This could cause troubles in some very specific testing scenarios, for further explanation jump
     * straight to {@link JadlerMocker#setRecordRequests(boolean)}.</p>
     *
     * <p>Please note this method should be used very rarely and definitely should not be treated as a default.</p>
     *
     * @see JadlerMocker#setRecordRequests(boolean)
     * @return this ongoing configuration
     */
    JadlerConfiguration withRequestsRecordingDisabled();
    
}
