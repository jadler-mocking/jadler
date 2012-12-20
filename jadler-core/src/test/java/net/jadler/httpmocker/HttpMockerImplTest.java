package net.jadler.httpmocker;

import java.nio.charset.Charset;
import net.jadler.stubbing.Stubbing;
import net.jadler.stubbing.StubRule;
import net.jadler.stubbing.StubResponse;
import net.jadler.stubbing.StubbingFactory;
import net.jadler.server.jetty.JettyStubHttpServer;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.jadler.exception.JadlerException;
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
import static org.mockito.Mockito.doThrow;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;



public class HttpMockerImplTest {
    
    private static final int DEFAULT_STATUS = 204;
    private static final String HEADER_NAME1 = "h1";
    private static final String HEADER_VALUE1 = "v1";
    private static final String HEADER_NAME2 = "h2";
    private static final String HEADER_VALUE2 = "v2";
     private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-16");
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor1() {
        new HttpMockerImpl(null);
        fail("server cannot be null");
    }
    
    
    @Test
    public void constructor2() {
        new HttpMockerImpl(mock(JettyStubHttpServer.class));
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void startTwice() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyStubHttpServer.class));
        
        mocker.start();
        mocker.start();
        
        fail("mocker cannot be started twice");
    }
    
    
    @Test(expected=JadlerException.class)
    public void startException() throws Exception {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        doThrow(new Exception()).when(server).start();
        new HttpMockerImpl(server).start();
        fail("server threw an exception");
    }
    
    
    @Test
    public void start() throws Exception {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        new HttpMockerImpl(server).start();
        
        verify(server, times(1)).start();
        verifyNoMoreInteractions(server);
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void stopNotStarted() {
        new HttpMockerImpl(mock(JettyStubHttpServer.class)).stop();
        fail("mocker cannot be stopped without being started");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void stopTwice() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyStubHttpServer.class));
        
        mocker.start();
        mocker.stop();
        mocker.stop();
        fail("mocker cannot be stopped twice");
    }
    
    
    @Test(expected=JadlerException.class)
    public void stopException() throws Exception {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        doThrow(new Exception()).when(server).start();
        final HttpMocker mocker = new HttpMockerImpl(server);
        
        mocker.start();
        mocker.stop();
        
        fail("server threw an exception");
    }
    
    
    @Test
    public void stop() throws Exception {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server);
        
        mocker.start();
        mocker.stop();
        
        verify(server, times(1)).stop();
    }
    
    
    @Test
    public void isStarted() throws Exception {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server);
        
        assertThat(mocker.isStarted(), is(false));
        mocker.start();
        assertThat(mocker.isStarted(), is(true));
        mocker.stop();
        assertThat(mocker.isStarted(), is(false));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void setDefaultHeadersWrongParam() {
        new HttpMockerImpl(mock(JettyStubHttpServer.class)).setDefaultHeaders(null);
        fail("defaultHeaders cannot be null");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void setDefaultHeadersWrongState() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyStubHttpServer.class));
        mocker.provideResponseFor(new MockHttpServletRequest());
        mocker.setDefaultHeaders(new MultiValueMap());
        fail("addDefaultHeaders cannot be called after provideResponseFor");
    }

    
    @Test(expected=IllegalArgumentException.class)
    public void setDefaultStatusWrongParam() {
        new HttpMockerImpl(mock(JettyStubHttpServer.class)).setDefaultStatus(-1);
        fail("defaultStatus must be at least 0");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void setDefaultStatusWrongState() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyStubHttpServer.class));
        mocker.provideResponseFor(new MockHttpServletRequest());
        mocker.setDefaultStatus(200);
        fail("setDefaultStatus cannot be called after provideResponseFor");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void setDefaultEncodingWrongParam() {
        new HttpMockerImpl(mock(JettyStubHttpServer.class)).setDefaultEncoding(null);
        fail("defaultEncoding mustn't be null");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void setDefaultEncodingWrongState() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyStubHttpServer.class));
        mocker.provideResponseFor(new MockHttpServletRequest());
        mocker.setDefaultEncoding(Charset.forName("UTF-8"));
        fail("setDefaultStatus cannot be called after provideResponseFor");
    }    
    
    
    @Test(expected=IllegalStateException.class)
    public void onRequestAfterFirstProvision() {
        final HttpMockerImpl mocker = new HttpMockerImpl(mock(JettyStubHttpServer.class));
        
        mocker.provideResponseFor(new MockHttpServletRequest());
        mocker.onRequest();
        fail("The mocker has already provided first response, cannot be configured anymore");
    }
    
    
    @Test
    public void onRequest() {
        final StubRule rule1 = new StubRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(),
                Arrays.asList(new StubResponse()));
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        
        final StubRule rule2 = new StubRule(Collections.<Matcher<? super HttpServletRequest>>emptyList(),
                Arrays.asList(new StubResponse()));
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class))).thenReturn(stubbing1, stubbing2);
        
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
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
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final StubbingFactory sf = spy(new StubbingFactory());
        
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
        final MultiMap defaultHeaders = new MultiValueMap();
        defaultHeaders.put(HEADER_NAME1, HEADER_VALUE1);
        defaultHeaders.put(HEADER_NAME2, HEADER_VALUE2);
        
        mocker.setDefaultStatus(DEFAULT_STATUS);
        mocker.setDefaultHeaders(defaultHeaders);
        mocker.setDefaultEncoding(DEFAULT_ENCODING);

        
          //ok, this is not a pure unit test, it depends on the Stubbing.respond() method as well.
          //the response() method is called so a HttpMockResponse is created internally.
        mocker.onRequest().respond();
        
          //verify the Stubbing instance was created with the given defaults
        verify(sf, times(1)).createStubbing(eq(DEFAULT_ENCODING), eq(DEFAULT_STATUS), eq(defaultHeaders));
        verifyNoMoreInteractions(sf);
    }
    
    
    @Test
    public void onRequestNoDefaultStatus() {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final StubbingFactory sf = spy(new StubbingFactory());
        
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //ok, this is not a pure unit test, it depends on the Stubbing.respond() method as well.
          //the respond() method is called so a HttpMockResponse is created internally.
        mocker.onRequest().respond();
        
          //verify the Stubbing instance was created with empty default headers and default status
        verify(sf, times(1)).createStubbing(any(Charset.class), eq(HttpServletResponse.SC_OK), any(MultiMap.class));
        verifyNoMoreInteractions(sf);
    }
    
    
    @Test
    public void onRequestNoDefaultEncoding() {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final StubbingFactory sf = spy(new StubbingFactory());
        
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //ok, this is not a pure unit test, it depends on the Stubbing.respond() method as well.
          //the respond() method is called so a HttpMockResponse is created internally.
        mocker.onRequest().respond();
        
          //verify the Stubbing instance was created with empty default headers and default status
        verify(sf, times(1)).createStubbing(eq(Charset.forName("UTF-8")), anyInt(), any(MultiMap.class));
        verifyNoMoreInteractions(sf);
    }
    
    
    @Test
    public void onRequestNoDefaultHeaders() {
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final StubbingFactory sf = spy(new StubbingFactory());
        
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //ok, this is not a pure unit test, it depends on the Stubbing.respond() method as well.
          //the respond() method is called so a HttpMockResponse is created internally.
        mocker.onRequest().respond();
        
          //verify the Stubbing instance was created with empty default headers and default status
        verify(sf, times(1)).createStubbing(any(Charset.class), anyInt(), eq(new MultiValueMap()));
        verifyNoMoreInteractions(sf);
    }
    
    
    @Test
    public void provideResponseFor() {
        final MockHttpServletRequest req = new MockHttpServletRequest();
    
          //exactly 1 rule matches
        final StubRule rule1 = mock(StubRule.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matchedBy(eq(req))).thenReturn(true);
        final StubResponse resp1 = new StubResponse();
        when(rule1.nextResponse()).thenReturn(resp1);
        
        final StubRule rule2  = mock(StubRule.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matchedBy(eq(req))).thenReturn(false);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class)))
                .thenReturn(stubbing1, stubbing2);
        
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
        
        assertThat(mocker.provideResponseFor(req), is(resp1));
    }
    
    
    @Test
    public void provideResponseFor2() {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        
          //no rule matches
        final StubRule rule1 = mock(StubRule.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matchedBy(eq(req))).thenReturn(false);
        
        
        final StubRule rule2  = mock(StubRule.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matchedBy(eq(req))).thenReturn(false);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class)))
                .thenReturn(stubbing1, stubbing2);
        
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
        
        assertThat(mocker.provideResponseFor(req), is(nullValue()));
    }
    
    
    @Test
    public void provideResponseFor3() {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        
          //two rules matches, the first one must be provided
        final StubRule rule1 = mock(StubRule.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matchedBy(eq(req))).thenReturn(true);
        final StubResponse resp1 = new StubResponse();
        when(rule1.nextResponse()).thenReturn(resp1);
        
        final StubRule rule2  = mock(StubRule.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matchedBy(eq(req))).thenReturn(true);
        final StubResponse resp2 = new StubResponse();
        when(rule2.nextResponse()).thenReturn(resp2);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class)))
                .thenReturn(stubbing1, stubbing2);
        
        final JettyStubHttpServer server = mock(JettyStubHttpServer.class);
        final HttpMockerImpl mocker = new HttpMockerImpl(server, sf);
        
          //calling the onRequest twice so stubbing1 and stubbing2 are created in the HttpMocker instance
        mocker.onRequest();
        mocker.onRequest();
        
        assertThat(mocker.provideResponseFor(req), is(resp1));
    }
}
