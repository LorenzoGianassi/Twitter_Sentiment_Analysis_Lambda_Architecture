package Classifier;



import au.com.bytecode.opencsv.CSVReader;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.CommaSeparatedValues;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ClassifierLambdaArchitecture {

    public static final String TRAINING_FILE = "datasets/Sentiment140.csv";
    public static final String TEST_FILE = "datasets/full-corpus2.csv";
    public static final String MODEL_FILE = "SentimentClassifierTrainedModel.model";



    private DynamicLMClassifier<NGramProcessLM> trainingClassifier;
    private LMClassifier<LanguageModel, MultivariateDistribution> trainedClassifier;

    ClassifierLambdaArchitecture() {
        trainingClassifier = DynamicLMClassifier.createNGramProcess(new String[]{"1", "0"}, 8);
    }

    public ClassifierLambdaArchitecture(File modelFile) throws IOException, ClassNotFoundException{
        trainedClassifier = (LMClassifier<LanguageModel, MultivariateDistribution>) AbstractExternalizable.readObject(modelFile);
    }

    void train(String fileName) throws IOException {
        System.out.println("Training classifier");
        File file = new File(fileName);
        CommaSeparatedValues csv = new CommaSeparatedValues(file, "UTF-8");
        String[][] rows = csv.getArray();

        int i = 0;
        for(String[] row : rows){
            if(row.length != 6){
                continue;
            }
            i++;
            System.out.println("Current Training-Row: " + i);
            // text of the tweet
            String text = row[5];
            // sentiment associated to the text tweet
            String sentiment = row[0];
            if(sentiment.equals("4")){
                sentiment = "1";
            }
            Classification classification = new Classification(sentiment);
            Classified<CharSequence> classified = new Classified<CharSequence>(text, classification);
            trainingClassifier.handle(classified);
        }
    }


    // Method to test the trained model
    void evaluate(String fileName) throws IOException {
        System.out.println("\nEvaluating classifier");

        List<List<String>> records = new ArrayList<>();
        try (CSVReader Reader = new CSVReader(new FileReader(TEST_FILE));) {
            String[] values;
            while ((values = Reader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        }
        records.remove(0);
        int countTests = 0;
        int countCorrect = 0;
        for(List<String> row: records){
            if(row.get(1).equals("irrelevant") || row.get(1).equals("neutral")){
                continue;
            }
            countTests++;
            String text = row.get(4);
            String sentiment = row.get(1);
            if(sentiment.equals("positive")){
                sentiment = "1";
            }
            else{
                sentiment = "0";
            }

            Classification classification = trainedClassifier.classify(text);
            if (classification.bestCategory().equals(sentiment))
                ++countCorrect;
        }
        System.out.println("  # Test Cases=" + countTests);
        System.out.println("  # Correct=" + countCorrect);
        System.out.println("  % Correct=" + ((double)countCorrect)/(double)countTests);
    }

    // method to save the model
    public void storeModel(String fileName) throws IOException {
        FileOutputStream fileOutStream = new FileOutputStream(fileName);
        ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
        trainingClassifier.compileTo(objectOutStream);
        objectOutStream.close();
    }

    public String classify(String tweet) {
        return trainedClassifier.classify(tweet).bestCategory();
    }

    public static void main(String[] args) throws Exception{
        File modelFile = new File(MODEL_FILE);
        if(modelFile.exists()){
            ClassifierLambdaArchitecture classifierModel = new ClassifierLambdaArchitecture(modelFile);
            classifierModel.evaluate(TEST_FILE);
        }
        else{
            ClassifierLambdaArchitecture classifierModel = new ClassifierLambdaArchitecture();
            classifierModel.train(TRAINING_FILE);
            classifierModel.train(TRAINING_FILE);
            classifierModel.storeModel(MODEL_FILE);
            System.out.println("Stored trained model in " + MODEL_FILE);
        }
    }
}
