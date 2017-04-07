import java.io.IOException;
import java.util.StringTokenizer;
import java.util.*;
import org.apache.hadoop.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

class WordCountMapper extends Mapper<LongWritable,Text,Text,Text>
{
        private Text one=new Text();
        private Text word=new Text();
        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException{
                String inp=value.toString();
                String[] lines=inp.split("[\t]");
                String line=lines[1];
                String documentId=lines[0];
                one.set(documentId);
                //System.out.println("LOG-ENTRY-IDENTIFIER: Mapping - " + documentId); // line[0] is the docID. line[1] is the actual text(book).
                StringTokenizer tokenizer=new StringTokenizer(line);
                while(tokenizer.hasMoreTokens()){
                        word.set(tokenizer.nextToken());
                        context.write(word,one);
                        //System.out.println("LOG-ENTRY-IDENTIFIER: Mapping - " +word+"-->"+ one); // line[0] is the docID. line[1] is the actual text(book).
                }
        }
}

class WordCountReducer extends Reducer<Text,Text,Text,Text>{
        public void reduce(Text key,Iterable<Text> values,Context context) throws IOException, InterruptedException{
        Text text=new Text();
        Map<String,Integer> map=new HashMap<String,Integer>();
        for(Text value:values){
        if(map.containsKey(value.toString())){
                map.put(value.toString(),map.get(value.toString())+1);
        }
        else{
                map.put(value.toString(),1);
        }
        }
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<String,Integer> entry:map.entrySet()){
                sb.append('\t');
                sb.append(entry.getKey());
                sb.append(':');
                sb.append(entry.getValue().toString());
        }
        text.set(sb.toString().trim());
        context.write(key,text);
        //System.out.println("Updated Reducer: "+key+"-->"+text);
        }
}
public class WordCount{
        public static void main(String[] args) throws IOException,ClassNotFoundException,InterruptedException{
                if(args.length!=2){
                        System.err.println("Usage: WordCount <input path> <output path>");
                        System.exit(-1);
                }
                Job job=new Job();
                job.setJarByClass(WordCount.class);
                job.setJobName("Word Count");
                FileInputFormat.addInputPath(job,new Path(args[0]));
                FileOutputFormat.setOutputPath(job,new Path(args[1]));
                job.setMapperClass(WordCountMapper.class);
                job.setReducerClass(WordCountReducer.class);
                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(Text.class);
                job.waitForCompletion(true);
        }
}