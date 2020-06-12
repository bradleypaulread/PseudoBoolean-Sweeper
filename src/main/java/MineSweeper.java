package main.java;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MineSweeper {

    private static final String PASSWORD = "hello";

    private final int width;
    private final int height;
    private final int mines;
    private final Board board;
    private int openCells;
    private GameState gameState;

    public MineSweeper(Difficulty diff) {
        this(diff.width, diff.height, diff.mines);
    }

    public MineSweeper(int width, int height, int mines) {
        this.gameState = GameState.RUNNING;
        this.width = width;
        this.height = height;
        this.mines = mines;
        this.openCells = 0;
        this.board = new Board(width, height, mines);
    }

    public Cell[][] getCells() {
        return this.board.getCells();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMines() {
        return mines;
    }

    public int getOpenCells() {
        return openCells;
    }

    public void openCell(int x, int y) {
        if (this.gameState == GameState.RUNNING) {
            this.openCells += 1;
            Cell cell = board.unveil(x, y);
            switch (cell.getNumber()) {
                case -1 -> {
                    this.gameState = GameState.LOST;
                    endGame();
                }
                case 0 -> openNeighbours(x, y);
                default -> {
                    if (this.openCells == (this.width * this.height) - this.mines) {
                        this.gameState = GameState.WON;
                        endGame();
                    }
                }
            }
        } else {
            board.unveil(x, y);
        }
    }

    public GameState getState() {
        return this.gameState;
    }

    private void openNeighbours(int x, int y) {
        List<Cell> neighbours = board.getNeighbours(x, y);
        for (Cell c : neighbours) {
            if (c.getState() == CellState.CLOSED) {
                openCell(c.getX(), c.getY());
            }
        }
    }

    public Cell getCell(int x, int y) {
        return this.board.getCell(x, y);
    }

    public void endGame() {
        try {
            this.board.openAllCells(PASSWORD);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Board getBoard() {
        return this.board;
    }
}
