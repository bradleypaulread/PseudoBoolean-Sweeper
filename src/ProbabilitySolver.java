import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.math.BigIntegerMath;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import org.apache.commons.math3.fraction.BigFraction;

public class ProbabilitySolver extends BoardSolver {

    public ProbabilitySolver(Minesweeper game) {
        super(game);
    }

    public ProbabilitySolver(Minesweeper game, AtomicBoolean running) {
        super(game, running);
    }

    public void makeBestMove() {
        Map<Cell, BigFraction> probs = calcAllCellsProb();
        if (probs == null) {
            return;
        }
        Cell bestCell = getBestMove(probs);
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

    public Cell getBestMove(Map<Cell, BigFraction> probs) { 
         List<Cell> cells = getBestProbCells(probs);
         if (cells == null) {
             return null;
         }
         Cell bestCell = getBestStratCell(cells);
         if (bestCell == null) {
             return null;
         }
         return bestCell;
    }

    public Cell getBestMove() {
       return getBestMove(calcAllCellsProb());
    }

    private Map<Cell, BigFraction> calcAllCellsProb() {
        IPBSolver pbSolver = SolverFactory.newDefault();
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
                    // System.out.print("" + i + ", ");
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

        List<Cell> bestProbCells = getBestProbCells(probs);
        for (Cell c : bestProbCells) {
            c.setBestCell();
        }
        game.refresh();
    }

    public List<Cell> getBestProbCells(Map<Cell, BigFraction> probs) {
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

    public void genBinaryConstraints(IPBSolver pbSolver) throws ContradictionException {
        cells = game.getCells();
        List<Cell> closedShore = getShoreClosedCells();
        List<Cell> seaCells = getSeaCells();
        int seaSize = seaCells.size();
        List<Cell> landCells = getLandCells();

        int noOfMines = game.getNoOfMines();
        int noOfLits = Integer.toBinaryString(seaCells.size()).length();
        IVecInt lits = new VecInt();
        IVecInt coeffs = new VecInt();

        for (int i = 0; i < noOfLits; i++) {
            int square = (int) Math.pow(2, i);
            lits.push(encodeLit(i));
            coeffs.push(square);
        }
        pbSolver.addAtMost(lits, coeffs, seaSize);

        int presumedMineCount = 0;
        for (int i = 0; i < closedShore.size() && running.get() && !Thread.interrupted(); i++) {
            Cell current = closedShore.get(i);
            lits.push(encodeCellId(current));
            coeffs.push(1);
        }
        pbSolver.addExactly(lits, coeffs, noOfMines - presumedMineCount);

        lits.clear();
        coeffs.clear();

        IVecInt landLits = new VecInt();
        IVecInt landCoeffs = new VecInt();
        // Every open cell is guarenteed to not be a mine (cell=0)
        for (Cell current : landCells) {
            landLits.push(encodeCellId(current));
            landCoeffs.push(1);
            // pbSolver.addExactly(landLits, landCoeffs, 0);
            // landLits.clear();
            // landCoeffs.clear();

            List<Cell> neighbours = getNeighbours(current);
            // neighbours.removeIf(c -> !c.isClosed());
            // if (neighbours.isEmpty()) {
            // continue;
            // }
            for (Cell c : neighbours) {
                lits.push(encodeCellId(c));
                coeffs.push(1);
            }
            pbSolver.addExactly(lits, coeffs, current.getNumber());
            lits.clear();
            coeffs.clear();
        }
        pbSolver.addExactly(landLits, landCoeffs, 0);

        lits.clear();
        coeffs.clear();

        int limit = 0;
        for (Cell c : getShoreOpenCells()) {
            limit += c.getNumber();
        }

        for (Cell c : closedShore) {
            lits.push(encodeCellId(c));
            coeffs.push(1);
        }
        pbSolver.addAtMost(lits, coeffs, limit);
        lits.clear();
        coeffs.clear();
    }
}