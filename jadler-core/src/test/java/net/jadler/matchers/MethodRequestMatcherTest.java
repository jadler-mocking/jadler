package net.jadler.matchers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static net.jadler.matchers.MethodRequestMatcher.requestMethod;
import static org.hamcrest.Matchers.*;


@RunWith(MockitoJUnitRunner.class)
public class MethodRequestMatcherTest {
    
    private static final String METHOD = "GET";

    private MockHttpServletRequest request;
    
    @Mock
    Matcher<String> mockMatcher;

    @Before
    public void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.request.setMethod(METHOD);
    }

    @Test
    public void retrieveValue() throws Exception {
        assertThat(requestMethod(mockMatcher).retrieveValue(request), is(METHOD));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestMethod(mockMatcher).provideDescription(), is("method is"));
    }
}
