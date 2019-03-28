import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.math.BigIntegerMath;
import com.google.gson.Gson;

import org.apache.commons.math3.fraction.BigFraction;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BoardSolver {

	private boolean quiet;
	private boolean strat;

	private Minesweeper game;
	private Cell[][] cells;
	AtomicBoolean running;

	public BoardSolver(Minesweeper game) {
		running = new AtomicBoolean(true);
		quiet = false;
		strat = false;
		this.game = game;
		cells = game.getCells();
	}

	public BoardSolver(Minesweeper game, AtomicBoolean running) {
		this.running = running;
		quiet = false;
		strat = false;
		this.game = game;
		cells = game.getCells();
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

	/**
	 * Search the board for a cell that is not a mine. When such a cell is found set
	 * its hint value to true. Results in its colour turning pink.
	 * 
	 * @return if a non-mine cell was found. Will only return false if there are no
	 *         cells left on the board that are considered "safe".
	 */
	public void patternMatchHint() {
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

	public void SATHint() {
		cells = game.getCells();
		Map<Cell, Boolean> known = binaryShallowSolve();
		if (known == null || !running.get()) {
			return;
		}
		for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
			Cell current = pair.getKey();
			boolean mine = pair.getValue();
			// Skip cells that are already marked as hints
			if (current.isHint()) {
				continue;
			}
			if (mine) {
				if (current.isBlank()) {
					current.setMineHint();
					game.getHintCells().add(current);
					game.refresh();
					return;
				}
			} else {
				if (current.isBlank()) {
					current.setSafeHint();
					game.getHintCells().add(current);
					game.refresh();
					return;
				}
			}
		}
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
	public boolean patternMatch() {
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
		if (game.getNoOfMoves() == 0 && strat) {

		}
		return false;
	}

	// new sat solve that uses binary lits
	public boolean SATSolve() {
		cells = game.getCells();
		boolean change = false;
		Map<Cell, Boolean> known = binaryShallowSolve();
		if (known == null || !running.get()) {
			return false;
		}
		// Iterate over map
		for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
			Cell current = pair.getKey();
			boolean mine = pair.getValue();
			if (mine) {
				if (current.isClosed() && !current.isFlagged()) {
					current.flag();
					game.decrementMines();
					current.setPresumedMine(true);
					String detail = "Flagging " + current + " as Cell is a Guarenteed Mine";
					game.setDetail(detail);
					change = true;
				}
			} else {
				if (current.isBlank()) {
					if (quiet) {
						game.quietSelect(current.getX(), current.getY());
					} else {
						String detail = "Selecting " + current + " as Cell is Safe";
						game.setDetail(detail);
						game.select(current.getX(), current.getY());
					}
					change = true;
				}
			}
		}
		if (!change && strat) {
			change = true;
			performStrat();
		}
		if (!quiet) {
			game.refresh();
		}
		return change;
	}

	public boolean patternAndSATSolve() {
		if (!patternMatch()) {
			if (!SATSolve()) {
				return false;
			}
		}
		return true;
	}

	public void fullSolve() {
		if (!patternAndSATSolve()) {
			performStrat();
		}
	}

	private void performStrat() {
		// System.out.println("perform strat");
		Map<Cell, BigFraction> probs = calcAllCellsProb();
		List<Cell> cells = getBestProbCell(probs);
		if (cells == null) {
			return;
		}
		Cell bestCell = getBestStratCell(cells);
		if (bestCell == null) {
			return;
		}
		if (quiet) {
			game.quietSelect(bestCell.getX(), bestCell.getY());
		} else {
			String detail = "Strategically Selecting Cell " + bestCell + " with prob. "
					+ probs.get(bestCell).doubleValue();
			game.setDetail(detail);
			game.select(bestCell.getX(), bestCell.getY());
		}
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
		// System.out.println(result);
	}

	private Map<Cell, Boolean> binaryShallowSolve() {
		IPBSolver pbSolver = SolverFactory.newDefault();

		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> closedShore = getShoreClosedCells();
		List<Cell> seaCells = getSeaCells();

		try {
			genBinaryConstraints(pbSolver);
		} catch (ContradictionException e) {
		}

		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (current.isPresumedMine()) {
				continue;
			}
			// Generate the known constraints on the board
			// Find if cell safe (weight=0) or mine (weight=1)
			IVecInt lit = new VecInt();
			IVecInt coeff = new VecInt();
			int cellWeight = 0;
			IConstr constrShoreCell1 = null;
			IConstr constrShoreCell2 = null;
			try {
				// Create literal for current cell
				lit.push(encodeCellId(current));
				coeff.push(1);
				// Safe/Mine
				constrShoreCell1 = pbSolver.addAtMost(lit, coeff, cellWeight);
				constrShoreCell2 = pbSolver.addAtLeast(lit, coeff, cellWeight);
				printConstraint(lit, coeff, "=", cellWeight);

				// Find if cell is a mine
				if (!pbSolver.isSatisfiable()) {
					boolean isMine = true;
					results.put(current, isMine);
					if (constrShoreCell1 != null) {
						pbSolver.removeConstr(constrShoreCell1);
					}
					if (constrShoreCell2 != null) {
						pbSolver.removeConstr(constrShoreCell2);
					}
					// Continue to next shore cell as no need to check if cell is also safe
					continue;
				}

				if (constrShoreCell1 != null) {
					pbSolver.removeConstr(constrShoreCell1);
				}
				if (constrShoreCell2 != null) {
					pbSolver.removeConstr(constrShoreCell2);
				}
				cellWeight = 1;
				constrShoreCell1 = pbSolver.addAtLeast(lit, coeff, cellWeight);
				constrShoreCell2 = pbSolver.addAtMost(lit, coeff, cellWeight);
				printConstraint(lit, coeff, "=", cellWeight);

				// If cell is safe
				if (!pbSolver.isSatisfiable()) {
					boolean isMine = false;
					results.put(current, isMine);
				}

				if (constrShoreCell1 != null) {
					pbSolver.removeConstr(constrShoreCell1);
				}
				if (constrShoreCell2 != null) {
					pbSolver.removeConstr(constrShoreCell2);
				}
			} catch (ContradictionException ce) {
				// Contradiction Exception is thrown when the tested cell is
				// already known to be safe/a mine.
				boolean isMine = cellWeight == 0 ? true : false;
				results.put(current, isMine);
				if (constrShoreCell1 != null) {
					pbSolver.removeConstr(constrShoreCell1);
				}
				if (constrShoreCell2 != null) {
					pbSolver.removeConstr(constrShoreCell2);
				}
			} catch (TimeoutException te) {
				System.out.println("time out");
			}
		}

		if (Thread.interrupted() || !running.get()) {
			return null;
		}

		if (!seaCells.isEmpty()) {
			IVecInt lits = new VecInt();
			IVecInt coeffs = new VecInt();
			try {
				// Generate the known constraints on the board
				int noOfLits = Integer.toBinaryString(seaCells.size()).length();
				for (int i = 0; i < noOfLits; i++) {
					lits.push(encodeLit(i));
					coeffs.push((int) Math.pow(2, i));

				}
				IConstr seaConstr = pbSolver.addAtMost(lits, coeffs, seaCells.size() - 1);
				// Find if cell is safe or mine
				if (!pbSolver.isSatisfiable()) {
					boolean isMine = true;
					for (Cell c : seaCells) {
						results.put(c, isMine);
					}
				}

				if (seaConstr != null) {
					pbSolver.removeConstr(seaConstr);
				}

				pbSolver.addAtLeast(lits, coeffs, 1);

				// Find if cell is safe or mine
				if (!pbSolver.isSatisfiable()) {
					boolean isMine = false;
					for (Cell c : seaCells) {
						results.put(c, isMine);
					}
				}
				pbSolver.reset();
			} catch (ContradictionException ce) {
				pbSolver.reset();
			} catch (TimeoutException te) {
			}
		}
		pbSolver.reset();

		if (Thread.interrupted() || !running.get()) {
			return null;
		}
		return results;
	}

	public void genBinaryConstraints(IPBSolver pbSolver) throws ContradictionException {
		cells = game.getCells();
		List<Cell> closedShore = getShoreClosedCells();
		List<Cell> seaCells = getSeaCells();
		List<Cell> landCells = getLandCells();

		int noOfMines = game.getNoOfMines();
		int noOfLits = Integer.toBinaryString(seaCells.size()).length();
		IVecInt lits = new VecInt();
		IVecInt coeffs = new VecInt();

		for (int i = 0; i < noOfLits; i++) {
			lits.push(encodeLit(i));
			coeffs.push((int) Math.pow(2, i));
		}
		printConstraint(lits, coeffs, "<=", seaCells.size());
		pbSolver.addAtMost(lits, coeffs, seaCells.size());

		int presumedMineCount = 0;
		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (current.isPresumedMine()) {
				presumedMineCount++;
				continue;
			}
			lits.push(encodeCellId(current));
			coeffs.push(1);
		}
		printConstraint(lits, coeffs, "=", noOfMines - presumedMineCount);
		pbSolver.addExactly(lits, coeffs, noOfMines - presumedMineCount);

		lits.clear();
		coeffs.clear();

		IVecInt landLits = new VecInt();
		IVecInt landCoeffs = new VecInt();
		// Every open cell is guarenteed to not be a mine (cell=0)
		for (Cell current : landCells) {
			landLits.push(encodeCellId(current));
			landCoeffs.push(1);
			List<Cell> neighbours = getNeighbours(current);
			// neighbours.removeIf(c -> !c.isClosed());
			// if (neighbours.isEmpty()) {
			// continue;
			// }
			for (Cell c : neighbours) {
				lits.push(encodeCellId(c));
				coeffs.push(1);
			}
			printConstraint(lits, coeffs, "=", current.getNumber());
			pbSolver.addExactly(lits, coeffs, current.getNumber());
			lits.clear();
			coeffs.clear();
		}
		printConstraint(landLits, landCoeffs, "=", 0);
		pbSolver.addExactly(landLits, landCoeffs, 0);
	}

	private Map<Cell, BigFraction> calcAllCellsProb() {
		PBSolver pbSolver = SolverFactory.newDefault();
		cells = game.getCells();
		// Key = Cell
		// Value = Map of Integer to Integer
		// Key = Number of mines in solution
		// Value = Coefficient
		// (number of times the cell appears in Key number of mine solutions)
		Map<Cell, BigInteger> cellT = new HashMap<>();

		Map<Cell, BigFraction> probs = new HashMap<>();

		BigInteger T = BigInteger.ZERO;
		BigFraction seaT = BigFraction.ZERO;
		int totalMines = game.getNoOfMines();
		int seaSize = getSeaCells().size();

		try {
			genBinaryConstraints(pbSolver);
		} catch (ContradictionException e3) {
			e3.printStackTrace();
		}

		try {
			while (pbSolver.isSatisfiable() && running.get()) {
				List<Cell> currentSol = new ArrayList<>();
				int[] model = pbSolver.model();
				int noOfMines = 0;
				for (int i : model) {
					// Test if lit is for a cell and if cell is also a mine
					boolean mine = i < 0 ? false : true;
					Cell testForCell = decodeCellId(i);
					if (testForCell != null && mine) {
						// System.out.print("" + testForCell + ", ");
						currentSol.add(testForCell);
						noOfMines++;
					}
				}
				// System.out.println("\n");
				// System.out.println("\n");
				// System.out.println("\n");
				// Increment cell config count
				// (number of times a cell has appeared in a certain config)

				int remainingMines = totalMines - noOfMines;

				BigInteger toAdd = BigInteger.ONE;
				if (seaSize > 0) {
					toAdd = BigIntegerMath.binomial(seaSize, remainingMines);
				}
				// Increment T
				T = T.add(toAdd);

				for (Cell c : currentSol) {
					BigInteger testForNull = cellT.get(c);
					if (testForNull == null) {
						cellT.put(c, toAdd);
					} else {
						testForNull = testForNull.add(toAdd);
						cellT.put(c, testForNull);
					}
				}

				BigFraction seaFrac = BigFraction.ZERO;
				if (seaSize > 0) {
					seaFrac = new BigFraction(remainingMines, seaSize);
				}
				BigFraction toAddFraction = new BigFraction(toAdd, BigInteger.ONE);
				BigFraction both = seaFrac.multiply(toAddFraction);

				seaT = seaT.add(both);

				// Find another solution
				for (int i = 0; i < model.length; i++) {
					model[i] = model[i] * -1;
				}
				IVecInt block = new VecInt(model);

				pbSolver.addBlockingClause(block);
			}
		} catch (TimeoutException e) {
			pbSolver.reset();
			e.printStackTrace();
		} catch (ContradictionException e) {
			System.out.println("shit");
			pbSolver.reset();
			e.printStackTrace();
		}
		pbSolver.reset();
		if (!running.get()) {
			return null;
		}
		// System.out.println("No. of solutions: " + noOfSolutions);
		// System.out.print("Solutions Map: ");
		// for (Integer i : solutions.keySet()) {
		// System.out.println("{" + i + "=" + solutions.get(i).size() + "}");
		// }
		// System.out.println("Solutions List: " + solutions);
		// System.out.println("T: " + T);
		if (seaSize > 0) {
			seaT = seaT.reduce();
			BigFraction TFrac = new BigFraction(T);
			BigFraction seaProb = seaT.divide(TFrac).reduce();
			List<Cell> seaCells = getSeaCells();
			for (Cell c : seaCells) {
				probs.put(c, seaProb);
			}
		}

		List<Cell> shore = getShoreClosedCells();
		for (Cell current : shore) {
			if (current.isFlagged()) {
				continue;
			}
			BigInteger currentCellT = cellT.get(current);
			// currentCellT is null when a cell does not appear in any solution
			// and is therefore safe, meaning 0.0 prob of being a mine
			if (currentCellT == null) {
				currentCellT = BigInteger.ZERO;
			}
			BigFraction cellProb = new BigFraction(currentCellT, T);
			probs.put(current, cellProb);
		}
		pbSolver.reset();

		return probs;
	}

	public void displayProb() {
		Map<Cell, BigFraction> probs = calcAllCellsProb();
		if (probs == null) {
			return;
		}
		for (Map.Entry<Cell, BigFraction> pair : probs.entrySet()) {
			Cell current = pair.getKey();
			BigFraction prob = pair.getValue();

			current.setProb(prob.doubleValue());
		}

		List<Cell> bestProbCells = getBestProbCell(probs);
		for (Cell c : bestProbCells) {
			c.setBestCell();
		}
		game.refresh();
	}

	public List<Cell> getBestProbCell(Map<Cell, BigFraction> probs) {
		List<Cell> cellsWithBestProb = new ArrayList<>();

		if (probs == null) {
			return cellsWithBestProb;
		}

		for (Map.Entry<Cell, BigFraction> pair : probs.entrySet()) {
			Cell current = pair.getKey();
			BigFraction prob = pair.getValue();
			if (cellsWithBestProb.isEmpty()) {
				cellsWithBestProb.add(current);
				continue;
			}
			BigFraction currentBestProb = probs.get(cellsWithBestProb.get(0));
			int val = prob.compareTo(currentBestProb);
			if (val == 0) {
				cellsWithBestProb.add(current);
			} else if (val < 0) {
				cellsWithBestProb.clear();
				cellsWithBestProb.add(current);
			}
		}
		// System.out.println(cellsWithBestProb);
		return cellsWithBestProb;
	}

	public void temp() {
		selectRandomCell();
	}

	public Cell getBestStratCell(List<Cell> bestProbCells) {
		if (bestProbCells.isEmpty()) {
			return null;
		}
		Cell bestCell = null;
		List<Cell> bestCells = new ArrayList<>();
		int lowestClosed = 9;
		for (Cell c : bestProbCells) {
			List<Cell> neighbours = getNeighbours(c);
			neighbours.removeIf(c2 -> c2.isOpen() || c2.isFlagged());
			int closedCount = neighbours.size();
			if (closedCount < lowestClosed) {
				lowestClosed = closedCount;
				bestCells.clear();
				bestCells.add(c);
			} else if (closedCount == lowestClosed) {
				bestCells.add(c);
			}
		}
		if (bestCells.size() > 1) {
			bestCell = getRandomCell(bestCells);
		} else {
			bestCell = bestCells.get(0);
		}
		return bestCell;
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
	 * @param c
	 *            Cell to encode.
	 * @param board
	 *            Board the cell is present in, used to get the width of the board.
	 * @return Unique integer identifier for given cell.
	 */
	private int encodeCellId(Cell c) {
		return (c.getY() * cells.length + c.getX()) + 1;
	}

	/**
	 * When passed an identity, decode and return the cell it is referring to.
	 * 
	 * @param id
	 *            Unique encoded identity id.
	 * @param board
	 *            Board the cell would be present in, used to get the width of the
	 *            board.
	 * @return Cell that the id refers to.
	 */
	private Cell decodeCellId(int id) {
		int posId = id < 0 ? id * -1 : id;
		if (posId > ((cells[0].length - 1) * cells.length + (cells.length - 1)) + 1) {
			return null;
		}
		int x = (posId - 1) % cells.length;
		int y = ((posId - 1) - x) / cells.length;
		return cells[x][y];
	}

	private int encodeLit(int i) {
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

	private List<Cell> getLandCells() {
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

	private List<Cell> getSeaCells() {
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

	private List<Cell> getShoreClosedCells() {
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

	private List<Cell> getShoreOpenCells() {
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
	 * @param x
	 *            X-axis coordinate of cell.
	 * @param y
	 *            Y-axis coordinate of cell.
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
	 * @param x
	 *            X-axis coordinate of cell.
	 * @param y
	 *            Y-axis coordinate of cell.
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
			game.quietSelect(selectedCell.getX(), selectedCell.getY());
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

	public void flipStrat() {
		this.strat = !this.strat;
	}

	public void setStrat(boolean strat) {
		this.strat = strat;
	}

	public boolean getStrat() {
		return strat;
	}

	private void genAllConstraints(IPBSolver solver) throws ContradictionException {

		IVecInt lits = new VecInt();
		IVec<BigInteger> coeffs = new Vec<BigInteger>();

		// Constraint that sum of all cells must be the no.
		// of mines present on the board
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

		List<Cell> landCells = getLandCells();
		for (int i = 0; i < landCells.size(); i++) {
			Cell current = landCells.get(i);

			lits.clear();
			coeffs.clear();

			// Every open cell is guaranteed to not be a mine
			lits.push(encodeCellId(current));
			coeffs.push(BigInteger.ONE);
			solver.addExactly(lits, coeffs, BigInteger.ZERO);
			lits.clear();
			coeffs.clear();

			List<Cell> neighbours = getNeighbours(current);
			// neighbours.removeIf(c -> !c.isClosed());
			// Normal constraint
			for (Cell c : neighbours) {
				lits.push(encodeCellId(c));
				coeffs.push(BigInteger.ONE);
			}
			solver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
			lits.clear();
			coeffs.clear();
		}
	}

	private Map<Cell, Boolean> oldSATSolve() {
		IPBSolver pbSolver = SolverFactory.newDefault();
		pbSolver.reset();

		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> closedShore = getShoreClosedCells();
		List<Cell> sea = getSeaCells();

		try {
			genAllConstraints(pbSolver);
		} catch (ContradictionException e) {}

		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (current.isPresumedMine()) {
				continue;
			}
			// Find if cell safe (weight=0) or mine (weight=1)
			for (int weight = 0; weight <= 1; weight++) {
				IVecInt lit = new VecInt();
				IVecInt coeff = new VecInt();
				IConstr atMostConstr = null;
				IConstr atLeastConstr = null;
				try {
					// Generate the known constraints on the board

					// Create literal for current cell
					lit.push(encodeCellId(current));
					coeff.push(1);
					// Safe/Mine
					atMostConstr = pbSolver.addAtMost(lit, coeff, weight);
					atLeastConstr = pbSolver.addAtLeast(lit, coeff, weight);
					// Find if cell is safe or mine
					if (!pbSolver.isSatisfiable()) {
						boolean isMine = weight == 1 ? false : true;
						results.put(current, isMine);
						if (atMostConstr != null) {
							pbSolver.removeConstr(atMostConstr);
						}
						if (atLeastConstr != null) {
							pbSolver.removeConstr(atLeastConstr);
						}
						// Break as no need to check if cell is safe,
						// cell is already proven to be a mine
						break;
					}
					if (atMostConstr != null) {
						pbSolver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						pbSolver.removeConstr(atLeastConstr);
					}
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is
					// already known to be safe/a mine.
					boolean isMine = weight == 1 ? false : true;
					results.put(current, isMine);
					if (atMostConstr != null) {
						pbSolver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						pbSolver.removeConstr(atLeastConstr);
					}
				} catch (TimeoutException te) {
				}
			}
		}
		
		if (!sea.isEmpty()) {
			Cell current = sea.get(0);
			for (int weight = 0; weight <= 1; weight++) {
				IVecInt lit = new VecInt();
				IVecInt coeff = new VecInt();
				IConstr atMostConstr = null;
				IConstr atLeastConstr = null;
				try {
					// Create literal for current cell
					lit.push(encodeCellId(current));
					coeff.push(1);
					// Safe/Mine
					atMostConstr = pbSolver.addAtMost(lit, coeff, weight);
					atLeastConstr = pbSolver.addAtLeast(lit, coeff, weight);
					// Find if cell is safe or mine
					if (!pbSolver.isSatisfiable()) {
						boolean isMine = weight == 1 ? false : true;
						results.put(current, isMine);
						if (atMostConstr != null) {
							pbSolver.removeConstr(atMostConstr);
						}
						if (atLeastConstr != null) {
							pbSolver.removeConstr(atLeastConstr);
						}
						// Break as no need to check if cell is safe,
						// cell is already proven to be a mine
						break;
					}
					if (atMostConstr != null) {
						pbSolver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						pbSolver.removeConstr(atLeastConstr);
					}
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is
					// already known to be safe/a mine.
					boolean isMine = weight == 1 ? false : true;
					results.put(current, isMine);
					if (atMostConstr != null) {
						pbSolver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						pbSolver.removeConstr(atLeastConstr);
					}
				} catch (TimeoutException te) {
				}
			}
		}

		pbSolver.reset();
		if (Thread.interrupted() || !running.get()) {
			return null;
		}
		pbSolver.reset();
		return results;
	}

	public boolean old() {
		cells = game.getCells();
		boolean change = false;
		Map<Cell, Boolean> known = oldSATSolve();
		if (known == null || !running.get()) {
			return false;
		}
		// Iterate over map
		for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
			Cell current = pair.getKey();
			boolean mine = pair.getValue();
			if (mine) {
				if (current.isClosed() && !current.isFlagged()) {
					current.flag();
					game.decrementMines();
					current.setPresumedMine(true);
					String detail = "Flagging " + current + " as Cell is a Guarenteed Mine";
					game.setDetail(detail);
					change = true;
				}
			} else {
				if (current.isBlank()) {
					if (quiet) {
						game.quietSelect(current.getX(), current.getY());
					} else {
						String detail = "Selecting " + current + " as Cell is Safe";
						game.setDetail(detail);
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
}