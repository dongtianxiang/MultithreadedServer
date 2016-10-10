package edu.upenn.cis.cis455.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis.cis455.webserver.HttpServerConfig;

public class MyHttpServletResponse implements HttpServletResponse {
	private OutputStream output;
	private Map<String, String> initMap;
	private Map<String, String> headerMap;
	private HttpServerConfig c;
	private String contentType = "text/html";
	private boolean isCommited = false;
	private boolean writerCalled = false;
	private int baseSize = 1024;
	private int bufferSize = baseSize;	
	private boolean charSet = false;
	private String statusString = "200 OK";
	private Locale currentLocale = null;
	private StringBuffer buffer = null;
	private String errorMessage = null;
	private String currentURL = null;
	private boolean errorStatus = false;
	private Map<String, StringBuffer> headerBuffer;
	private List<StringBuffer> cookieBuffer = new ArrayList<>();
	
	public MyHttpServletResponse(OutputStream output, HttpServerConfig c, Map<String, String> initMap, Map<String, String> headerMap){
		this.output = output;
		this.initMap = initMap;
		this.headerMap = headerMap;
		this.c = c;
		this.headerBuffer = new HashMap<String, StringBuffer>();
		this.buffer = new StringBuffer();
	}
	
	private void generateBuffer(){
		buffer.append(initMap.get("Protocol") + " " + statusString + "\n");
		
		for(StringBuffer sb : cookieBuffer) {
			buffer.append("Set-Cookie: " + sb + "\n");
		}
		
		for(String header : headerBuffer.keySet()) {
			StringBuffer sb = headerBuffer.get(header);
			buffer.append(header + ":" + sb + "\n");
		}
		
		buffer.append("\n");
		System.out.println(buffer.toString());
	}
	
	/**
	 * Forces any content in the buffer to be written to the client. 
	 * A call to this method automatically commits the response, meaning the status code and headers will be written.
	 * @throws IOException
	 */
	@Override
	public void flushBuffer() throws IOException {
		isCommited = true;
		output.flush();
		
		generateBuffer();
		output.write(buffer.toString().getBytes());
		output.flush();
		buffer = buffer.delete(0, buffer.length());
		statusString = "200 OK";
		headerBuffer.clear();
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public Locale getLocale() {
		return currentLocale;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// Deprecated
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		writerCalled = true;
		flushBuffer();
		return new PrintWriter(output,true);
	}

	@Override
	public boolean isCommitted() {
		return isCommited;
	}
	
	
	/**
	 * Clears any data that exists in the buffer as well as the status code and headers. 
	 * If the response has been committed, this method throws an IllegalStateException.
	 */
	@Override
	public void reset() throws IllegalStateException {
		if(isCommited) throw new IllegalStateException();
		else {
			buffer.delete(0, buffer.length());
			headerBuffer.clear();
			statusString = null;
		}
	}
	
    /**
     * Clears the content of the underlying buffer in the response without clearing headers or status code. 
     * If the response has been committed, this method throws an IllegalStateException.
     */
	@Override
	public void resetBuffer() throws IllegalStateException{
		if(isCommited) throw new IllegalStateException();
		else {
			buffer.delete(0, buffer.length());
		}
	}

	/**
	 * Sets the preferred buffer size for the body of the response. 
	 * The servlet container will use a buffer at least as large as the size requested. 
	 * The actual buffer size used can be found using getBufferSize
	 * This method must be called before any response body content is written; 
	 * if content has been written or the response object has been committed, this method throws an IllegalStateException.
	 * @param size the preferred buffer size
	 */
	@Override
	public void setBufferSize(int size) {
		if(isCommited) throw new IllegalStateException();
		if(size > baseSize) this.bufferSize = size;
	}
	
	/**
	 * Sets the character encoding (MIME charset) of the response being sent to the client, for example, to UTF-8.
	 * This method has no effect if it is called after getWriter has been called or after the response has been committed.
	 * @param encodinig
	 */
	@Override
	public void setCharacterEncoding(String encoding) {
		charSet = true;
		if(isCommited || writerCalled) {
			//DO NOTHING, since getWriter() has been called or the response has been commited.
			return;
		}
		if(headerBuffer.containsKey("CONTENT-TYPE"))
		{
			StringBuffer b = headerBuffer.get("CONTENT-TYPE");
			String[] a = b.toString().split(";");
			b=b.delete(0, b.length());
			b.append(a[0]);
			b.append("; charset=" + encoding);
		}
		else{
			StringBuffer temp = new StringBuffer();
			temp.append("text/html; charset="+encoding);
			headerBuffer.put("CONTENT-Type", temp);
		}
	}
	
	/**
	 * Sets the length of the content body in the response In HTTP servlets, this method sets the HTTP Content-Length header.
	 * @param len
	 */
	@Override
	public void setContentLength(int len) {
		if(headerBuffer.containsKey("CONTENT-LENGTH"))
		{
			StringBuffer b = headerBuffer.get("CONTENT-LENGTH");
			b = b.delete(0, b.length());
			b.append(len);
		}
		else{
			StringBuffer temp = new StringBuffer();
			temp.append(len);
			headerBuffer.put("CONTENT-LENGTH", temp);
		}
	}
	
	/**
	 * Sets the content type of the response being sent to the client, if the response has not been committed yet. 
	 * The given content type may include a character encoding specification, for example, text/html;charset=UTF-8. 
	 * The response's character encoding is only set from the given content type if this method is called before getWriter is called.
	 * @param contentType
	 */
	@Override
	public void setContentType(String contentType) {
		if(isCommited) {
			// Nothing happens if the response has been commited.
			return;
		}
		
		if(headerBuffer.get("CONTENT-TYPE") == null) {
			charSet = true;
			StringBuffer b = new StringBuffer();
			b.append(contentType);
			headerBuffer.put("CONTENT-TYPE", b);
			return;
		}
		
		if(writerCalled) {
			// character encoding not change after the writer has been called 
			StringBuffer b = headerBuffer.get("CONTENT-TYPE");
			if(b.toString().contains("charset")) {
				String[] split = b.toString().split(";");
				split[0] = contentType.split(";")[0];    // the given content-type may include charset
				b.delete(0, b.length());
				b.append(split[0] + ";");
				if( split.length >= 2 ) b.append(split[1]); 
			} else {
				StringBuffer newBuffer = new StringBuffer();
				newBuffer.append(contentType);
				headerBuffer.put("CONTENT-TYPE", newBuffer);
			}
			return;
		}
		// change total content-type header of the StringBuffer
		charSet = true;
		StringBuffer b = headerBuffer.get("CONTENT-TYPE");
		b.delete(0, b.length());
		b.append(contentType);
		headerBuffer.put("CONTENT-TYPE", b);
	}
	
	/**
	 * This method may be called repeatedly to change locale and character encoding. 
	 * The method has no effect if called after the response has been committed. 
	 * @param responseLocale
	 */
	@Override
	public void setLocale(Locale responseLocale) {
		if(isCommited || writerCalled)
		{
			throw new IllegalStateException();
		}
		if(headerBuffer.containsKey("CONTENT-LANGUAGE"))
		{
			StringBuffer b = headerBuffer.get("CONTENT-LANGUAGE");
			b=b.delete(0, b.length());
			b.append(responseLocale.getLanguage());
		}
		else{
			StringBuffer temp = new StringBuffer();
			temp.append(responseLocale.getLanguage());
			headerBuffer.put("CONTENT-LANGUAGE",temp);
		}
		
		currentLocale = responseLocale;
		
		if(headerBuffer.containsKey("CONTENT-TYPE") && !charSet)
		{
			StringBuffer b = headerBuffer.get("CONTENT-TYPE");
			String[] temp = b.toString().split(";");
			b=b.delete(0, b.length());
			b.append(temp[0]);
			b.append("; charset="+getCharacterEncoding());
		}
	}

	@Override
	public void addCookie(Cookie cookie) {
		if(isCommited || writerCalled) return;
		SimpleDateFormat dateFormate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");  // must use "HH" to set 24 hour
		dateFormate.setTimeZone(TimeZone.getTimeZone("GMT"));
//		dateFormate.setTimeZone(TimeZone.getDefault());
		Date now = new Date();
		long validLen = now.getTime() + ((long)cookie.getMaxAge()) * 1000;  /* Expires in seconds */
		
		Date expireDate = new Date(validLen);
		StringBuilder cookieString = new StringBuilder();
		cookieString.append(cookie.getName() + "=" + cookie.getValue());
		String dateFinalGMT = dateFormate.format(expireDate);
		cookieString.append("; Expires=" + dateFinalGMT );
		
//		System.out.println("Final Date: " + dateFinalGMT);
//		System.out.println("Now Date: " + dateFormate.format(now.getTime()));
		
		if(cookie.getPath() != null) cookieString.append("; Path=" + cookie.getPath());
		if(cookie.getDomain() != null) cookieString.append("; Domain=" + cookie.getDomain());
		
		cookieBuffer.add(new StringBuffer(cookieString));
	}

	@Override
	public void addDateHeader(String header, long date) {
		if(isCommited || writerCalled) return;
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = new Date(date);
		String headerDate = dateFormat.format(d);
		if(headerBuffer.containsKey(header)) {
			StringBuffer sb = headerBuffer.get(header);
			sb.append("," + headerDate);
		} else{
			headerBuffer.put(header, new StringBuffer(headerDate));
		}
		
	}

	@Override
	public void addHeader(String header, String value) {
		if(isCommited || writerCalled) return;
		header = header.toUpperCase();
		if(headerBuffer.containsKey(header)) {
			headerBuffer.get(header).append(","+ value);
		}
		else{
			headerBuffer.put(header,new StringBuffer(value));
		}
	}

	@Override
	public void addIntHeader(String header, int value) {
		if(isCommited || writerCalled) return;
		header = header.toUpperCase();
		if( headerBuffer.containsKey(header))
		{
			headerBuffer.get(header).append("," + value);
		}
		else{
			headerBuffer.put(header,new StringBuffer(value));
		}
	}

	@Override
	public boolean containsHeader(String key) {
		key = key.toUpperCase();
		return headerBuffer.containsKey(key);
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		return null;
	}

	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub
		statusString = "302 Redirect";
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStatus(int statusCode) {
		statusString = c.protocolSupported + " "+ statusCode;
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		
	}
	
}