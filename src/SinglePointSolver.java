import java.util.ArrayList;
import java.util.List;

/**
 * A class that uses the single point algorithm, see
 * {@link #isSinglePointSafe(Cell) isSingePointSafe} and
 * {@link #isSinglePointMine(Cell) isSinglePointMine} methods.
 * 
 * @author Bradley Read
 * @version 1.0
 * @since 2019-03-11
 */
public class SinglePointSolver extends Solver {

    /**
     * Constructor for SinglePointSolver.
     * 
     * @param game the game that the solver is going to perform moves on.
     */
    public SinglePointSolver(Minesweeper game) {
        super(game);
    }

    public boolean hint() {
        cells = game.getCells();
        for (int i = 0; i < cells.length; ++i) {
            for (int j = 0; j < cells[i].length; ++j) {
                if (game.is_good(i, j)) {
                    Cell current = cells[i][j];
                    // Only check open cells
                    if (current.isOpen() && !current.isFlagged()) {
                        if (isSinglePointSafe(current)) {
                            for (Cell c : getSinglePointSafe(current)) {
                                if (c.isBlank() && !c.isHint()) {
                                    c.setSafeHint(true);
                                    game.getHintCells().add(c);
                                    game.refresh();
                                    return true;
                                }
                            }
                        }
                        if (isSinglePointMine(current)) {
                            for (Cell c : getSinglePointMine(current)) {
                                if (c.isBlank() && !c.isHint()) {
                                    c.setMineHint(true);
                                    game.getHintCells().add(c);
                                    game.refresh();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean assist() {
        cells = game.getCells();
        for (int i = 0; i < cells.length && running.get(); i++) {
            for (int j = 0; j < cells[i].length && running.get(); j++) {
                if (game.is_good(i, j)) {
                    Cell current = cells[i][j];
                    if (current.isOpen()) {
                        if (isSinglePointSafe(current)) {
                            for (Cell c : getSinglePointSafe(current)) {
                                if (c.isBlank()) {
                                    if (quiet) {
                                        game.quietProbe(c.getX(), c.getY());
                                    } else {
                                        game.probe(c.getX(), c.getY());
                                        game.refresh();
                                        String detail = "Probing cell " + c + " as it matched Single Point";
                                        game.setDetail(detail);
                                    }
                                    return true;
                                }
                            }
                        }
                        if (isSinglePointMine(current)) {
                            for (Cell c : getSinglePointMine(current)) {
                                if (c.isBlank()) {
                                    c.setFlagged(true);
                                    game.decrementMines();
                                    if (!quiet) {
                                        game.refresh();
                                        String detail = "Flagging cell " + c + " as a mine as it matched Single Point";
                                        game.setDetail(detail);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!quiet) {
            String detail = "Could not find a move.";
            game.setDetail(detail);
        }
        return false;
    }

    public void solve() {
        while (assist())
            ;
    }

    /**
     * Checks if the cell is safe using the single point method. If the cell's
     * number is equal to the number of flagged neighbours then the cell is classed
     * as safe.
     * 
     * @param cell the cell to check using single point.
     * 
     * @return if the cell is safe. True if it matches single point and is safe,
     *         false otherwise.
     */
    private boolean isSinglePointSafe(Cell cell) {
        int flagsNo = calcFlaggedNeighbours(cell.getX(), cell.getY());
        return cell.getNumber() == flagsNo;
    }

    /**
     * Checks if the cell is a mine using the single point method. If the cell's
     * number is equal to the number of closed neighbours then the cell a mine.
     * 
     * @param cell the cell to check using single point.
     * 
     * @return if the cell is a mine. True if it matches single point and is a mine,
     *         false otherwise.
     */
    private boolean isSinglePointMine(Cell cell) {
        int closedNo = calcClosedNeighbours(cell.getX(), cell.getY());
        return cell.getNumber() == closedNo;
    }

    /**
     * Fetch a list of all the safe cells surround a cell. Should only be called
     * after {@link #isSinglePointSafe(Cell) isSinglePointSafe} is checked.
     * 
     * @param cell the cell to fetch the safe surround cells.
     * 
     * @return the list of all safe cells surrounding the passed cell.
     */
    private List<Cell> getSinglePointSafe(Cell cell) {
        List<Cell> safeCells = new ArrayList<>();
        // No. of flagged neighbours
        int flagsNo = calcFlaggedNeighbours(cell.getX(), cell.getY());
        if (cell.getNumber() == flagsNo) {
            for (Cell c : getNeighbours(cell)) {
                if (c.isClosed()) {
                    safeCells.add(c);
                }
            }
        }
        return safeCells;
    }

    /**
     * Fetch a list of all the mines surrounding a cell. Should only be called after
     * {@link #isSinglePointMine(Cell) isSinglePointMine} is checked.
     * 
     * @param cell the cell to fetch the surrounding mines.
     * 
     * @return the list of all surrounding mines of the passed cell.
     */
    private List<Cell> getSinglePointMine(Cell cell) {
        List<Cell> mineCells = new ArrayList<>();
        // No. of closed neighbours
        int closedNo = calcClosedNeighbours(cell.getX(), cell.getY());
        if (cell.getNumber() == closedNo) {
            for (Cell c : getNeighbours(cell)) {
                if (c.isClosed()) {
                    mineCells.add(c);
                }
            }
        }
        return mineCells;
    }
}