package net.jadler.matchers;

import net.jadler.server.MultipleReadsHttpServletRequest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.*;

import static net.jadler.matchers.GzipBodyRequestMatcher.gzipRequestBody;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class GzipBodyRequestMatcherTest {

    private static final String BODY = "Sample body";
    
    @Mock
    private Matcher<String> mockMatcher;
    /** Mock request for handy setup */
    private MockHttpServletRequest mockRequest;
    /** This is the request which is used in by HttpMocker by default - tests should use this request. */
    private MultipleReadsHttpServletRequest request;


    @Before
    public void setUp() throws IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(BODY.getBytes());
        this.mockRequest = request;
        this.request = new MultipleReadsHttpServletRequest(request);
    }

    @Test
    public void retrieveCompressedValue() throws Exception {
        mockRequest.setContent(createGzip(BODY));
        addGzipHeader();
        assertThat(gzipRequestBody(mockMatcher).retrieveValue(request), is(BODY));
    }


    @Test(expected = ZipException.class)
    public void retrieveCompressedValueInIncorrectFormat() throws Exception {
        addGzipHeader();
        gzipRequestBody(mockMatcher).retrieveValue(request);
    }


    @Test
    public void retrieveUncompressedValue() throws Exception {
        assertThat(gzipRequestBody(mockMatcher).retrieveValue(request), is(BODY));
    }
    
    
    @Test
    public void provideDescription() {
        assertThat(gzipRequestBody(mockMatcher).provideDescription(), is("body is"));
    }


    private void addGzipHeader() {
        this.mockRequest.addHeader("Content-Encoding", "gzip");
    }



    private static byte[] createGzip(String plainContent) throws IOException {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final GZIPOutputStream zipOut = new GZIPOutputStream(byteOut);
        zipOut.write(plainContent.getBytes("utf-8"));
        zipOut.close();
        return byteOut.toByteArray();
    }

}
