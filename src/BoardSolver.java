import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sat4j.specs.IVecInt;

public abstract class BoardSolver {

	protected boolean quiet;

	protected Minesweeper game;
	protected Cell[][] cells;
	protected AtomicBoolean running;

	public BoardSolver(Minesweeper game) {
		running = new AtomicBoolean(true);
		quiet = false;
		this.game = game;
		cells = game.getCells();
	}

	public BoardSolver(Minesweeper game, AtomicBoolean running) {
		this.running = running;
		quiet = false;
		this.game = game;
		cells = game.getCells();
	}

	private void printConstraint(IVecInt lits, IVecInt coeffs, String type, int degree) {
		String result = "";
		for (int i = 0; i < lits.size(); i++) {
			result += coeffs.get(i) + " x" + lits.get(i) + " + ";
		}
		// if (result.length() == 0) {
		// return;
		// }
		// 2 x2 +3 x4 +2 x1 +3 x5 = 5;
		if (result.length() == 0) {
			return;
		}
		result = result.substring(0, result.length() - 2);
		result += type + " " + degree;
		System.out.println(result);
	}

	public abstract boolean hint();

	public abstract boolean assist();

	public abstract void solve();
	
	// To Remove
	public void temp() {
		selectRandomCell();
	}

	public void printLits(int[] model) {
		String str = "";
		for (int i = 0; i < model.length; i++) {
			str += "" + model[i] + ", ";
		}
		System.out.println(str);
		if (!str.equals("")) {
			System.out.println();
		}
	}

	/**
	 * When passed a cell and a board, create a unique identifier (a single integer)
	 * for that cell. To be used for creating litrals
	 * 
	 * @param c     Cell to encode.
	 * @param board Board the cell is present in, used to get the width of the
	 *              board.
	 * @return Unique integer identifier for given cell.
	 */
	protected int encodeCellId(Cell c) {
		return (c.getY() * cells.length + c.getX()) + 1;
	}

	/**
	 * When passed an identity, decode and return the cell it is referring to.
	 * 
	 * @param id    Unique encoded identity id.
	 * @param board Board the cell would be present in, used to get the width of the
	 *              board.
	 * @return Cell that the id refers to.
	 */
	public Cell decodeCellId(int id) {
		int posId = id < 0 ? id * -1 : id;
		if (posId > ((cells[0].length - 1) * cells.length + (cells.length - 1)) + 1) {
			return null;
		}
		int x = (posId - 1) % cells.length;
		int y = ((posId - 1) - x) / cells.length;
		return cells[x][y];
	}

	protected int encodeLit(int i) {
		return ((cells[0].length - 1) * cells.length + (cells.length - 1)) + (i + 2);
	}

	private Cell decodeLit(int lit) {
		int posLit = lit < 0 ? lit * -1 : lit;
		if (posLit > (cells[0].length - 1) * cells.length + (cells.length - 1) + 2) {
			return null;
		}
		int x = (posLit - 1) % cells.length;
		int y = ((posLit - 1) - x) / cells.length;
		return cells[x][y];
	}

	public List<Cell> getNeighbours(Cell c) {
		return getNeighbours(c.getX(), c.getY());
	}

	public List<Cell> getNeighbours(int x, int y) {
		cells = game.getCells();
		List<Cell> neighbours = new ArrayList<Cell>();
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y + 1; ++j) {
				if (i >= 0 && i < cells.length && j >= 0 && j < cells[i].length && !(i == x && j == y)) {
					neighbours.add(cells[i][j]);
				}
			}
		}
		return neighbours;
	}

	public List<Cell> getLandCells() {
		List<Cell> landCells = new ArrayList<>();
		for (Cell col[] : cells) {
			for (Cell c : col) {
				if (c.isOpen()) {
					landCells.add(c);
				}
			}
		}
		return landCells;
	}

	public List<Cell> getSeaCells() {
		List<Cell> sea = new ArrayList<>();
		List<Cell> shoreClosed = getShoreClosedCells();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isClosed() && !shoreClosed.contains(current)) {
					sea.add(current);
				}
			}
		}
		return sea;
	}

	public List<Cell> getShoreClosedCells() {
		List<Cell> shoreClosed = new ArrayList<>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isClosed()) {
					List<Cell> neighbours = getNeighbours(current);
					int noOfNeighbours = neighbours.size();
					neighbours.removeIf(c -> c.isOpen());
					int noOfClosedNeighbours = neighbours.size();
					if (noOfClosedNeighbours < noOfNeighbours) {
						shoreClosed.add(current);
					}
				}
			}
		}
		return shoreClosed;
	}

	public List<Cell> getShoreOpenCells() {
		List<Cell> shoreOpen = new ArrayList<>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isOpen()) {
					List<Cell> neighbours = getNeighbours(current);
					neighbours.removeIf(cell -> cell.isOpen());
					if (!neighbours.isEmpty()) {
						shoreOpen.add(current);
					}
				}
			}
		}
		return shoreOpen;
	}

	/**
	 * Count the amount of flagged cells are around a cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 * @return Number of flagged neighbouring cells.
	 */
	public int calcFlaggedNeighbours(int x, int y) {
		int flagCount = 0;
		// for loop to count how many flagged cells are around a cell
		List<Cell> neighbours = getNeighbours(x, y);
		for (Cell c : neighbours) {
			if (c.isFlagged()) {
				++flagCount;
			}
		}
		return flagCount;
	}

	/**
	 * Count the amount of closed cells are around a cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 * @return Number of closed neighbouring cells.
	 */
	public int calcClosedNeighbours(int x, int y) {
		int closedCount = 0;
		// for loop to count how many closed cells are around a cell
		List<Cell> neighbours = getNeighbours(x, y);
		for (Cell c : neighbours) {
			if (c.isClosed()) {
				++closedCount;
			}
		}
		return closedCount;
	}

	public void setQuiet() {
		quiet = true;
	}

	public void selectRandomCell() {
		cells = game.getCells();
		List<Cell> sea = getSeaCells();
		List<Cell> shoreClosed = getShoreClosedCells();
		sea.addAll(shoreClosed);
		sea.removeIf(c -> !c.isBlank());

		if (!sea.isEmpty()) {
			Cell selectedCell = sea.get(new Random().nextInt(sea.size()));
			game.quietProbe(selectedCell.getX(), selectedCell.getY());
		} else {
			game.setGameOver(true);
		}
	}

	public Cell getRandomCell(List<Cell> cellList) {
		if (!cellList.isEmpty()) {
			Cell selectedCell = cellList.get(new Random().nextInt(cellList.size()));
			return selectedCell;
		}
		return null;
	}
}