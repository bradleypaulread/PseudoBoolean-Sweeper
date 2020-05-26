package main.java;

import java.util.Objects;

/**
 * A class that stores a cell's attributes. (E.g. its number, if it has been
 * flagged, if its a mine, its probability etc.).
 *
 * @author Bradley Read
 * @version 1.0
 * @since 2019-01-28
 */
public class Cell {
    // ID of cell (cell coordinates)
    private int x, y;

    // Surrounding mines
    private int number;

    private boolean open; // Cell was probed by user
    private boolean flagged;

    /**
     * Constructor for main.java.Cell class.
     *
     * @param x cell's x-axis position
     * @param y cell's y-axis position
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        open = false;
        flagged = false;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    public boolean isOpen() {
        return open;
    }

    /**
     * @param open the open to set
     */
    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int i) {
        number = i;
    }

    /**
     * @return if number is -1 the cell is a mine
     */
    public boolean isMine() {
        return (number == -1);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x == cell.x &&
                y == cell.y &&
                number == cell.number &&
                open == cell.open;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, number, open);
    }
}