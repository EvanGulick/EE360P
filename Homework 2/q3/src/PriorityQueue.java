/**
 * Priority Queue implementation. Requires PriorityString.java
 * @author Scott Larson and Evan Gulick
 *
 */

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	
	private ReentrantLock addLock, pollLock;
	private Condition isFull, isEmpty;
	private LinkedList<PriorityString> queue;	// Index ~ Priority
	private AtomicInteger size;
	private int capacity;
    
	// Creates a Priority queue with maximum allowed size as capacity	
	public PriorityQueue(int maxSize) {
		this.addLock = new ReentrantLock();
		this.pollLock = new ReentrantLock();
		this.isEmpty = pollLock.newCondition();
		this.isFull = addLock.newCondition();
		
		this.queue = new LinkedList<PriorityString>();
		this.size = new AtomicInteger(0);
		this.capacity = maxSize;
	}
	
    // Adds the name with its priority to this queue.
    // Returns the current position in the list where the name was inserted;
    // otherwise, returns -1 if the name is already present in the list.
    // This method blocks when the list is full.
	public int add(String name, int priority) throws InterruptedException {
		int placement;
		addLock.lock();
		boolean wakePollers = false;
		
		try {
			while(size.get() >= capacity) {
				try {
					isFull.await();
				} catch (InterruptedException e) {}
			}
			
			if(search(name) != -1) { throw new IllegalArgumentException(); }
			
			PriorityString newString = new PriorityString(name, priority);
			placement = newString.findPriority(queue);
			queue.add(placement, newString);
			size.getAndIncrement();
			wakePollers = true;
		} catch (IllegalArgumentException e) {
			placement = -1;
		} finally {
			addLock.unlock();
		}
		
		if(wakePollers && placement != -1) {
			pollLock.lock();
			try {
				isEmpty.signalAll();
			} finally {
				pollLock.unlock();
			}
		}
		
		return placement;
	}

    // Returns the position of the name in the list;
    // otherwise, returns -1 if the name is not found.
	public int search(String name) throws InterruptedException {
		for(int i = 0; i < size.get(); i++) {
			if(queue.get(i).getName().equals(name)) { return i; }
		}
		return -1;
	}

    // Retrieves and removes the name with the highest priority in the list,
    // or blocks the thread if the list is empty.
	public String poll() throws InterruptedException {
		pollLock.lock();
		boolean wakeAdders = false;
		PriorityString first;
		
		try {
			while(size.get() <= 0) {
				try {
					isEmpty.await();
				} catch (InterruptedException e) {}
			}
			first = queue.pollLast();
			size.getAndDecrement();
			wakeAdders = true;
		} finally {
			pollLock.unlock();
		}
		
		if(wakeAdders) {
			addLock.lock();
			try {
				isFull.signalAll();
			} finally {
				addLock.unlock();
			}
		}
		
		return first.getName();
	}
}
