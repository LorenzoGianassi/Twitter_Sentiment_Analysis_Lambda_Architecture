package speed_layer.spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class TwStreamSpout extends BaseRichSpout{

    private TwitterStream twitterStream;
    private LinkedBlockingQueue<Status> queue = null;
    private SpoutOutputCollector collector;

    private String CKey;
    private String CSecret;
    private String AccToken;
    private String AccTokenSecret;
    private String[] keywords;

    public TwStreamSpout(String CKey, String CSecret, String AccToken, String AccTokenSecret, String[] keywords){
        this.CKey = CKey;
        this.CSecret = CSecret;
        this.AccToken = AccToken;
        this.AccTokenSecret = AccTokenSecret;
        this.keywords = keywords;
    }

    public void open(Map map, TopologyContext TopContext, SpoutOutputCollector spoutOutCollector){
        this.collector = spoutOutCollector;
        queue = new LinkedBlockingQueue<Status>(1000);

        StatusListener listener = new StatusListener(){
            public void onStatus(Status status){
                queue.offer(status);
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice){}

            public void onTrackLimitationNotice(int i){}

            public void onScrubGeo(long l, long l1){}

            public void onStallWarning(StallWarning stallWarning){}

            public void onException(Exception e){}
        };

        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(CKey);
        cb.setOAuthConsumerSecret(CSecret);
        cb.setOAuthAccessToken(AccToken);
        cb.setOAuthAccessTokenSecret(AccTokenSecret);

        this.twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);

        if(keywords.length == 0){
            twitterStream.sample();
        }
        else{
            FilterQuery query = new FilterQuery().track(keywords);
            twitterStream.filter(query);
        }

    }

    public void nextTuple(){
        Status tweet = queue.poll();
        if(tweet == null){
            Utils.sleep(50);
        }
        else {
            if(tweet.getLang().equals("en")){
                collector.emit(new Values(tweet));
            }
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer){
        outputFieldsDeclarer.declare(new Fields("tweet"));
    }

    @Override
    public void close(){
        twitterStream.shutdown();
    }
}
