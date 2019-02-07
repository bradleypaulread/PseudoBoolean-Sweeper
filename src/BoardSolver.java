import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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

	private Minesweeper game;
	private Cell[][] cells;

	public BoardSolver(Minesweeper game) {
		pbSolver = SolverFactory.newDefault();
		this.game = game;
		cells = game.getCells();
	}

	public Map<Cell, Integer> solve() throws ContradictionException, TimeoutException {
		cells = game.getCells();
		pbSolver = SolverFactory.newDefault();
		Map<Cell, Integer> knownCells = new HashMap<Cell, Integer>();
		IVecInt lits = new VecInt();
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				lits.push(encodeCellId(cells[i][j], cells));
				coeffs.push(BigInteger.ONE);
			}
		}
		pbSolver.addAtMost(lits, coeffs, BigInteger.valueOf(game.getNoOfMines()));
		lits.clear();
		coeffs.clear();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isOpen()) {
					List<Cell> neighbours = getNeighbours(cells, i, j);
					lits.clear();
					coeffs.clear();
					
					if (current.getNumber() == 0) {
						lits.push(encodeCellId(current, cells));
						coeffs.push(BigInteger.ONE);
						pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
						lits.clear();
						coeffs.clear();
					}
					
					for (Cell c : neighbours) {
						lits.push(encodeCellId(c, cells));
						coeffs.push(BigInteger.ONE);
					}
					pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
				}
			}
		}

		OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

		if (optimiser.isSatisfiable()) {
			System.out.println("SAT!");
			for (int i : optimiser.model()) {
				int sign = i < 0 ? -1 : 1;
				
				knownCells.put(decodeCellId(i, cells), sign);
			}
		} else {
			System.out.println("NOT SAT!");
		}
		return knownCells;
	}

	public List<Cell> getNeighbours(Cell[][] board, int x, int y) {
		List<Cell> neighbours = new ArrayList<Cell>();
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y + 1; ++j) {
				if (i >= 0 && i < board.length && j >= 0 && j < board[i].length && !(i == x && j == y)) {
					neighbours.add(board[i][j]);
				}
			}
		}
		return neighbours;
	}
	
	/**
	 * When passed a cell and a board, create a unique identifier (a single integer) for that cell.
	 * 
	 * @param c Cell to encode.
	 * @param board Board the cell is present in, used to get the width of the board.
	 * @return Unique integer identifier for given cell.
	 */
	private int encodeCellId(Cell c, Cell[][] board) {
		return (c.getY() * board.length + c.getX()) + 1;
	}
	
	/**
	 * When passed an identity, decode and return the cell it is referring to.
	 * 
	 * @param id Unique encoded identity id.
	 * @param board Board the cell would be present in, used to get the width of the board.
	 * @return Cell that the id refers to.
	 */
	private Cell decodeCellId(int id, Cell[][] board) {
		int posId = id < 0 ? id * -1 : id;
		int x = (posId - 1) % board.length;
		int y = ((posId - 1) - x) / board.length;
		return board[x][y];
	}

}