package edu.upenn.cis.cis455.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;

import edu.upenn.cis.cis455.webserver.HttpServerConfig;

/**
 * This is the class implemented from HttpServletRequest for Servlets
 * {@inheritDoc}
 * @author cis555
 *
 */
public class MyHttpServletRequest implements HttpServletRequest{
	
	String encoding = "ISO-8859-1";
	MyHttpSession session = null;
	private String method = null;
	private Properties parameters = null;
	private Properties attributes = null;
	//private String serverName = null;
	private String protocol = null;
	private int port;
	BufferedReader bufferedReader = null;
	String servletPath = null;
	String pathInfo = null;
	String requestURL = null;
	InetAddress address = null;
	String fullPath = null;
	Socket clientSocket = null;
	HttpServerConfig c = null;
	String QueryString;
	Cookie[] cookieArray = null;
	String sessionID = null;
	boolean sessionInCookie = false;
	boolean sessionInURL = false;
	MyHttpServletResponse responseObject = null;
	private long sessionTimeout = -1;
	private String queryString = "";
	String contentType = "text/html";
	Map<String, String> initMap;
	Map<String, String> headerMap;
	
	public MyHttpServletRequest(HttpServerConfig c, Socket s, Map<String, String> initMap, Map<String, String> headerMap, String queryString, long sessionTimeout) throws IOException
	{	
		this.sessionTimeout = sessionTimeout * 60;  // time out in minutes 
		this.parameters = new Properties();
		this.attributes = new Properties();
        this.clientSocket = s;
        this.method = initMap.get("Type");
        this.protocol = initMap.get("Protocol");
        this.requestURL = initMap.get("Path");
        this.fullPath = c.rootDir + requestURL;
        this.queryString = queryString;
        this.address = s.getInetAddress();
        this.c = c;
        this.port = s.getLocalPort();
        this.initMap = initMap;
        this.headerMap = headerMap;
        getCookiesFromHeader();
        
        
//        for(String str : headerMap.keySet())
//        	System.out.println(str + ": " + headerMap.get(str));
//        System.out.println("----Request End-----");
        
	}
	
	/**
	 * Method used in constructor to get cookies from headers in request
	 */
	private void getCookiesFromHeader(){
        String cookiesString = headerMap.get("cookie");
        List<Cookie> cookieList = new ArrayList<>();
        if (cookiesString != null) {
            String[] cookiesStringList = cookiesString.split(";");

            for (String cookie : cookiesStringList) {
                String cookiePair = cookie.trim(); // cookie pairs
                String[] cookieString = cookiePair.split("=");
                cookieList.add(new Cookie(cookieString[0], cookieString[1]));
            }
        }
        cookieArray = new Cookie[cookieList.size()];
        for(int i = 0; i < cookieList.size(); i++) cookieArray[i] = cookieList.get(i); 
        for(Cookie c : cookieArray){
        	if(c.getName().equals("SESSIONID")) {
        		String id = c.getValue();
	        	MyHttpSession s = ServletContainer.sessionCache.get(id);
	        	if( s == null ) continue;
	        	if(s.isValid()) {
	        		this.session = s;
	        	} 
//	        	else {
//	        		ServletContainer.sessionCache.remove(id);
//	        	}
        	}
        }
        ServletContainer.removeInvalidSessions();  // remove invalid sessions periodically
	}
	
	/**
	 * Method to add Session from the cookie got in headers
	 * @param s
	 */
	public void addSession(MyHttpSession s) {
		session  = s;
		sessionID = s.getId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttribute(String key) {
		key = key.toLowerCase();
		return attributes.get(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration getAttributeNames() {
		Set<Object> keys = attributes.keySet();
		Vector<Object> atts = new Vector<Object>(keys);
		return atts.elements();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCharacterEncoding() {
		return encoding;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getContentLength() {
		if(headerMap.get("content-length") == null) return -1;
		else return Integer.parseInt(headerMap.get("content-length"));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentType() {
		return "text/html";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		// Implementation Not Required 
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalAddr() {
		return c.serverIP.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalName() {
		return c.serverName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLocalPort() {
		return port;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Locale getLocale() {
		if(headerMap.get("accept-language") != null) {
			String s = headerMap.get("accept-language");
			return new Locale(s);
		}
		return Locale.US;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration getLocales() {
		// Implementation Not Required 
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParameter(String key) {
		key = key.toLowerCase();
		return parameters.getProperty(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map getParameterMap() {
		return parameters;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration getParameterNames() {
		Set<Object> keys = parameters.keySet();
		Vector<Object> atts = new Vector<Object>(keys);
		return atts.elements();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getParameterValues(String key) {
		key = key.toLowerCase();
		List<String> temp = (List<String>)parameters.get(key);
		if(temp == null) {
			return null;
		}
		String[] res = new String[temp.size()];
		for(int i = 0; i < res.length; i++) {
			res[i] = new String(temp.get(i));
		}
		return res;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		InputStreamReader reader= new InputStreamReader(clientSocket.getInputStream());
        this.bufferedReader = new BufferedReader(reader);
		return this.bufferedReader;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRemoteAddr() {
		return address.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRemoteHost() {
		return clientSocket.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRemotePort() {
		return clientSocket.getPort();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// Implementation Not Required 
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getScheme() {
		return "http";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServerName() {
		return c.hostName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getServerPort() {
		return port;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSecure() {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAttribute(String key) {
		key = key.toLowerCase();
		attributes.remove(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAttribute(String key, Object val) {
		key = key.toLowerCase();
		attributes.put(key, val);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
		this.encoding = encoding;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContextPath() {
		return "";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cookie[] getCookies() {
		return cookieArray;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getDateHeader(String key) {
		key = key.toLowerCase();
		if(headerMap.get(key) == null) return -1;
		String date = headerMap.get(key);
		SimpleDateFormat f1 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
		SimpleDateFormat f2 = new SimpleDateFormat("EEEEE, dd-MMM-yy hh:mm:ss z");
		SimpleDateFormat f3 = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy z");
		f1.setTimeZone(TimeZone.getTimeZone("GMT"));
		f2.setTimeZone(TimeZone.getTimeZone("GMT"));
		f3.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = null;
		try {
			if(f1.parse(date)!=null)
			{
				d = f1.parse(date);
				return d.getTime();
			}
			else if(f2.parse(date)!=null)
			{
				d=f2.parse(date);
				return d.getTime();
			}
			else{
				d=f3.parse(date);
				return d.getTime();
			}
		} catch (java.text.ParseException e) {
			HttpErrorLog.addError(e.getMessage() + "\n\n");
			return -1;
		}
		//return -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHeader(String key) {
		key = key.toLowerCase();
		return headerMap.get(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration getHeaderNames() {
		Set<String> keys = headerMap.keySet();
		Vector<Object> atts = new Vector<Object>(keys);
		return atts.elements();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration getHeaders(String key) {
        List<String> arr = new ArrayList<String>();
        arr.add(headerMap.get(key));
        Enumeration<String> e = Collections.enumeration(arr);
        return e;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIntHeader(String key) throws NumberFormatException {
		key = key.toLowerCase();
		if(!headerMap.containsKey(key)) {
			return -1;
		}
		return Integer.parseInt(headerMap.get(key));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		return method;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPathInfo() {
        String path = initMap.get("Path");
        String res = ServletContainer.getPathInfo(path);
        return res;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPathTranslated() {
		// Deprecated
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQueryString() {
        if(initMap.get("Type").equalsIgnoreCase("GET")) return queryString;
        else return "";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRemoteUser() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRequestURI() {
		return requestURL;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringBuffer getRequestURL() {
		StringBuffer b = new StringBuffer();
		b.append("http://"+c.hostName+":"+c.port+""+requestURL);
		return b;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRequestedSessionId() {
		return sessionID;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getServletPath() {
		return null;
	}
	
	/**
	 * This is the method to add response object into request
	 * @param responseObject
	 */
	public void setResponseObject(MyHttpServletResponse responseObject){
		this.responseObject = responseObject;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpSession getSession() {
		if(session==null){
			session = new MyHttpSession(sessionTimeout);
			StringBuilder message = new StringBuilder();
			Cookie c = new Cookie("SESSIONID",session.getId());
			c.setMaxAge(session.getMaxInactiveInterval());
			responseObject.addCookie(c);
			synchronized(ServletContainer.sessionCache){
				ServletContainer.sessionCache.put(session.getId(),session);
				ServletContainer.sessionCache.notify();
			}
		}
		return session;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpSession getSession(boolean toCreate) {
		if(session == null && toCreate)
		{
			session = new MyHttpSession(sessionTimeout);
			StringBuilder message = new StringBuilder();
			Cookie c = new Cookie("SESSIONID",session.getId());
			c.setMaxAge(session.getMaxInactiveInterval());
			responseObject.addCookie(c);
			synchronized(ServletContainer.sessionCache){
				ServletContainer.sessionCache.put(session.getId(),session);
				ServletContainer.sessionCache.notify();
			}
			
		}
		return session;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Principal getUserPrincipal() {
		// Deprecated
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestedSessionIdValid() {
		if(session!=null) {
			return session.isValid();
		} else {
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserInRole(String arg0) {
		// Deprecated 
		return false;
	}
}
