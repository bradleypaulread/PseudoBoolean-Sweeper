package main.java;

import main.java.gui.BoardPanel;
import main.java.solvers.ProbabilitySolver;
import main.java.solvers.Solver;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolverSwingWorker extends SwingWorker<Boolean, Boolean> {

    private final List<JComponent> disableComponents;
    private final List<Solver> solvers;
    private final MineSweeper game;
    private final BoardPanel board;
    private final boolean loop;
    private volatile boolean running;

    private SolverSwingWorker(MineSweeper game, BoardPanel board, List<JComponent> disableComponents, List<Solver> solvers, boolean loop) {
        this.running = true;
        this.disableComponents = disableComponents;
        this.game = game;
        this.board = board;
        this.loop = loop;
        this.solvers = solvers;
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

    private boolean makeChanges(Solver solver) {
        Map<Cell, Boolean> knownCells = solver.getKnownCells();
        boolean somethingChanged = false;
        for (Map.Entry<Cell, Boolean> pair : knownCells.entrySet()) {
            Cell cell = pair.getKey();
            boolean isMine = pair.getValue();
            if (!running) {
                break;
            }
            if (isMine) {
                if (cell.getState() == CellState.CLOSED) {
                    cell.setState(CellState.FLAGGED);
                    somethingChanged = true;
                }
            } else {
                // Goes by the assumption that user's flagged cells are correct
                if (cell.getState() == CellState.CLOSED) {
                    game.openCell(cell.getX(), cell.getY());
                    somethingChanged = true;
                }
            }
        }
        return somethingChanged;
    }

    @Override
    protected Boolean doInBackground() {
        disableComponents();
        for (int i = 0; i < solvers.size() && this.running && game.getState() == GameState.RUNNING; i++) {
            Solver solver = solvers.get(i);
            if (solver instanceof ProbabilitySolver) {
                Cell bestCell = ((ProbabilitySolver) solver).getBestCell();
                if (!running) {
                    break;
                }
                game.openCell(bestCell.getX(), bestCell.getY());
                if (!loop) {
                    break;
                }
                i = -1;
            } else {
                boolean somethingChanged = makeChanges(solver);
                if (somethingChanged) {
                    if (!loop) {
                        break;
                    }
                    i = -1;
                }
            }
        }
        enableComponents();
        board.refreshOpenCellBtns();
        board.refreshAllCellBtns();
        return this.running;
    }

    public static class Builder {

        private final MineSweeper game;
        private final boolean running;
        private List<JComponent> disableComponents;
        private List<Solver> solvers;
        private BoardPanel board;
        private boolean loop;

        public Builder(MineSweeper game) {
            this.game = game;
            this.running = true;
        }

        public Builder withSolvers(List<Class> solvers) {
            Cell[][] cells = game.getCells();
            int width = game.getWidth();
            int height = game.getHeight();
            int mines = game.getMines();
            List<Solver> solverList = new ArrayList<>();
            for (Class solver : solvers) {
                try {
                    Constructor constructor = solver.getDeclaredConstructor(cells.getClass(), int.class, int.class, int.class);
                    solverList.add((Solver) constructor.newInstance(cells, width, height, mines));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.solvers = solverList;
            return this;
        }

        public Builder disableComponents(List<JComponent> components) {
            this.disableComponents = components;
            return this;
        }

        public Builder withBoardPanel(BoardPanel board) {
            this.board = board;
            return this;
        }

        public Builder setLoop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public SolverSwingWorker build() {
            return new SolverSwingWorker(game, board, disableComponents, solvers, loop);
        }

    }
}
