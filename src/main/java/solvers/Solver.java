package main.java.solvers;

import main.java.Cell;

import java.util.Map;

public interface Solver {
    /**
     * Return a mapping of cells to if they are a mine.
     * <p>
     * Only known cells are return.
     *
     * @return Mapping of cell to Boolean, true means mine and false means safe.
     */
    Map<Cell, Boolean> getKnownCells();
}
