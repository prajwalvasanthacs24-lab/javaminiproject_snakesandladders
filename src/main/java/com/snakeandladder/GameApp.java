package com.snakeandladder;

import javafx.application.Application;
import javafx.stage.Stage;

public class GameApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameController controller = new GameController();
        controller.initialize(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
