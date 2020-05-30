package main.java;

import main.java.gui.BoardGUI;
import main.java.solvers.Solver;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class SolverSwingWorker extends SwingWorker<Boolean, Boolean> {

    private final List<JComponent> disableComponents;
    private final Solver solver;
    private final MineSweeper game;
    private volatile boolean running;

    public SolverSwingWorker(List<JComponent> disableComponents, Solver solver,
                             MineSweeper game) {
        this.running = true;
        this.disableComponents = disableComponents;
        this.solver = solver;
        this.game = game;
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
    protected Boolean doInBackground() throws Exception {
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
                    if (!cell.isFlagged()) {
                        somethingChanged = true;
                        cell.setFlagged(true);
                    }
                } else {
                    if (!cell.isOpen()) {
                        somethingChanged = true;
                        game.openCell(cell.getX(), cell.getY());
                    }
                }
            }
            BoardGUI.refreshCellGUIs();
        }
        enableComponents();
        return this.running;
    }
}
