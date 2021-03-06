package main.java.gui;

import main.java.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class GameStatsPanel extends JPanel {

    private final JLabel gameTimeLbl;
    private final JLabel movesLbl;
    private final JLabel minesLeftLbl;
    private final JLabel gameStateLbl;
    private int gameTime;
    private int moves;
    private int minesLeft;
    private GameState gameState;
    private Timer gameTimer;

    public GameStatsPanel(int mines) {
        super();
        gameTimeLbl = new JLabel();
        movesLbl = new JLabel();
        minesLeftLbl = new JLabel();
        gameStateLbl = new JLabel();
        reset(mines);
        setupLayout();
        setVisible(true);
    }

    private void setupLayout() {
        TitledBorder statsTitle = new TitledBorder("Stats");
        statsTitle.setTitleJustification(TitledBorder.CENTER);
        setBorder(new TitledBorder(statsTitle));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JPanel left = new JPanel();
        left.setBorder(new EmptyBorder(0, 0, 0, 25));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        left.add(gameTimeLbl);
        left.add(movesLbl);
        right.add(minesLeftLbl);
        right.add(gameStateLbl);
        panel.add(left);
        panel.add(right);
        add(panel);
    }

    public int getMinesLeft() {
        return minesLeft;
    }

    public void setMinesLeft(int minesLeft) {
        this.minesLeft = minesLeft;
        updateMinesLeftLbl();
    }

    public void reset(int mines) {
        gameTime = 0;
        moves = 0;
        minesLeft = mines;
        gameState = GameState.RUNNING;
        stopTimer();
        addListeners();
        updateMovesLbl();
        updateMinesLeftLbl();
        updateGameStateLbl();
    }

    public void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private void addListeners() {
        gameTimer = new Timer(1000, e -> {
            if (gameTime < 100000) {
                gameTimeLbl.setText("Time: " + gameTime);
            } else {
                ((Timer) (e.getSource())).stop();
            }
            gameTime += 1;
        });
        gameTimer.setInitialDelay(0);
        gameTimer.start();
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
        updateMovesLbl();
    }

    private void updateMovesLbl() {
        movesLbl.setText("Moves: " + moves);
    }

    private void updateMinesLeftLbl() {
        minesLeftLbl.setText("Mines left: " + minesLeft);
    }

    private void updateGameStateLbl() {
        String gameStateStr = gameState.toString().toLowerCase();
        gameStateStr = Character.toUpperCase(gameStateStr.charAt(0)) + gameStateStr.substring(1);
        gameStateLbl.setText("Game state: " + gameStateStr);
    }
}
