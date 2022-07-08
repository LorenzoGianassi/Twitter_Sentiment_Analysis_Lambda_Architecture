package speed_layer.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import twitter4j.Status;

import java.util.ArrayList;
import java.util.Map;

// Parsing of a tweet object to a tuple object

public class ParserTweetBolt extends BaseRichBolt{

    private OutputCollector collector;
    String[] keywords;

    public ParserTweetBolt(String[] keywords){
        this.keywords = keywords;
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext TopContext, OutputCollector OutCollector){
        this.collector = OutCollector;
    }

    // tuples ("tweet_ID", "text", "keywords")
    @Override
    public void declareOutputFields(OutputFieldsDeclarer Declarer){
        Declarer.declare(new Fields("tweet_ID", "text", "keywords"));
    }

    @Override
    public void execute(Tuple tuple){
        Status tweet = (Status) tuple.getValueByField("tweet");
        ArrayList<String> tweetKeywords = new ArrayList<String>();
        for(String keyword : keywords){
            if(tweet.getText().contains(keyword) || tweet.getText().contains(keyword.toLowerCase())){
                tweetKeywords.add(keyword);
            }
        }
        if(tweetKeywords.size() > 0){
            collector.emit(new Values(String.valueOf(tweet.getId()), tweet.getText(), tweetKeywords));
        }
    }

}

