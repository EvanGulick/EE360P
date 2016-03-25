import org.apache.hadoop.fs.Path;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {
	
    public static class TextMapper extends Mapper<LongWritable, Text, Text, TupleList> {
    	private static final IntWritable ONE = new IntWritable(1);
    	
        public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
        {
        	String line = value.toString().toLowerCase().replaceAll("\\W", " ");
        	StringTokenizer tokens = new StringTokenizer(line, " ");
        	ArrayList<String> words = new ArrayList<String>(); 
        	while (tokens.hasMoreTokens()) {
        		words.add(tokens.nextToken());
        	}
        	ArrayList<String> contextwordcheck = new ArrayList<String>();
        	for(int i = 0; i < words.size(); i++){
        		if(!contextwordcheck.contains(words.get(i))) {
    				Text contextWord = new Text(words.get(i));
        			ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
        			for(int j = 0; j < words.size(); j++) {
        				if(i != j) {
	        				Text queryWord = new Text(words.get(j));
	        				Tuple tuple = new Tuple(queryWord, ONE);
	        				tupleList.add(tuple);
        				}
        			}
                	context.write(contextWord, new TupleList(tupleList));
        			contextwordcheck.add(words.get(i));
        		}
        	}
        }
    }

    public static class TextReducer extends Reducer<Text, TupleList, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<TupleList> queryTupleList, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function
        	ArrayList<Tuple> queryTuples = new ArrayList<Tuple>();
        	for(TupleList sentence : queryTupleList) {
        		queryTuples.addAll(sentence.tupleList);
        	}
        	
        	ArrayList<String> queryWords = new ArrayList<String>();
        	ArrayList<Integer> count = new ArrayList<Integer>();
        	for(Tuple query : queryTuples) {
        		int qIndex = queryWords.indexOf(query.name.toString());
        		if(qIndex == -1) {
        			queryWords.add(query.name.toString());
        			count.add(query.value.get());
        		} else {
        			count.add(qIndex, count.get(qIndex) + query.value.get());
        			count.remove(qIndex + 1);
        		}
        	}

            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out the current context key
            context.write(key, emptyText);
            //   Write out query words and their count
            Iterator<String> wordItr = queryWords.iterator();
            Iterator<Integer> countItr = count.iterator();
            while(wordItr.hasNext() && countItr.hasNext()) {
            	Text queryWordText = new Text();
            	queryWordText.set("<" + wordItr.next() + ",");
            	String countStr = countItr.next().toString() + ">";
            	context.write(queryWordText, new Text(countStr));
            }
            //   Empty line for ending the current context key
            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "EG22492_SRL888"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);
        //   Uncomment the following line if you want to use Combiner class
        //job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(TupleList.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }

    // You may define sub-classes here
	public static class Tuple implements WritableComparable<Tuple> {
    	public Text name;
    	public IntWritable value;
    	public Tuple() {
    		this.name = new Text();
    		this.value = new IntWritable();
    	}
    	public Tuple(Text name, IntWritable value) {
    		this.name = name;
    		this.value = value;
    	}
		@Override
		public void readFields(DataInput in) throws IOException {
			name.readFields(in);
			value.readFields(in);
		}
		@Override
		public void write(DataOutput out) throws IOException {
			name.write(out);
			value.write(out);
		}
		@Override
		public int compareTo(Tuple other) {
			return this.name.compareTo(other.name);
		}
    }
	public static class TupleList implements WritableComparable<TupleList> {
		public ArrayList<Tuple> tupleList;

		public TupleList() {
			this.tupleList = new ArrayList<Tuple>();
		}
		
		public TupleList(ArrayList<Tuple> newList) {
			this.tupleList = newList;
		}
		
		@Override
		public void readFields(DataInput in) throws IOException {
	        int size = in.readInt();
	        this.tupleList = new ArrayList<Tuple>();
	        for(int i = 0; i < size; i++) {
	            Tuple tuple = new Tuple();
	            tuple.readFields(in);
	            tupleList.add(tuple);
	        }
		}

		@Override
		public void write(DataOutput out) throws IOException {
	        out.writeInt(this.tupleList.size());
	        for(int i = 0; i < this.tupleList.size(); i++) {
	             tupleList.get(i).write(out);
	        }
		}

		@Override
		public int compareTo(TupleList other) {
			if(tupleList.size() == other.tupleList.size()) {
				return 0;
			} else if(tupleList.size() < other.tupleList.size()) {
				return -1;
			}
			return 1;
		}
	}
}
