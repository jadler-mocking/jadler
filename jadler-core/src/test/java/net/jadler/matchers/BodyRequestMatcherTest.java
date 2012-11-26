package net.jadler.matchers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static net.jadler.matchers.BodyRequestMatcher.requestBody;


@RunWith(MockitoJUnitRunner.class)
public class BodyRequestMatcherTest {

    private static final String BODY = "Sample body";
    
    private MockHttpServletRequest request;
    
    @Mock
    private Matcher<String> mockMatcher;


    @Before
    public void setUp() {
        this.request = new MockHttpServletRequest();
        this.request.setContent(BODY.getBytes());
    }

    
    @Test
    public void retrieveValue() throws Exception {
        assertThat(requestBody(mockMatcher).retrieveValue(request), is(BODY));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(requestBody(mockMatcher).provideDescription(), is("body is"));
    }
    
}
