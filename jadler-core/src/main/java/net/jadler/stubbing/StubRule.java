/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription; 

import static org.hamcrest.Matchers.*;


/**
 * A stub rule is a <i>WHEN</i>-<i>THEN</i> pair (when a http request with specific properties arrives, then respond
 * with a defined http response).
 * The <i>WHEN<i> part is a list of predicates (in form of Hamcrest matchers) applied to a request. All of these
 * matchers must be evaluated to <tt>true</tt> in order to apply the <i>THEN</i> part.
 * 
 * The <i>THEN</i> part if defined by a non-empty list of stub response definition (in form of {@link StubResponse}
 * instances). These responses are returned in the same order as were defined. (if there is no new stub response 
 * definition to respond, the last one is returned).
 * 
 * Instances of this class are stateful (they are aware of the next response to be returned) and thread-safe.
 * 
 * One should never create new instances of this class directly, see {@link net.jadler.Jadler} for explanation and tutorial.
 */
public class StubRule {

    private final Collection<Matcher<? super Request>> predicates;
    private final List<StubResponse> stubResponses;
    private int responsePointer = 0;

    
    /**
     * @param predicates list of predicates. Cannot be null, however can be empty (which means this rule would
     * be matched by every request)
     * @param stubResponses list of stub response definitions. Must contain at least one stub response.
     */
    public StubRule(final Collection<Matcher<? super Request>> predicates,
            final List<StubResponse> stubResponses) {
        Validate.notNull(predicates, "predicates cannot be null, use an empty list instead");
        this.predicates = new ArrayList<Matcher<? super Request>>(predicates);
        
        Validate.notEmpty(stubResponses, "at least one stub response must be defined");
        this.stubResponses = new ArrayList<StubResponse>(stubResponses);
    }

    
    /**
     * @param request an http request to be checked whether it matches this stub rule.
     * @return true if and only if all predicates defined in this rule were evaluated to <tt>true</tt>
     * by the given request.
     */
    public boolean matchedBy(final Request request) {
        return allOf(this.predicates).matches(request);
    }

    
    /**
     * @return next stub response definition. Once the last response definition is reached
     * this method keeps returning this definition.
     */
    public synchronized StubResponse nextResponse() {
        if (responsePointer < stubResponses.size() - 1) {
            return stubResponses.get(responsePointer++);
        }
        else {
            return stubResponses.get(responsePointer);
        }
    }
    
    
    /**
     * Returns a reason why the given request doesn't match this rule. This method should be called if
     * and only if {@link  #matchedBy(Request)} would returned <tt>false</tt>.
     * However, this is not checked.
     * @param request an http request to describe the mismatch for
     * @return a human readable mismatch reason 
     */
    public String describeMismatch(final Request request) {
        final Description desc = new StringDescription();
        
        boolean first = true;
        for (final Iterator<Matcher<? super Request>> it = this.predicates.iterator(); it.hasNext();) {
            final Matcher<? super Request> m = it.next();
            
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
        for(final Iterator<Matcher<? super Request>> it = this.predicates.iterator(); it.hasNext();) {
            desc.appendText("  ");
            desc.appendDescriptionOf(it.next());
            if (it.hasNext()) {
                desc.appendText(" AND\n");
            }
        }
        desc.appendText(")");
        
        for (final StubResponse resp: this.stubResponses) {
            desc.appendText("\nTHEN respond with ");
            desc.appendText(resp.toString());
        }
        
        return desc.toString();
    }
}