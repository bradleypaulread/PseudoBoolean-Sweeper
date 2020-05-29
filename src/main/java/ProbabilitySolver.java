package main.java;

import org.sat4j.core.VecInt;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class ProbabilitySolver extends MyPBSolver {
    public ProbabilitySolver(Cell[][] cells, int width, int height, int mines) {
        super(cells, width, height, mines);
    }

    @Override
    protected void addBoardConstraint(PBSolver solver)
            throws ContradictionException {
        int seaSize = getSeaCells().size();
        int noOfLits = Integer.toBinaryString(seaSize).length();
        IVecInt lits = new VecInt();
        IVecInt coeffs = new VecInt();
        String lit = "";
        for (int i = 0; i < noOfLits; i++) {
            int square = (int) Math.pow(2, i);
            lits.push(encodeLit(i));
            coeffs.push(square);
            lit += "" + square + "x" + encodeLit(i) + " + ";
        }
        solver.addAtMost(lits, coeffs, seaSize);
        logConstraint(lit, seaSize, "<=");

        for (Cell cell : getClosedShoreCells()) {
            lits.push(encodeCellId(cell));
            coeffs.push(1);
            lit += "" + cell + " + ";
        }
        solver.addAtMost(lits, coeffs, mines);
        solver.addAtLeast(lits, coeffs, mines);
        logConstraint(lit, mines, "<=");
        logConstraint(lit, mines, ">=");
    }
}
