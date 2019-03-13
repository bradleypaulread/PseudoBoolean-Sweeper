import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.math.BigIntegerMath;

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

	private boolean quiet;

	private List<Cell> knownMines = new ArrayList<>();

	private Minesweeper game;
	private Cell[][] cells;
	AtomicBoolean running;

	public BoardSolver(Minesweeper game) {
		running = new AtomicBoolean(true);
		quiet = false;
		pbSolver = SolverFactory.newDefault();
		this.game = game;
		cells = game.getCells();
	}

	public BoardSolver(Minesweeper game, AtomicBoolean running) {
		this.running = running;
		quiet = false;
		pbSolver = SolverFactory.newDefault();
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

	private List<Cell> fromEdge(Cell cell) {
		List<Cell> safeCells = new ArrayList<>();
		int xPos = cell.getX();
		int yPos = cell.getY();
		if (yPos == 0 && cell.getNumber() == cells[xPos][yPos + 1].getNumber()) {
			List<Cell> currentN = getNeighbours(cell);
			List<Cell> compareN = getNeighbours(xPos, yPos + 1);
			currentN.removeIf(c -> c.isOpen());
			compareN.removeIf(c -> c.isOpen());
			for (Cell c : compareN) {
				if (c.isBlank() && !currentN.contains(c)) {
					safeCells.add(c);
				}
			}
		} else if (yPos == cells[xPos].length - 2 && cell.getNumber() == cells[xPos][yPos + 1].getNumber()) {
			List<Cell> currentN = getNeighbours(cell);
			List<Cell> compareN = getNeighbours(xPos, yPos + 1);
			currentN.removeIf(c -> c.isOpen());
			compareN.removeIf(c -> c.isOpen());
			for (Cell c : currentN) {
				if (c.isBlank() && !compareN.contains(c)) {
					safeCells.add(c);
				}
			}
		} else if (xPos == 0 && cell.getNumber() == cells[xPos + 1][yPos].getNumber()) {
			List<Cell> currentN = getNeighbours(cell);
			List<Cell> compareN = getNeighbours(xPos + 1, yPos);
			currentN.removeIf(c -> c.isOpen());
			compareN.removeIf(c -> c.isOpen());
			for (Cell c : compareN) {
				if (c.isBlank() && !currentN.contains(c)) {
					safeCells.add(c);
				}
			}
		} else if (xPos == cells.length - 2 && cell.getNumber() == cells[xPos + 1][yPos].getNumber()) {
			List<Cell> currentN = getNeighbours(cell);
			List<Cell> compareN = getNeighbours(xPos + 1, yPos);
			currentN.removeIf(c -> c.isOpen());
			compareN.removeIf(c -> c.isOpen());
			for (Cell c : currentN) {
				if (c.isBlank() && !compareN.contains(c)) {
					safeCells.add(c);
				}
			}
		}
		return safeCells;
	}

	private List<Cell> oneTwoPattern(Cell cell) {
		List<Cell> mineCells = new ArrayList<>();
		if (isLineX(cell)) {
			List<List<Cell>> lines = getLineX(cell);
			if (!lines.isEmpty()) {
				List<Cell> openLine = lines.get(0);
				List<Cell> closedLine = lines.get(1);
				for (int lineIdx = 0; lineIdx < openLine.size() - 2; lineIdx++) {
					if ((openLine.get(lineIdx).getNumber() == 1 && openLine.get(lineIdx + 1).getNumber() == 2)
							&& calcClosedNeighbours(openLine.get(lineIdx + 1).getX(),
									openLine.get(lineIdx + 1).getY()) <= 3) {
						if (closedLine.size() > lineIdx + 2) {
							Cell c = closedLine.get(lineIdx + 2);
							if (c.isBlank()) {
								mineCells.add(c);
							}
						}
					}
				}
				for (int lineIdx = 0; lineIdx < openLine.size() - 2; lineIdx++) {
					if ((openLine.get(lineIdx).getNumber() == 2 && openLine.get(lineIdx + 1).getNumber() == 1)
							&& calcClosedNeighbours(openLine.get(lineIdx).getX(), openLine.get(lineIdx).getY()) <= 3) {
						if (openLine.get(lineIdx).getX() - 1 >= 0 && openLine.get(lineIdx).getY() - 1 >= 0) {
							Cell c = cells[openLine.get(lineIdx).getX() - 1][openLine.get(lineIdx).getY() - 1];
							if (c.isBlank()) {
								mineCells.add(c);
							}
						}
					}
				}
			}
		} else if (isLineY(cell)) {
			List<List<Cell>> lines = getLineY(cell);
			if (!lines.isEmpty()) {
				List<Cell> openLine = lines.get(0);
				List<Cell> closedLine = lines.get(1);
				for (int lineIdx = 0; lineIdx < openLine.size() - 1; lineIdx++) {
					if ((openLine.get(lineIdx).getNumber() == 1 && openLine.get(lineIdx + 1).getNumber() == 2)
							&& calcClosedNeighbours(openLine.get(lineIdx + 1).getX(),
									openLine.get(lineIdx + 1).getY()) <= 3) {
						if (closedLine.size() > lineIdx + 2) {
							Cell c = closedLine.get(lineIdx + 2);
							if (c.isBlank()) {
								mineCells.add(c);
							}
						}
					}
				}
				for (int lineIdx = 0; lineIdx < openLine.size() - 1; lineIdx++) {
					if ((openLine.get(lineIdx).getNumber() == 2 && openLine.get(lineIdx + 1).getNumber() == 1)
							&& calcClosedNeighbours(openLine.get(lineIdx).getX(), openLine.get(lineIdx).getY()) <= 3) {
						if (openLine.get(lineIdx).getX() - 1 >= 0 && openLine.get(lineIdx).getY() - 1 >= 0) {
							Cell c = cells[openLine.get(lineIdx).getX() - 1][openLine.get(lineIdx).getY() - 1];
							if (c.isBlank()) {
								mineCells.add(c);
							}
						}
					}
				}
			}
		}
		return mineCells;
	}

	private boolean isLineX(Cell cell) {
		List<Cell> neighbours = getNeighbours(cell);
		neighbours.removeIf(c -> c.isOpen());
		if (neighbours.size() < 2) {
			return false;
		}
		int xAxis = neighbours.get(0).getX();
		int smallY = neighbours.get(0).getY();
		int largeY = neighbours.get(0).getY();

		for (Cell c : neighbours) {
			if (c.getX() != xAxis) {
				return false;
			}
			smallY = c.getY() < smallY ? c.getY() : smallY;
			largeY = c.getY() > largeY ? c.getY() : largeY;
		}
		if (largeY - smallY > 2) {
			return false;
		}
		int mid = (largeY + smallY) / 2;
		if (cells[xAxis][mid].isOpen()) {
			return false;
		}
		return true;
	}

	private List<List<Cell>> getLineX(Cell cell) {
		List<List<Cell>> lines = new ArrayList<>();
		List<Cell> lineOpen = new ArrayList<>();
		List<Cell> lineClosed = new ArrayList<>();
		int xAxis = cell.getX();
		int yAxis = cell.getY();
		int closedX;
		if (xAxis == 0) {
			closedX = 0;
		} else if (xAxis == cells.length - 1) {
			closedX = cells.length - 1;
		} else if (cells[xAxis - 1][yAxis].isClosed()) {
			closedX = xAxis - 1;
		} else {
			closedX = xAxis + 1;
		}
		while (yAxis < cells[0].length && cells[xAxis][yAxis].isOpen() && cells[closedX][yAxis].isClosed()) {
			lineOpen.add(cells[xAxis][yAxis]);
			lineClosed.add(cells[closedX][yAxis]);
			yAxis++;
		}
		lines.add(lineOpen);
		lines.add(lineClosed);
		return lines;
	}

	private boolean isLineY(Cell cell) {
		List<Cell> neighbours = getNeighbours(cell);
		neighbours.removeIf(c -> c.isOpen());
		if (neighbours.size() < 2) {
			return false;
		}
		int yAxis = neighbours.get(0).getY();
		int smallX = neighbours.get(0).getX();
		int largeX = neighbours.get(0).getX();

		for (Cell c : neighbours) {
			if (c.getY() != yAxis) {
				return false;
			}
			smallX = c.getX() < smallX ? c.getX() : smallX;
			largeX = c.getX() > largeX ? c.getX() : largeX;
		}
		if (largeX - smallX > 2) {
			return false;
		}
		int mid = (largeX + smallX) / 2;
		if (cells[mid][yAxis].isOpen()) {
			return false;
		}
		return true;
	}

	private List<List<Cell>> getLineY(Cell cell) {
		List<List<Cell>> lines = new ArrayList<>();
		List<Cell> lineOpen = new ArrayList<>();
		List<Cell> lineClosed = new ArrayList<>();
		int xAxis = cell.getX();
		int yAxis = cell.getY();
		int closedY;
		if (yAxis == 0) {
			closedY = 1;
		} else if (yAxis == cells[0].length - 1) {
			closedY = cells[0].length - 1;
		} else if (cells[xAxis][yAxis - 1].isClosed()) {
			closedY = yAxis - 1;
		} else {
			closedY = yAxis + 1;
		}
		while (xAxis < cells.length && cells[xAxis][yAxis].isOpen() && cells[xAxis][closedY].isClosed()) {
			lineOpen.add(cells[xAxis][yAxis]);
			lineClosed.add(cells[xAxis][closedY]);
			xAxis++;
		}
		lines.add(lineOpen);
		lines.add(lineClosed);
		return lines;
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
					if (current.isOpen()) {
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
						if (!(cells = oneTwoPattern(current)).isEmpty()) {
							for (Cell c : cells) {
								if (c.isBlank() && !c.isHint()) {
									c.setMineHint();
									game.getHintCells().add(c);
									game.refresh();
									return;
								}
							}
						}
						if (!(cells = fromEdge(current)).isEmpty()) {
							for (Cell c : cells) {
								if (c.isBlank() && !c.isHint()) {
									c.setSafeHint();
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

	public void oldSATHint() {
		cells = game.getCells();
		Map<Cell, Boolean> known = shallowSolve();
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
						if (!(cells = oneTwoPattern(current)).isEmpty()) {
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
						if (!(cells = fromEdge(current)).isEmpty()) {
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
					}
				}
			}
		}
		return false;
	}

	public boolean oldSATSolve() {
		boolean change = false;
		Map<Cell, Boolean> known = shallowSolve();
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
		if (!game.isGameOver() && game.getProb()) {
			calcAllCellsProb();
		}
		return change;
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
				if (!knownMines.contains(current)) {
					knownMines.add(current);
				}
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

	public boolean jointSolve() {
		if (!patternMatch()) {
			if (!SATSolve()) {
				return false;
			}
		}
		return true;
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

	private Map<Cell, Boolean> deepSolveMines() {
		Map<Cell, Boolean> results = new HashMap<>();
		for (int i = 0; i < cells.length && running.get() && !Thread.interrupted(); i++) {
			for (int j = 0; j < cells[i].length && running.get() && !Thread.interrupted(); j++) {
				Cell current = cells[i][j];
				// Skip looking at cells open cell and cells that have already been checked
				if (current.isOpen()) {
					continue;
				}

				// Find if cell safe (weight=0) or mine (weight=1)
				for (int weight = 0; weight <= 1; weight++) {
					IVecInt lit = new VecInt();
					IVec<BigInteger> coeff = new Vec<BigInteger>();
					BigInteger cellWeight = BigInteger.valueOf(weight);
					try {
						pbSolver = SolverFactory.newDefault();
						// Generate the known constraints on the board
						genAllConstraints(pbSolver);

						// Create literal for current cell
						lit.push(encodeCellId(current));
						coeff.push(BigInteger.ONE);
						// Safe/Mine
						pbSolver.addExactly(lit, coeff, cellWeight);

						// Optimise wrapper
						OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

						// Find if cell is safe or mine
						if (!optimiser.isSatisfiable()) {
							boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
							results.put(current, isMine);
							pbSolver.reset();
							optimiser.reset();
							break; // Break as no need to check if cell is also a mine
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
		}
		if (Thread.interrupted() || !running.get()) {
			return null;
		}
		return results;
	}

	private Map<Cell, Boolean> shallowSolve() {
		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> closedShore = getShoreClosedCells();
		List<Cell> sea = getSeaCells();

		if (!sea.isEmpty()) {
			Cell current = sea.get(0);
			for (int weight = 0; weight <= 1; weight++) {
				IVecInt lit = new VecInt();
				IVec<BigInteger> coeff = new Vec<BigInteger>();
				BigInteger cellWeight = BigInteger.valueOf(weight);
				try {
					pbSolver = SolverFactory.newDefault();
					// Generate the known constraints on the board
					genAllConstraints(pbSolver);

					// Create literal for current cell
					lit.push(encodeCellId(current));
					coeff.push(BigInteger.ONE);
					// Safe/Mine
					pbSolver.addExactly(lit, coeff, cellWeight);

					// Optimise wrapper
					OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

					// Find if cell is safe or mine
					if (!optimiser.isSatisfiable()) {
						boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
						for (Cell c : sea) {
							results.put(c, isMine);
						}
						pbSolver.reset();
						optimiser.reset();
						break; // Break as no need to check if cell is also a mine
					}
					pbSolver.reset();
					optimiser.reset();
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is already known to be
					// safe/a mine.
					boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
					for (Cell c : sea) {
						results.put(c, isMine);
					}
				} catch (TimeoutException te) {
				}
			}
		}
		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (current.isFlagged()) {
				continue;
			}
			// Find if cell safe (weight=0) or mine (weight=1)
			for (int weight = 0; weight <= 1; weight++) {
				IVecInt lit = new VecInt();
				IVec<BigInteger> coeff = new Vec<BigInteger>();
				BigInteger cellWeight = BigInteger.valueOf(weight);
				try {
					pbSolver = SolverFactory.newDefault();
					// Generate the known constraints on the board
					genAllConstraints(pbSolver);

					// Create literal for current cell
					lit.push(encodeCellId(current));
					coeff.push(BigInteger.ONE);
					// Safe/Mine
					pbSolver.addExactly(lit, coeff, cellWeight);

					// Optimise wrapper
					OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

					// Find if cell is safe or mine
					if (!optimiser.isSatisfiable()) {
						boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
						results.put(current, isMine);
						pbSolver.reset();
						optimiser.reset();
						break; // Break as no need to check if cell is also a mine
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

	private Map<Cell, Boolean> binaryShallowSolve() {
		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> closedShore = getShoreClosedCells();
		List<Cell> sea = getSeaCells();

		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (current.isFlagged()) {
				continue;
			}
			// Find if cell safe (weight=0) or mine (weight=1)
			for (int weight = 0; weight <= 1 && running.get() && !Thread.interrupted(); weight++) {
				IVecInt lit = new VecInt();
				IVec<BigInteger> coeff = new Vec<BigInteger>();
				BigInteger cellWeight = BigInteger.valueOf(weight);
				try {
					pbSolver = SolverFactory.newDefault();
					// Generate the known constraints on the board
					genBinaryConstraints(pbSolver, false);

					// Create literal for current cell
					lit.push(encodeCellId(current));
					coeff.push(BigInteger.ONE);
					// Safe/Mine
					pbSolver.addExactly(lit, coeff, cellWeight);

					// Optimise wrapper
					OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

					// Find if cell is safe or mine
					if (!optimiser.isSatisfiable()) {
						boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
						results.put(current, isMine);
						break; // Break as no need to check if cell is also a mine
					}
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

		if (!sea.isEmpty()) {
			Cell current = sea.get(0);
			for (int weight = 0; weight <= 1 && running.get() && !Thread.interrupted(); weight++) {
				IVecInt lit = new VecInt();
				IVec<BigInteger> coeff = new Vec<BigInteger>();
				BigInteger cellWeight = BigInteger.valueOf(weight);
				try {

					pbSolver = SolverFactory.newDefault();
					// Generate the known constraints on the board
					genBinaryConstraints(pbSolver, true);

					// Create literal for current cell
					lit.push(encodeCellId(current));
					coeff.push(BigInteger.ONE);
					// Safe/Mine
					pbSolver.addExactly(lit, coeff, cellWeight);

					// Optimise wrapper
					OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

					// Find if cell is safe or mine
					if (!optimiser.isSatisfiable()) {
						boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
						for (Cell c : sea) {
							results.put(c, isMine);
						}
						pbSolver.reset();
						optimiser.reset();
						break; // Break as no need to check if cell is also a mine
					}
					pbSolver.reset();
					optimiser.reset();
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is already known to be
					// safe/a mine.
					boolean isMine = cellWeight.compareTo(BigInteger.valueOf(1)) == 0 ? false : true;
					for (Cell c : sea) {
						results.put(c, isMine);
					}
				} catch (TimeoutException te) {
				}
			}
		}

		if (Thread.interrupted() || !running.get()) {
			return null;
		}

		return results;
	}

	public void genBinaryConstraints(IPBSolver pbSolver, boolean checkSea) throws ContradictionException {
		cells = game.getCells();
		List<Cell> closedShore = getShoreClosedCells();

		int noOfMines = game.getNoOfMines();
		int noOfLits = Integer.toBinaryString(noOfMines).length();
		IVecInt lits = new VecInt();
		IVec<BigInteger> coeffs = new Vec<BigInteger>();

		for (int i = 0; i < noOfLits; i++) {
			lits.push(encodeLit(i));
			coeffs.push(BigInteger.valueOf((long) Math.pow(2, i)));
		}
		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (!knownMines.contains(current)) {
				lits.push(encodeCellId(current));
				coeffs.push(BigInteger.ONE);
			}
		}
		// Add a singular sea cell (if there is any sea) to represent the entire sea
		List<Cell> sea = getSeaCells();
		if (checkSea && !sea.isEmpty()) {
			Cell seaCell = sea.get(0);
			lits.push(encodeCellId(seaCell));
			coeffs.push(BigInteger.ONE);
		}
		int lastRunKnownMines = knownMines.size();
		pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(noOfMines - lastRunKnownMines));

		lits.clear();
		coeffs.clear();
		// Every open cell is guarenteed to not be a mine (cell=0)
		List<Cell> landCells = getLandCells();
		for (Cell current : landCells) {
			lits.push(encodeCellId(current));
			coeffs.push(BigInteger.ONE);
			pbSolver.addExactly(lits, coeffs, BigInteger.ZERO);
			lits.clear();
			coeffs.clear();
			List<Cell> neighbours = getNeighbours(current);
			// neighbours.removeIf(c -> !c.isClosed());
			for (Cell c : neighbours) {
				lits.push(encodeCellId(c));
				coeffs.push(BigInteger.ONE);
			}
			pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
			lits.clear();
			coeffs.clear();
		}

		lits.clear();
		coeffs.clear();

		// for (int i = 0; i < openShore.size() && running.get() &&
		// !Thread.interrupted(); i++) {
		// lits.clear();
		// coeffs.clear();
		// Cell current = openShore.get(i);

		// for (Cell c : getNeighbours(current)) {
		// lits.push(encodeCellId(c));
		// coeffs.push(BigInteger.ONE);
		// }
		// pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));

		// lits.clear();
		// coeffs.clear();
		// }
	}

	public Map<Cell, Double> oldCalcAllCellsProb() {
		cells = game.getCells();

		Map<Cell, Double> results = new HashMap<>();

		List<Cell> adjacentCells = getShoreClosedCells();

		pbSolver = SolverFactory.newDefault();

		try {
			genAllConstraints(pbSolver);
		} catch (ContradictionException e3) {
			e3.printStackTrace();
		}

		for (Cell[] col : cells) {
			for (Cell c : col) {
				results.put(c, 0.0);
			}
		}

		OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));
		int noOfSolutions = 0;
		try {
			while (optimiser.isSatisfiable() && running.get()) {
				++noOfSolutions;
				int[] model = pbSolver.model();
				for (int i : model) {
					boolean mine = i < 0 ? false : true;
					if (mine) {
						results.put(decodeCellId(i), results.get(decodeCellId(i)) + 1.0);
					}
				}
				// Find another solution
				for (int i = 0; i < model.length; i++) {
					model[i] = model[i] * -1;
				}
				IVecInt block = new VecInt(model);

				optimiser.addBlockingClause(block);
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}

		Double div = (double) noOfSolutions;
		results.replaceAll((key, val) -> {
			return val / div;
		});
		Map<Cell, Double> sortedByCount = results.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		if (!running.get()) {
			return null;
		}

		// To Remove, debug
		// Remove all cells that are 100% mines
		// sortedByCount.values().removeAll(Collections.singleton(1.0));
		// System.out.println(noOfSolutions);
		// System.out.println(sortedByCount);
		for (Map.Entry<Cell, Double> pair : sortedByCount.entrySet()) {
			Cell current = pair.getKey();
			Double prob = pair.getValue();
			if (current.isBlank()) {
				current.setProb(prob);
			}
		}
		game.refresh();
		System.out.println();
		return sortedByCount;
	}

	public void printModel(int[] model) {
		String str = "";
		for (int i = 0; i < model.length; i++) {
			int pos;
			boolean isMine;
			if (model[i] < 0) {
				pos = model[i] * -1;
				isMine = false;
			} else {
				pos = model[i];
				isMine = true;
			}
			Cell testForNull = decodeCellId(pos);
			if (testForNull == null) {
				String mine = isMine ? "" : "-";
				str += "" + mine + testForNull + ", ";
			}
		}
		System.out.println(str);
		if (!str.equals("")) {
			System.out.println();
		}
	}

	public void calcAllCellsProb() {
		cells = game.getCells();
		// Key = number of mines in solution(s)
		// Value = list of the solutions containing key number of mines
		Map<Integer, List<List<Cell>>> solutions = new HashMap<>();

		pbSolver = SolverFactory.newDefault();

		try {
			genBinaryConstraints(pbSolver, false);
		} catch (ContradictionException e3) {
			e3.printStackTrace();
		}

		OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));
		try {
			while (optimiser.isSatisfiable() && running.get()) {
				List<Cell> currentSol = new ArrayList<>();
				int[] model = pbSolver.model();
				int noOfMines = 0;
				for (int i : model) {
					boolean mine = i < 0 ? false : true;
					Cell testForCell = decodeCellId(i);
					if (testForCell != null && mine) {
						// System.out.print("" + testForCell + ", ");
						currentSol.add(testForCell);
						noOfMines++;
					}
				}
				List<List<Cell>> testForNull = solutions.get(noOfMines);
				if (testForNull == null) {
					List<List<Cell>> temp = new ArrayList<>();
					temp.add(currentSol);
					solutions.put(noOfMines, temp);
				} else {
					testForNull.add(currentSol);
				}

				// Find another solution
				for (int i = 0; i < model.length; i++) {
					model[i] = model[i] * -1;
				}
				IVecInt block = new VecInt(model);

				optimiser.addBlockingClause(block);
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		if (!running.get()) {
			return;
		}
		BigInteger T = new BigInteger("0");
		BigDecimal seaT = new BigDecimal("0");
		int totalMines = game.getNoOfMines();
		int seaSize = getSeaCells().size();
		// Iterate over map
		for (Map.Entry<Integer, List<List<Cell>>> pair : solutions.entrySet()) {
			Integer noOfMines = pair.getKey();
			Integer noOfConfigs = pair.getValue().size();

			int remainingMines = totalMines - noOfMines;
			// System.out.println(pair.getValue());
			BigInteger bioExpan = BigIntegerMath.binomial(seaSize, remainingMines);
			BigInteger toAdd = bioExpan.multiply(BigInteger.valueOf(noOfConfigs));

			T = T.add(toAdd);
			if (seaSize > 0) {
				BigDecimal temp1 = new BigDecimal(remainingMines);
				BigDecimal temp2 = new BigDecimal(seaSize);
				BigDecimal temp = temp1.divide(temp2, MathContext.DECIMAL128);

				seaT = seaT.add(temp.multiply(new BigDecimal(toAdd)));
			}
		}
		// System.out.println("No. of solutions: " + noOfSolutions);
		// System.out.print("Solutions Map: ");
		// for (Integer i : solutions.keySet()) {
		// System.out.println("{" + i + "=" + solutions.get(i).size() + "}");
		// }
		// System.out.println("Solutions List: " + solutions);
		// System.out.println("T: " + T);
		if (seaSize > 0) {
			BigDecimal seaProb = seaT;
			seaProb = seaProb.divide(new BigDecimal(T), MathContext.DECIMAL128);
			for (Cell c : getSeaCells()) {
				c.setProb(seaProb.doubleValue());
			}
		}

		List<Cell> shore = getShoreClosedCells();
		for (Cell current : shore) {
			if (current.isFlagged()) {
				continue;
			}
			BigInteger top = new BigInteger("0");
			for (Map.Entry<Integer, List<List<Cell>>> pair : solutions.entrySet()) {
				int count = 0;
				Integer noOfMines = pair.getKey();
				int remainingMines = totalMines - noOfMines;
				List<List<Cell>> configs = pair.getValue();
				// Count the number of times the cell occurs in solutions
				for (List<Cell> list : configs) {
					for (Cell c : list) {
						if (current == c) {
							count++;
							break;
						}
					}
				}
				top = top.add(BigIntegerMath.binomial(seaSize, remainingMines).multiply(BigInteger.valueOf(count)));
			}
			BigDecimal prob = new BigDecimal(top);
			prob = prob.divide(new BigDecimal(T), MathContext.DECIMAL128);
			current.setProb(prob.doubleValue());
		}
		game.refresh();
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
}