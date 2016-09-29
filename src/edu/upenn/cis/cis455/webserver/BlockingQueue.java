package edu.upenn.cis.cis455.webserver;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the BlockingQueue class implemented for storing incoming Socket, to ensure the thread safety.
 * @author Tianxiang Dong
 *
 * @param <T> Any Generic Class. For the server, the generic class is Socket.
 */
public class BlockingQueue<T> {
	private List<T> queue = new LinkedList<T>();
	private int queueSize;
	
	public BlockingQueue(int size) throws IllegalArgumentException{
		if(size > 0) {
			this.queueSize = size;
		}
		else throw new IllegalArgumentException("Non-positive queue size");
	}
	
	/**
	 * Determine if the queue has reached its capacity
	 * @return True if full, false if not full.
	 */
	private boolean isFull(){
		return queue.size() == queueSize;
	}
	
	/**
	 * Determine if the queue is empty
	 * @return True if empty, false if not empty.
	 */
	private boolean isEmpty(){
		return queue.size() == 0;
	}
	
	/**
	 * This is the synchronized method that only one thread can access and add object into the queue.
	 * If the queue is full, the thread has to wait until notification. In the mean time, the thread releases the lock on it.
	 * @param object
	 * @throws InterruptedException
	 */
	public synchronized void enqueue(T object) throws InterruptedException{
		while(isFull()){
			wait();
		}
		if(isEmpty()){
			notifyAll();
		}
		this.queue.add(object);
	}
	
	/**
	 * This is the synchronized method that only one thread can access and pull object out of the queue.
	 * If the queue is empty and has nothing to dequeue, the thread accessing has to wait until notification. In the mean time, the thread releases the lock on it.
	 * @return The object dequeued.
	 * @throws InterruptedException
	 */
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
