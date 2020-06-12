package main.java;

public class Cell {
    // ID of cell (cell coordinates)
    private final int x;
    private final int y;

    // Surrounding mines
    private int number;

    private CellState state;
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = CellState.CLOSED;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

        if (x != cell.x) return false;
        if (y != cell.y) return false;
        return state == cell.state;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}