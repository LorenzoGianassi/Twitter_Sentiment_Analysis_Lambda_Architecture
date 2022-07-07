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


public class topology{
    public static void main(String[] args) throws Exception{

        // Initialize the twitter credentials requested by Twitter API
        List<String> lines = Files.readLines(new File("TwitterCredentials"), Charset.defaultCharset());
        String consumerKey = Arrays.asList(lines.get(0).split(":")).get(1).trim();
        String consumerSecret = Arrays.asList(lines.get(1).split(":")).get(1).trim();
        String accessToken = Arrays.asList(lines.get(2).split(":")).get(1).trim();
        String accessTokenSecret = Arrays.asList(lines.get(3).split(":")).get(1).trim();

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


        // implementation of spouts and bolts
    }
}
