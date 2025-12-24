package com.snakeandladder;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    private Group boardGroup;
    private List<Tile> tiles;
    private final int ROWS = 10;
    private final int COLS = 10;
    private double tileSize = 60; // Default size, will resize
    private double width;
    private double height;
    
    // Logic for Snakes and Ladders
    private Map<Integer, Integer> snakes;
    private Map<Integer, Integer> ladders;
    private Group slElementsGroup; // Group to hold graphical lines for snakes/ladders

    public Board() {
        boardGroup = new Group();
        tiles = new ArrayList<>();
        slElementsGroup = new Group();
        
        initializeSnakesAndLadders();
        createGrid();
        
        // Add layers: Tiles at bottom, then Snakes/Ladders on top
        boardGroup.getChildren().addAll(slElementsGroup); 
        // Note: Tiles are added directly to boardGroup in createGrid, so we need to manage order
    }
    
    public Group getBoardGroup() {
        return boardGroup;
    }

    private void createGrid() {
        // Clear previous tiles if any
        boardGroup.getChildren().removeAll(tiles);
        tiles.clear();
        
        // Grid logic: 1 is bottom-left, 100 is top-left
        // Rows: 9 down to 0 (visual) mapping to 10..1 or 1..10
        // We iterate 1 to 100 and calculate x,y
        
        for (int i = 0; i < ROWS * COLS; i++) {
            Tile tile = new Tile(i + 1, tileSize);
            tiles.add(tile);
            boardGroup.getChildren().add(0, tile); // Add to bottom
        }
        
        drawBoard();
    }

    private void initializeSnakesAndLadders() {
        snakes = new HashMap<>();
        ladders = new HashMap<>();
        
        // Classic positions (Simplified for now)
        // Snakes (Start -> End, where End < Start)
        snakes.put(16, 6);
        snakes.put(47, 26);
        snakes.put(49, 11);
        snakes.put(56, 53);
        snakes.put(62, 19);
        snakes.put(64, 60);
        snakes.put(87, 24);
        snakes.put(93, 73);
        snakes.put(95, 75);
        snakes.put(98, 78);

        // Ladders (Start -> End, where End > Start)
        ladders.put(1, 38);
        ladders.put(4, 14);
        ladders.put(9, 31);
        ladders.put(21, 42);
        ladders.put(28, 84);
        ladders.put(36, 44);
        ladders.put(51, 67);
        ladders.put(71, 91);
        ladders.put(80, 100);
    }
    
    private void drawBoard() {
        // Position tiles
        for (int i = 0; i < tiles.size(); i++) {
            int number = i + 1;
            Point p = getCoordinatesForNumber(number);
            Tile t = tiles.get(i);
            t.setTranslateX(p.x);
            t.setTranslateY(p.y);
            t.updateSize(tileSize);
        }
        
        drawSnakesAndLadders();
    }
    
    private void drawSnakesAndLadders() {
        slElementsGroup.getChildren().clear();
        
        // Draw Ladders (Green with rungs)
        ladders.forEach((start, end) -> {
            Point p1 = getCenterMoveCoordinates(start);
            Point p2 = getCenterMoveCoordinates(end);
            
            // Vector direction
            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;
            double length = Math.sqrt(dx*dx + dy*dy);
            
            // Normal vector for width
            double nx = -dy / length;
            double ny = dx / length;
            double width = 10;
            
            // Side rails
            Line leftRail = new Line(p1.x - nx*width, p1.y - ny*width, p2.x - nx*width, p2.y - ny*width);
            Line rightRail = new Line(p1.x + nx*width, p1.y + ny*width, p2.x + nx*width, p2.y + ny*width);
            
            leftRail.setStroke(Color.DARKGREEN);
            leftRail.setStrokeWidth(3);
            rightRail.setStroke(Color.DARKGREEN);
            rightRail.setStrokeWidth(3);
            
            slElementsGroup.getChildren().addAll(leftRail, rightRail);
            
            // Rungs
            int steps = (int)(length / 20);
            for (int i = 0; i <= steps; i++) {
                double t = (double)i / steps;
                double cx = p1.x + dx*t;
                double cy = p1.y + dy*t;
                Line rung = new Line(cx - nx*width, cy - ny*width, cx + nx*width, cy + ny*width);
                rung.setStroke(Color.DARKGREEN);
                rung.setStrokeWidth(2);
                slElementsGroup.getChildren().add(rung);
            }
        });

        // Draw Snakes (Red Curves)
        snakes.forEach((start, end) -> {
            Point p1 = getCenterMoveCoordinates(start); // Head
            Point p2 = getCenterMoveCoordinates(end);   // Tail
            
            javafx.scene.shape.QuadCurve curve = new javafx.scene.shape.QuadCurve();
            curve.setStartX(p1.x);
            curve.setStartY(p1.y);
            curve.setEndX(p2.x);
            curve.setEndY(p2.y);
            
            // Control point: midpoint + offset to make it curvy
            double midX = (p1.x + p2.x) / 2;
            double midY = (p1.y + p2.y) / 2;
            
            // Randomish offset for variety or fixed based on direction
            // Simple approach: Curve out perpendicular to the line connecting them
            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;
            // Perpendicular
            curve.setControlX(midX + dy * 0.3); 
            curve.setControlY(midY - dx * 0.3);
            
            curve.setStroke(Color.RED);
            curve.setStrokeWidth(4);
            curve.setFill(null);
            curve.setStrokeLineCap(StrokeLineCap.ROUND);
            
            // Head (Circle for now)
            javafx.scene.shape.Circle head = new javafx.scene.shape.Circle(p1.x, p1.y, 6, Color.DARKRED);
            
            slElementsGroup.getChildren().addAll(curve, head);
        });
    }

    // Helper class for coordinates
    public static class Point {
        public double x, y;
        public Point(double x, double y) { this.x = x; this.y = y; }
    }
    
    // Calculates top-left corner of the tile for placement
    private Point getCoordinatesForNumber(int number) {
        int row = (number - 1) / ROWS; 
        // visual row 0 is top, grid row 0 is bottom (1-10)
        // Let's align:
        // Number 1 -> Row 0 (bottom), Col 0 (left)
        // If we want 1 at bottom-left:
        int viewRow = (ROWS - 1) - row;
        
        int col = (number - 1) % COLS;
        // Snake pattern: If row is even (0, 2, ... from bottom), left-to-right
        // If row is odd (1, 3, ...), right-to-left
        if (row % 2 == 1) {
            col = (COLS - 1) - col;
        }
        
        return new Point(col * tileSize, viewRow * tileSize);
    }

    // Calculates center of the tile for pieces/lines
    public Point getCenterMoveCoordinates(int number) {
        Point p = getCoordinatesForNumber(number);
        return new Point(p.x + tileSize/2, p.y + tileSize/2);
    }
    
    public double getTileSize() {
        return tileSize;
    }

    // Called by Controller when window resizes
    public void resizeBoard(double width, double height) {
        this.width = width;
        this.height = height;
        
        // Keep square aspect ratio or fill? 
        // Let's fill 80% of the smallest dimension to leave room for UI
        double minDim = Math.min(width, height);
        this.tileSize = (minDim * 0.9) / ROWS;
        
        // Center the board
        double startX = (width - (tileSize * COLS)) / 2;
        double startY = (height - (tileSize * ROWS)) / 2;
        
        boardGroup.setTranslateX(startX);
        boardGroup.setTranslateY(startY);
        
        drawBoard();
    }
    
    // Methods for game logic to query board
    public int getSnakeTail(int head) {
        return snakes.getOrDefault(head, -1);
    }
    
    public int getLadderTop(int bottom) {
        return ladders.getOrDefault(bottom, -1);
    }
}
