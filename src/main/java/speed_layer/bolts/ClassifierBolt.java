package speed_layer.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import Classifier.ClassifierLambdaArchitecture;

public class ClassifierBolt extends BaseRichBolt{

    private OutputCollector collector;
    private String classifierModelPath;
    private ClassifierLambdaArchitecture classifier;

    public ClassifierBolt(String classifierModelPath){
        this.classifierModelPath = classifierModelPath;
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext TopContext, OutputCollector OutCollector){
        this.collector = OutCollector;
        File modelFile = new File(classifierModelPath);
        try{
            classifier = new ClassifierLambdaArchitecture(modelFile);
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple){
        String text = (String) tuple.getValueByField("text");
        ArrayList<String> keywords = (ArrayList<String>) tuple.getValueByField("keywords");
        String sentiment = classifier.classify(text);
        for(String keyword: keywords){
            collector.emit(new Values(keyword, sentiment));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer){
        declarer.declare(new Fields("keyword", "sentiment"));
    }
}