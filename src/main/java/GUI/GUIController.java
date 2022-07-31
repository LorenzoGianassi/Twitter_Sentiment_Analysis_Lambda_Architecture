package GUI;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class GUIController implements Initializable{

    @FXML
    private TableView<String[]> realTimeTableView;
    @FXML
    private TableColumn<String[], String> realTimeKeywordColumn;
    @FXML
    private TableColumn<String[], String> realTimePositiveColumn;
    @FXML
    private TableColumn<String[], String> realTimeNegativeColumn;

    @FXML
    private TableView<String[]> batchTableView;
    @FXML
    private TableColumn<String[], String> batchKeywordColumn;
    @FXML
    private TableColumn<String[], String> batchPosColumn;
    @FXML
    private TableColumn<String[], String> batchNegColumn;

    @FXML
    private BarChart<String, Number> combinedViewsChart;

    private Table realTimeViewTable;
    private Table batchViewTable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        try{
            Configuration config = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(config);
            this.realTimeViewTable = connection.getTable(TableName.valueOf("tweet_realtime_database"));
            this.batchViewTable = connection.getTable(TableName.valueOf("tweet_batch_view"));


            //Real Time TableView

            realTimeTableView.setPlaceholder(new Label("Empty Table"));

            realTimeKeywordColumn.setCellValueFactory(p -> {
                String[] row = p.getValue();
                return new SimpleStringProperty(row[0]);
            });
            realTimePositiveColumn.setCellValueFactory(p -> {
                String[] row = p.getValue();
                return new SimpleStringProperty(row[1]);
            });
            realTimeNegativeColumn.setCellValueFactory(p -> {
                String[] row = p.getValue();
                return new SimpleStringProperty(row[2]);
            });

            realTimeTableView.getItems().setAll(parseRealTimeResultsList(realTimeViewTable));


            //Batch TableView

            batchTableView.setPlaceholder(new Label("Empty Table"));

            batchKeywordColumn.setCellValueFactory(p -> {
                String[] row = p.getValue();
                return new SimpleStringProperty(row[0]);
            });
            batchPosColumn.setCellValueFactory(p -> {
                String[] row = p.getValue();
                return new SimpleStringProperty(row[1]);
            });
            batchNegColumn.setCellValueFactory(p -> {
                String[] row = p.getValue();
                return new SimpleStringProperty(row[2]);
            });

            batchTableView.getItems().setAll(parseBatchResultsList(batchViewTable));


            //Bar Chart

            XYChart.Series<String, Number> posSeries = new XYChart.Series<String, Number>();
            posSeries.setName("Positive");
            XYChart.Series<String, Number> negSeries = new XYChart.Series<String, Number>();
            negSeries.setName("Negative");

            for(String[] row: parseChartSeries(batchViewTable, realTimeViewTable)){
                posSeries.getData().add(new XYChart.Data<String, Number>(row[0], Integer.parseInt(row[1])));
                negSeries.getData().add(new XYChart.Data<String, Number>(row[0], Integer.parseInt(row[2])));
            }
            combinedViewsChart.getData().addAll(posSeries, negSeries);


            //Refresh TableViews and Chart with the new data

            Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                try{
                    realTimeTableView.getItems().setAll(parseRealTimeResultsList(realTimeViewTable));
                    batchTableView.getItems().setAll(parseBatchResultsList(batchViewTable));
                    realTimeTableView.refresh();
                    batchTableView.refresh();

                    posSeries.getData().clear();
                    negSeries.getData().clear();
                    combinedViewsChart.setAnimated(false);
                    for(String[] row: parseChartSeries(batchViewTable, realTimeViewTable)){
                        posSeries.getData().add(new XYChart.Data<String, Number>(row[0], Integer.parseInt(row[1])));
                        negSeries.getData().add(new XYChart.Data<String, Number>(row[0], Integer.parseInt(row[2])));
                    }
                    combinedViewsChart.getData().clear();
                    combinedViewsChart.getData().addAll(posSeries, negSeries);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }),
                    new KeyFrame(Duration.seconds(1))
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    // Aggregating data chart
    private ArrayList<String[]> parseBatchResultsList(Table table) throws IOException{
        ArrayList<String[]> resultList = new ArrayList<>();

        Scan GUIscan = new Scan();
        GUIscan.addFamily(Bytes.toBytes("sentiment_count"));
        ResultScanner resultScanner = table.getScanner(GUIscan);
        for(Result result = resultScanner.next(); result != null; result = resultScanner.next()){
            String rowKey = Bytes.toString(result.getRow());
            String posCount = Bytes.toString(result.getValue(Bytes.toBytes("sentiment_count"), Bytes.toBytes("positive")));
            String negCount = Bytes.toString(result.getValue(Bytes.toBytes("sentiment_count"), Bytes.toBytes("negative")));

            negCount = negCount != null ? negCount : "0";
            posCount = posCount != null ? posCount : "0";

            resultList.add(new String[]{rowKey, posCount, negCount});
        }

        resultList.sort(new Comparator<String[]>(){
            @Override
            public int compare(String[] firstString, String[] secondString){
                return firstString[0].compareTo(secondString[0]);
            }
        });

        return resultList;
    }

    private ArrayList<String[]> parseRealTimeResultsList(Table table) throws IOException{
        ArrayList<String[]> resultList = new ArrayList<>();

        Scan GUIscan = new Scan();
        GUIscan.addFamily(Bytes.toBytes("content"));
        ResultScanner resultScanner = table.getScanner(GUIscan);
        ArrayList<String> keywords = new ArrayList<>();
        ArrayList<Integer> posCount = new ArrayList<>();
        ArrayList<Integer> negCount = new ArrayList<>();
        for(Result result = resultScanner.next(); result != null; result = resultScanner.next()){
            String keyword = Bytes.toString(result.getValue(Bytes.toBytes("content"), Bytes.toBytes("keyword")));
            String sentimentString = Bytes.toString(result.getValue(Bytes.toBytes("content"), Bytes.toBytes("sentiment")));
            int sentiment = Integer.parseInt(sentimentString);

            if(keywords.contains(keyword)){
                int index = keywords.indexOf(keyword);
                if(sentiment == 1){
                    posCount.set(index, posCount.get(index) + 1);
                }
                else{
                    negCount.set(index, negCount.get(index) + 1);
                }
            }
            else{
                keywords.add(keyword);
                if(sentiment == 1){
                    posCount.add(1);
                    negCount.add(0);
                }
                else{
                    posCount.add(0);
                    negCount.add(1);
                }
            }
        }

        for(int i = 0; i < keywords.size(); i++){
            resultList.add(new String[]{keywords.get(i), posCount.get(i).toString(), negCount.get(i).toString()});
        }

        resultList.sort(new Comparator<String[]>(){
            @Override
            public int compare(String[] firstString, String[] secondString){
                return firstString[0].compareTo(secondString[0]);
            }
        });

        return resultList;
    }

    private ArrayList<String[]> parseChartSeries(Table batchTable, Table realTimeTable) throws IOException{
        ArrayList<String[]> chartSeries = new ArrayList<>();
        ArrayList<String[]> batchResults = parseBatchResultsList(batchTable);
        ArrayList<String[]> realTimeResults = parseRealTimeResultsList(realTimeTable);

        for(String[] batchRow: batchResults){
            int posCount = Integer.parseInt(batchRow[1]);
            int negCount = Integer.parseInt(batchRow[2]);
            for(String[] realTimeRow: realTimeResults){
                if(batchRow[0].equals(realTimeRow[0])){
                    posCount += Integer.parseInt(realTimeRow[1]);
                    negCount += Integer.parseInt(realTimeRow[2]);
                }
            }
            chartSeries.add(new String[]{batchRow[0], String.valueOf(posCount), String.valueOf(negCount)});
        }

        for(String[] realTimeRow : realTimeResults){
            boolean found = false;
            for(String[] chartRow : chartSeries){
                found = realTimeRow[0].equals(chartRow[0]);
                if(found){
                    break;
                }
            }
            if(!found){
                chartSeries.add(realTimeRow);
            }
        }

        chartSeries.sort(new Comparator<String[]>(){
            @Override
            public int compare(String[] firstString, String[] secondString){
                return firstString[0].compareTo(secondString[0]);
            }
        });
        return chartSeries;
    }
}
