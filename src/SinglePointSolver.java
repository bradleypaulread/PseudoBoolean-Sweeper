import java.util.ArrayList;
import java.util.List;

public class SinglePointSolver extends BoardSolver {

    public SinglePointSolver(Minesweeper game) {
        super(game);
    }

    public void hint() {
        cells = game.getCells();
        for (int i = 0; i < cells.length; ++i) {
            for (int j = 0; j < cells[i].length; ++j) {
                if (game.is_good(i, j)) {
                    Cell current = cells[i][j];
                    // Only check open cells
                    if (current.isOpen() && !current.isFlagged()) {
                        List<Cell> cells;
                        if (!(cells = singlePointSafe(current)).isEmpty()) {
                            for (Cell c : cells) {
                                if (c.isBlank() && !c.isHint()) {
                                    c.setSafeHint();
                                    game.getHintCells().add(c);
                                    game.refresh();
                                    return;
                                }
                            }
                        }
                        if (!(cells = singlePointMine(current)).isEmpty()) {
                            for (Cell c : cells) {
                                if (c.isBlank() && !c.isHint()) {
                                    c.setMineHint();
                                    game.getHintCells().add(c);
                                    game.refresh();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean assist() {
        cells = game.getCells();
        for (int i = 0; i < cells.length && running.get(); i++) {
            for (int j = 0; j < cells[i].length && running.get(); j++) {
                if (game.is_good(i, j)) {
                    Cell current = cells[i][j];
                    if (current.isOpen()) {
                        List<Cell> cells;
                        if (!(cells = singlePointSafe(current)).isEmpty()) {
                            for (Cell c : cells) {
                                if (c.isBlank()) {
                                    if (quiet) {
                                        game.quietSelect(c.getX(), c.getY());
                                    } else {
                                        game.select(c.getX(), c.getY());
                                        game.refresh();
                                    }
                                    return true;
                                }
                            }
                        }
                        if (!(cells = singlePointMine(current)).isEmpty()) {
                            for (Cell c : cells) {
                                if (c.isBlank()) {
                                    c.flag();
                                    game.decrementMines();
                                    if (!quiet) {
                                        game.refresh();
                                    }
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

    public void solve() {
        while (assist());
    }

    private List<Cell> singlePointSafe(Cell cell) {
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

    private List<Cell> singlePointMine(Cell cell) {
        List<Cell> mineCells = new ArrayList<>();
        // No. of flagged neighbours
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