package test.edu.upenn.cis455;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import edu.upenn.cis.cis455.servlets.ServletContainer;
import edu.upenn.cis.cis455.webserver.HttpServerConfig;
import junit.framework.TestCase;

public class ServletContainerTest extends TestCase{
	ServletContainer container = new ServletContainer("./conf/web.xml");
	public void testA() {
		assertEquals(container.lookUp("/demo"), "demo");
		assertEquals(container.lookUp("/init/foo"), "init");
		assertEquals(container.lookUp("/init/foo/demo/abs"), "demo");
		assertEquals(container.lookUp("/cookie1"), "cookie1");
		assertEquals(container.lookUp("/cookie2"), "cookie2");
		assertEquals(container.lookUp("/cookie3"), "cookie3");
		assertEquals(container.lookUp("/session1"), "session1");
		assertEquals(container.lookUp("/session2"), "session2");
		assertEquals(container.lookUp("/session3"), "session3");
		assertEquals(container.lookUp("/init"), "init");
		assertEquals(container.lookUp("/init?key=value"), "init");
		assertNull(container.lookUp("/ini"));
		assertEquals(container.getQueryString("/init?key=value"), "key=value");
	}
}
