package test.edu.upenn.cis455;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.upenn.cis.cis455.servlets.MyHttpServletRequest;
import edu.upenn.cis.cis455.servlets.MyHttpSession;
import edu.upenn.cis.cis455.webserver.HttpServerConfig;
import junit.framework.TestCase;

/**
 * Test class for MyHttpSession
 * @author dongtianxiang
 *
 */
public class MyHttpSessionTest extends TestCase {
    MyHttpServletRequest request;
    MyHttpSession testSession1 = new MyHttpSession(30000);
    MyHttpSession testSession2 = new MyHttpSession(60000);
    MyHttpSession testSession3 = new MyHttpSession(20000);
    MyHttpSession testSession4 = new MyHttpSession(10000);
    MyHttpSession testSession5 = new MyHttpSession(0);
    
    public void testA() throws IOException {
        Map<String, String> initialLineDict = new HashMap<String, String>();
        Map<String, String> headLinesDict = new HashMap<String, String>();

        request = new MyHttpServletRequest(new HttpServerConfig(8080, "/home", "/home/cis555/web.xml"), new Socket(), initialLineDict, headLinesDict, "", 30);
        
        assertNotSame(testSession1.getId(), testSession2.getId());
        assertNotSame(testSession2.getId(), testSession3.getId());
        assertNotSame(testSession3.getId(), testSession4.getId());
        assertNotSame(testSession4.getId(), testSession5.getId());

        
        assertEquals(testSession1.isValid(), true);
        assertEquals(testSession2.isValid(), true);
        assertEquals(testSession3.isValid(), true);
        assertEquals(testSession4.isValid(), true);
        assertEquals(testSession5.isValid(), false);
        
        testSession1.invalidate();
        testSession2.invalidate();
        testSession3.invalidate();
        testSession4.invalidate();
        testSession5.invalidate();
        
        assertEquals(testSession1.isValid(), false);
        assertEquals(testSession2.isValid(), false);
        assertEquals(testSession3.isValid(), false);
        assertEquals(testSession4.isValid(), false);
        assertEquals(testSession5.isValid(), false);
    }
}