package com.snakeandladder;

import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Player {
    private String name;
    private int id; // 1-based index
    private int currentPosition; // 1-100
    private Circle token;
    private Color color;

    public Player(String name, int id, Color color) {
        this.name = name;
        this.id = id;
        this.color = color;
        this.currentPosition = 1; // Start at position 1

        this.token = new Circle(15, color);
        this.token.setStroke(Color.BLACK);
        this.token.setStrokeWidth(2);
    }

    public Circle getToken() {
        return token;
    }
    
    public int getPosition() {
        return currentPosition;
    }

    public void setPosition(int position) {
        this.currentPosition = position;
    }
    
    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    // Move animation to a specific X, Y coordinate
    public void animateMove(double x, double y, Runnable onFinished) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), token);
        // We need to set 'To' positions relative to the token's Parent (Group)
        // However, TranslateTransition works on translation delta or absolutes if node is at 0,0
        // It's safer if the token is managed by the Board or Controller which sets layoutX/Y.
        // But if we use setTranslateX/Y, we can animate those.
        
        tt.setToX(x);
        tt.setToY(y);
        tt.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        tt.play();
    }
    
    // Immediate placement without animation (for setup/resize)
    public void placeAt(double x, double y) {
        token.setTranslateX(x);
        token.setTranslateY(y);
    }
}
