package company.appxeiva;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Starter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Starter.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("App Xeiva");
        stage.setMinHeight(400);
        stage.setMaxHeight(400);
        stage.setMaxWidth(600);
        stage.setMinWidth(600);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}