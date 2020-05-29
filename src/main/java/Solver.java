package main.java;

import java.util.Map;

public interface Solver {
    /**
     * Highlights a safe/recommended move to the user.
     *
     * @return True if a hint (one that has not already been hinted) is found. False
     * if no hint or futher hints are found.
     */
    // List<Cell> getMineCells();

    /**
     * Probe a safe/recommended cell.
     *
     * @return True if a cell is found and probed. False if no safe cell is found
     * and probed.
     */
    // List<Cell> getSafeCells();

    /**
     * Return a mapping of cells to if they are a mine.
     * <p>
     * Only known cells are return.
     *
     * @return Mapping of cell to Boolean, true means mine and false means safe.
     */
    Map<Cell, Boolean> getKnownCells();
}
