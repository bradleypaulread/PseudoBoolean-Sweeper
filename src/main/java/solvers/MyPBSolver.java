package main.java.solvers;

import main.java.Cell;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.util.*;
import java.util.stream.Collectors;

public class MyPBSolver extends AbstractSolver {

    public List<String> constraintLog;

    public MyPBSolver(Cell[][] cells, int width, int height, int mines) {
        super(cells, width, height, mines);
        constraintLog = new ArrayList<>();
    }

    protected void logConstraint(String lits, int value, String ex) {
        this.constraintLog.add("" + lits + " " + ex + " " + value);
    }

    protected PBSolver generateBaseConstraints() {
        constraintLog.clear();
        PBSolver solver = SolverFactory.newDefault();
        try {
            addBoardConstraint(solver);
            addOpenCellConstraint(solver);
        } catch (ContradictionException e) {
        }
        return solver;
    }

    protected void addBoardConstraint(PBSolver solver)
            throws ContradictionException {
        IVecInt lits = new VecInt();
        IVecInt coeffs = new VecInt();

        // Constraint that sum of all cells must be the no.
        // of mines present on the board
        String lit = "";
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell current = cells[i][j];
                lit += current + " + ";
                lits.push(encodeCellId(current));
                coeffs.push(1);
            }
        }
        logConstraint(lit, mines, "<=");
        solver.addAtMost(lits, coeffs, mines);
        logConstraint(lit, mines, ">=");
        solver.addAtLeast(lits, coeffs, mines);
        lits.clear();
        coeffs.clear();
    }

    protected void addOpenCellConstraint(PBSolver solver)
            throws ContradictionException {
        IVecInt lits = new VecInt();
        IVecInt coeffs = new VecInt();

        List<Cell> openCells = getLandCells();

        for (Cell cell : openCells) {
            lits.push(encodeCellId(cell));
            coeffs.push(1);
            String lit = "";
            lit += cell;
            logConstraint(lit, 0, "<=");
            logConstraint(lit, 0, ">=");
            solver.addAtMost(lits, coeffs, 0);
            solver.addAtLeast(lits, coeffs, 0);
            lits.clear();
            coeffs.clear();
            lit = "";

            // Normal constraint
            List<Cell> neighbours = getNeighbours(cell.getX(), cell.getY());
            for (Cell c : neighbours) {
                lit += c + " + ";
                lits.push(encodeCellId(c));
                coeffs.push(1);
            }
            logConstraint(lit, cell.getNumber(), "<=");
            logConstraint(lit, cell.getNumber(), ">=");
            solver.addAtMost(lits, coeffs, cell.getNumber());
            solver.addAtLeast(lits, coeffs, cell.getNumber());
            lits.clear();
            coeffs.clear();
        }
    }

    public Map<Cell, Boolean> getKnownCells() {
        Map<Cell, Boolean> results = new HashMap<>();

        PBSolver solver = generateBaseConstraints();

        List<Cell> shoreCells = getClosedShoreCells();

        // Test all shore cells
        for (Cell cell : shoreCells) {
            for (int weight = 0; weight <= 1; weight++) {
                Optional<Boolean> isMine =
                        checkCellWithWeight(solver, cell, weight);
                if (isMine.isPresent()) {
                    results.put(cell, isMine.get());
                    break;
                }
            }
        }

        // Test a sea cell
        List<Cell> seaCells = getSeaCells();
        if (!seaCells.isEmpty()) {
            // if one sea cell is safe/a mine than all sea cells are safe/a mine
            Cell cell = seaCells.get(0);
            for (int weight = 0; weight <= 1; weight++) {
                Optional<Boolean> isMine =
                        checkCellWithWeight(solver, cell, weight);
                if (isMine.isPresent()) {
                    for (Cell c : seaCells) {
                        results.put(c, isMine.get());
                    }
                    break;
                }
            }
        }

        solver.reset();

        return results;
    }

    private Optional<Boolean> checkCellWithWeight(PBSolver solver, final Cell cell, final int weight) {
        IVecInt lit = new VecInt();
        IVecInt coeff = new VecInt();
        IConstr atMostConstr = null;
        IConstr atLeastConstr = null;

        Optional<Boolean> result = Optional.empty();

        lit.push(encodeCellId(cell));
        coeff.push(1);

        try {
            atMostConstr = solver.addAtMost(lit, coeff, weight);
            atLeastConstr = solver.addAtLeast(lit, coeff, weight);
            if (!solver.isSatisfiable()) {
                boolean isMine = weight != 1;
                result = Optional.of(isMine);
            }
        } catch (ContradictionException e) {
            result = Optional.of(weight != 1);
        } catch (TimeoutException t) {
            t.printStackTrace();
        }

        if (atMostConstr != null) {
            solver.removeConstr(atMostConstr);
        }
        if (atLeastConstr != null) {
            solver.removeConstr(atLeastConstr);
        }

        return result;
    }

    public List<Cell> getMineCells() {
        return getKnownCells().entrySet().stream()
                .filter(entry -> entry.getValue())
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }

    public List<Cell> getSafeCells() {
        return getKnownCells().entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }
}
