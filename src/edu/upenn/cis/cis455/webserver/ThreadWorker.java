package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.servlets.HttpErrorLog;
import edu.upenn.cis.cis455.servlets.ServletContainer;

/**
 * The thread worker is used to read in the request, dequeue the socket and respond to it.
 * @author dongtianxiang
 *
 */
public class ThreadWorker extends Thread{
	private BlockingQueue socketQueue;
	private static String homeFolderDirectory;
	private String initLine = new String();
	private Map<String, String> initMap = new HashMap<>(); 
	private List<String> headers = new ArrayList<>();
	private Map<String, String> headerMap = new HashMap<>();
	private Map<String, String> parsedRequestMap = new HashMap<>();
	private boolean runFlag = true;
	private Socket client = null;
	private static final Logger log = Logger.getLogger(ThreadWorker.class);
	private Map<String, String> expect = new HashMap<>();
	private Set<String> supportingMethod = new HashSet<>();
	private ServletContainer container;
	
	public ThreadWorker(BlockingQueue taskQueue, ServletContainer container){
		this.socketQueue = taskQueue;
		supportingMethod.add("get");
		supportingMethod.add("head");
		supportingMethod.add("post");
		this.container = container;
		
//		this.homeFolderDirectory = "." + homeFolderDirectory;
//    	System.out.println(homeFolderDirectory);
	}
	
	/**
	 * The static method used by Thread pool to set the home directory, which should be called when the threaded is created.
	 * @param path Home directory
	 */
	public static void setHome(String path){
<<<<<<< HEAD
//		if(path.length() > 0){
//			homeFolderDirectory = path.charAt(0) == '.' ? path : "." + path;
//		} else {
//			homeFolderDirectory = ".";
//		}
=======
		/* Relative path handling  */
		
		/*
		if(path.length() > 0){
			homeFolderDirectory = path.charAt(0) == '.' ? path : "." + path;
		} else {
			homeFolderDirectory = ".";
		}
		*/
		
		/* Should be Absolute path for home directory */
>>>>>>> 69e3bbf0177d38d56862c9e2b388bf16a1caf80b
		homeFolderDirectory = path;
	}

	/**
	 * Main Method to run this thread.
	 */
	@Override
	public void run() {
		while(runFlag) {   /* runFlag is true defaultly */
			client = null;
			try{
				client = (Socket) socketQueue.dequeue();
				
				initMap.clear();
				headers.clear();
				headerMap.clear();
				parsedRequestMap.clear();
				
				// client socket time out setting
				client.setSoTimeout(1000000);
				
				/* Get socket read in and prepare output stream */
                InputStreamReader reader= new InputStreamReader(client.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                PrintStream output = new PrintStream(client.getOutputStream(), true); 
                boolean isRequest = processRequest(bufferedReader);
                if(!isRequest){
                	client.close();
                	continue;
                }
                
                /* Supporting Method Check */
                if(!supportingMethodCheck(output)) {
                	client.close();
                	continue;
                }
                
                String fileName = initMap.get("Path");
                
        		/* Special Request for /ShutDown */
                if(initMap.get("Path").trim().equalsIgnoreCase("/shutdown")) {
                	generateShutDownPage(output);
                	this.setRunFlag(false);
                	ThreadPool.closeThreads();
                	HttpServer.closeSocket();
                	client.close();
                	System.out.println("shutdown thread closed");
                	break;
                }
                
        		/* Special Request for /Control */
                if(initMap.get("Path").trim().equalsIgnoreCase("/control")) {
                	generateControlPage(output);
                	client.close();
                	continue;
                }             
                
                /* To ensure only files under home directory can be accessed */
                if( !securityCheck(fileName) ) {
                	errorResponse(output, "400");
                	continue;
                } 
                
                int validNum = validationCheck();
                if( validNum != 200) {
                	errorResponse(output, Integer.toString(validNum));
                	continue;
                }
                String URL = initMap.get("Path").trim();
                String servletName = container.lookUp(URL);
                if(servletName != null) {
                	container.dispatchRequest(HttpServer.c, client, servletName, initMap, headerMap, URL);
                } else {
                	sendResponse(output);
                }
                
                client.close();
			} catch(InterruptedException e){
				HttpErrorLog.addError(e.getMessage()+ "\n\n");
				System.out.println("One thread has been shut down");
				break;
			} catch(NullPointerException e) {
				HttpErrorLog.addError(e.getMessage()+ "\n\n");
			    //log.warn("NULL POINTER EXCEPTION.");
				System.out.println("Catch Null Pointer Exception");
				e.printStackTrace();
				break;
			} catch (MalformedURLException e){
				HttpErrorLog.addError(e.getMessage()+ "\n\n");
				System.out.println("MalformedURLException Caught");
				continue;
			} catch (Exception e) {
				HttpErrorLog.addError(e.getMessage()+ "\n\n");
				System.out.println("One thread has been shut down by Exception");
				break;
			}
		}
	}
	
	
	/**
	 *  Here we Read in and process the request.
	 */
	private boolean processRequest(BufferedReader bufferedReader) throws InterruptedException{
		// read in request 
		ArrayList<String> request = new ArrayList<>();
		try{
			String line = bufferedReader.readLine();
			//System.out.println(line);
			//if(line == null) System.out.println("line is null");
			
			while( line != null && !line.trim().equals("")) {
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
				request.add(line);
				line = bufferedReader.readLine();
				//System.out.println(line);
				//if(line == null) System.out.println("line is null");
			}
			
			/* Avoid Empty Request sent by Browser */
			if( (line == null || line.trim().equals("")) && request.size() == 0) {
				return false;
			}
		} catch (IOException e){
			/* Socket timeout should be caught in this exception */
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
			throw new InterruptedException();
		}
		
		initLine = request.isEmpty() ? "" : request.get(0);
		for( int i = 1; i < request.size(); i++ ) headers.add(request.get(i));
		
		parseInitialLine(initLine);
		parseHeaderLine(headers);
		return true;
	}
	
	/**
	 * The method to determine whether the incoming request type is supported, like "GET" or "HEAD".
	 * @param output The PrintStream which might be used if error response is necessary
	 * @return True if the request is supported
	 */
	private boolean supportingMethodCheck(PrintStream output){
		String method = initMap.get("Type");
		if( method == null || !supportingMethod.contains(method.trim().toLowerCase()) ) {
			errorResponse(output, "501");
			return false;
		}
		return true;
	}
	
	/**
	 * The method used to parse the first line of request.
	 * @param initLine First line of request
	 */
	private void parseInitialLine(String initLine){
		String[] split = initLine.split("\\s+");  /* Split by White Space */
		
		/* Server Control case handling */
		if( split.length == 2 ) {
			initMap.put("Type", split[0]);
			initMap.put("Path", split[1]);
			return;
		}
		
		/* Error Case handling */
		if( split.length != 3 ) return;
		
		initMap.put("Type", split[0]);
		initMap.put("Path", split[1]);
		initMap.put("Protocol", split[2]);
		
		
		/* Special request with http:// URL */
		
		/*
		try{
			URL url = new URL(initMap.get("Path"));
			url.getPath();
		} catch(Exception e) {
			e.printStackTrace();
		}
		*/
		
	}
	
	/**
	 * The method used to parse all the header lines.
	 * @param headers The result from processRequest, each element in the list is one line in the request.
	 */
	private void parseHeaderLine(List<String> headers){
		String preHeader = null;
		for(String head : headers) {
			String[] split = head.split(":", 2);
			if( split.length == 2 ) {
				headerMap.put(split[0].trim().toLowerCase(), split[1].trim());
				preHeader = split[0].trim().toLowerCase();
			} 
			else if ( split.length == 1 ){
				headerMap.put(preHeader, headerMap.get(preHeader) + split[0].trim());
			}
		}
	}
	
	/**
	 * Check whether the requested file is supported.
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private boolean fileTypeCheck(String fileName) throws IOException{
		ArrayList<String> sufixList = new ArrayList<String>();
        sufixList.add(".gif");
        sufixList.add(".png");
        sufixList.add(".jpg");
        sufixList.add(".txt");
        sufixList.add(".html");
        sufixList.add(".pdf");
        String[] split = fileName.split("/");
        fileName = split[split.length - 1];  /* get rid of previous ./../path/index.html in the fileName*/
        if( fileName.contains(".") ) {
        	for( String sufix : sufixList ) {
        		if(fileName.contains(sufix)) {
        			if(sufix.equalsIgnoreCase(".gif")) {
                        parsedRequestMap.put("Content-Type", "image/gif");
                    }
                    if(sufix.equalsIgnoreCase(".png")) {
                        parsedRequestMap.put("Content-Type", "image/png");
                    }
                    if(sufix.equalsIgnoreCase(".jpg")) {
                    	parsedRequestMap.put("Content-Type", "image/jpeg");

                    }
                    if(sufix.equalsIgnoreCase(".txt")) {
                    	parsedRequestMap.put("Content-Type", "text/plain");
                    }
                    if(sufix.equalsIgnoreCase(".html")) {
                    	parsedRequestMap.put("Content-Type", "text/html");
                    }
                    if(sufix.equalsIgnoreCase(".pdf")) {
                    	parsedRequestMap.put("Content-Type", "application/pdf");
                    }
        		}
        	}
        } else {
        	/* We should cover the whole file name to get access to it */
        	//parsedRequestMap.put("Content-Type", "text/html");
        	
        	throw new IOException("Can not find the required file");
        }
        return true;
	}
	
	/**
	 * The method to determine the files outside the specified home directory cannot be accessed.
	 * @param fileName
	 * @return
	 */
	private boolean securityCheck(String fileName) {
		String[] path = fileName.split("/");
		Stack<String> stack = new Stack<>();
		for( String str : path ) {
			if( str.length() == 0 || str.equals(".") ) continue;
			else if ( str.equals("..") ){
				if( stack.isEmpty() ) return false;
				else stack.pop();
			} else {
				stack.push(str);
			}
		}
		return true;
	}
	
	/**
	 * General error response page Generator.
	 * @param output PrintStream to send response back to client.
	 * @param errorType ErrorType to be checked.
	 */
	private void errorResponse(PrintStream output, String errorType) {
		if(errorType.equals("400")) {
			output.print("HTTP/1.1 400 Bad Request\n");
			generateErrorPage(output, "400 Bad Request");
			output.flush();
			output.close();
		}
		if(errorType.equals("403")) {
			output.print("HTTP/1.1 403 Request Not Allowed\n");
			generateErrorPage(output, "403 Request Not Allowed");
			output.flush();
			output.close();
		}
		if(errorType.equals("404")) {
			output.print("HTTP/1.1 404 Not Found\n");
			generateErrorPage(output, "404 Not Found");
			output.flush();
			output.close();
		}
		if(errorType.equals("304")) {
			output.print("HTTP/1.1 Not Modified\n");
			SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	    	date_format.setTimeZone(TimeZone.getTimeZone("GMT"));;
			output.println("Date: " + date_format.format(new Date()));
			output.print("\r\n");
			output.flush();
			output.close();
		}
		if(errorType.equals("412")) {
			output.print("HTTP/1.1 412 Precondition Failed\n");
			output.print("\r\n");
			output.flush();
			output.close();
		}
		if(errorType.equals("501")) {
			output.print("HTTP/1.1 501 Not Implemented\n");
			output.print("\r\n");
			output.flush();
			output.close();
		}
	}
	
	/**
	 * Private class only be used by errorResponse method. It is used to generate html page.
	 * @param output
	 * @param errorMessage
	 */
	private void generateErrorPage(PrintStream output, String errorMessage) {
		if(initMap.get("Type") != null && initMap.get("Type").equalsIgnoreCase("HEAD")){
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append(errorMessage);
		sb.append("</head>");
		sb.append("</html>");
		
    	SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    	date_format.setTimeZone(TimeZone.getTimeZone("GMT"));;
		output.println("Date: " + date_format.format(new Date()));
		output.println("Content-Type: " + "text/html");
		output.println("Content-Length: " + sb.length());
		output.println("Connection: Close");
		output.println("\r\n");
		output.print(sb.toString());
	}
	
	/**
	 * Check whether the absolute path is supported
	 * @return
	 * @throws MalformedURLException
	 */
	private int validationCheck() throws MalformedURLException {
		if(initMap.get("Path").contains("http://") ) {
			if(initMap.get("Protocol").equalsIgnoreCase("HTTP/1.0") || initMap.get("Protocol").equalsIgnoreCase("HTTP/1.1")){
				 return 400;
			} else {
				URL url = new URL(initMap.get("Path"));
				initMap.put("Path", url.getPath());
				//System.out.println("Host:" + url.getHost());
				//System.out.println("Path:" + url.getPath());
				return 200;
			}
        }
        if(initMap.get("Protocol").equalsIgnoreCase("HTTP/1.1")) {
            if( headerMap.get("host") == null ) {
                return 400;
            }
        }
		return 200;
	}
	
	/**
	 * The method is used to prepare the response's head lines. 
	 * @param output The PrintStream to be sent back to client
	 * @return whether or not the printStream is ready to send, true means it's ready to send, false means something else should be added
	 */
	private boolean sendResponse(PrintStream output) {
		String HTTPVersion = "HTTP/1.1";  /* Default HTTP version */
		
		if(headerMap.get("expect") != null && !initMap.get("Protocol").equalsIgnoreCase("HTTP/1.0")){
			output.println("HTTP/1.1 100 Continue");
			output.println();
		}
		
		if(initMap.size() == 3) {	
			String path = initMap.get("Path");
			path = path.replace("%20", " ");  /* In some cases, the file contains white space, which is transferred into "%20" instead. */
			//System.out.println("fileLocation: " + homeFolderDirectory);
			//System.out.println("path: " + path);

			String fileLocation = homeFolderDirectory + path;
			//System.out.println("fileLocation : " + fileLocation); 
			File file = new File(fileLocation);
			if( file.isDirectory() ) {  /* File is a directory */
				/* Deal with directory request, which is better to be an html with href to its file-list */
				generateFolderPage(file, output);
				return true;
			} else {
				if(!file.exists()) {
					errorResponse(output, "404");
					return true;
				}
				try{
					/* Add header here */
					fileTypeCheck(path);
					String fileType = parsedRequestMap.get("Content-Type");
					if( fileType == null ) {
						throw new IOException("The file type not supported");
					}
					
					
					//SimpleDateFormat date_format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
					SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
					date_format.setTimeZone(TimeZone.getTimeZone("GMT"));;
					Date now = new Date();
					Date lastModified = new Date(file.lastModified());
					
					if(headerMap.get("if-modified-since") != null) {
						String ifModified = headerMap.get("if-modified-since");
						Date requireDate = HttpTools.parseDateFormat(ifModified);
						if(requireDate != null && requireDate.after(lastModified)) {
							errorResponse(output, "304");
							return true;
						}
					}
					
					if(headerMap.get("if-unmodified-since") != null) {
						String ifUnmodified = headerMap.get("if-unmodified-since");
						System.out.println("ifUnmodified :" + ifUnmodified);
						Date requireDate = HttpTools.parseDateFormat(ifUnmodified);
						System.out.println("requireDate :" + requireDate.toString());
						if(requireDate != null && requireDate.before(lastModified)) {
							errorResponse(output, "412");
							return true;
						}
					}
					
					output.println(HTTPVersion + " 200 OK");
					output.println("Date: " + date_format.format(now));
					output.println("Content-Type: " + fileType);
					output.println("Content-Length: " + Integer.toString((int)file.length()));
					output.println("Last Modified: " + date_format.format(lastModified));
					output.println("Connection: Close");
					
					/* Add content here for GET Request */
					if(initMap.get("Type").equalsIgnoreCase("GET") ) {
						addContentFile(file, output);
					}
					output.flush();
					output.close();
					return true;
				} catch(IOException e) {
					/* Exception thrown during file read */
					output.println(HTTPVersion + " 500 Internal Server Error");
					output.println(file.getAbsolutePath() + " cannot be read");
					output.println("Connection: Close");
					output.flush();
					output.close();
					return true;
				}
			}
			//return true;  /* It depends on the later check of GET or HEAD */
		}
		else {   /* Bad Request */
			output.println(HTTPVersion + " 400 Bad Request");
			output.println("Connection: Close");
			output.flush();
			output.close();
			return true;   /* Ready to be flushed and closed */
		}
		
	}
	
	/**
	 * Private class only used to add content folder
	 * @param file
	 * @param output
	 */
	private void addContentFolder(File file, PrintStream output){
		generateFolderPage(file, output);
	}
	
	/**
	 * The method used to transfer the content of requested file
	 * @param file
	 * @param output
	 * @throws IOException
	 */
	private void addContentFile(File file, PrintStream output) throws IOException{
		try{
			FileInputStream fileStream = new FileInputStream(file);
			byte[] file_content = new byte[(int)file.length()];
			fileStream.read(file_content);
			output.print("\r\n");
			output.write(file_content);
		} catch(FileNotFoundException e){
			errorResponse(output, "404");
		}
	}
	
	/**
	 * Method usually used to set the runFlag to false.
	 * @param runFlag
	 */
    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }
    
    /**
     * Used to show the control page. The special status if the socket is connected but nothing has been read
     * @return
     */
    public String getPath(){
    	String path = initMap.get("Path");
    	if(path == null) return "Listening For Request";
    	return path;
    }
    
    /**
     * The method used to close the client socket of this thread.
     * @throws IOException
     */
    public void closeSocket() throws IOException{
    	if( client != null ) client.close();
    }
    
    /**
     * Shut down page generator
     * @param output
     */
    public void generateShutDownPage(PrintStream output){
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html>\n");
    	sb.append("<body>\n");
    	sb.append("The Server has been shut down<br>");
    	sb.append("</body>\n");
    	sb.append("</html>\n");
    	
		//SimpleDateFormat date_format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
		SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		date_format.setTimeZone(TimeZone.getTimeZone("GMT"));;
		output.println("HTTP/1.1" + " 200 OK");
		output.println("Date: " + date_format.format(new Date()));
		output.println("Content-Type: " + "text/html");
		output.println("Content-Length: " + sb.length());
		output.println("Connection: Close");
		output.println("\r\n");
    	output.println(sb.toString());
    	output.flush();
    	output.close();
    }
    
    /**
     * Control Page generator
     * @param output
     */
    public void generateControlPage(PrintStream output){
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html>\n");
    	sb.append("<title>\n");
    	sb.append("Welcome to the Server\n");
    	sb.append("</title>\n");
    	sb.append("<body>\n");
    	sb.append("<font size=\"5\">\n");
    	sb.append("<b>");
    	sb.append("Developer : Tianxiang Dong<br>");
    	sb.append("SEAS login: dtianx<br>");
    	sb.append("</b>");
    	sb.append("</font>\n");
    	sb.append("<p>");
    	
    	sb.append("<table style=\"font-size:20px;\">");
    	Map<ThreadWorker, String> statuses = ThreadPool.getStatus();
    	int count = 1;
    	sb.append("<tr><td>");
    	sb.append("<b>");
    	sb.append("Thread");
    	sb.append("</b>");
    	sb.append("</td><td>");
    	sb.append("<b>");
    	sb.append("Status");
    	sb.append("</b>");
    	sb.append("</td></tr>");
    	
		for (ThreadWorker thread : statuses.keySet()) { // build thread status
														// table
			sb.append("<tr><td>");
			sb.append("Thread" + count);
			sb.append("</td><td>");
			sb.append(statuses.get(thread));
			sb.append("</td></tr>");
			count++;
		}
		sb.append("</table>");
		sb.append("<p>");
		sb.append("<b>");
		sb.append("<font size=\"4\">\n");
		sb.append("Error Log Information: ");
		if(HttpErrorLog.getErrorLog().size() == 0) sb.append("None" + "<br>");
		sb.append("</font>\n");
		sb.append("</b>");
		for(String err : HttpErrorLog.getErrorLog()) {
			sb.append(err + "<br>");
		}
		sb.append("<p>");
		sb.append("<a href=\"/shutdown\"><button>" +  "Shutdown" + "</button></a>");
    	sb.append("</body>\n");
    	sb.append("</html>\n");
 
		//sb.append(HttpConstants.HTTP_RESPONSE_END);
    	
    	generateHeader(sb, output);
    	
    	if(initMap.get("Type").equalsIgnoreCase("GET")) {
    		output.println(sb.toString());
    	}
    	output.flush();
    	output.close();
    }
    
    /**
     * Folder Page Generator
     * @param file
     * @param output
     */
    public void generateFolderPage(File file, PrintStream output){
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html>\n");
    	sb.append("<title>\n");
    	sb.append("Welcome to the Server\n");
    	sb.append("</title>\n");
    	sb.append("<body>\n");
    	sb.append("<font size=\"5\">\n");
    	sb.append("<b>");
    	sb.append("Developer : Tianxiang Dong<br>");
    	sb.append("SEAS login: dtianx<br>");
    	sb.append("</b>");
    	sb.append("</font>\n");
    	sb.append("<p>");
    	
    	sb.append("<table style=\"font-size:20px;\">");
    	Map<ThreadWorker, String> statuses = ThreadPool.getStatus();
    	sb.append("<tr><td>");
    	sb.append("<b>");
    	sb.append("Files");
    	sb.append("</b>");
    	sb.append("</td><td>");
    	sb.append("<b>");
    	sb.append("Last Modified");
    	sb.append("</b>");
    	sb.append("</td></tr>");
    	SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		for (File f : file.listFiles()) { // build thread status
														// table
			sb.append("<tr><td>");
			String prePath = initMap.get("Path");
			prePath = prePath.replaceAll("/{2,}", "/");
			if(prePath.equals("/")) prePath = "";
			String newPath = prePath + "/" + f.getName();
			sb.append("<a href=\"" + newPath + "\">" +  f.getName() + "</a>");
			
			sb.append("</td><td>");
			sb.append(date_format.format(new Date(f.lastModified())).toString());
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		sb.append("<p>");
		sb.append("<a href=\"/shutdown\"><button>" +  "Shutdown" + "</button></a>");
    	sb.append("</body>\n");
    	sb.append("</html>\n");
 
    	generateHeader(sb, output);
        
    	// Add content part
    	if(initMap.get("Type").equalsIgnoreCase("GET")) {
    		output.println(sb.toString());
    	}
    	
    	output.flush();
    	output.close();
    }
    
    /**
     * General Response Headers generator.
     * @param sb
     * @param output
     */
    public void generateHeader(StringBuilder sb, PrintStream output) {
    	SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    	date_format.setTimeZone(TimeZone.getTimeZone("GMT"));;
		output.println("HTTP/1.1" + " 200 OK");
		output.println("Date: " + date_format.format(new Date()));
		output.println("Content-Type: " + "text/html");
		output.println("Content-Length: " + sb.length());
		output.println("Connection: Close");
		output.println("\r\n");
    }
}
