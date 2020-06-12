package main.java.solvers;

import main.java.Cell;
import main.java.CellState;

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
                .filter(c -> c.getState() == CellState.OPEN)
                .filter(this::hasSinglePointMinePattern)
                .collect(Collectors.toList());

        // Set so duplicates are ignored
        return getAllNeighbouringClosedCells(haveSurroundingMineCells);
    }

    public List<Cell> getSafeCells() {
        List<Cell> haveSurroundingSafeCells = cellMatrixToStream()
                .filter(c -> c.getState() == CellState.OPEN)
                .filter(this::hasSinglePointSafePattern)
                .collect(Collectors.toList());

        // Set so duplicates are ignored
        return getAllNeighbouringClosedCells(haveSurroundingSafeCells);
    }

    public List<Cell> getAllNeighbouringClosedCells(List<Cell> knownCells) {
        Set<Cell> safeCells = new HashSet<>();
        for (Cell cell : knownCells) {
            safeCells.addAll(
                    getNeighbours(cell.getX(), cell.getY()).stream()
                            .filter(c -> c.getState() == CellState.CLOSED)
                            .collect(Collectors.toList())
            );
        }
        return new ArrayList<>(safeCells);
    }

    private boolean hasSinglePointSafePattern(final Cell cell) {
        int flagCount = calcFlaggedNeighbours(cell.getX(), cell.getY());
        return cell.getNumber() == flagCount;
    }

    private boolean hasSinglePointMinePattern(final Cell cell) {
        int closedCount = calcClosedNeighbours(cell.getX(), cell.getY());
        return cell.getNumber() == closedCount;
    }
}