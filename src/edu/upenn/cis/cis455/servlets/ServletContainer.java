package edu.upenn.cis.cis455.servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

/**
 * Important class as a servlet container, which is responsible for parsing XML, loading servlets
 * The class is also responsible for dispatching incoming URL into mapped servlets.
 * @author cis555
 *
 */
public class ServletContainer {
	private static MyHandler h;
	private ServerServletContext context;
	private static HashMap<String,HttpServlet> servletMap;
	public static HashMap<String, MyHttpSession> sessionCache = new HashMap<>();
	
	/**
	 * The constructor 
	 * @param webdotxml The file path of xml file
	 */
	public ServletContainer(String webdotxml){
		try {
			h = new MyHandler();
			parseWebdotxml(webdotxml);
			context = createContext(h);
			servletMap = createServlets(h, context);
		} catch (ParserConfigurationException e){
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
		} catch (SAXException e) {
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
		} catch (IOException e) {
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
			HttpErrorLog.addError(e.getMessage()+ "\n\n"); // Comes from createServlets 
		}
	}
	
	/**
	 * Private class used to parse the XML file
	 * @param webdotxml
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
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
	
	/**
	 * Use the parameters from XML files to create servlet context
	 * @param h
	 * @return
	 */
	private ServerServletContext createContext(MyHandler h) {
		ServerServletContext context = new ServerServletContext();
		for (String param : h.m_contextParams.keySet()) {
			context.setInitParams(param, h.m_contextParams.get(param));
		}
		return context;
	}
	
	/**
	 * Load all servlets on startup into memory.
	 * @param h
	 * @param context
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ServletException
	 */
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
	
	/**
	 * Check whether the incoming URL can match one of the servlets
	 * @param URL
	 * @return
	 */
	public String lookUp(String URL) {
		String noQueryURL = "";
		if(URL.contains("?")) {
			String[] split = URL.split("\\?+");
			URL = split[0];
		}
		noQueryURL = URL;
		String res = "";
		for(String pattern : h.m_urlPattern.keySet()) {
			int len = matchString(pattern, noQueryURL);
			if(len != -1 && len > res.length()) res = pattern;
		}
		return h.m_urlPattern.get(res);
	}
	
	/**
	 * To get the reminder of URL after the portion which has been mapped into servlets
	 * @param URL
	 * @return
	 */
	public static String getPathInfo(String URL){
		String noQueryURL = "";
		if(URL.contains("?")) {
			String[] split = URL.split("\\?+");
			URL = split[0];
		}
		noQueryURL = URL;
		String res = "";
		for(String pattern : h.m_urlPattern.keySet()) {
			int len = matchString(pattern, noQueryURL);
			if(len != -1 && len > res.length()) res = pattern;
		}
		if( res.charAt(res.length() - 1) == '*' ) res = res.substring(0, res.length() - 1);
		String pathInf = URL.substring(res.length());
		if(pathInf.length() >= 1 && pathInf.charAt(0) != '/') pathInf = "/" + pathInf;
		return pathInf;
	}
	
	/**
	 * To get the queryString from URL
	 * @param URL
	 * @return
	 */
	public String getQueryString(String URL) {
		String[] split = URL.split("\\?+");
		if(split.length <= 1) return "";
		else return split[1];
	}
	
	/**
	 * To check the longest pattern the URL can match, return -1 if nothing has been matched
	 * @param pattern
	 * @param URL
	 * @return
	 */
	public static int matchString(String pattern, String URL) {
		if( !pattern.contains("*") ) {
			if( pattern.equals(URL)) return pattern.length();
			else return -1;
		} else {
			if(pattern.charAt(pattern.length() - 1) == '*' && pattern.charAt(pattern.length() - 2) == '/') {
				pattern = pattern.substring(0, pattern.length() - 2);
				if(URL.contains(pattern)) return pattern.length();
				else return -1;
			}
			pattern = pattern.substring(0, pattern.length() - 1);
			if(URL.contains(pattern)) return pattern.length();
			else return -1;
		}
	}
	
	/**
	 * The class we are going to call after we have make sure the given URL pattern matches one of the servlet 
	 * @param c
	 * @param client
	 * @param initMap
	 * @param headerMap
	 * @throws IOException 
	 * @throws ServletException 
	 */
	public void dispatchRequest(HttpServerConfig c, Socket s, String servletName, Map<String, String> initMap, Map<String, String> headerMap, String URL) throws IOException, ServletException{
		HttpServlet servlet = servletMap.get(servletName);
//		System.out.println(servletName + "has been initialized");
		
		/* How to set the time-out for sessions? Is it from web.xml? */
		//long sessionInterval = h.timeout;
		long sessionTimeout = 30;
		
		try {
			String queryString = getQueryString(URL);
			MyHttpServletRequest request = new MyHttpServletRequest(c, s, initMap, headerMap, queryString, sessionTimeout);
			MyHttpServletResponse response = new MyHttpServletResponse(s.getOutputStream(), c, initMap, headerMap);
			request.setResponseObject(response);
			servlet.service(request, response);
		} catch (IOException e) {
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
			throw e;
		} catch (ServletException e) {
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
			throw e;
		}
	}
	
	/**
	 * To remove the invalid sessions periodically
	 */
	public static void removeInvalidSessions(){
		if(sessionCache.size() > 100000000) {
			List<String> invalidID = new ArrayList<>();
			for(String id : sessionCache.keySet()) {
				if(!sessionCache.get(id).isValid()){ 
					invalidID.add(id);
				}
			}
			for(String id : invalidID) {
				sessionCache.remove(id);
			}
		}
	}
	
	/**
	 * Unload all servlets upon shutdown
	 */
	public void shutdown(){
		for(String str : servletMap.keySet()) {
			HttpServlet s = servletMap.get(str);
			s.destroy();
		}
	}
}
