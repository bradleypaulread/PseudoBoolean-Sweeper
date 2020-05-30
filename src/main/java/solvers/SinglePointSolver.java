package main.java.solvers;

import main.java.Cell;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that uses the single point algorithm, see
 *
 * @author Bradley Read
 * @version 1.0
 * @since 2019-03-11
 */
public class SinglePointSolver extends AbstractSolver {

    public SinglePointSolver(Cell[][] cells, int width, int height, int mines) {
        super(cells, width, height, mines);
    }

    public Map<Cell, Boolean> getKnownCells() {
        Map<Cell, Boolean> results = new HashMap<>();

        for (Cell cell : getMineCells()) {
            results.put(cell, true);
        }

        for (Cell cell : getSafeCells()) {
            results.put(cell, false);
        }

        return results;
    }

    public List<Cell> getMineCells() {
        List<Cell> haveSurroundingMineCells = cellMatrixToStream()
                .filter(Cell::isOpen)
                .filter(cell -> hasSinglePointMinePattern(cell))
                .collect(Collectors.toList());

        // Set so duplicates are ignored
        return getAllNeighbouringClosedCells(cells, haveSurroundingMineCells);
    }

    public List<Cell> getSafeCells() {
        List<Cell> haveSurroundingSafeCells = cellMatrixToStream()
                .filter(cell -> hasSinglePointSafePattern(cell))
                .collect(Collectors.toList());

        // Set so duplicates are ignored
        return getAllNeighbouringClosedCells(cells, haveSurroundingSafeCells);
    }

    public List<Cell> getAllNeighbouringClosedCells(Cell[][] cells, List<Cell> knownCells) {
        Set<Cell> safeCells = new HashSet<>();
        for (Cell cell : knownCells) {
            safeCells.addAll(
                    getNeighbours(cell.getX(), cell.getY()).stream()
                            .filter(c -> !c.isOpen())
                            .collect(Collectors.toList())
            );
        }
        return new ArrayList<>(safeCells);
    }

    /**
     * Checks if the cell is safe using the single point method. If the cell's
     * number is equal to the number of flagged neighbours then the cell is classed
     * as safe.
     *
     * @param cell the cell to check using single point.
     * @return if the cell is safe. True if it matches single point and is safe,
     * false otherwise.
     */
    private boolean hasSinglePointSafePattern(final Cell cell) {
        int flagCount = calcFlaggedNeighbours(cell.getX(), cell.getY());
        return cell.getNumber() == flagCount;
    }

    /**
     * Checks if the cell is a mine using the single point method. If the cell's
     * number is equal to the number of closed neighbours then the cell a mine.
     *
     * @param cell the cell to check using single point.
     * @return if the cell is a mine. True if it matches single point and is a mine,
     * false otherwise.
     */
    private boolean hasSinglePointMinePattern(final Cell cell) {
        int closedCount = calcClosedNeighbours(cell.getX(), cell.getY());
        return cell.getNumber() == closedCount;
    }
}