package edu.upenn.cis.cis455.webserver;

import java.util.LinkedList;
import java.util.List;

public class BlockingQueue<T> {
	private List<T> queue = new LinkedList<T>();
	private int queueSize;
	
	public BlockingQueue(int size) throws IllegalArgumentException{
		if(size > 0) {
			this.queueSize = size;
		}
		else throw new IllegalArgumentException("Non-positive queue size");
	}
	
	private boolean isFull(){
		return queue.size() == queueSize;
	}
	
	private boolean isEmpty(){
		return queue.size() == 0;
	}
	
	public synchronized void enqueue(T object) throws InterruptedException{
		while(isFull()){
			wait();
		}
		if(isEmpty()){
			notifyAll();
		}
		this.queue.add(object);
	}
	
	public synchronized T dequeue() throws InterruptedException{
		while(isEmpty()){
			wait();
		}
		if(isFull()){
			notifyAll();
		}
		return this.queue.remove(0);
	}
}
