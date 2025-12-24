package com.snakeandladder;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Tile extends StackPane {
    
    private Rectangle border;
    private Text text;
    private int number;

    public Tile(int number, double size) {
        this.number = number;
        
        border = new Rectangle(size, size);
        // Checkerboard effect logic or custom colors can go here
        Color color = (number % 2 == 0) ? Color.LIGHTYELLOW : Color.LIGHTCYAN;
        border.setFill(color);
        border.setStroke(Color.BLACK);

        text = new Text(String.valueOf(number));
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        getChildren().addAll(border, text);
        
        // Initial position will be set by the Board class
    }

    public void updateSize(double newSize) {
        border.setWidth(newSize);
        border.setHeight(newSize);
        text.setFont(Font.font("Arial", FontWeight.BOLD, newSize * 0.3));
    }
}
