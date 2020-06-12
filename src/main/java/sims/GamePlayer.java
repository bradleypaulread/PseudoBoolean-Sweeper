package main.java.sims;

import main.java.Cell;
import main.java.CellState;
import main.java.GameState;
import main.java.MineSweeper;
import main.java.solvers.Solver;

import java.util.Map;
import java.util.Random;

public class GamePlayer {

    private final MineSweeper game;
    private final Solver solver;
    private double startTime;
    private double endTime;

    public GamePlayer(MineSweeper game, Solver solver) {
        this.game = game;
        this.solver = solver;
    }

    public void play() {
        Random rand = new Random();
//        do {
//            int x = rand.nextInt(game.getWidth());
//            int y = rand.nextInt(game.getHeight());
//            startTime = System.nanoTime();
//            game.openCell(x, y);
//        } while (game.getState() != GameState.RUNNING);
        startTime = System.nanoTime();
        while (game.getState() == GameState.RUNNING) {
            Map<Cell, Boolean> known = solver.getKnownCells();
            boolean change = false;
            for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
                Cell cell = pair.getKey();
                if (pair.getValue()) {
                    if (cell.getState() == CellState.CLOSED) {
                        cell.setState(CellState.FLAGGED);
                        change = true;
                    }
                } else {
                    if (cell.getState() == CellState.CLOSED) {
                        game.openCell(cell.getX(), cell.getY());
                        change = true;
                    }
                }
            }
            if (!change) {
                int x = rand.nextInt(game.getWidth());
                int y = rand.nextInt(game.getHeight());
                game.openCell(x, y);
            }
        }
        endTime = System.nanoTime();
    }

    public double getEndTime() {
        return endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getElapsedTime() {
        return endTime - startTime;
    }
}
