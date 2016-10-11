package edu.upenn.cis.cis455.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import java.io.File;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the class to parse the web.xml file
 * @author cis555
 *
 */
public class MyHandler extends DefaultHandler{
	private int m_state = 0;
	private String m_servletName;
	private String m_servletName400;
	private String m_paramName;
	HashMap<String,String> m_servlets = new HashMap<String,String>();
	HashMap<String, String> m_urlPattern = new HashMap<String, String>();
	HashMap<String,String> m_contextParams = new HashMap<String,String>();
	HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
	
	public MyHandler() {}
	
	/**
	 * Specify the starting elements of xml mapping
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.compareTo("servlet") == 0) {
            m_state = 1;
        } else if (qName.compareTo("servlet-mapping") == 0) {
            m_state = 2;
        } else if (qName.compareTo("context-param") == 0) {
            m_state = 3;
        } else if (qName.compareTo("init-param") == 0) {
            m_state = 4;
        } else if (qName.compareTo("servlet-name") == 0) {
            m_state = (m_state == 1) ? 300 : 400;
        } else if (qName.compareTo("servlet-class") == 0) {
            m_state = 301;
        } else if (qName.compareTo("url-pattern") == 0) {
            m_state = 401;
        } else if (qName.compareTo("param-name") == 0) {
            m_state = (m_state == 3) ? 10 : 20;
        } else if (qName.compareTo("param-value") == 0) {
            m_state = (m_state == 10) ? 11 : 21;
        }
	}
	/**
	 * Parse the exact words in xml
	 */
	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length);
        if (m_state == 300) {
            m_servletName = value;
            m_state = 0;
        } else if (m_state == 301) {
            m_servlets.put(m_servletName, value);
            m_state = 0;
        } else if (m_state == 400) {
            m_servletName400 = value;
            m_state = 0;
        } else if(m_state == 401) {
            m_urlPattern.put(value, m_servletName400);
            m_state = 0;
        } else if (m_state == 1) {
            m_servletName = value;
            m_state = 0;
        } else if (m_state == 2) {
            m_servlets.put(m_servletName, value);
            m_state = 0;
        } else if (m_state == 10 || m_state == 20) {
            m_paramName = value;
        } else if (m_state == 11) {
            if (m_paramName == null) {
                System.err.println("Context parameter value '" + value
                        + "' without name");
                System.exit(-1);
            }
            m_contextParams.put(m_paramName, value);
            m_paramName = null;
            m_state = 0;
        } else if (m_state == 21) {
            if (m_paramName == null) {
                System.err.println("Servlet parameter value '" + value
                        + "' without name");
                System.exit(-1);
            }
            HashMap<String, String> p = m_servletParams.get(m_servletName);
            if (p == null) {
                p = new HashMap<String, String>();
                m_servletParams.put(m_servletName, p);
            }
            p.put(m_paramName, value);
            m_paramName = null;
            m_state = 0;
        }
	}
}
