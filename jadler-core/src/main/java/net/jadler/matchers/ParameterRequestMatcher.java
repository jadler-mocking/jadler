package net.jadler.matchers;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.Validate;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


public class ParameterRequestMatcher extends RequestMatcher<List<String>> {

    private final String paramName;
    private final String desc;


    public ParameterRequestMatcher(final Matcher<? super List<String>> pred, final String paramName) {
        super(pred);

        Validate.notEmpty(paramName, "paramName cannot be empty");
        this.paramName = paramName;
        
        this.desc = "parameter " + paramName + " is";
    }


    @Override
    protected List<String> retrieveValue(final HttpServletRequest req) throws Exception {
        final String[] values = req.getParameterValues(this.paramName);
        if (values == null) {
            return null;
        }
        
        return Arrays.asList(values);
    }
    
    
    @Override
    protected String provideDescription() {
        return this.desc;
    }


    @Factory
    public static ParameterRequestMatcher requestParameter(final String paramName, final Matcher<? super List<String>> pred) {
        return new ParameterRequestMatcher(pred, paramName);
    }
}