package com.jaceg18.jclicker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    public static final String version = "1.0.1", author = "ftJace", appName = "JClicker";
    private static Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 603, 310);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("dark-theme.css")).toExternalForm());
        stage.setTitle(appName + " v" + version);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("logo.png"))));
        stage.setScene(scene);

        controller = fxmlLoader.getController();

        stage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.cleanup();
            }
        });
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}