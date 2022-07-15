package batch_layer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Classifier.ClassifierLambdaArchitecture;
import org.apache.hadoop.io.Writable;


public class BatchMapper extends TableMapper<Text, Text>{

    private ClassifierLambdaArchitecture classifierModel;
    private long startTime;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException{

        Configuration config = context.getConfiguration();
        startTime = Long.parseLong(config.get("start"));

        File modelFile = new File(context.getCacheFiles()[0].toString());
        try{
            classifierModel = new ClassifierLambdaArchitecture(modelFile);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException{
        long check = value.rawCells()[0].getTimestamp();

        if(check <= startTime){
            byte[] byteText = value.getValue(Bytes.toBytes("content"), Bytes.toBytes("text"));
            String text = new String(byteText);
            byte[] byteKeywords = value.getValue(Bytes.toBytes("content"), Bytes.toBytes("keywords"));
            ArrayWritable writable = new ArrayWritable(Text.class);
            writable.readFields(new DataInputStream(new ByteArrayInputStream(byteKeywords)));
            ArrayList<String> keywords = fromWritable(writable);

            String sentiment = classifierModel.classify(text);
            for(String keyword: keywords){
                context.write(new Text(keyword), new Text(sentiment));
            }
        }

    }

   // from Writable to ArrayList<String>
    private static ArrayList<String> fromWritable(ArrayWritable writable){
        Writable[] writables = ((ArrayWritable) writable).get();
        ArrayList<String> writableList = new ArrayList<String>(writables.length);
        for (Writable w : writables){
            writableList.add(((Text)w).toString());
        }
        return writableList;
    }

}
