package edu.upenn.cis.cis455.servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis.cis455.webserver.HttpServerConfig;
import edu.upenn.cis.cis455.testServlets.*;

public class ServletContainer {
	private MyHandler h;
	private ServerServletContext context;
	private HashMap<String,HttpServlet> servletMap;
	
	
	public ServletContainer(String webdotxml){
		try {
			h = new MyHandler();
			parseWebdotxml(webdotxml);
			context = createContext(h);
			servletMap = createServlets(h, context);
		} catch (ParserConfigurationException e){
			
		} catch (SAXException e) {
			
		} catch (IOException e) {
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
			e.printStackTrace();  // Comes from createServlets 
		}
	}
	
	private MyHandler parseWebdotxml(String webdotxml) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	private ServerServletContext createContext(MyHandler h) {
		ServerServletContext context = new ServerServletContext();
		for (String param : h.m_contextParams.keySet()) {
			context.setInitParams(param, h.m_contextParams.get(param));
		}
		return context;
	}
	
	private HashMap<String,HttpServlet> createServlets(MyHandler h, ServerServletContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ServletException {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			ServerServletConfig config = new ServerServletConfig(servletName, context);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParams(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}
	
	public String lookUp(String URL) {
		return h.m_urlPattern.get(URL);
	}
	
	
	/**
	 * The class we are going to call after we have make sure the given URL pattern matches one of the servlet 
	 * @param c
	 * @param client
	 * @param initMap
	 * @param headerMap
	 */
	public void dispatchRequest(HttpServerConfig c, Socket s, String servletName, Map<String, String> initMap, Map<String, String> headerMap){
		HttpServlet servlet = servletMap.get(servletName);
//		System.out.println(servletName + "has been initialized");
		
		/* How to set the time-out for sessions? Is it from web.xml? */
		//long sessionInterval = h.timeout;
		long sessionTimeout = 30;
		
		try {
			MyHttpServletRequest request = new MyHttpServletRequest(c, s, initMap, headerMap, "", sessionTimeout);
			MyHttpServletResponse response = new MyHttpServletResponse(s.getOutputStream(), c, initMap, headerMap);
			servlet.service(request, response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
