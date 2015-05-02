/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import net.jadler.Request;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription; 

import static org.hamcrest.Matchers.allOf;


/**
 * <p>An http stub is a <em>WHEN</em>-<em>THEN</em> pair (when an http request with specific properties arrives,
 * then respond with a defined response).</p>
 * 
 * <p>The <em>WHEN</em> part is a list of predicates (in form of Hamcrest matchers) applicable to a request.
 * All of these matchers must be evaluated to {@code true} in order to apply the <em>THEN</em> part.</p>
 * 
 * <p>The <em>THEN</em> part is defined by an instance of the {@link Responder} interface.
 * This instance is capable of constructing stub http responses to be returned to the client.</p>
 * 
 * <p>Instances of this class are thread-safe if and only if the provided {@link Responder} instance
 * is thread-safe.</p>
 * 
 * <p>One should never create new instances of this class directly,see {@link net.jadler.Jadler}
 * for explanation and tutorial.</p>
 */
public class HttpStub {

    private final Collection<Matcher<? super Request>> predicates;
    private final Responder responder;

    
    /**
     * @param predicates list of predicates. Cannot be {@code null}, however can be empty (which means this rule will
     * match every request)
     * @param responder an instance to provide stub http responses
     */
    public HttpStub(final Collection<Matcher<? super Request>> predicates, final Responder responder) {
        Validate.notNull(predicates, "predicates cannot be null, use an empty list instead");
        this.predicates = new ArrayList<Matcher<? super Request>>(predicates);
        
        Validate.notNull(responder, "responder cannot be null");
        this.responder = responder;
    }

    
    /**
     * @param request an http request to be checked whether it matches this stub rule.
     * @return {@code true} if and only if all predicates defined in this rule were evaluated to {@code true}
     * by the given request.
     */
    public boolean matches(final Request request) {
        return allOf(this.predicates).matches(request);
    }
    
    
    /**
     * @param request an http request the stub response will be generated for
     * @return next http stub response as produced by the {@link Responder} instance provided in
     * {@link #HttpStub(java.util.Collection, net.jadler.stubbing.Responder)}
     */
    public StubResponse nextResponse(final Request request) {
        return this.responder.nextResponse(request);
    }
    
    
    /**
     * Returns a reason why the given request doesn't match this rule. This method should be called if
     * and only if {@link  #matches(net.jadler.Request)} would return {@code false}. However, this is not checked.
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
        desc.appendText(")\n");
        desc.appendText("THEN respond with ");
        desc.appendText(this.responder.toString());
                
        return desc.toString();
    }
}