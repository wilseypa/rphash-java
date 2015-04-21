/*package edu.uc.rphash.MR;

import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
 
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

 
public class MrRPHash{
 
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            System.out.println(line);
            String[] indicesAndValue = line.split(",");
            Text outputKey = new Text();
            Text outputValue = new Text();
            if (indicesAndValue[0].equals("A")) {
                outputKey.set(indicesAndValue[2]);
                outputValue.set("A," + indicesAndValue[1] + "," + indicesAndValue[3]);
                context.write(outputKey, outputValue);
            } else {
                outputKey.set(indicesAndValue[1]);
                outputValue.set("B," + indicesAndValue[2] + "," + indicesAndValue[3]);
                context.write(outputKey, outputValue);
            }
        }
    }
 
    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String[] value;
            ArrayList<Entry<Integer, Float>> listA = new ArrayList<Entry<Integer, Float>>();
            ArrayList<Entry<Integer, Float>> listB = new ArrayList<Entry<Integer, Float>>();
            for (Text val : values) {
                value = val.toString().split(",");
                if (value[0].equals("A")) {
                    listA.add(new SimpleEntry<Integer, Float>(Integer.parseInt(value[1]), Float.parseFloat(value[2])));
                } else {
                    listB.add(new SimpleEntry<Integer, Float>(Integer.parseInt(value[1]), Float.parseFloat(value[2])));
                }
            }
            String i;
            float a_ij;
            String k;
            float b_jk;
            Text outputValue = new Text();
            for (Entry<Integer, Float> a : listA) {
                i = Integer.toString(a.getKey());
                a_ij = a.getValue();
                for (Entry<Integer, Float> b : listB) {
                    k = Integer.toString(b.getKey());
                    b_jk = b.getValue();
                    outputValue.set(i + "," + k + "," + Float.toString(a_ij*b_jk));
                    context.write(null, outputValue);
                }
            }
        }
    }
 
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        
        conf.set("m", "2");
        conf.set("n", "5");
        conf.set("p", "3");
        
        Job job = new Job(conf, "MatrixMatrixMultiplicationTwoSteps");
        job.setJarByClass(MrRPHash.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
 
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
 
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
        job.waitForCompletion(true);
    }
}*/