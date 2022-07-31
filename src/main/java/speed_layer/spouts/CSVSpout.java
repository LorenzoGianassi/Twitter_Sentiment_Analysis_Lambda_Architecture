package speed_layer.spouts;

import au.com.bytecode.opencsv.CSVReader;

import jodd.util.StringUtil;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSVSpout extends BaseRichSpout{

    public static final String FILE = "datasets/Full_Corpus_Reducted.csv";

    private SpoutOutputCollector collector;
    private List<List<String>> records;

    public CSVSpout() throws IOException{
        this.records = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(FILE));) {
            String[] values;
            while ((values = reader.readNext()) != null) {
                this.records.add(Arrays.asList(values));
            }
        }
        this.records.remove(0);
        Collections.shuffle(records);
    }

    @Override
    public void open(Map<String, Object> map, TopologyContext TopContext, SpoutOutputCollector spoutOutCollector){
        this.collector = spoutOutCollector;
    }

    @Override
    public void nextTuple(){
        for(List<String> record: records){
            if(!record.get(1).equals("irrelevant") && !record.get(1).equals("neutral")){
                ArrayList<String> keywords = new ArrayList<>();
                keywords.add("#" + StringUtil.capitalize(record.get(0)));
                String IDTweet = record.get(2);
                String text = record.get(4);
                collector.emit(new Values(IDTweet, text, keywords));
                Utils.sleep(1000);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputDeclarer){
        outputDeclarer.declare(new Fields("tweet_ID", "text", "keywords"));
    }
}

