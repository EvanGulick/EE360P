/**
 * ReadWriteLock.java
 * @author Scott Larson and Evan Gulick
 *
 */

import java.util.concurrent.Semaphore;

//This class has to provide the following properties:
//	a. There is no read-write or write-write conflict.
//	b. A writer thread that invokes beginWrite() will be block only when
// 		there is a thread holding the lock.
//	c. A reader thread that invokes beginRead() will be block if either
// 		the lock is held by a writer or there is a waiting writer thread.
//	d. A reader thread cannot be blocked if all preceding writer threads
// 		have acquired and released the lock or no preceding writer thread
// 		exists.
public class ReadWriteLock {
    Semaphore mutex = new Semaphore(1);
    Semaphore rlock = new Semaphore(1);
    int numWriters = 0;

	public void beginRead() throws InterruptedException {
		 rlock.acquire();
	}

	public void endRead() throws InterruptedException {
		 rlock.release();

	}

	public void beginWrite() throws InterruptedException {
		 mutex.acquire();
		 numWriters++;
	     if (numWriters == 1) rlock.acquire();
		 mutex.release();
	}

	public void endWrite() throws InterruptedException {
		 mutex.acquire();
		 numWriters--;
	     if (numWriters == 0) rlock.release();
		 mutex.release();
	}
}
