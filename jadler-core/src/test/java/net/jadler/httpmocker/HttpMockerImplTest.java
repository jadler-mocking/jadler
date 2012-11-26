package net.jadler.httpmocker;

import net.jadler.stubbing.Stubbing;
import net.jadler.rule.HttpMockRule;
import net.jadler.rule.HttpMockResponse;
import net.jadler.httpmocker.HttpMockerImpl;
import net.jadler.stubbing.StubbingFactory;
import net.jadler.server.jetty.JettyMockHttpServer;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;



public class HttpMockerImplTest {
    
    private static final int DEFAULT_STATUS = 204;
    private static final String HEADER_NAME1 = "h1";
    private static final String HEADER_VALUE1 = "v1";
    private static final String HEADER_NAME2 = "h2";
    private static final String HEADER_VALUE2 = "v2";
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor1() {
        new HttpMockerImpl(null);
        fail("server cannot be null");
    }
    
    
    @Test
    public void constructor2() {
        new HttpMockerImpl(mock(JettyMockHttpServer.class));
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void startTwice() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyMockHttpServer.class));
        
        mocker.start();
        mocker.start();
        
        fail("mocker cannot be started twice");
    }
    
    
    @Test
    public void start() throws Exception {
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        new HttpMockerImpl(server).start();
        
        verify(server, times(1)).start();
        verifyNoMoreInteractions(server);
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void stopNotStarted() {
        new HttpMockerImpl(mock(JettyMockHttpServer.class)).stop();
        fail("mocker cannot be stopped without being started");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void stopTwice() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyMockHttpServer.class));
        
        mocker.start();
        mocker.stop();
        mocker.stop();
        fail("mocker cannot be stopped twice");
    }
    
    
    @Test
    public void stop() throws Exception {
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server);
        
        mocker.start();
        mocker.stop();
        
        verify(server, times(1)).stop();
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void addDefaultHeader() {
        new HttpMockerImpl(mock(JettyMockHttpServer.class)).addDefaultHeaders(null);
        fail("defaultHeaders cannot be null");
    }

    
    @Test(expected=IllegalArgumentException.class)
    public void setDefaultStatus() {
        new HttpMockerImpl(mock(JettyMockHttpServer.class)).setDefaultStatus(-1);
        fail("defaultStatus must be at least 0");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void onRequestAfterFirstProvision() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyMockHttpServer.class));
        
        mocker.provideResponseFor(new MockHttpServletRequest());
        mocker.onRequest();
        fail("The mocker has already provided first response, cannot be configured anymore");
    }
    
    
    @Test
    public void onRequest() {
        final HttpMockRule rule1 = new HttpMockRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(),
                Arrays.asList(new HttpMockResponse()));
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        
        final HttpMockRule rule2 = new HttpMockRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(),
                Arrays.asList(new HttpMockResponse()));
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(MultiMap.class), anyInt())).thenReturn(stubbing1, stubbing2);
        
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
          //calling provideResponseFor so mock rules are generated from stubing objects
        mocker.provideResponseFor(new MockHttpServletRequest());
        
        assertThat(mocker.getHttpMockRules(), contains(rule1, rule2));
    }
    
    
    @Test
    public void onRequestWithDefaults() {
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final StubbingFactory sf = spy(new StubbingFactory());
        
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
        final MultiMap defaultHeaders = new MultiValueMap();
        defaultHeaders.put(HEADER_NAME1, HEADER_VALUE1);
        defaultHeaders.put(HEADER_NAME2, HEADER_VALUE2);
        
        mocker.setDefaultStatus(DEFAULT_STATUS);
        mocker.addDefaultHeaders(defaultHeaders);
        
          //ok, this is not a pure unit test, it depends on the Stubbing.response() method as well.
          //the response() method is called so a HttpMockResponse is created internally.
        mocker.onRequest().respond();
        mocker.start();
        
          //verify the Stubbing instance was created with the given defaults
        verify(sf, times(1)).createStubbing(eq(defaultHeaders), eq(DEFAULT_STATUS));
        verifyNoMoreInteractions(sf);
    }
    
    
    @Test
    public void onRequestNoDefaultStatus() {
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final StubbingFactory sf = spy(new StubbingFactory());
        
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //ok, this is not a pure unit test, it depends on the Stubbing.respond() method as well.
          //the respond() method is called so a HttpMockResponse is created internally.
        mocker.onRequest().respond();
        mocker.start();
        
          //verify the Stubbing instance was created with empty default headers and default status
        verify(sf, times(1)).createStubbing(eq(new MultiValueMap()), eq(HttpServletResponse.SC_OK));
        verifyNoMoreInteractions(sf);
    }
    
    
    @Test
    public void provideRuleFor() {
        final MockHttpServletRequest req = new MockHttpServletRequest();
    
          //exactly 1 rule matches
        final HttpMockRule rule1 = mock(HttpMockRule.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(true);
        final HttpMockResponse resp1 = new HttpMockResponse();
        when(rule1.nextResponse()).thenReturn(resp1);
        
        final HttpMockRule rule2  = mock(HttpMockRule.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(false);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(MultiMap.class), anyInt())).thenReturn(stubbing1, stubbing2);
        
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
        mocker.start();
        
        assertThat(mocker.provideResponseFor(req), is(resp1));
    }
    
    
    @Test
    public void provideRuleFor2() {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        
          //no rule matches
        final HttpMockRule rule1 = mock(HttpMockRule.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(false);
        
        
        final HttpMockRule rule2  = mock(HttpMockRule.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(false);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(MultiMap.class), anyInt())).thenReturn(stubbing1, stubbing2);
        
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
        mocker.start();
        
        assertThat(mocker.provideResponseFor(req), is(nullValue()));
    }
    
    
    @Test
    public void provideRuleFor3() {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        
          //two rules matches, the first one must be provided
        final HttpMockRule rule1 = mock(HttpMockRule.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(true);
        final HttpMockResponse resp1 = new HttpMockResponse();
        when(rule1.nextResponse()).thenReturn(resp1);
        
        final HttpMockRule rule2  = mock(HttpMockRule.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(true);
        final HttpMockResponse resp2 = new HttpMockResponse();
        when(rule2.nextResponse()).thenReturn(resp2);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(MultiMap.class), anyInt())).thenReturn(stubbing1, stubbing2);
        
        final JettyMockHttpServer server = mock(JettyMockHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
        mocker.start();
        
        assertThat(mocker.provideResponseFor(req), is(resp1));
    }
}
