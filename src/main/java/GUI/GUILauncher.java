package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GUILauncher extends Application {

    private static Scene GUIscene;

    @Override
    public void start(Stage stage) throws IOException{
        stage.setTitle("Twitter Sentiment Analysis");
        GUIscene = new Scene(loadFXML("GUI_Interface"));
        GUIscene.getStylesheets().add(GUILauncher.class.getResource("Style.css").toExternalForm());
        stage.setScene(GUIscene);
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUILauncher.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}