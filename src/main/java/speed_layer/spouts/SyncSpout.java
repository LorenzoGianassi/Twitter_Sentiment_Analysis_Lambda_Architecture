package speed_layer.spouts;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.io.IOException;
import java.util.Map;

public class SyncSpout extends BaseRichSpout{

    long startTime;
    long endTime;
    String tableName;
    Table table;
    SpoutOutputCollector collector;

    public SyncSpout(String tableName){
        this.startTime= 0;
        this.endTime = 0;
        this.tableName = tableName;
    }

    @Override
    public void open(Map<String, Object> map, TopologyContext TopContext, SpoutOutputCollector spoutOutCollector){
        this.collector = spoutOutCollector;
        try{
            Connection connection = ConnectionFactory.createConnection(HBaseConfiguration.create());
            this.table = connection.getTable(TableName.valueOf(tableName));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void nextTuple(){
        try{
            Result startResult = table.get(new Get(Bytes.toBytes("MapReduce_start_timestamp")));
            Result endResult = table.get(new Get(Bytes.toBytes("MapReduce_end_timestamp")));
            long resultStartTime= startResult.rawCells()[0].getTimestamp();
            long resultEndTime = endResult.rawCells()[0].getTimestamp();
            if(resultStartTime != startTime && resultEndTime != endTime){
                startTime = resultStartTime;
                endTime = resultEndTime;
                collector.emit(new Values(startTime));
            }
            else{
                Utils.sleep(50);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer Declarer){
        Declarer.declare(new Fields("timestamp"));
    }
}
