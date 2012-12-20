package net.jadler.httpmocker;

import net.jadler.stubbing.StubResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * A component which provides mock http response definitions for the given http request.
 */
public interface ResponseProvider {
    
    /**
     * @param req http request to return a mock response for
     * @return definition of a mock response to be returned for the given request or null,
     * if no response is defined for this request
     */
    StubResponse provideResponseFor(HttpServletRequest req);
}
