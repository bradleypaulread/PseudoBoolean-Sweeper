import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

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

public class BoardSolver {

	private IPBSolver pbSolver;
	private volatile boolean execute = true;

	private Minesweeper game;
	private Cell[][] cells;

	private List<HashMap<Cell, Integer>> lastRun;

	public BoardSolver(Minesweeper game) {
		pbSolver = SolverFactory.newDefault();
		this.game = game;
		cells = game.getCells();
	}

	public void run() {
		while (execute) {
			try {
				Thread.sleep((long) 15000);
			} catch (InterruptedException e) {
				execute = false;
			}
		}
	}

	public void terminate() {
		execute = false;
	}

	/**
	 * Search the board for a cell that is not a mine. When such a cell is found set
	 * its hint value to true. Results in its colour turning pink.
	 * 
	 * @return if a non-mine cell was found. Will only return false if there are no
	 *         cells left on the board that are considered "safe".
	 */
	public boolean genHint() {
		cells = game.getCells();
		// Find cells that have N surrounding mines but N flagged neighbours
		for (int i = 0; i < cells.length; ++i) {
			for (int j = 0; j < cells[i].length; ++j) {
				if (game.is_good(i, j)) {
					Cell current = cells[i][j];
					// Only apply logic to open cells with n surrounding mines
					// and n surrounding flags
					int flagsNo = calcFlaggedNeighbours(i, j);
					if (current.isOpen() && current.getNumber() == flagsNo) {
						List<Cell> n = getNeighbours(current); // List of
																// neighbours
						for (int k = 0; k < n.size(); ++k) {
							// If the cell has not been affected by the user (is
							// blank of behaviour)
							if (n.get(k).isBlank()) {
								n.get(k).setHint();
								game.getHintCells().add(n.get(k));
								game.refresh();
								return true;
							}
						}
					}
				}
			}
		}
		return false;
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
	public boolean assist() {
		cells = game.getCells();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (game.is_good(i, j)) {
					Cell current = cells[i][j];
					if (current.isOpen() && current.getNumber() != 0
							&& current.getNumber() == calcFlaggedNeighbours(i, j)) {
						List<Cell> n = getNeighbours(current); // List of
																// neighbours
						for (int k = 0; k < n.size(); k++) {
							// If the cell has not been affected by the user (is
							// blank of behaviour)
							if (n.get(k).isClosed() && !n.get(k).isFlagged()) {
								game.select(n.get(k).getX(), n.get(k).getY());
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
						game.refresh();
						return true;
					}
				}
			}
		}
		if (SATSolve()) {
			return true;
		}
		return false;
	}

	public boolean SATSolve() {
		boolean change = false;
		try {
			List<HashMap<Cell, Integer>> allSolutions = solveMines();
			HashMap<Cell, Integer> map = allSolutions.get(0);
			for (Cell cell : map.keySet()) {
				if (map.containsKey(cell)) {
					boolean known = true;
					int sign = map.get(cell);
					for (int i = 1; i < allSolutions.size(); i++) {
						if (allSolutions.get(i).get(cell) != sign) {
							known = false;
							break;
						}
					}
					if (known) {
						if (sign > 0) {
							if (cell.isClosed() && !cell.isFlagged()) {
								cell.flag();
								game.decrementMines();
								change = true;
							}
						} else {
							if (cell.isBlank()) {
								game.select(cell.getX(), cell.getY());
								change = true;
							}
						}
					}
				}
			}
			game.refresh();
		} catch (ContradictionException | TimeoutException e1) {
			e1.printStackTrace();
		}
		return change;
	}

	public List<HashMap<Cell, Integer>> solveMines() throws ContradictionException, TimeoutException {
		cells = game.getCells();
		pbSolver = SolverFactory.newDefault();
		List<HashMap<Cell, Integer>> allSolutions = new ArrayList<HashMap<Cell, Integer>>();
		IVecInt lits = new VecInt();
		IVec<BigInteger> coeffs = new Vec<BigInteger>();

		lastRun = new ArrayList<>();

		int blanksAmt = 0;

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isBlank()) {
					lits.push(encodeCellId(current));
					coeffs.push(BigInteger.ONE);
					++blanksAmt;
				}
			}
		}
		if (blanksAmt <= (cells.length * cells[0].length) / 10) {
			// pbSolver.addAtMost(lits, coeffs, BigInteger.valueOf(game.getMinesLeft()));
		}
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
					pbSolver.addExactly(lits, coeffs, BigInteger.ZERO);

					lits.clear();
					coeffs.clear();
					if (calcClosedNeighbours(current.getX(), current.getY()) == current.getNumber()) {
						for (Cell c : neighbours) {
							if (c.isClosed()) {
								lits.push(encodeCellId(c));
								coeffs.push(BigInteger.ONE);
								pbSolver.addExactly(lits, coeffs, BigInteger.ONE);
								lits.clear();
								coeffs.clear();
							}
						}
					} else {
						for (Cell c : neighbours) {
							lits.push(encodeCellId(c));
							coeffs.push(BigInteger.ONE);
						}
						pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
						lits.clear();
						coeffs.clear();
					}
				}
			}
		}

		OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

		while (optimiser.isSatisfiable()) {
			HashMap<Cell, Integer> knownCells = new HashMap<Cell, Integer>();
			int[] model = pbSolver.model();
			for (int i : model) {
				int sign = i < 0 ? -1 : 1;
				knownCells.put(decodeCellId(i), sign);
			}
			allSolutions.add(knownCells);
			// Find another solution
			for (int i = 0; i < model.length; i++) {
				model[i] = model[i] * -1;
			}
			IVecInt block = new VecInt(model);

			optimiser.addBlockingClause(block);
		}
		System.out.println("NOT SAT!");

		// Save solutions in case there are no 100% known moves left
		if (!allSolutions.isEmpty())
			lastRun = allSolutions;
		return allSolutions;
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

	public Cell calcCellOdds() {
		// No last run data (field has never been able to be SAT solved)
		// just return a random cell
		if (lastRun.isEmpty()) {
			List<Cell> allCells = new ArrayList<>();
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[i].length; j++) {
					allCells.add(cells[i][j]);
				}
			}
			return calcBestStrategicCell(allCells);
		}

		// Cell to return
		Cell cellWithBestProb;

		// Compliation of cells present in every computed solution
		Map<Cell, Double> knownCellOdds = new HashMap<Cell, Double>();

		// List of non adjacent cells (cells that are never present in a solution)
		List<Cell> nonAdjacentCells = new ArrayList<Cell>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				// If the cell has not been touched and is not part of any known possible
				// solution
				if (current.isBlank() && !knownCellOdds.containsKey(current)) {
					nonAdjacentCells.add(current);
				}
			}
		}

		// Max amount of mines featured in a solution, used to compute non adjacent mine
		// probability
		int maxMineSolution = 0;
		// Sum the number of occurances of a cell appearing as a mine
		for (int i = 0; i < lastRun.size(); i++) {
			Map<Cell, Integer> map = lastRun.get(i);
			int noOfMines = 0;
			for (Cell cell : map.keySet()) {
				// If the cell is marked as a mine in the current solution
				if (map.get(cell) != null && map.get(cell) == 1) {
					noOfMines++;
					if (!knownCellOdds.containsKey(cell)) {
						knownCellOdds.put(cell, 1.0);
					} else {
						knownCellOdds.put(cell, knownCellOdds.get(cell) + 1.0);
					}
				}
			}
			maxMineSolution = noOfMines > maxMineSolution ? noOfMines : maxMineSolution;
		}

		// Turn number of occurances in to proabilities
		knownCellOdds.replaceAll((cell, prob) -> (prob / (double) lastRun.size()));
		// Order mapping key (the cells) by value in ascending order
		knownCellOdds = knownCellOdds.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));
		System.out.println(knownCellOdds);

		List<Cell> orderedCells = new ArrayList<Cell>(knownCellOdds.keySet());
		cellWithBestProb = orderedCells.get(0);
		BigDecimal cellBestProb = new BigDecimal(knownCellOdds.get(cellWithBestProb));

		BigDecimal nonAdjacentProb = new BigDecimal(
				(game.getNoOfMines() - maxMineSolution) / (double) nonAdjacentCells.size());

		if (nonAdjacentProb.compareTo(cellBestProb) == -1) {
			// Select a random non adjacent cell
			cellWithBestProb = calcBestStrategicCell(nonAdjacentCells);
		} else if (cellBestProb.compareTo(new BigDecimal(knownCellOdds.get(orderedCells.get(1)))) == 0) {
			List<Cell> equalOddCells = new ArrayList<>();
			for (int i = 0; i < orderedCells.size(); i++) {
				Cell current = orderedCells.get(i);
				BigDecimal currentProb = new BigDecimal(knownCellOdds.get(current));
				if (cellBestProb.compareTo(currentProb) != 0) {
					break;
				}
				equalOddCells.add(current);
			}
			if (cellBestProb.compareTo(nonAdjacentProb) == 0) {
				equalOddCells.addAll(nonAdjacentCells);
			}
			cellWithBestProb = calcBestStrategicCell(equalOddCells);
		}

		// https://puzzling.stackexchange.com/questions/50948/optimal-next-move-in-minesweeper-game
		// If there is a better chance of selecting a safe non adjacent cell

		// To Remove
		if (knownCellOdds.containsKey(cellWithBestProb)) {
			System.out.printf("SELECTING SAFEST CELL " + cellWithBestProb + " WITH MINE PROBABILTY OF %.2f%%%n",
					((knownCellOdds.get(cellWithBestProb)) * 100));
		} else {
			System.out.printf("SELECTING SAFEST CELL " + cellWithBestProb + " WITH MINE PROBABILTY OF %.2f%%%n",
					(nonAdjacentProb.multiply(new BigDecimal(100))));
		}
		return cellWithBestProb;
	}

	private Cell calcBestStrategicCell(List<Cell> equalOddCells) {
		int bestCellIdx = 0;
		BigDecimal d = new BigDecimal(1.1);
		for (int i = 0; i < equalOddCells.size(); i++) {
			List<Cell> neighbours = getNeighbours(equalOddCells.get(i));
			int neughbourCount = neighbours.size();
			neighbours.removeIf(c -> c.isClosed());
			int openCount = neighbours.size();
			BigDecimal openPercent = new BigDecimal(openCount / neughbourCount);
			if (openPercent.compareTo(d) == 1) {
				d = openPercent;
				bestCellIdx = i;
			}
		}
		System.out.println(equalOddCells.get(bestCellIdx));
		return equalOddCells.get(bestCellIdx); // Placeholder
	}
}