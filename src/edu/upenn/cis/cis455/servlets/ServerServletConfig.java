package edu.upenn.cis.cis455.servlets;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * ServletConfig object is created by web container for each servlet to pass information to a servlet during initialization.
 * This object can be used to get configuration information from web.xml file.
 * @author dongtianxiang
 *
 */
public class ServerServletConfig implements ServletConfig {
	private String name;
    private ServerServletContext context;
    private HashMap<String,String> initParams;
    
    public ServerServletConfig(String name, ServerServletContext context) {
        this.name = name;
        this.context = context;
        initParams = new HashMap<String,String>();
    }
    
	@Override
	public String getInitParameter(String key) {
	     return initParams.get(key);
	}

	@Override
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
        Vector<String> atts = new Vector<String>(keys);
        return atts.elements();
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public String getServletName() {
		return name;
	}
	
	public void setInitParams(String key, String value) {
		initParams.put(key, value);
	}

}
