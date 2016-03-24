/*import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.security.auth.login.Configuration;
//import javax.xml.soap.Text;

import org.apache.hadoop.conf.Configured;
//import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
//import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
*/
import org.apache.hadoop.fs.Path;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
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
	//private Text contextword = new Text();
	//private Text queryword = new Text();
    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
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
        	for(int i = 0; i < words.size()-1; i++){
        		if(!contextwordcheck.contains(words.get(i))){
        			for(int j = i + 1; j < words.size(); j++){
        				Text contextWord = new Text(words.get(j));
        				Tuple tuple = new Tuple(contextWord, ONE);
        				context.write(contextWord, tuple);
        			}
        			contextwordcheck.add(words.get(i));
        		}
        	}
        	
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
    	private static final IntWritable ONE = new IntWritable(1);
        public void reduce(Text key, Iterable<Tuple> tuples, Context context)
            throws IOException, InterruptedException
        {
        	for(Tuple tuple: tuples){
        		if(!(key.equals(tuple.name))){
        			Tuple newtuple = new Tuple(key, ONE);
        			context.write(tuple.name, newtuple);
        		}
        		context.write(key, tuple);
        	}
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<Tuple> queryTuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function
        	ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
        	for(Tuple query : queryTuples){
        		boolean queryinlist = false;
        		for(int i = 0; i < tupleList.size(); i++){
        			if(tupleList.get(i).name.equals(query.name)){
        				int tmpValue = tupleList.get(i).value.get() + query.value.get();
        				tupleList.get(i).value.set(tmpValue);
        				queryinlist = true;
        				break;
        			}
        		}
        		if(!queryinlist){
        			tupleList.add(query);
        		}
        	}

            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out the current context key
            context.write(key, emptyText);
            //   Write out query words and their count
            for(Tuple tupleQuery: tupleList){
            	String queryWord = tupleQuery.name.toString();
                String count = tupleQuery.value.toString() + ">";
                Text queryWordText = new Text();
                queryWordText.set("<" + queryWord + ",");
                context.write(queryWordText, new Text(count));
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
        job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Tuple.class);

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
    	//Tool tool = new TextAnalyzer();
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
}
