package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import edu.upenn.cis.cis455.servlets.HttpErrorLog;

/**
 * This is the main class to invoke HttpServer
 * @author Tianxiang Dong
 *
 */
public class HttpServer {
	public static int port = 8080;
	public static String homeDirectory = "";
	public static String webdotxml = "";
	public static boolean runFlag = true;
	public static ServerSocket server;
	public static final int MAX_TASK = 10000;
	public static final int MAX_WORKER = 10;
	public static HttpServerConfig c;
	
	/**
	 * To invoke the Server Properly, port number should be provided, as well as the home directory
	 * @param args  The first argument is port number, second is the home directory. Otherwise the server cannot run.
	 */
	public static void main(String[] args) {
		if( args.length == 0 ) {
			System.out.println("Developer: Tianxiang Dong");
			System.out.println("SEAS login: dtianx");
			return;
		}
		if( args.length != 3 ){
			System.out.println("Wrong number of arguments!");
			return;
		}
		port = Integer.valueOf(args[0]);
		homeDirectory = args[1];
		webdotxml = args[2];
		c = new HttpServerConfig(port, homeDirectory, webdotxml);
		ThreadPool threadPool = new ThreadPool(MAX_WORKER, MAX_TASK, homeDirectory, webdotxml);
		
        try {
			server = new ServerSocket(port, 200000);
		} catch (IOException e) {
			HttpErrorLog.addError(e.getMessage()+ "\n\n");
			e.printStackTrace();
		}
 
		while(runFlag) {
			Socket client = null;
            try {
                client = server.accept();
                //System.out.println("One client socket accepted");
                threadPool.handleSocket(client);
            } catch (SocketException e){
            	System.out.println("Server shut down");
                break;
            } catch (IOException e) {
            	HttpErrorLog.addError(e.getMessage()+ "\n\n");
                e.printStackTrace();
                //break;
            } 
            //System.out.println("Server Socket is listening another socket.");
		}
	}
	
	/**
	 * The class used to close the server socket.
	 */
    public static void closeSocket() {
        try {
            runFlag = false;
            server.close();
        } catch (IOException e) {
        	HttpErrorLog.addError(e.getMessage()+ "\n\n");
            e.printStackTrace();
        }
    }

}
