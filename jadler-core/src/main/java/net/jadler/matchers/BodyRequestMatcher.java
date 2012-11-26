package net.jadler.matchers;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Implementation of <tt>RequestMatcher</tt> used for matching request body decoded from HTTP request.
 */
public class BodyRequestMatcher extends RequestMatcher<String> {


    public BodyRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }

    
    @Override
    protected String retrieveValue(final HttpServletRequest req) throws Exception {
        return IOUtils.toString(req.getReader());
    }
    
    

    @Override
    protected String provideDescription() {
        return "body is";
    }


    @Factory
    public static BodyRequestMatcher requestBody(final Matcher<? super String> pred) {
        return new BodyRequestMatcher(pred);
    }
}
