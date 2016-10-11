package edu.upenn.cis.cis455.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class MyHttpSession implements HttpSession{
	static Long id_pool = new Long(0);
	private Properties attributes;
	private Date date;
	private final String SESSION_ID;
	private boolean valid;
	private boolean isnew;
	private long maxInactiveInterval;
	private long lastAccessed;
	private long creationTime;
	private ServerServletContext context;
	
	public MyHttpSession(long sessionTimeOut){
		this.maxInactiveInterval = sessionTimeOut;	
		this.attributes = new Properties();
		this.date = new Date();
		Random random = new Random();
		String id = "";
		do {
			id = ((Long)Math.abs(random.nextLong())).toString();
		} while(ServletContainer.sessionCache.get(id) != null);
		SESSION_ID = id;
		
		this.creationTime = date.getTime();
		this.lastAccessed = creationTime;
		this.valid = true;
		this.isnew = true;
	}
	
	public MyHttpSession(long sessionTimeOut, ServerServletContext context){
		this(sessionTimeOut);
		setContext(context);
	}
	
	public void setContext(ServerServletContext context){
		this.context = context;
	}
	
	public boolean isValid()
	{
		long currentTime = (new Date()).getTime();
		if(maxInactiveInterval >= 0 && currentTime-lastAccessed > maxInactiveInterval){
			invalidate();
			return false;
		}	
		this.lastAccessed = currentTime;
		return valid;
	}
	
	public void setOld() {
		this.isnew = false;
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public Enumeration getAttributeNames() {
		Set<Object> keys = attributes.keySet();
		Vector<Object> atts = new Vector<Object>(keys);
		return atts.elements();
	}

	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public String getId() {
		return SESSION_ID;
	}

	@Override
	public long getLastAccessedTime() {
		return this.lastAccessed;
	}

	@Override
	public int getMaxInactiveInterval() {
		return (int)maxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		// deprecated 
		return null;
	}

	@Override
	public Object getValue(String key) {
		return attributes.get(key);
	}

	@Override
	public String[] getValueNames() {
		// deprecated 
		return null;
	}

	@Override
	public void invalidate() {
		this.valid = false;
	}

	@Override
	public boolean isNew() {
		return this.isnew;
	}

	@Override
	public void putValue(String key, Object value) {
		this.attributes.put(key, value);
	}

	@Override
	public void removeAttribute(String key) {
		this.attributes.remove(key);
	}

	@Override
	public void removeValue(String key) {
		this.attributes.remove(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}
}
