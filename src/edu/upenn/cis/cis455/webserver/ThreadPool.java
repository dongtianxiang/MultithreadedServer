package edu.upenn.cis.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
	private BlockingQueue taskQueue;
	private List<ThreadWorker> threads = new ArrayList<ThreadWorker>();
	
	public ThreadPool(int threadLimit, int taskLimit, String homeDirectory) {
	    taskQueue = new BlockingQueue(taskLimit);
	    
	    // add threads to the threads list
	    for(int i = 0; i < threadLimit; i++) {
	    	ThreadWorker worker = new ThreadWorker(taskQueue);
	    	ThreadWorker.setHome(homeDirectory);
	    	threads.add(worker);
	    }
	    // let the thread worker in the threads list start work
	    for(ThreadWorker thread : threads){
	    	System.out.println("Thread is running");
	    	new Thread(thread).start();
	    }    
	}
	
	 // add tasks to the task queue
	 public void handleSocket(Socket s) {
	    try {
	      taskQueue.enqueue(s);
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	 }
	 
	  // control
	  public void closeThreads() {
	      for(ThreadWorker thread: threads) {
	          thread.setRunFlag(false);
	      }
	  }
}
