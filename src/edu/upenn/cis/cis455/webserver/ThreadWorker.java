package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import org.apache.log4j.Logger;

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
	
	public ThreadWorker(BlockingQueue taskQueue){
		this.socketQueue = taskQueue;
//		this.homeFolderDirectory = "." + homeFolderDirectory;
//    	System.out.println(homeFolderDirectory);
	}
	
	public static void setHome(String path){
		homeFolderDirectory = "." + path;
	}

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
                
                sendResponse(output);
                
                client.close();
			} catch(InterruptedException e){
				System.out.println("One thread has been shut down");
				break;
			} catch(NullPointerException e) {
			    //log.warn("NULL POINTER EXCEPTION.");
				System.out.println("Catch Null Pointer Exception");
				break;
			} catch (Exception e) {
				System.out.println("One thread has been shut down by Exception");
				break;
			}
		}
	}
	
	
	/* #####  Here we Read in and process the request ##### */
	private boolean processRequest(BufferedReader bufferedReader) throws InterruptedException{
		// read in request 
		ArrayList<String> request = new ArrayList<>();
		try{
			String line = bufferedReader.readLine();
			System.out.println(line);
			if(line == null) System.out.println("line is null");
			while( line != null && !line.trim().equals("")) {
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
				request.add(line);
				line = bufferedReader.readLine();
				System.out.println(line);
				if(line == null) System.out.println("line is null");
			}
			
			/* Avoid Empty Request sent by Browser */
			if( (line == null || line.trim().equals("")) && request.size() == 0) {
				return false;
			}
		} catch (IOException e){
			/* Socket timeout should be caught in this exception */
			throw new InterruptedException();
		}
		
		initLine = request.isEmpty() ? "" : request.get(0);
		for( int i = 1; i < request.size(); i++ ) headers.add(request.get(i));
		
		parseInitialLine(initLine);
		parseHeaderLine(headers);
		return true;
	}
	
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
	
	private void parseHeaderLine(List<String> headers){
		for(String head : headers) {
			String[] split = head.split(":");
			if( split.length >= 2 ) {
				if(split[0].trim().equalsIgnoreCase("Host")) {
					headerMap.put("Host", split[1]);
				}
			}
		}
	}
	
	private boolean fileTypeCheck(String fileName) throws IOException{
		ArrayList<String> sufixList = new ArrayList<String>();
        sufixList.add(".gif");
        sufixList.add(".png");
        sufixList.add(".jpg");
        sufixList.add(".txt");
        sufixList.add(".html");
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
        		}
        	}
        } else {
        	/* We should cover the whole file name to get access to it */
        	//parsedRequestMap.put("Content-Type", "text/html");
        	
        	throw new IOException("Can not find the required file");
        }
        return true;
	}
	
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
	
	private void errorResponse(PrintStream output, String errorType) {
		if(errorType.equals("400")) {
			output.print("HTTP/1.1 400 Bad Request\n");
			output.flush();
			output.close();
		}
		if(errorType.equals("403")) {
			output.print("HTTP/1.1 403 Request Not Allowed\n");
			output.flush();
			output.close();
		}
		if(errorType.equals("404")) {
			output.print("HTTP/1.1 404 Not Found\n");
			output.flush();
			output.close();
		}
	}
	
	private int validationCheck() {
		if(initMap.get("Path").contains("http://")) {
            return 403;
        }
        if(initMap.get("Protocol").equalsIgnoreCase("HTTP/1.1")) {
            if( headerMap.get("Host") == null ) {
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
		 
		if(initMap.size() == 3) {	
			String path = initMap.get("Path");
			path = path.replace("%20", " ");  /* In some cases, the file contains white space, which is transferred into "%20" instead. */
			String fileLocation = homeFolderDirectory + path;
			System.out.println("fileLocation : " + fileLocation); 
			File file = new File(fileLocation);
			if( file.isDirectory() ) {  /* File is a directory */
				/* Deal with directory request, which is better to be an html with href to its file-list */
			
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
						throw new IOException("The file type now supported");
					}
					
					//SimpleDateFormat date_format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
					SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
					date_format.setTimeZone(TimeZone.getTimeZone("GMT"));;
					//date_format.format(new Date(file.lastModified()));
					
					output.println(HTTPVersion + " 200 OK");
					output.println("Date: " + date_format.format(new Date()));
					output.println("Content-Type: " + fileType);
					output.println("Content-Length: " + Integer.toString((int)file.length()));
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
			return true;  /* It depends on the later check of GET or HEAD */
		}
		else {   /* Bad Request */
			output.println(HTTPVersion + " 400 Bad Request");
			output.println("Connection: Close");
			output.flush();
			output.close();
			return true;   /* Ready to be flushed and closed */
		}
		
	}
	
	private void addContentFolder(File file, PrintStream output){
		
	}
	
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
	
    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }
    
    public String getPath(){
    	String path = initMap.get("Path");
    	if(path == null) return "Listening For Request";
    	return path;
    }
    
    public void closeSocket() throws IOException{
    	if( client != null ) client.close();
    }
    
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
    
    public void generateControlPage(PrintStream output){
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html>\n");
    	sb.append("<body>\n");
    	sb.append("Tianxiang Dong<br>");
    	sb.append("dtianx<br>");
    	Map<ThreadWorker, String> status = ThreadPool.getStatus();
    	int count = 1;
    	for(ThreadWorker thread : status.keySet()) {
    		sb.append("Thread" + count + ": " + status.get(thread) + "<br>");
    		count++;
    	}
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
}
