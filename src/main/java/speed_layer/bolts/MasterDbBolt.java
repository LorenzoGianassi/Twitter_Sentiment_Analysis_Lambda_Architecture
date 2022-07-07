package speed_layer.bolts;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MasterDbBolt extends BaseRichBolt{

    private String tableName;
    private Table table;


    public MasterDbBolt(String tableName){
        this.tableName = tableName;

    }
    // convert a list to an arraywritable object
    public static Writable toWritable(ArrayList<String> list) {
        Writable[] content = new Writable[list.size()];
        for (int i = 0; i < content.length; i++) {
            content[i] = new Text(list.get(i));
        }
        return new ArrayWritable(Text.class, content);
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector){
        try{
            Configuration config = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(config);
            this.table = connection.getTable(TableName.valueOf(tableName));
        }catch(IOException e){
            e.printStackTrace();
        }
    }
   // for each tuple put the values inside MasterDB
    @Override
    public void execute(Tuple tuple){
        String tweetID = (String) tuple.getValueByField("tweet_ID");
        String tweetText = (String) tuple.getValueByField("tweet_text");
        ArrayList<String> keywords = (ArrayList<String>) tuple.getValueByField("keywords");
        Put row = new Put(Bytes.toBytes(tweetID));
        row.addColumn(Bytes.toBytes("content"), Bytes.toBytes("text"), Bytes.toBytes(tweetText));
        row.addColumn(Bytes.toBytes("content"), Bytes.toBytes("keywords"), WritableUtils.toByteArray(toWritable(keywords)));
        try{
            table.put(row);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer){}

}
