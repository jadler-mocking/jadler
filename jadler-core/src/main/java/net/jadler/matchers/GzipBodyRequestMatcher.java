package net.jadler.matchers;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Matcher which is able to handle request body in gzip format.
 * It handles request body as a gzip only if value of header "Content-Encoding" is "gzip".
 */
public class GzipBodyRequestMatcher extends BodyRequestMatcher {

    private static final String GZIP_CONTENT_ENCODING = "gzip";

    private static final Logger logger = LoggerFactory.getLogger(GzipBodyRequestMatcher.class);

    public GzipBodyRequestMatcher(Matcher<? super String> pred) {
        super(pred);
    }

    @Override
    protected String retrieveValue(HttpServletRequest request) throws Exception {
        logger.trace("Matching request body...");
        InputStream requestBodyInputStream = request.getInputStream();
        if (GZIP_CONTENT_ENCODING.equalsIgnoreCase(request.getHeader("Content-Encoding"))) {
            logger.trace("Request body is in GZIP format.");
            requestBodyInputStream = new GZIPInputStream(requestBodyInputStream);
        }
        final String requestBody = IOUtils.toString(requestBodyInputStream, "utf-8");
        logger.trace("Request body=" + requestBody);
        return requestBody;
    }


    @Factory
    public static GzipBodyRequestMatcher gzipRequestBody(final Matcher<? super String> pred) {
        return new GzipBodyRequestMatcher(pred);
    }

}