import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that constucts a set of pseudo-Boolean constraints from the current
 * board configuration and uses SAT4J's PBSolver to solve them.
 * 
 * @author Bradley Read
 * @version 1.0
 * @since 2019-03-11
 */
public class PBSolver extends Solver {

	/**
	 * Constructor for PBSolver.
	 * 
	 * @param game the game that the solver is going to perform moves on.
	 */
	public PBSolver(Minesweeper game) {
		super(game);
	}

	/**
	 * Constructor for PBSolver. The solver can be stopped by changing the passed
	 * running boolean to false.
	 * 
	 * @param game    the game that the solver is going to perform moves on.
	 * @param running the value that controls whether the solver should
	 *                continue/stop solving.
	 */
	public PBSolver(Minesweeper game, AtomicBoolean running) {
		super(game, running);
	}

	/**
	 * Generates the pseudo-boolean constraints from the board.
	 * 
	 * @throws ContradictionException when a contraint is added that directly
	 *                                contradicts an already existing constraint.
	 */
	protected void genConstraints() throws ContradictionException {

		IVecInt lits = new VecInt();
		IVecInt coeffs = new VecInt();

		// Constraint that sum of all cells must be the no.
		// of mines present on the board
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				lits.push(encodeCellId(current));
				coeffs.push(1);
			}
		}
		solver.addExactly(lits, coeffs, game.getNoOfMines());
		lits.clear();
		coeffs.clear();

		List<Cell> landCells = getLandCells();
		for (int i = 0; i < landCells.size(); i++) {
			Cell current = landCells.get(i);

			lits.clear();
			coeffs.clear();

			// Every open cell is guaranteed to not be a mine
			lits.push(encodeCellId(current));
			coeffs.push(1);
			solver.addExactly(lits, coeffs, 0);
			lits.clear();
			coeffs.clear();

			List<Cell> neighbours = getNeighbours(current);
			// Normal constraint
			for (Cell c : neighbours) {
				lits.push(encodeCellId(c));
				coeffs.push(1);
			}
			solver.addExactly(lits, coeffs, current.getNumber());
			lits.clear();
			coeffs.clear();
		}
	}

	public boolean hint() {
		cells = game.getCells();

		// Retrieve all the cells that the solver has guarenteed the value of
		Map<Cell, Boolean> known = getKnownCells();

		// if the solver has been told to stop running
		if (known == null || !running.get()) {
			return false;
		}

		// For every found cell
		for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
			Cell current = pair.getKey();
			boolean mine = pair.getValue();
			// Skip cells that are already marked as hints
			if (current.isHint()) {
				continue;
			}
			if (mine) {
				if (current.isBlank()) {
					current.setMineHint(true);
					game.getHintCells().add(current);
					game.refresh();
					return true;
				}
			} else {
				if (current.isBlank()) {
					current.setSafeHint(true);
					game.getHintCells().add(current);
					game.refresh();
					return true;
				}
			}
		}
		return false;
	}

	public boolean assist() {
		cells = game.getCells();
		boolean change = false;
		Map<Cell, Boolean> known = getKnownCells();
		if (known == null || !running.get()) {
			return false;
		}
		// Iterate over map
		for (Map.Entry<Cell, Boolean> pair : known.entrySet()) {
			Cell current = pair.getKey();
			boolean mine = pair.getValue();
			if (mine) {
				if (current.isClosed() && !current.isFlagged()) {
					current.setFlagged(true);
					game.decrementMines();
					String detail = "Flagging " + current + " as Cell is a Guarenteed Mine";
					game.setDetail(detail);
					change = true;
				}
			} else {
				if (current.isBlank()) {
					if (quiet) {
						game.quietProbe(current.getX(), current.getY());
					} else {
						String detail = "Selecting " + current + " as Cell is Safe";
						game.setDetail(detail);
						game.probe(current.getX(), current.getY());
					}
					change = true;
				}
			}
		}
		if (!quiet) {
			game.refresh();
			if (!change) {
				String detail = "Could not find a move";
				game.setDetail(detail);
			}
		}
		return change;
	}

	public void solve() {
		while (assist())
			;
	}

	/**
	 * Runs through the problem set and finds known cells. First adds constraint that
	 * cell is a safe, if the problem is UNSAT then cell is a mine. Secondly adds
	 * constraint that cell is a mine, if the problem is UNSAT then cell is safe.
	 * 
	 * @return a mapping of cells to their found boolean values. True means the cell
	 *         is a mine, false means it is safe.
	 */
	protected Map<Cell, Boolean> getKnownCells() {
		solver.reset();

		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> closedShore = getClosedShoreCells();
		List<Cell> sea = getSeaCells();

		try {
			genConstraints();
		} catch (ContradictionException e) {
		}

		for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
			Cell current = closedShore.get(i);
			if (current.isFlagged()) {
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
					atMostConstr = solver.addAtMost(lit, coeff, weight);
					atLeastConstr = solver.addAtLeast(lit, coeff, weight);
					// Find if cell is safe or mine
					if (!solver.isSatisfiable()) {
						boolean isMine = weight != 1;
						results.put(current, isMine);
						if (atMostConstr != null) {
							solver.removeConstr(atMostConstr);
						}
						if (atLeastConstr != null) {
							solver.removeConstr(atLeastConstr);
						}
						// Break as no need to check if cell is safe,
						// cell is already proven to be a mine
						break;
					}
					if (atMostConstr != null) {
						solver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						solver.removeConstr(atLeastConstr);
					}
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is
					// already known to be safe/a mine.
					boolean isMine = weight != 1;
					results.put(current, isMine);
					if (atMostConstr != null) {
						solver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						solver.removeConstr(atLeastConstr);
					}
				} catch (TimeoutException te) {
				}
			}
		}

		// Test if all the sea cells are a mine or are all safe
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
					atMostConstr = solver.addAtMost(lit, coeff, weight);
					atLeastConstr = solver.addAtLeast(lit, coeff, weight);
					// Find if cell is safe or mine
					if (!solver.isSatisfiable()) {
						boolean isMine = weight != 1;
						for (Cell c : sea) {
							results.put(c, isMine);
						}
						if (atMostConstr != null) {
							solver.removeConstr(atMostConstr);
						}
						if (atLeastConstr != null) {
							solver.removeConstr(atLeastConstr);
						}
						// Break as no need to check if cell is safe,
						// cell is already proven to be a mine
						break;
					}
					if (atMostConstr != null) {
						solver.removeConstr(atMostConstr);
					}
					if (atLeastConstr != null) {
						solver.removeConstr(atLeastConstr);
					}
				} catch (ContradictionException ce) {
					// Contradiction Exception is thrown when the tested cell is
					// already known to be safe/a mine.
					boolean isMine = weight != 1;
					results.put(current, isMine);
					if (atLeastConstr != null) {
						solver.removeConstr(atLeastConstr);
					}
					if (atMostConstr != null) {
						solver.removeConstr(atMostConstr);
					}
				} catch (TimeoutException te) {
				}
			}
		}

		solver.reset();
		if (Thread.interrupted() || !running.get()) {
			return null;
		}
		solver.reset();
		return results;
	}

	/*
	 * Code to retrieve known shore cells using improved PB constraint algorithm.
	 * Still has issues. private Map<Cell, Boolean> getKnownCells() { IPBSolver
	 * pbSolver = SolverFactory.newDefault(); cells = game.getCells(); Map<Cell,
	 * Boolean> results = new HashMap<>(); List<Cell> closedShore =
	 * getShoreClosedCells(); List<Cell> seaCells = getSeaCells(); try {
	 * genConstraints(pbSolver); } catch (ContradictionException e) { } for (int i =
	 * 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
	 * Cell current = closedShore.get(i); // Generate the known constraints on the
	 * board // Find if cell safe (weight=0) or mine (weight=1) IVecInt lit = new
	 * VecInt(); IVecInt coeff = new VecInt(); int cellWeight = 0; IConstr
	 * constrShoreCell1 = null; IConstr constrShoreCell2 = null; try { // Create
	 * literal for current cell lit.push(encodeCellId(current)); coeff.push(1); //
	 * Safe/Mine constrShoreCell1 = pbSolver.addAtMost(lit, coeff, cellWeight);
	 * constrShoreCell2 = pbSolver.addAtLeast(lit, coeff, cellWeight); //
	 * printConstraint(lit, coeff, "=", cellWeight); // Find if cell is a mine if
	 * (!pbSolver.isSatisfiable()) { boolean isMine = true; results.put(current,
	 * isMine); if (constrShoreCell1 != null) {
	 * pbSolver.removeConstr(constrShoreCell1); } if (constrShoreCell2 != null) {
	 * pbSolver.removeConstr(constrShoreCell2); } // Continue to next shore cell as
	 * no need to check if cell is also safe continue; } if (constrShoreCell1 !=
	 * null) { pbSolver.removeConstr(constrShoreCell1); } if (constrShoreCell2 !=
	 * null) { pbSolver.removeConstr(constrShoreCell2); } cellWeight = 1;
	 * constrShoreCell1 = pbSolver.addAtLeast(lit, coeff, cellWeight);
	 * constrShoreCell2 = pbSolver.addAtMost(lit, coeff, cellWeight); //
	 * printConstraint(lit, coeff, "=", cellWeight); // If cell is safe if
	 * (!pbSolver.isSatisfiable()) { boolean isMine = false; results.put(current,
	 * isMine); } if (constrShoreCell1 != null) {
	 * pbSolver.removeConstr(constrShoreCell1); } if (constrShoreCell2 != null) {
	 * pbSolver.removeConstr(constrShoreCell2); } } catch (ContradictionException
	 * ce) { // Contradiction Exception is thrown when the tested cell is // already
	 * known to be safe/a mine. boolean isMine = cellWeight == 0 ? true : false;
	 * results.put(current, isMine); if (constrShoreCell1 != null) {
	 * pbSolver.removeConstr(constrShoreCell1); } if (constrShoreCell2 != null) {
	 * pbSolver.removeConstr(constrShoreCell2); } } catch (TimeoutException te) {
	 * pbSolver.reset(); } } if (Thread.interrupted() || !running.get()) {
	 * pbSolver.reset(); return null; } pbSolver.reset(); if (!seaCells.isEmpty()) {
	 * IVecInt lits = new VecInt(); IVecInt coeffs = new VecInt(); try {
	 * genBinaryConstraints(pbSolver); // Generate the known constraints on the
	 * board int noOfLits = Integer.toBinaryString(seaCells.size()).length(); for
	 * (int i = 0; i < noOfLits; i++) { lits.push(encodeLit(i)); coeffs.push((int)
	 * Math.pow(2, i)); } pbSolver.addAtMost(lits, coeffs, seaCells.size() - 1); //
	 * Find if cell is safe or mine if (!pbSolver.isSatisfiable()) { boolean isMine
	 * = true; for (Cell c : seaCells) { results.put(c, isMine); } }
	 * pbSolver.reset(); genBinaryConstraints(pbSolver); pbSolver.addAtLeast(lits,
	 * coeffs, 1); // Find if cell is safe or mine if (!pbSolver.isSatisfiable()) {
	 * boolean isMine = false; for (Cell c : seaCells) { results.put(c, isMine); } }
	 * pbSolver.reset(); } catch (ContradictionException ce) { } catch
	 * (TimeoutException te) { pbSolver.reset(); } } pbSolver.reset(); if
	 * (Thread.interrupted() || !running.get()) { return null; } return results; }
	 */

}