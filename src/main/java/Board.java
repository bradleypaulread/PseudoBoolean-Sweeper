package main.java;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int width;
    private final int height;
    private final int mines;
    private final Cell[][] cells;
    private final MineField field;

    public Board(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
        this.field = new MineField(height, width, mines);
        this.cells = createCells();
    }

    public MineField getField() {
        return this.field;
    }

    public Cell getCell(int x, int y) {
        return this.cells[x][y];
    }

    /**
     * Returns a list of of the neighbouring cells around the specified cell.
     *
     * @param x X-axis coordinate of cell.
     * @param y Y-axis coordinate of cell.
     * @return a list of neighbouring cells.
     */
    public List<Cell> getNeighbours(int x, int y) {
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

    public Cell[][] getCells() {
        return this.cells;
    }

    public Cell unveil(int x, int y) {
        int num = field.uncover(x, y);
        Cell cell = getCell(x, y);
        cell.setNumber(num);
        cell.setState(CellState.OPEN);
        return cell;
    }

    private Cell[][] createCells() {
        Cell[][] cells = new Cell[width][height];
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
        return cells;
    }

    public void openAllCells(final String PASSWORD) throws NoSuchAlgorithmException {
        this.field.open(PASSWORD);
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                Cell cell = this.cells[i][j];
                if (cell.getState() == CellState.CLOSED) {
                    int num = this.field.uncover(i, j);
                    cell.setNumber(num);
                    cell.setState(CellState.OPEN);
                }
            }
        }
    }

}
