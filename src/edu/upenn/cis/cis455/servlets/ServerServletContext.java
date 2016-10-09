package edu.upenn.cis.cis455.servlets;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * ServletContext is a configuration Object which is created when web application is started. 
 * It contains different initialization parameter that can be configured in web.xml.
 * The class implements a set of methods that a servlet uses to communicate with its servlet container
 * @author dongtianxiang
 *
 */
public class ServerServletContext implements ServletContext{
	private HashMap<String, Object> attributes;
	private HashMap<String, String> initParams;
	private String rootDir = "";
	private String servletContextName = "";
	
	public ServerServletContext() {
		this.attributes = new HashMap<>();
		this.initParams = new HashMap<>();
	}
	
	public void setRootDic(String rootDir){
		this.rootDir = rootDir;
	}
	
	public void setContextName(String name){
		this.servletContextName = name;
	}
	
	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
        Vector<String> atts = new Vector<String>(keys);
        return atts.elements();
	}
	
	@Override
	// Is it used to get the Context itself? Or to check the existence of itself in container then get it?
	public ServletContext getContext(String name) {
		return null;
	}

	@Override
	public String getInitParameter(String key) {
		key = key.toLowerCase();
		return initParams.get(key);
	}

	@Override
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
        Vector<String> atts = new Vector<String>(keys);
        return atts.elements();
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public String getMimeType(String arg0) {
		// not implemented
		return null;
	}

	@Override
	public int getMinorVersion() {
		return 4;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		// not implemented
		return null;
	}

	@Override
	public String getRealPath(String path) {
		File f = new File(rootDir+"/"+path);
		if(f.exists())
		{
			return rootDir+"/"+path;
		}
		else{
			return null;
		}
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String name) {
		// not implemented
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// Not Implemented
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// Not Implemented
		return null;
	}

	@Override
	public Set getResourcePaths(String arg0) {
		// Not Implemented
		return null;
	}

	@Override
	public String getServerInfo() {
		// Not Implemented
		return null;
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		// Deprecated
		return null;
	}

	@Override
	public String getServletContextName() {
		return servletContextName;
	}

	@Override
	public Enumeration getServletNames() {
		// Deprecated
		return null;
	}

	@Override
	public Enumeration getServlets() {
		// Deprecated
		return null;
	}

	@Override
	public void log(String arg0) {
		//Not Implemented
		
	}

	@Override
	public void log(Exception arg0, String arg1) {
		//Not Implemented
		
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		//Not Implemented
		
	}

	@Override
	public void removeAttribute(String key) {
		key = key.toLowerCase();
		attributes.remove(key);
	}

	@Override
	public void setAttribute(String key, Object val) {
		key = key.toLowerCase();
		attributes.put(key, val);
	}
	
	public void setInitParams(String key, String val) {
		key = key.toLowerCase();
		initParams.put(key, val);
	}
}
