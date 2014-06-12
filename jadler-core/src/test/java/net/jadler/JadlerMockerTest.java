/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler;

import java.net.URI;
import net.jadler.stubbing.server.StubHttpServerManager;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import net.jadler.stubbing.Stubbing;
import net.jadler.stubbing.HttpStub;
import net.jadler.stubbing.StubResponse;
import net.jadler.stubbing.StubbingFactory;
import net.jadler.exception.JadlerException;
import net.jadler.mocking.Verifying;
import net.jadler.stubbing.server.StubHttpServer;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;


public class JadlerMockerTest {
    
    private static final int DEFAULT_STATUS = 204;
    private static final String HEADER_NAME1 = "h1";
    private static final String HEADER_VALUE1 = "v1";
    private static final String HEADER_NAME2 = "h2";
    private static final String HEADER_VALUE2 = "v2";
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-16");
    private static final int PORT = 12345;
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor1() {
        new JadlerMocker(null);
        fail("server cannot be null");
    }
    
    
    @Test
    public void constructor2() {
        new JadlerMocker(mock(StubHttpServer.class));
    }
    

    @Test(expected=IllegalArgumentException.class)
    public void constructor3() {
        new JadlerMocker(null, null);
        fail("neither server nor stubbing factory can be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor4() {
        new JadlerMocker(null, mock(StubbingFactory.class));
        fail("server cannot be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void constructor5() {
        new JadlerMocker(mock(StubHttpServer.class), null);
        fail("stubbing factory cannot be null");
    }
    
    @Test
    public void constructor6() {
        new JadlerMocker(mock(StubHttpServer.class), mock(StubbingFactory.class));
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void startNotStopped() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        
        mocker.start();
        mocker.start();
        
        fail("mocker has been started already, cannot be started again without being stopped first");
    }
    
    
    @Test(expected=JadlerException.class)
    public void startException() throws Exception {
        final StubHttpServer server = mock(StubHttpServer.class);
        doThrow(new Exception()).when(server).start();
        new JadlerMocker(server).start();
        fail("server threw an exception");
    }
    
    
    @Test
    public void start() throws Exception {
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker jadlerMocker = new JadlerMocker(server);
        jadlerMocker.start();
        
        verify(server, times(1)).start();
        verify(server, times(1)).registerRequestManager(eq(jadlerMocker));
        verifyNoMoreInteractions(server);
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void closeNotStarted() {
        new JadlerMocker(mock(StubHttpServer.class)).close();
        fail("mocker cannot be stopped without being started");
    }
    
    
    @Test(expected=JadlerException.class)
    public void closeException() throws Exception {
        final StubHttpServer server = mock(StubHttpServer.class);
        doThrow(new Exception()).when(server).start();
        final StubHttpServerManager mocker = new JadlerMocker(server);
        
        mocker.start();
        mocker.close();
        
        fail("server threw an exception");
    }
    
    
    @Test
    public void close() throws Exception {
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker mocker = new JadlerMocker(server);
        
        mocker.start();
        mocker.close();
        
        verify(server, times(1)).stop();
    }
    
    
    @Test
    public void isStarted() throws Exception {
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker jadlerMocker = new JadlerMocker(server);
        
        assertThat(jadlerMocker.isStarted(), is(false));
        jadlerMocker.start();
        assertThat(jadlerMocker.isStarted(), is(true));
        jadlerMocker.close();
        assertThat(jadlerMocker.isStarted(), is(false));
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void getStubHttpServerPortNotStarted() {       
        final StubHttpServerManager serverManager = new JadlerMocker(mock(StubHttpServer.class));
        serverManager.getStubHttpServerPort();
        fail("server has not been started yet, cannot retrieve the port number");
    }
    
    
    @Test
    public void getStubHttpServerPort() {
        final StubHttpServer server = mock(StubHttpServer.class);
        when(server.getPort()).thenReturn(PORT);
        
        final StubHttpServerManager serverManager = new JadlerMocker(server);
        serverManager.start();
        assertThat(serverManager.getStubHttpServerPort(), is(PORT));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void addDefaultHeaderWrongParam1() {
        new JadlerMocker(mock(StubHttpServer.class)).addDefaultHeader(null, "abcd");
        fail("default header name cannot be null");
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void addDefaultHeaderWrongParam2() {
        new JadlerMocker(mock(StubHttpServer.class)).addDefaultHeader("abcd", null);
        fail("default header value cannot be null");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void addDefaultHeaderWrongState() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        
          //calling provideStubResponseFor finishes the configuration phase, a default header cannot be added anymore
        mocker.provideStubResponseFor(prepareEmptyMockRequest());
        
        mocker.addDefaultHeader("abcd", "efgh");
        fail("default header cannot be added anymore");
    }
    
    
    @Test
    public void addDefaultHeader1() {
          //empty header value is valid
        new JadlerMocker(mock(StubHttpServer.class)).addDefaultHeader("abcd", "");
    }
    
    
    @Test
    public void addDefaultHeader2() {
        new JadlerMocker(mock(StubHttpServer.class)).addDefaultHeader("abcd", "efgh");
    }

    
    @Test(expected=IllegalArgumentException.class)
    public void setDefaultStatusWrongParam() {
        new JadlerMocker(mock(StubHttpServer.class)).setDefaultStatus(-1);
        fail("defaultStatus must be at least 0");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void setDefaultStatusWrongState() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        mocker.provideStubResponseFor(prepareEmptyMockRequest());
        mocker.setDefaultStatus(200);
        fail("default status cannot be set anymore");
    }
    
    
    @Test
    public void setDefaultStatus() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        mocker.setDefaultStatus(200);
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void setDefaultEncodingWrongParam() {
        new JadlerMocker(mock(StubHttpServer.class)).setDefaultEncoding(null);
        fail("defaultEncoding mustn't be null");
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void setDefaultEncodingWrongState() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        mocker.provideStubResponseFor(prepareEmptyMockRequest());
        mocker.setDefaultEncoding(DEFAULT_ENCODING);
        fail("default encoding cannot be set anymore");
    }
    
    
    @Test
    public void setDefaultEncoding() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        mocker.setDefaultEncoding(DEFAULT_ENCODING);
    } 
    
    
    @Test(expected=IllegalStateException.class)
    public void onRequestWrongState() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        mocker.provideStubResponseFor(prepareEmptyMockRequest());
        mocker.onRequest();
        fail("The mocker has already provided first response, cannot do any stubbing anymore");
    }
    

    @Test
    public void onRequest() {
        final Stubbing stubbing = mock(Stubbing.class);
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class))).thenReturn(stubbing);
        
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
        final Stubbing result = (Stubbing) mocker.onRequest();
        
        assertThat(result, is(stubbing));
    }

    
    /*
     * Tests that defaults (status, headers, encoding) are used correctly when creating a stubbing instance.
     */
    @Test
    public void onRequestWithDefaults() {
        final StubHttpServer server = mock(StubHttpServer.class);
        final StubbingFactory sf = mock(StubbingFactory.class);
        
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
        mocker.setDefaultStatus(DEFAULT_STATUS);
        mocker.addDefaultHeader(HEADER_NAME1, HEADER_VALUE1);
        mocker.addDefaultHeader(HEADER_NAME2, HEADER_VALUE2);
        mocker.setDefaultEncoding(DEFAULT_ENCODING);

        mocker.onRequest();
        
          //verify the Stubbing instance was created with the given defaults
        final MultiMap defaultHeaders = new MultiValueMap();
        defaultHeaders.put(HEADER_NAME1, HEADER_VALUE1);
        defaultHeaders.put(HEADER_NAME2, HEADER_VALUE2);
        verify(sf, times(1)).createStubbing(eq(DEFAULT_ENCODING), eq(DEFAULT_STATUS), eq(defaultHeaders));
    }
    
    
    /*
     * Tests that if no default status is set, 200 is used as a super-default
     */
    @Test
    public void onRequestNoDefaultStatus() {
        final StubHttpServer server = mock(StubHttpServer.class);
        final StubbingFactory sf = mock(StubbingFactory.class);
        
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
        mocker.onRequest();
        
          //verify the Stubbing instance was created with the default 200 response status
        verify(sf, times(1)).createStubbing(any(Charset.class), eq(200), any(MultiMap.class));
    }
    
    
    /*
     * 
     */
    @Test
    public void onRequestNoDefaultEncoding() {
        final StubHttpServer server = mock(StubHttpServer.class);
        final StubbingFactory sf = mock(StubbingFactory.class);
        
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
        mocker.onRequest();
        
          //verify the Stubbing instance was created with the default UTF-8 response encoding
        verify(sf, times(1)).createStubbing(eq(Charset.forName("UTF-8")), anyInt(), any(MultiMap.class));
    }
    
    
    @Test
    public void onRequestNoDefaultHeaders() {
        final StubHttpServer server = mock(StubHttpServer.class);
        final StubbingFactory sf = mock(StubbingFactory.class);
        
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
        mocker.onRequest();
        
          //verify the Stubbing instance was created with no default headers
        verify(sf, times(1)).createStubbing(any(Charset.class), anyInt(), eq(new MultiValueMap()));
    }
    
    
    //following provideResponseFor() tests are far from being just standard unit tests since they
    //need a cooperation of two or more JadlerMocker methods.
    
    @Test
    public void provideStubResponseFor() {
        final Request req = prepareEmptyMockRequest();
    
          //rule1 matches the given request (param of the provideResponseFor method) so it must be returned from
          //the tested method
        final HttpStub rule1 = mock(HttpStub.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(true);
        final StubResponse resp1 = StubResponse.EMPTY;
        when(rule1.nextResponse(eq(req))).thenReturn(resp1);
        
          //rule2 doesn't match the given request
        final HttpStub rule2  = mock(HttpStub.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(false);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class))).thenReturn(stubbing1, stubbing2);
        
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
          //calling onRequest twice so stubbing1 and stubbing2 are created in the JadlerMocker instance
        mocker.onRequest();
        mocker.onRequest();
        
        assertThat(mocker.provideStubResponseFor(req), is(resp1));
    }
    
    
    @Test
    public void provideStubResponseFor2() {
        final Request req = prepareEmptyMockRequest();
        
          //neither rule1 nor rule2 matches, default not-found response must be returned
        final HttpStub rule1 = mock(HttpStub.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(false);
        
        
        final HttpStub rule2  = mock(HttpStub.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(false);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class)))
                .thenReturn(stubbing1, stubbing2);
        
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
          //calling onRequest twice so stubbing1 and stubbing2 are created in the JadlerMocker instance
        mocker.onRequest();
        mocker.onRequest();
        
        final StubResponse res = mocker.provideStubResponseFor(req);
        assertThat(res, is(not(nullValue())));
        assertThat(res.getStatus(), is(404));
        assertThat(res.getDelay(), is(0L));
        assertThat(res.getBody(), is("No stub response found for the incoming request".getBytes()));
        assertThat(res.getEncoding(), is(Charset.forName("UTF-8")));
        
        final KeyValues expectedHeaders = new KeyValues().add("Content-Type", "text/plain; charset=utf-8");
        assertThat(res.getHeaders(), is(expectedHeaders));
    }
    
    
    @Test
    public void provideStubResponseFor3() {
        final Request req = prepareEmptyMockRequest();
        
          //both rules matches the request, the latter must be provided
        final HttpStub rule1 = mock(HttpStub.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(true);
        final StubResponse resp1 = StubResponse.EMPTY;
        when(rule1.nextResponse(eq(req))).thenReturn(resp1);
        
        final HttpStub rule2  = mock(HttpStub.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(true);
        final StubResponse resp2 = StubResponse.EMPTY;
        when(rule2.nextResponse(eq(req))).thenReturn(resp2);
        
        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class)))
                .thenReturn(stubbing1, stubbing2);
        
        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker mocker = new JadlerMocker(server, sf);
        
          //calling onRequest twice so stubbing1 and stubbing2 are created in the JadlerMocker instance
        mocker.onRequest();
        mocker.onRequest();
        
        assertThat(mocker.provideStubResponseFor(req), is(resp2));
    }

    @Test
    public void reset() {
        final Request req = prepareEmptyMockRequest();

        final HttpStub rule1 = mock(HttpStub.class);
        final Stubbing stubbing1 = mock(Stubbing.class);
        when(stubbing1.createRule()).thenReturn(rule1);
        when(rule1.matches(eq(req))).thenReturn(true);
        final StubResponse resp1 = StubResponse.builder().build();
        when(rule1.nextResponse(eq(req))).thenReturn(resp1);
        
        final HttpStub rule2 = mock(HttpStub.class);
        final Stubbing stubbing2 = mock(Stubbing.class);
        when(stubbing2.createRule()).thenReturn(rule2);
        when(rule2.matches(eq(req))).thenReturn(true);
        final StubResponse resp2 = StubResponse.builder().build();
        when(rule2.nextResponse(eq(req))).thenReturn(resp2);

        final StubbingFactory sf = mock(StubbingFactory.class);
        when(sf.createStubbing(any(Charset.class), anyInt(), any(MultiMap.class))).thenReturn(stubbing1, stubbing2);

        final StubHttpServer server = mock(StubHttpServer.class);
        final JadlerMocker mocker = new JadlerMocker(server, sf);

        //calling onRequest so stubbing1 is created in the JadlerMocker instance
        mocker.onRequest();
        assertThat(mocker.provideStubResponseFor(req), is(resp1));

        mocker.reset();

        //calling onRequest stubbing2 is created in the JadlerMocker instance
        mocker.onRequest();
        assertThat(mocker.provideStubResponseFor(req), is(resp2));
    }
    
    
    @Test
    public void verifyThatRequest() {
        final Verifying ongoingVerifying = new JadlerMocker(mock(StubHttpServer.class)).verifyThatRequest();
        assertThat(ongoingVerifying, is(not(nullValue())));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void numberOfRequestsMatchingInvalidArgument() {
        new JadlerMocker(mock(StubHttpServer.class)).numberOfRequestsMatching(null);
        fail("matchers cannot be null");
    }
    
    
    @Test
    public void numberOfRequestsMatchingNoReceivedRequest() {
        
        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m1 = mock(Matcher.class);
        when(m1.matches(anyObject())).thenReturn(true);
        
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        final int cnt = mocker.numberOfRequestsMatching(Collections.<Matcher<? super Request>>singletonList(m1));
        
        assertThat(cnt, is(0));  //no request received yet, must be zero
    }
    
    
    @Test
    public void numberOfRequestsMatchingNoPredicates() {        
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        
          //calling provideStubResponseFor for all three requests so these get recorder in the mocker
        mocker.provideStubResponseFor(mock(Request.class));
        mocker.provideStubResponseFor(mock(Request.class));
        mocker.provideStubResponseFor(mock(Request.class));
        
        final int cnt = mocker.numberOfRequestsMatching(Collections.<Matcher<? super Request>>emptyList());
        
        assertThat(cnt, is(3));
    }
    
    
    @Test
    public void numberOfRequestsMatching() {
        final Request req1 = mock(Request.class);
        final Request req2 = mock(Request.class);
        final Request req3 = mock(Request.class);
        
        @SuppressWarnings("unchecked")
        final Matcher<? super Request> m1 = mock(Matcher.class);
        when(m1.matches(req1)).thenReturn(true);
        when(m1.matches(req2)).thenReturn(false);
        when(m1.matches(req3)).thenReturn(true);
        
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        
          //calling provideStubResponseFor for all three requests so these get recorder in the mocker
        mocker.provideStubResponseFor(req1);
        mocker.provideStubResponseFor(req2);
        mocker.provideStubResponseFor(req3);
        
        final Collection<Matcher<? super Request>> singletonMatcher =
                Collections.<Matcher<? super Request>>singletonList(m1);
        
        assertThat(mocker.numberOfRequestsMatching(singletonMatcher), is(2));
        
        mocker.reset();
        
        assertThat(mocker.numberOfRequestsMatching(singletonMatcher), is(0));
    }

    @Test(expected = IllegalStateException.class)
    public void noRequestRecording() {
        final JadlerMocker mocker = new JadlerMocker(mock(StubHttpServer.class));
        mocker.setRecordRequests(false);
        mocker.verifyThatRequest();
    }
    
    
    private Request prepareEmptyMockRequest() {
        return Request.builder()
                .method("GET")
                .requestURI(URI.create("http://localhost/"))
                .build();
    }
}
