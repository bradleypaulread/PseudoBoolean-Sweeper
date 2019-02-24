import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BoardSolver1 {

	private IPBSolver pbSolver;

	private boolean quiet;

	private Minesweeper game;
	private Cell[][] cells;

	public BoardSolver1(Minesweeper game) {
		pbSolver = SolverFactory.newDefault();
		this.game = game;
		cells = game.getCells();
		quiet = false;
	}

	/**
	 * Search the board for a cell that is not a mine. When such a cell is found set
	 * its hint value to true. Results in its colour turning pink.
	 * 
	 * @return if a non-mine cell was found. Will only return false if there are no
	 *         cells left on the board that are considered "safe".
	 */
	public void patternMatchHint() {
		cells = game.getCells();
		// Find cells that have N surrounding mines but N flagged neighbours
		for (int i = 0; i < cells.length; ++i) {
			for (int j = 0; j < cells[i].length; ++j) {
				if (game.is_good(i, j)) {
					Cell current = cells[i][j];
					if (game.getHintCells().contains(current)) {
						continue;
					}
					// Only apply logic to open cells with n surrounding mines
					// and n surrounding flags
					int flagsNo = calcFlaggedNeighbours(i, j);
					if (current.isOpen()) {
						if (current.getNumber() == flagsNo) {
							List<Cell> neighbours = getNeighbours(current); // List of
							// neighbours
							for (Cell c : neighbours) {
								// If the cell has not been affected by the user (is
								// blank of behaviour)
								if (c.isBlank() && !c.isHint()) {
									c.setSafeHint();
									game.getHintCells().add(c);
									game.refresh();
									return;
								}
							}
						}
						if (current.getNumber() == calcClosedNeighbours(i, j)) {
							List<Cell> neighbours = getNeighbours(current); // List of
							for (Cell c : neighbours) {
								// If the cell has not been affected by the user (is
								// blank of behaviour)
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

	public void SATHint() {
		// cells = game.getCells();
		// Map<Cell, Boolean> known = solveMines();
		// for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
		// 	Cell current = pair.getKey();
		// 	boolean mine = pair.getValue();
		// 	if (current.isHint() || !current.isBlank()) {
		// 		continue;
		// 	}
		// 	if (mine) {
		// 		current.setMineHint();
		// 		game.getHintCells().add(current);
		// 		game.refresh();
		// 		return;
		// 	} else {
		// 		if (current.isBlank()) {
		// 			current.setSafeHint();
		// 			game.getHintCells().add(current);
		// 			game.refresh();
		// 			return;
		// 		}
		// 	}
		// }

	}

	/**
	 * Search the board for a cell that is not a mine and cells that are guaranteed
	 * to be a mine. When "safe" cell found, selected it and return true; When a
	 * guaranteed mine found, set its flag value to true. Results in its colour
	 * turning yellow.
	 * 
	 * @return if either pattern was found. Will only return false if there are no
	 *         cells left on the board that are considered "safe" and no cells that
	 *         are guaranteed mines.
	 */
	public boolean patternMatch(AtomicBoolean running) {
		cells = game.getCells();
		for (int i = 0; i < cells.length && running.get(); i++) {
			for (int j = 0; j < cells[i].length && running.get(); j++) {
				if (game.is_good(i, j)) {
					Cell current = cells[i][j];
					if (current.isOpen() && current.getNumber() != 0
							&& current.getNumber() == calcFlaggedNeighbours(i, j)) {
						List<Cell> n = getNeighbours(current); // List of
																// neighbours
						for (int k = 0; k < n.size(); k++) {
							// If the cell has not been affected by the user (is
							// blank of behaviour)
							Cell cell = n.get(k);
							if (cell.isClosed() && !cell.isFlagged()) {
								if (quiet) {
									game.quietSelect(cell.getX(), cell.getY());
								} else {
									game.select(cell.getX(), cell.getY());
								}
								return true;
							}
						}
					} else if (current.getNumber() != 0 && current.getNumber() == calcClosedNeighbours(i, j)
							&& current.getNumber() != calcFlaggedNeighbours(i, j)) {
						List<Cell> n = getNeighbours(current); // List of neighbouring cells
						for (Cell c : n) {
							if (c.isClosed() && !c.isFlagged()) {
								c.flag();
								game.decrementMines();
							}
						}
						if (!quiet) {
							game.refresh();
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean SATSolve(AtomicBoolean running) {
		boolean change = false;
		Map<Cell, Boolean> results = solveMines(running);
		if (results == null || !running.get()) {
			return false;
		}
		// Iterate over map
		for (Map.Entry<Cell, Boolean> pair : results.entrySet()) {
			Cell current = pair.getKey();
			boolean mine = pair.getValue();
			if (mine) {
				if (current.isClosed() && !current.isFlagged()) {
					current.flag();
					game.decrementMines();
					change = true;
				}
			} else {
				if (current.isBlank()) {
					if (quiet) {
						game.quietSelect(current.getX(), current.getY());
					} else {
						game.select(current.getX(), current.getY());
					}
					change = true;
				}
			}
		}
		if (!quiet) {
			game.refresh();
		}
		return change;
	}

	public boolean jointSolve(AtomicBoolean running) {
		if (!patternMatch(running)) {
			if (!SATSolve(running)) {
				return false;
			}
		}
		return true;
	}

	/** */
	public boolean fullSolve(AtomicBoolean running) {
		if (!patternMatch(running)) {
			if (!SATSolve(running)) {
				// implement strategy here
				return false;
			}
		}
		return true;
	}

	public List<Cell> getAdjacentCells(Cell[][] c) {
		List<Cell> adjacentCells = new ArrayList<>();
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[i].length; j++) {
				Cell current = c[i][j];
				List<Cell> neighbours = getNeighbours(current);
				int noOfNeighbours = neighbours.size();
				neighbours.removeIf(cell -> cell.isOpen());
				int noOfClosedNeighbours = neighbours.size();
				if (current.isClosed() && noOfClosedNeighbours < noOfNeighbours) {
					adjacentCells.add(current);
				}
			}
		}
		return adjacentCells;
	}

	private void genBasicConstraints(IPBSolver solver) throws ContradictionException {

		IVecInt lits = new VecInt();
		IVec<BigInteger> coeffs = new Vec<BigInteger>();

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				lits.push(encodeCellId(current));
				coeffs.push(BigInteger.ONE);
			}
		}
		solver.addExactly(lits, coeffs, BigInteger.valueOf(game.getNoOfMines()));
		lits.clear();
		coeffs.clear();

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];

				if (current.isOpen()) {
					List<Cell> neighbours = getNeighbours(i, j);
					lits.clear();
					coeffs.clear();

					// Every open cell is guaranteed to not be a mine
					lits.push(encodeCellId(current));
					coeffs.push(BigInteger.ONE);
					solver.addExactly(lits, coeffs, BigInteger.ZERO);
					lits.clear();
					coeffs.clear();

					// Normal constraint
					for (Cell c : neighbours) {
						if (c.isClosed()) {
							lits.push(encodeCellId(c));
							coeffs.push(BigInteger.ONE);
						}
					}
					solver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
					lits.clear();
					coeffs.clear();
				}
			}
		}
	}

	public Map<Cell, Boolean> solveMines(AtomicBoolean running) {
		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> adjacentCells = getAdjacentCells(this.cells);
		for (int i = 0; i < adjacentCells.size() && running.get() && !Thread.interrupted(); i++) {

			Cell current = adjacentCells.get(i);
			for (int weight = 0; weight <= 1; weight++) {
				IVecInt lit = new VecInt();
				IVec<BigInteger> coeff = new Vec<BigInteger>();
				BigInteger cellWeight = BigInteger.valueOf(weight);
				try {
					pbSolver = SolverFactory.newDefault();
					genBasicConstraints(pbSolver);

					lit.push(encodeCellId(current));
					coeff.push(BigInteger.ONE);
					pbSolver.addExactly(lit, coeff, cellWeight);

					OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

					if (!optimiser.isSatisfiable()) {
						boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
						results.put(current, isMine);
						pbSolver.reset();
						optimiser.reset();
						break;
					}
					pbSolver.reset();
					optimiser.reset();
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is already known to be
					// safe/a mine.
					boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
					results.put(current, isMine);
				} catch (TimeoutException te) {
				}
			}
		}
		if (Thread.interrupted() || !running.get()) {
			return null;
		}
		return results;
	}

	public void SATStratergy() {
		// cells = game.getCells();

		// int knownMines = 0;
		// for (boolean isMine : solveMines().values()) {
		// 	if (isMine) {
		// 		knownMines++;
		// 	}
		// }

		// int noOfMines = game.getNoOfMines();
		// int noOfLits = Integer.toBinaryString(noOfMines).length();
		// IVecInt lits = new VecInt();
		// IVec<BigInteger> coeffs = new Vec<BigInteger>();

		// for (int i = 0; i < noOfLits; i++) {
		// 	lits.push(i);
		// 	coeffs.push(BigInteger.valueOf((long) Math.pow(2, i)));
		// }

		// try {
		// 	pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(noOfMines - knownMines));
		// } catch (ContradictionException e) {
		// 	e.printStackTrace();
		// }
	}

	/**
	 * When passed a cell and a board, create a unique identifier (a single integer)
	 * for that cell.
	 * 
	 * @param c     Cell to encode.
	 * @param board Board the cell is present in, used to get the width of the
	 *              board.
	 * @return Unique integer identifier for given cell.
	 */
	private int encodeCellId(Cell c) {
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
	private Cell decodeCellId(int id) {
		int posId = id < 0 ? id * -1 : id;
		int x = (posId - 1) % cells.length;
		int y = ((posId - 1) - x) / cells.length;
		return cells[x][y];
	}

	private List<Cell> getNeighbours(Cell c) {
		return getNeighbours(c.getX(), c.getY());
	}

	private List<Cell> getNeighbours(int x, int y) {
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

	/**
	 * Count the amount of flagged cells are around a cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 * @return Number of flagged neighbouring cells.
	 */
	private int calcFlaggedNeighbours(int x, int y) {
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
	private int calcClosedNeighbours(int x, int y) {
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

	// To Remove
	public Cell selectRandomCell() {
		cells = game.getCells();
		List<Cell> closedCells = new ArrayList<>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isClosed()) {
					closedCells.add(current);
				}
			}
		}
		return closedCells.get(new Random().nextInt(closedCells.size()));
	}

	public void setQuiet() {
		quiet = true;
	}
}