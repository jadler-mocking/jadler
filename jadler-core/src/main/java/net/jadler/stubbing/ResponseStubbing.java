/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;


/**
 * This interface defines methods for the http stubbing <i>THEN</i> part. These methods provide
 * a way to define one or more http responses for this stubbing. 
 * 
 * Multiple responses can be defined for one stubbing. These responses are returned in the same order
 * as were defined. Once there is no next response left, the last one is used for 
 * all following requests (if you define only one response, which is a common
 * scenario, this response will be used for every request that meets the <i>WHEN</i> part).
 */
public interface ResponseStubbing {
    
    
    /**
     * Sets the content type of the http stub response. Calling this method overrides any previous calls.
     * The content type information is communicated via the http <tt>Content-Type</tt> response header.
     * 
     * This method either adds this header or overwrites any existing <tt>Content-Type</tt> header ensuring
     * at most one such header will be present in the stub response.
     * @param contentType response content type
     * @return this ongoing stubbing
     */
    ResponseStubbing withContentType(String contentType);
    
    
    /**
     * Sets the character encoding of the http stub response. 
     * Calling this method overrides any previous calls or the default encoding
     * (set by {@link net.jadler.Jadler.OngoingConfiguration#respondsWithDefaultEncoding(java.nio.charset.Charset)}).
     * 
     * Please note this method doesn't set the <tt>Content-Type</tt> header <tt>charset</tt> part,
     * {@link #withContentType(java.lang.String)} must be called to do so. You can even set different
     * stub response body encoding and <tt>Content-Type</tt> if your testing scenario requires it.
     * 
     * @param encoding response body encoding
     * @return this ongoing stubbing
     */
    ResponseStubbing withEncoding(Charset encoding);
    

    /**
     * Sets the http stub response status. Calling this method overrides any previous calls or the default status
     * (set by {@link net.jadler.Jadler.OngoingConfiguration#respondsWithDefaultStatus(int)}).
     * @param status http status code
     * @return this ongoing stubbing
     */
    ResponseStubbing withStatus(int status);

    
    /**
     * Adds a stub http response header.
     * This method can be called multiple times for the same header name. The response will contain a header with
     * all (multiple) values.
     * 
     * @param name header name
     * @param value header value
     * @return this ongoing stubbing
     */
    ResponseStubbing withHeader(String name, String value);
    

    /**
     * Sets the stub http response body as a string. This string will be encoded using an encoding
     * set by {@link #withEncoding(java.nio.charset.Charset) } 
     * Calling this method overrides any previous calls of this or any other <tt>withBody</tt> method.
     * @param responseBody response body
     * @return this ongoing stubbing
     */
    ResponseStubbing withBody(String responseBody);


    /**
     * Sets the stub http response body as the content of the given reader. The string retrieved
     * from the given reader will be encoded using an encoding set by {@link #withEncoding(java.nio.charset.Charset) }
     * Calling this method overrides any previous calls of this or any other <tt>withBody</tt> method.
     * @param reader response body source
     * @return this ongoing stubbing
     */
    ResponseStubbing withBody(Reader reader);
    
    
    /**
     * Sets the stub http response body as the content of the given input stream. The source input stream
     * is copied to the stub response body as-is, it is not affected by the encoding set by
     * {@link #withEncoding(java.nio.charset.Charset)} in any way.
     * Calling this method overrides any previous calls of this or any other <tt>withBody</tt> method.
     * @param is response body source
     * @return this ongoing stubbing
     */
    ResponseStubbing withBody(InputStream is);
    
    
    /**
     * Sets the stub http response body as an array of bytes. The given array of bytes
     * is used as the stub response body as-is, it is not affected by the encoding set by
     * {@link #withEncoding(java.nio.charset.Charset)} in any way.
     * Calling this method overrides any previous calls of this or any other <tt>withBody</tt> method.
     * @param responseBody response body
     * @return this ongoing stubbing
     */
    ResponseStubbing withBody(byte[] responseBody);

    
    /**
     * Sets the response delay. The stub http response is returned after the specified amount of time.
     * Calling this method overrides any previous calls of this method.
     * @param delayValue a delay (in units defined by the {@code delayUnit} parameter) 
     * this stub response will be returned after
     * @param delayUnit unit of the delay parameter
     * @return this ongoing stubbing
     */
    ResponseStubbing withDelay(long delayValue, TimeUnit delayUnit);

    
    /**
     * Starts a definition of a subsequent stub response.
     * @return this ongoing stubbing
     */
    ResponseStubbing thenRespond();
}
