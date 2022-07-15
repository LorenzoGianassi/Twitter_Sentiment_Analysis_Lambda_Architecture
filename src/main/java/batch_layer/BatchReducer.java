package batch_layer;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import java.io.IOException;


public class BatchReducer extends TableReducer<Text, Text, ImmutableBytesWritable>{
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
        int posCount = 0;
        int negCount = 0;
        for(Text value: values){
            if(value.equals(new Text("1"))){
                posCount++;
            }
            else{
                negCount++;
            }
        }
        Put newRow = new Put(Bytes.toBytes(key.toString()));
        newRow.addColumn(Bytes.toBytes("sentiment_count"), Bytes.toBytes("positive"), Bytes.toBytes(Integer.toString(posCount)));
        newRow.addColumn(Bytes.toBytes("sentiment_count"), Bytes.toBytes("negative"), Bytes.toBytes(Integer.toString(negCount)));
        context.write(null, newRow);
    }
}
