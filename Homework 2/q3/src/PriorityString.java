/**
 * PriorityString Class for the elements of our priority queue
 * @author Scott Larson and Evan Gulick
 * 
 */

import java.util.List;

public class PriorityString {

	private String name;
	private int priority;
	
	// Construct a PriorityString object with its name and priority
	public PriorityString(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}
	
	// Assumes duplicates have already been addressed if necessary
	// Returns 
	public int findPriority(List<PriorityString> queue) {
		int i = 0;
		while(i < queue.size()) {
			if(queue.get(i).getPriority() >= priority) { break; }
			i += 1;
		}
		return i;
	}

	// Return the name of a PriorityString object
	public String getName() {
		return name;
	}
	
	// Return the priority of a PriorityString object
	public int getPriority() {
		return priority;
	}
}
