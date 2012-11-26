package net.jadler.rule;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.hamcrest.Matchers.allOf;


/**
 * Http mock rule consists of a list of http request matchers and http response definitions.
 * Instances of this class are stateful (they are aware of the next response to be returned) and thread-safe.
 * 
 * One should never create new instances of this class directly, see {@link HttpMockers} for explanation and tutorial.
 */
public class HttpMockRule {

    private final Collection<Matcher<? super HttpServletRequest>> matchers;
    private final List<HttpMockResponse> responses;
    private int executionCount = 0;

    
    /**
     * @param matchers list of matchers. Cannot be null, however can be empty (which means this rule would
     * match every request)
     * @param responses list of responses. Must contain at least one response.
     */
    public HttpMockRule(final Collection<Matcher<? super HttpServletRequest>> matchers,
            final List<HttpMockResponse> responses) {
        Validate.notNull(matchers, "matchers cannot be null, use an empty list instead");
        this.matchers = new ArrayList<>(matchers);
        
        Validate.notEmpty(responses, "at least one response must be defined");
        this.responses = new ArrayList<>(responses);
    }

    
    /**
     * @param request an http request to be matched against this rule
     * @return true if the given request matches this rule, otherwise false
     */
    public boolean matches(final HttpServletRequest request) {
        return allOf(this.matchers).matches(request);
    }

    
    /**
     * @return next response definition. Once the last response definition is reached
     * this method keeps returning this definition.
     */
    public synchronized HttpMockResponse nextResponse() {
        if (executionCount < responses.size() - 1) {
            return responses.get(executionCount++);
        }
        else {
            return responses.get(executionCount);
        }
    }
    
    
    /**
     * Returns a reason why the given request doesn't match this rule. This method should be called if
     * and only if {@code this.matches(request)} returned false. 
     * @param request an http request to describe the mismatch for
     * @return a human readable mismatch reason 
     */
    public String describeMismatch(final HttpServletRequest request) {
        final Description desc = new StringDescription();
        
        boolean first = true;
        for (final Iterator<Matcher<? super HttpServletRequest>> it = this.matchers.iterator(); it.hasNext();) {
            final Matcher<? super HttpServletRequest> m = it.next();
            
            if (!m.matches(request)) {
                if (!first) {
                    desc.appendText(" AND\n");
                }
                desc.appendText("  ");
                m.describeMismatch(request, desc);
                first = false;
            }
        }
        return desc.toString();
    }
    

    @Override
    public String toString() {
        final Description desc = new StringDescription();
        desc.appendText("WHEN request (\n");
        for(final Iterator<Matcher<? super HttpServletRequest>> it = this.matchers.iterator(); it.hasNext();) {
            desc.appendText("  ");
            desc.appendDescriptionOf(it.next());
            if (it.hasNext()) {
                desc.appendText(" AND\n");
            }
        }
        desc.appendText(")");
        
        for (final HttpMockResponse resp: this.responses) {
            desc.appendText("\nTHEN respond with ");
            desc.appendText(resp.toString());
        }
        
        return desc.toString();
    }
}