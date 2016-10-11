package test.edu.upenn.cis455;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;
import edu.upenn.cis.cis455.servlets.MyHttpServletRequest;
import edu.upenn.cis.cis455.webserver.HttpServerConfig;

/**
 * Test class for MyHttpServletRequest
 * @author dongtianxiang
 *
 */
public class MyHttpServletRequestTest extends TestCase{
    MyHttpServletRequest request;
    public void testA() throws IOException {
        MyHttpServletRequest request1;
        Map<String, String> initialLineDict = new HashMap<String, String>();
        Map<String, String> headLinesDict = new HashMap<String, String>();
        initialLineDict.put("Type", "GET");
        HttpServerConfig c = new HttpServerConfig(8080, "/home", "/home/cis555/web.xml");
        request1 = new MyHttpServletRequest(c, new Socket(), initialLineDict, headLinesDict, "", 30);
        assertEquals(request1.getScheme(), "http");
        assertEquals(request1.getCharacterEncoding(), "ISO-8859-1");
        assertEquals(request1.getServerName(), "Tianxiang");
        assertEquals(request1.getMethod(), "GET");
        assertNull(request1.getPathTranslated());
        assertEquals(request1.getContextPath(), "");
        assertEquals(request1.isUserInRole(""), false);
        assertNull(request1.getUserPrincipal());
        assertNull(request1.getPathTranslated());
    }
}
