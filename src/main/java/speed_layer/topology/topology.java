package speed_layer.topology;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.shade.com.google.common.io.Files;
import org.apache.storm.topology.TopologyBuilder;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import speed_layer.bolts.*;
import speed_layer.spouts.*;

public class topology{
    public static void main(String[] args) throws Exception{

        // Initialize the twitter credentials requested by Twitter API
        List<String> lines = Files.readLines(new File("TwitterCredentials"), Charset.defaultCharset());
        String CKey = Arrays.asList(lines.get(0).split(":")).get(1).trim();
        String CSecret = Arrays.asList(lines.get(1).split(":")).get(1).trim();
        String AccToken = Arrays.asList(lines.get(2).split(":")).get(1).trim();
        String AccTokenSecret = Arrays.asList(lines.get(3).split(":")).get(1).trim();

        //Read keywords as an argument
        String[] arguments = args.clone();
        String[] keywords = Arrays.copyOfRange(arguments, 0, arguments.length);

        Config config = new Config();
        config.setDebug(true);

        // Creating the Hbase table for the serving layer
        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        Admin admin = connection.getAdmin();

        // Batch View
        if(!admin.tableExists(TableName.valueOf("batch_view"))){
            TableDescriptor BatchView = TableDescriptorBuilder.newBuilder(TableName.valueOf("atch_view"))
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("sentiment_count".getBytes()).build()).build();

            admin.createTable(BatchView);
        }


        // Master Database
        if(!admin.tableExists(TableName.valueOf("master_database"))){
            TableDescriptor MasterDatabase = TableDescriptorBuilder.newBuilder(TableName.valueOf("master_database"))
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("content".getBytes()).build()).build();

            admin.createTable(MasterDatabase);
        }

        // Realtime Database
        if(!admin.tableExists(TableName.valueOf("realtime_database"))){
            TableDescriptor RealtimeView = TableDescriptorBuilder.newBuilder(TableName.valueOf("realtime_database"))
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("content".getBytes()).build()).build();

            admin.createTable(RealtimeView);
        }


        // Syncchronization table
        if(!admin.tableExists(TableName.valueOf("sync_table"))){
            TableDescriptor sync = TableDescriptorBuilder.newBuilder(TableName.valueOf("sync_table"))
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("placeholder".getBytes()).build()).build();

            admin.createTable(sync);
            Table table = connection.getTable(TableName.valueOf("sync_table"));
            table.put(new Put(Bytes.toBytes("MapRed_start_timestamp"), 0)
                    .addColumn(Bytes.toBytes("placeholder"), Bytes.toBytes(""), Bytes.toBytes("")));
            table.put(new Put(Bytes.toBytes("MapRed_end_timestamp"), 0)
                    .addColumn(Bytes.toBytes("placeholder"), Bytes.toBytes(""), Bytes.toBytes("")));
        }

        // Building the topology using bolts and spouts
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("Stream-Spout", new TwStreamSpout(CKey,CSecret,AccToken,AccTokenSecret,keywords));

        builder.setBolt("Parser-Bolt", new ParserTweetBolt(keywords)).shuffleGrouping("Stream-Spout");

        builder.setSpout("CSV-Spout", new CSVSpout());

        builder.setBolt("Classifier-Bolt", new ClassifierBolt("SentimentClassifierTrainedModel.model")).shuffleGrouping("Parser-Bolt").shuffleGrouping("CSV-Spout");

        builder.setBolt("MasterDb-Bolt", new MasterDbBolt("tweet_master_Db")).shuffleGrouping("Parser-Bolt").shuffleGrouping("CSV-Spout");

        builder.setBolt("RealTimeDb-Bolt", new RealTimeDbBolt("Real_time_Db")).shuffleGrouping("Classifier-Bolt");

        builder.setSpout("Syncronization-Spout", new SyncSpout("Synchronization"));

        builder.setBolt("Synchronization-Bolt", new SyncBolt("Real_time_Db")).shuffleGrouping("Synchronizaion-Spout");


        //Initialize the Cluster
        LocalCluster Cluster = new LocalCluster();
        Cluster.submitTopology("StormTopology", config, builder.createTopology());
        Thread.sleep(1200000);
        Cluster.shutdown();



    }
}
