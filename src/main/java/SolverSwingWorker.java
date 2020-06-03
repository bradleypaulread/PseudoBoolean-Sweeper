package main.java;

import main.java.gui.BoardPanel;
import main.java.solvers.Solver;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class SolverSwingWorker extends SwingWorker<Boolean, Boolean> {

    private final List<JComponent> disableComponents;
    private final Solver solver;
    private final MineSweeper game;
    private final BoardPanel board;
    private volatile boolean running;

    public SolverSwingWorker(List<JComponent> disableComponents, Solver solver,
                             MineSweeper game, BoardPanel board) {
        this.running = true;
        this.disableComponents = disableComponents;
        this.solver = solver;
        this.game = game;
        this.board = board;
    }

    public void stop() {
        this.running = false;
    }

    private void disableComponents() {
        this.disableComponents.forEach(e -> e.setEnabled(false));
    }

    private void enableComponents() {
        this.disableComponents.forEach(e -> e.setEnabled(true));
    }

    @Override
    protected Boolean doInBackground() {
        disableComponents();
        boolean somethingChanged = true;
        while (somethingChanged) {
            if (!this.running) {
                break;
            }
            Map<Cell, Boolean> knownCells = solver.getKnownCells();
            somethingChanged = false;
            for (Map.Entry<Cell, Boolean> pair : knownCells.entrySet()) {
                Cell cell = pair.getKey();
                boolean isMine = pair.getValue();
                if (isMine) {
                    if (cell.getState() == CellState.CLOSED) {
                        somethingChanged = true;
                        cell.setState(CellState.FLAGGED);
                    }
                } else {
                    // Goes by the assumption that user's flagged cells are correct
                    if (cell.getState() == CellState.CLOSED) {
                        somethingChanged = true;
                        game.openCell(cell.getX(), cell.getY());
                    }
                }
            }
        }
        board.refreshAllCellBtns();
        board.refreshOpenCellBtns();
        enableComponents();
        return this.running;
    }
}
