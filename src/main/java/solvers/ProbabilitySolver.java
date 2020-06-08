package main.java.solvers;

import com.google.common.math.BigIntegerMath;
import main.java.Cell;
import main.java.CellState;
import org.apache.commons.math3.fraction.BigFraction;
import org.sat4j.core.VecInt;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class ProbabilitySolver extends MyPBSolver {

    public ProbabilitySolver(Cell[][] cells, int width, int height, int mines) {
        super(cells, width, height, mines);
    }

    public Cell getBestCell() {
        var probabilities = getProbabilities();
        List<Cell> lowestProbCells = new ArrayList<>();
        BigFraction bestProb = BigFraction.ONE;
        for (var pair : probabilities.entrySet()) {
            Cell cell = pair.getKey();
            BigFraction prob = pair.getValue();
            if (prob.compareTo(bestProb) <= 0) {
                lowestProbCells.clear();
                bestProb = prob;
            }
            lowestProbCells.add(cell);
        }

        if (lowestProbCells.size() == 1) {
            return lowestProbCells.get(0);
        }

        Cell bestStrategicCell = lowestProbCells.get(0);
        int leastUnknownNeighbours = (int) getNeighbours(bestStrategicCell.getX(), bestStrategicCell.getY())
                .stream()
                .filter(c -> c.getState() != CellState.OPEN)
                .filter(c -> c.getState() != CellState.FLAGGED)
                .count();

        for (int i = 1; i < lowestProbCells.size(); i++) {
            Cell cell = lowestProbCells.get(i);
            int unknownNeighbours = (int) getNeighbours(bestStrategicCell.getX(), bestStrategicCell.getY())
                    .stream()
                    .filter(c -> c.getState() != CellState.OPEN)
                    .filter(c -> c.getState() != CellState.FLAGGED)
                    .count();
            if (unknownNeighbours < leastUnknownNeighbours) {
                bestStrategicCell = cell;
                leastUnknownNeighbours = unknownNeighbours;
            }
        }
        return bestStrategicCell;
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

    public Map<Cell, BigFraction> getProbabilities() {
        Map<Cell, BigInteger> cellMineCount = new HashMap<>();
        Map<Cell, BigFraction> probs = new HashMap<>();

        var totalModels = BigInteger.ZERO;
        var totalSeaModels = BigFraction.ZERO;
        var seaSize = getSeaCells().size();

        PBSolver solver = generateBaseConstraints();

        try {
            while (solver.isSatisfiable()) {
                int[] model = solver.model();
                List<Cell> modelShoreMines = Arrays.stream(model)
                        .filter(i -> i >= 0)
                        .mapToObj(i -> decodeCellId(i))
                        .filter(Optional::isPresent)
                        .map(cell -> cell.get())
                        .collect(Collectors.toList());

                int remainingMinesInModel = mines - modelShoreMines.size();

                BigInteger totalPossibleModels = seaSize > 0 ?
                        BigIntegerMath.binomial(seaSize, remainingMinesInModel) :
                        BigInteger.ONE; // One to include the current model

                totalModels = totalModels.add(totalPossibleModels);

                // update mine counts
                for (Cell cell : modelShoreMines) {
                    BigInteger currentCellMineCount = cellMineCount.getOrDefault(cell, BigInteger.ZERO);
                    BigInteger newCellMineCount = currentCellMineCount.add(totalPossibleModels);
                    cellMineCount.put(cell, newCellMineCount);
                }

                // update sea probability
                BigFraction currentModelSeaProb = seaSize > 0 ?
                        new BigFraction(remainingMinesInModel, seaSize) :
                        BigFraction.ZERO;

                totalSeaModels = totalSeaModels.add(currentModelSeaProb.multiply(totalPossibleModels));

                // Remove current solution from possible solutions
                for (int i = 0; i < model.length; i++) {
                    model[i] *= -1;
                }
                IVecInt block = new VecInt(model);
                solver.addBlockingClause(block);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

        solver.reset();

        if (seaSize > 0) {
            BigFraction seaProb = totalSeaModels.divide(totalModels).reduce();
            getSeaCells().forEach(cell -> probs.put(cell, seaProb));
        }

        for (Cell cell : getClosedShoreCells()) {
            BigInteger currentCellMineCount = cellMineCount.getOrDefault(cell, BigInteger.ZERO);
            probs.put(cell, new BigFraction(currentCellMineCount, totalModels).reduce());
        }

        return probs;
    }
}
