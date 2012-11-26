package net.jadler.stubbing;

import java.io.Reader;
import java.util.concurrent.TimeUnit;


/**
 * This interface defines methods for the <i>THEN</i> part of this http stubbing. These methods provide
 * a way to define an http response for this stubbing. This response is returned when the <i>WHEN</i> criteria of
 * this stubbing are met.
 * 
 * Multiple responses can be defined for one stubbing. Once there is no next response left,
 * the last one is used for all following requests (if you define only one response, which is a common
 * scenario, this response will be used for every request that meets the <i>WHEN</i> part).
 */
public interface ResponseStubbing {

    /**
     * Sets status on HTTP response. Calling this method overrides any previous calls or the default status defined
     * for the http mocker instance.
     * @param status http status code
     * @return this stubbing
     */
    ResponseStubbing withStatus(int status);

    
    /**
     * Adds a response header to the response.
     * 
     * This method can be called multiple times for the same header name. The response will contain a header with
     * all the (multiple) values.
     * 
     * @param name header name
     * @param value header value
     * @return this stubbing
     */
    ResponseStubbing withHeader(String name, String value);
    

    /**
     * Sets the response body. Calling this method overrides any previous calls of this or
     * {@link #withBody(java.io.Reader)} method.
     * @param responseBody response body
     * @return this stubbing
     */
    ResponseStubbing withBody(String responseBody);


    /**
     * Sets the response body from the given reader.Calling this method overrides any previous calls of this or
     * {@link #withBody(java.lang.String)} method.
     * @param responseBody response body
     * @return this stubbing
     */
    ResponseStubbing withBody(Reader reader);

    
    /**
     * Sets the response timeout. The response is returned after the specified amount of time.
     * @param timeoutValue timeout value
     * @param timeoutUnit unit of the timeout parameter
     * @return this stubbing
     */
    ResponseStubbing withTimeout(long timeoutValue, TimeUnit timeoutUnit);

    
    /**
     * Starts defining a subsequent response.
     * @return this stubbing
     */
    ResponseStubbing thenRespond();
}
