/**
 * Priority Queue implementation. Requires PriorityString.java
 * @author Scott Larson and Evan Gulick
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class PriorityQueue {
	
	private Semaphore mutex;
	private Semaphore isFull;
	private Semaphore isEmpty;
	private List<PriorityString> queue;	// Index ~ Priority
	private int capacity;
	private int size;
    
	// Creates a Priority queue with maximum allowed size as capacity	
	public PriorityQueue(int maxSize) {
		System.out.println("Capacity: " + maxSize);
		this.mutex = new Semaphore(1);
		this.isFull = new Semaphore(1);
		this.isEmpty = new Semaphore(1);
		this.queue = new ArrayList<PriorityString>(maxSize);
		this.capacity = maxSize;
		this.size = 0;
	}
	
    // Adds the name with its priority to this queue.
    // Returns the current position in the list where the name was inserted;
    // otherwise, returns -1 if the name is already present in the list.
    // This method blocks when the list is full.
	public int add(String name, int priority) throws InterruptedException {
		if (size >= capacity) {	//block
			isFull.acquire();
		}
//		mutex.acquire();		
		
		if(search(name) != -1) {return -1;}
		
		PriorityString newString = new PriorityString(name, priority);
		int placement = newString.findPriority(queue);
		queue.add(placement, newString);
		size += 1;
		
//		mutex.release();
		isEmpty.release();
		
		return placement;
	}

    // Returns the position of the name in the list;
    // otherwise, returns -1 if the name is not found.
	public int search(String name) throws InterruptedException {
//		mutex.acquire();
		
		for(int i = 0; i < size; i++) {
			if(queue.get(i).getName().equals(name)) { return i; }
		}
		
//		mutex.release();
		return -1;
	}

    // Retrieves and removes the name with the highest priority in the list,
    // or blocks the thread if the list is empty.
	public String poll() throws InterruptedException {
//		mutex.acquire();
		if(size <= 0) { //block
			isEmpty.acquire();
		}
		
		PriorityString first = queue.get(size-1);
		queue.remove(size-1);
		size -= 1;

//		mutex.release();
		isFull.release();
		
		return first.getName();
	}
}
