import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class PBSolver extends BoardSolver {

    public PBSolver (Minesweeper game) {
        super(game);
    }

    public PBSolver(Minesweeper game, AtomicBoolean running) {
        super(game, running);
    }

    protected void genConstraints(IPBSolver solver) throws ContradictionException {

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
		// printConstraint(lits, coeffs, "=", game.getNoOfMines());
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
			// printConstraint(lits, coeffs, "=", 0);
			lits.clear();
			coeffs.clear();

			List<Cell> neighbours = getNeighbours(current);
			// neighbours.removeIf(c -> !c.isClosed());
			// Normal constraint
			for (Cell c : neighbours) {
				lits.push(encodeCellId(c));
				coeffs.push(1);
			}
			// printConstraint(lits, coeffs, "=", current.getNumber());
			solver.addExactly(lits, coeffs, current.getNumber());
			lits.clear();
			coeffs.clear();
		}
	}

    public boolean hint() {
        cells = game.getCells();
		Map<Cell, Boolean> known = gestKnownCells();
		if (known == null || !running.get()) {
			return false;
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
					return true;
				}
			} else {
				if (current.isBlank()) {
					current.setSafeHint();
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
		Map<Cell, Boolean> known = gestKnownCells();
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
		}
		return change;
    }

    public void solve() {
        while(assist());
    }

    protected Map<Cell, Boolean> gestKnownCells() {
		IPBSolver pbSolver = SolverFactory.newDefault();
		pbSolver.reset();

		cells = game.getCells();
		Map<Cell, Boolean> results = new HashMap<>();
		List<Cell> closedShore = getShoreClosedCells();
		List<Cell> sea = getSeaCells();

		try {
			genConstraints(pbSolver);
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
                        for (Cell c : sea) {
                            results.put(c, isMine);
                        }
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
}