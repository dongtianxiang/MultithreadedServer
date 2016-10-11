package test.edu.upenn.cis455;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.upenn.cis.cis455.servlets.MyHttpServletResponse;
import edu.upenn.cis.cis455.webserver.HttpServerConfig;
import junit.framework.TestCase;

/**
 * Test class for MyHttpServletResponse
 * @author dongtianxiang
 *
 */
public class MyHttpServletResponseTest extends TestCase{
	MyHttpServletResponse response;
	public void testA() throws IOException {
		Map<String, String> initMap = new HashMap<>();
		Map<String, String> headerMap = new HashMap<>();
		initMap.put("Type", "GET");
		initMap.put("Path", "/home");
		initMap.put("Protocol", "HTTP/1.0");
		response = new MyHttpServletResponse(null, new HttpServerConfig(8080, "/home", "/home/cis555"), initMap, headerMap);
		assertEquals(response.getBufferSize(), 1024);
		assertEquals(response.getCharacterEncoding(), "ISO-8859-1");
		assertEquals(response.getContentType(), "text/html");
		response.setContentType("image/png");
		assertEquals(response.getContentType(), "image/png");
		assertEquals(response.isCommitted(), false);
	}

}
