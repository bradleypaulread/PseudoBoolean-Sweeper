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

	public BoardSolver() {
		pbSolver = SolverFactory.newDefault();
	}

	public BoardSolver(Minesweeper game) {
		pbSolver = SolverFactory.newDefault();
		this.game = game;
		cells = game.getCells();
	}

	public boolean solve() throws ContradictionException, TimeoutException {
		// Update cell array
		cells = game.getCells();

		List<Cell> openCells = game.getAllOpenCells();
		// For every open cell
		for (Cell sourceCell : openCells) {
			IVecInt lits = new VecInt();
			IVec<BigInteger> coeffs = new Vec<BigInteger>();

			List<Cell> neighbours = game.getNeighbours(sourceCell.getX(), sourceCell.getY());
			// For every neighbouring closed cell create Pseudo Boolean Constraint (PBC)
			for (Cell c : neighbours) {
				if (c.isClosed()) {
					lits.push(Integer.parseInt("1" + c.getX() + c.getY()));
					coeffs.push(BigInteger.ONE);
				}
			}
			pbSolver.addAtMost(lits, coeffs, BigInteger.valueOf(sourceCell.getNumber()));
		}

		OptToPBSATAdapter optimizer = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

		if (optimizer.isSatisfiable()) {

			System.out.println("SATISFIABLE!");
			int[] model = optimizer.model();
			// lits.clear();
			for (int i : model) {
				String s = Integer.toString(i);
				int x = Integer.parseInt(String.valueOf(s.charAt(s.length() - 2)));
				int y = Integer.parseInt(String.valueOf(s.charAt(s.length() - 1)));

				System.out.println(s);
				if (i > 0 && cells[x][y].isClosed()) {
					System.out.print("FLAG! - ");
					cells[x][y].flag();
				} else if (i < 0 && cells[x][y].isClosed()) {
					System.out.print("SAFE! - ");
					game.select(x, y);
				}
				System.out.println("[" + x + "," + y + "]");

				// lits.push(i*-1);
			}
			// pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(2));
			// optimizer = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));
			System.out.println("\n");
			return true;
		}
		if (!optimizer.isSatisfiable()) {
			return false;
		}
		return false;
	}

	public Map<Cell, Integer> solve(Cell[][] board) throws ContradictionException, TimeoutException {
		Map<Cell, Integer> knownCells = new HashMap<Cell, Integer>();

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				Cell current = board[i][j];
				if (current.isOpen()) {
					List<Cell> neighbours = getNeighbours(board, i, j);
					IVecInt lits = new VecInt();
					IVec<BigInteger> coeffs = new Vec<BigInteger>();
					for (Cell c : neighbours) {
						lits.push((c.getY() * board.length + c.getX())+1);
						coeffs.push(BigInteger.ONE);
						// id = y * WIDTH + x;
					}
					//int num = current.isClosed() ? 8 : current.getNumber();
					pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(current.getNumber()));
				}
			}
		}

		OptToPBSATAdapter optimiser = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

		if (optimiser.isSatisfiable()) {
			System.out.println("SAT!");
			for (int i : optimiser.model()) {
				int num = i < 0 ? i * -1 : i;
				int sign = i < 0 ? -1 : 1;
				int x = (num-1) % board.length;
				int y = ((num-1) - x) / board.length;
				System.out.println("" + x + "," + y);
				knownCells.put(board[x][y], sign);
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

}