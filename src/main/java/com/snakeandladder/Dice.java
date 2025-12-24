package com.snakeandladder;

import javafx.animation.RotateTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.Random;
import java.util.function.Consumer;

public class Dice extends VBox {
    
    private StackPane diceGraphics;
    private Rectangle diceFace;
    private Text diceValueText;
    private Button rollButton;
    private Random random;
    private boolean isRolling = false;

    public Dice(Consumer<Integer> onRollCallback) {
        this.random = new Random();
        this.setSpacing(10);
        this.setAlignment(javafx.geometry.Pos.CENTER);

        // Graphics
        diceGraphics = new StackPane();
        diceFace = new Rectangle(60, 60, Color.WHITE);
        diceFace.setArcHeight(15);
        diceFace.setArcWidth(15);
        diceFace.setStroke(Color.BLACK);
        diceFace.setStrokeWidth(2);

        diceValueText = new Text("1");
        diceValueText.setFont(Font.font(30));

        diceGraphics.getChildren().addAll(diceFace, diceValueText);

        // Roll Button
        rollButton = new Button("ROLL");
        rollButton.setStyle("-fx-font-size: 16px; -fx-base: #4a90e2; -fx-text-fill: white;");
        rollButton.setOnAction(e -> {
            if (!isRolling) {
                performRoll(onRollCallback);
            }
        });

        getChildren().addAll(diceGraphics, rollButton);
    }

    public void setRollingDisable(boolean disable) {
        rollButton.setDisable(disable);
    }

    private void performRoll(Consumer<Integer> callback) {
        isRolling = true;
        setRollingDisable(true); // Prevent double click

        // Animation
        RotateTransition rt = new RotateTransition(Duration.millis(500), diceGraphics);
        rt.setByAngle(360);
        rt.setCycleCount(2);
        rt.setAutoReverse(true);
        
        rt.setOnFinished(e -> {
            int rolledNumber = random.nextInt(6) + 1;
            diceValueText.setText(String.valueOf(rolledNumber));
            isRolling = false; // Logic will determine when to re-enable
            callback.accept(rolledNumber);
        });
        
        rt.play();
    }
}
