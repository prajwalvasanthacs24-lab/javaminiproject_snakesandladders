package com.snakeandladder;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameController {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Board gameBoard;
    private Dice dice;
    
    private List<Player> players;
    private int currentPlayerIndex = 0;
    private boolean gameRunning = false;
    
    // UI Elements
    private VBox sidePanel;
    private Label statusLabel;
    private Label turnLabel;

    public void initialize(Stage stage) {
        this.primaryStage = stage;
        this.rootLayout = new BorderPane();
        this.players = new ArrayList<>();

        // Initialize Board
        gameBoard = new Board();
        rootLayout.setCenter(gameBoard.getBoardGroup());

        // Initialize Side Panel
        createSidePanel();
        rootLayout.setRight(sidePanel);

        Scene scene = new Scene(rootLayout, 1000, 700);
        
        primaryStage.setTitle("Snake and Ladder - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        
        // Ensure the board resizes with the window
        setupResizeListeners(scene);
        
        // Start Game Setup
        setupGame();
    }
    
    private void createSidePanel() {
        sidePanel = new VBox(20);
        sidePanel.setPrefWidth(250);
        sidePanel.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 20;");
        sidePanel.setAlignment(Pos.CENTER);
        
        Label title = new Label("Snake & Ladder");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        turnLabel = new Label("Waiting for start...");
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        turnLabel.setTextFill(Color.DARKBLUE);
        
        statusLabel = new Label("Welcome!");
        statusLabel.setWrapText(true);
        statusLabel.setFont(Font.font("Arial", 14));
        
        // Initialize Dice with callback
        dice = new Dice(this::handleRoll);
        dice.setRollingDisable(true); // Disabled until game starts
        
        sidePanel.getChildren().addAll(title, turnLabel, dice, statusLabel);
    }

    private void setupGame() {
        // Simple input for now - can be expanded to a custom dialog
        TextInputDialog dialog = new TextInputDialog("2");
        dialog.setTitle("Game Setup");
        dialog.setHeaderText("Welcome to Snake & Ladder");
        dialog.setContentText("Enter number of players (2-6):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int count = Integer.parseInt(result.get());
                if (count < 2) count = 2;
                if (count > 6) count = 6;
                
                initializePlayers(count);
            } catch (NumberFormatException e) {
                initializePlayers(2); // Default
            }
        } else {
             // User cancelled, maybe exit or default
             initializePlayers(2);
        }
    }
    
    private void initializePlayers(int count) {
        players.clear();
        Color[] availableColors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.CYAN};
        
        for (int i = 0; i < count; i++) {
            Player p = new Player("Player " + (i + 1), i + 1, availableColors[i % availableColors.length]);
            players.add(p);
            gameBoard.getBoardGroup().getChildren().add(p.getToken());
            // Initial position update handled by resize or first draw, but let's force place them at 1
            placePlayerAt(p, 1);
        }
        
        gameRunning = true;
        currentPlayerIndex = 0;
        dice.setRollingDisable(false);
        updateTurnUI();
    }
    
    private void placePlayerAt(Player p, int position) {
        p.setPosition(position);
        var point = gameBoard.getCenterMoveCoordinates(position);
        // Add small offset based on player ID to avoid total overlap
        double offset = (p.getToken().getRadius() * 0.5) * (players.indexOf(p) % 3);
        p.placeAt(point.x + offset, point.y + offset);
    }

    private void handleRoll(int rolledValue) {
        if (!gameRunning) return;
        
        Player currentPlayer = players.get(currentPlayerIndex);
        statusLabel.setText(currentPlayer.getName() + " rolled a " + rolledValue);
        
        movePlayer(currentPlayer, rolledValue);
    }
    
    private void movePlayer(Player player, int steps) {
        int currentPos = player.getPosition();
        int targetPos = currentPos + steps;
        
        if (targetPos > 100) {
            statusLabel.setText(player.getName() + " needs exact roll to win!");
            switchTurn();
            return;
        }
        
        // Animate move (simple direct move for now, can be step-by-step later)
        var point = gameBoard.getCenterMoveCoordinates(targetPos);
        double offset = (player.getToken().getRadius() * 0.5) * (players.indexOf(player) % 3);
        
        player.animateMove(point.x + offset, point.y + offset, () -> {
            player.setPosition(targetPos);
            checkTileEvents(player);
        });
    }
    
    private void checkTileEvents(Player player) {
        int pos = player.getPosition();
        
        // Check Snake
        int snakeTail = gameBoard.getSnakeTail(pos);
        if (snakeTail != -1) {
            statusLabel.setText("Oh no! " + player.getName() + " bitten by a snake!");
            animateSpecialMove(player, snakeTail);
            return; // Turn ends after slide
        }
        
        // Check Ladder
        int ladderTop = gameBoard.getLadderTop(pos);
        if (ladderTop != -1) {
            statusLabel.setText("Yay! " + player.getName() + " climbed a ladder!");
            animateSpecialMove(player, ladderTop);
            return; // Turn ends after climb
        }
        
        // Check Win
        if (pos == 100) {
            gameRunning = false;
            statusLabel.setText("WINNER: " + player.getName());
            showVictoryDialog(player);
            return;
        }
        
        switchTurn();
    }
    
    private void animateSpecialMove(Player player, int targetPos) {
        var point = gameBoard.getCenterMoveCoordinates(targetPos);
        double offset = (player.getToken().getRadius() * 0.5) * (players.indexOf(player) % 3);
        
        // Small delay before sliding/climbing
        var pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pause.setOnFinished(e -> {
             player.animateMove(point.x + offset, point.y + offset, () -> {
                player.setPosition(targetPos);
                
                // Check win condition again just in case (Ladder to 100)
                if (targetPos == 100) {
                    gameRunning = false;
                    statusLabel.setText("WINNER: " + player.getName());
                    showVictoryDialog(player);
                } else {
                    switchTurn();
                }
            });
        });
        pause.play();
    }
    
    private void switchTurn() {
        if (!gameRunning) return;
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        updateTurnUI();
        dice.setRollingDisable(false); // Enable for next player
    }
    
    private void updateTurnUI() {
        Player p = players.get(currentPlayerIndex);
        turnLabel.setText("Turn: " + p.getName());
        turnLabel.setTextFill(p.getColor());
    }
    
    private void showVictoryDialog(Player winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Victory!");
        alert.setHeaderText("We have a winner!");
        alert.setContentText(winner.getName() + " has won the game!");
        alert.show();
        dice.setRollingDisable(true);
    }

    private void setupResizeListeners(Scene scene) {
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (gameBoard != null) {
                // Adjust board size (keeping 250px for side panel)
                gameBoard.resizeBoard(newVal.doubleValue() - 250, scene.getHeight());
                // Re-position players
                refreshPlayerPositions();
            }
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (gameBoard != null) {
                gameBoard.resizeBoard(scene.getWidth() - 250, newVal.doubleValue());
                refreshPlayerPositions();
            }
        });
    }
    
    private void refreshPlayerPositions() {
        for (Player p : players) {
            placePlayerAt(p, p.getPosition());
        }
    }
}
