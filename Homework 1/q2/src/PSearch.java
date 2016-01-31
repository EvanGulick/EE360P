import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PSearch implements Callable<Integer> {
	
	private static ExecutorService es;
	private int k;
	private int[] A;
	private int begin;
	private int end;
	
	public static int parallelSearch(int k, int[] A, int numThreads){
		
		if(A.length == 0) { //array is empty
			return -1;
		}

		//acquire portion size
		int portionSize = (int) Math.ceil((double)A.length/numThreads);
		es = Executors.newFixedThreadPool(numThreads);
		List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
		for(int i = 0; i< A.length; i+=portionSize){
			int ips = i+portionSize;
			if(ips>A.length){
				ips = A.length;
			}
			PSearch arr = new PSearch(k, A, i, ips - 1);
			Future<Integer> f = es.submit(arr);
			futureList.add(f);
		}
		
		for(int i=0; i<futureList.size(); i++){
			try {
				int indexnum = futureList.get(i).get();
				if(indexnum !=-1){
					return indexnum;
				}
			} catch(ExecutionException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	@Override
	public Integer call() throws Exception {
		//search array
		for(int i = begin; i<=end; i++){
			if(A[i] == k){
				return i;
			}
		}
		//return index if it finds it
		//or return -1
		return -1;
	}
	
	private PSearch (int k, int[]A, int begin, int end) {
		this.k = k;
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
}
