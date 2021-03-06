package main.java.solvers;

import main.java.Cell;
import main.java.CellState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSolver implements Solver {

    protected final Cell[][] cells;
    protected final int width;
    protected final int height;
    protected final int mines;

    public AbstractSolver(Cell[][] cells, int width, int height, int mines) {
        this.cells = cells;
        this.width = width;
        this.height = height;
        this.mines = mines;
    }

    protected Stream<Cell> cellMatrixToStream() {
        return Arrays.stream(cells)
                .flatMap(Arrays::stream);
    }

    /**
     * When passed a cell, create a unique identifier (a single integer) for that
     * cell. To be used for creating literals.
     *
     * @param c cell to encode.
     * @return a unique integer identifier for given cell.
     */
    protected int encodeCellId(final Cell c) {
        return (c.getY() * width + c.getX()) + 1;
    }

    /**
     * When passed an identity, decode and return the cell it is referring to.
     *
     * @param id Unique encoded identity id literal.
     * @return the cell that the id refers to. Null if it is impossible for the
     * passed id to be a cell.
     */
    public Optional<Cell> decodeCellId(final int id) {
        final int width = cells.length;
        final int height = cells[0].length;
        int posId = id < 0 ? id * -1 : id;
        if (posId > ((height - 1) * width + (width - 1)) + 1) {
            return Optional.empty();
        }
        int x = (posId - 1) % width;
        int y = ((posId - 1) - x) / width;
        return Optional.of(cells[x][y]);
    }

    public List<Cell> getNeighbours(final int x, final int y) {
        final int width = cells.length;
        final int height = cells[0].length;
        List<Cell> neighbours = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && i < width && j >= 0 && j < height && !(i == x && j == y)) {
                    neighbours.add(cells[i][j]);
                }
            }
        }
        return neighbours;
    }

    /**
     * Encodes a literal so that it does not collide with any cell literals.
     *
     * @param lit the ith literal wanting to be encoded.
     * @return an encoded literal.
     */
    protected int encodeLit(final int lit) {
        return (height * width) + width + lit;
    }

    /**
     * Return a list of all the games land cells. Note: a land cell is a cell that
     * has been probed (is open).
     *
     * @return a list of cells that are classed as land cells.
     */
    public List<Cell> getLandCells() {
        return cellMatrixToStream()
                .filter(cell -> cell.getState() == CellState.OPEN)
                .collect(Collectors.toList());
    }

    /**
     * Return a list of all the games sea cells. Note: a sea cell is a cell that has
     * not been probed and does not touch an open cell.
     *
     * @return a list of cells that are classed as sea cells.
     */
    public List<Cell> getSeaCells() {
        return cellMatrixToStream()
                .filter(cell -> cell.getState() != CellState.OPEN)
                .filter(cell -> ((int) getNeighbours(cell.getX(), cell.getY()).stream()
                        .filter(c -> c.getState() == CellState.OPEN)
                        .limit(1)
                        .count()) == 0)
                .collect(Collectors.toList());
    }

    /**
     * Return a list of all the games closed shore cells. Note: a "closed" shore
     * cell is a cell that has not been probed (is closed) and touches both a land
     * cell and a sea cell.
     *
     * @return a list of cells that are classed as closed shore cells.
     */
    public List<Cell> getClosedShoreCells() {
        return cellMatrixToStream()
                .filter(cell -> cell.getState() != CellState.OPEN)
                .filter(cell -> {
                    List<Cell> neighbours = getNeighbours(cell.getX(), cell.getY());
                    return ((int) neighbours.stream()
                            .filter(c -> c.getState() == CellState.OPEN)
                            .limit(1)
                            .count()) != 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * Return a list of all the games open sore cells. Note: a "open" shore cell is
     * a cell that has has been probed (is open) and touches a closed cell.
     *
     * @return a list of cells that are classed as open shore cells.
     */
    public List<Cell> getOpenShoreCells() {
        return cellMatrixToStream()
                .filter(cell -> cell.getState() != CellState.OPEN)
                .filter(cell -> {
                    List<Cell> neighbours = getNeighbours(cell.getX(), cell.getY());
                    return ((int) neighbours.stream()
                            .filter(c -> c.getState() != CellState.OPEN)
                            .limit(1)
                            .count()) != 0;
                })
                .collect(Collectors.toList());
    }

    public int calcFlaggedNeighbours(final int x, final int y) {
        return (int) getNeighbours(x, y).stream()
                .filter(cell -> cell.getState() == CellState.FLAGGED)
                .count();
    }

    public int calcClosedNeighbours(final int x, final int y) {
        return (int) getNeighbours(x, y).stream()
                .filter(cell -> cell.getState() != CellState.OPEN)
                .count();
    }
}