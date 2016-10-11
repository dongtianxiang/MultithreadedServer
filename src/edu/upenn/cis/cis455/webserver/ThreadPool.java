package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import edu.upenn.cis.cis455.servlets.HttpErrorLog;
import edu.upenn.cis.cis455.servlets.ServletContainer;

/**
 * This is the thread pool includes ThreadWorkers. All the threadWorker are created at the first time the poll is created.
 * @author Tianxiang Dong
 *
 */
public class ThreadPool {
	private BlockingQueue taskQueue;
	private static List<ThreadWorker> threads = new ArrayList<ThreadWorker>();
	private static Map<ThreadWorker, String> status = new HashMap<>();
	private static ServletContainer container;
	
	public ThreadPool(int threadLimit, int taskLimit, String homeDirectory, String webdotxml) {
	    taskQueue = new BlockingQueue(taskLimit);
	    container = new ServletContainer(webdotxml);
	    
	    // add threads to the threads list
	    for(int i = 0; i < threadLimit; i++) {
	    	ThreadWorker worker = new ThreadWorker(taskQueue, container);
	    	ThreadWorker.setHome(homeDirectory);
	    	threads.add(worker);
	    }
	    // let the thread worker in the threads list start work
	    for(ThreadWorker thread : threads){
	    	System.out.println("Thread is running");
	    	thread.start();
	    }    
	}
	
	 /**
	  * Add Sockets to the BlockingQueue
	  * @param s New socket that has been accepted by server.
	  */
	 public void handleSocket(Socket s) {
	    try {
	      taskQueue.enqueue(s);
	    } catch (InterruptedException e) {
	      HttpErrorLog.addError(e.getMessage()+ "\n\n");
	      e.printStackTrace();
	    }
	 }
	 
	  // shutdown threads
	 /**
	  * Method used to close all the opened socket of all threads as well as all the threads in this pool.
	  * @throws IOException
	  */
	  public synchronized static void closeThreads() throws IOException {
	      for(ThreadWorker thread: threads) {
	    	  thread.closeSocket();  /* Socket listening for incoming request should be closed by socket closing */
	          thread.interrupt();
	      }
	      container.shutdown();
	  }
	  
	  /**
	   * Method to get status of all threads, which is actually used by /control request.
	   * @return The map which includes all information.
	   */
	  public static Map<ThreadWorker, String> getStatus(){
		  for(ThreadWorker thread: threads) {
			  status.put(thread, thread.getState().toString().equalsIgnoreCase("Waiting") ? "Waiting" : thread.getPath());
		  }
		  return status;
	  }
}
