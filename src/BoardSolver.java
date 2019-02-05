import java.math.BigInteger;
import java.util.List;

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
					lits.push(Integer.parseInt("1"+c.getX()+c.getY()));
					coeffs.push(BigInteger.ONE);
				}
			}
			pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(sourceCell.getNumber()));
		}
		
		
		OptToPBSATAdapter optimizer = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

		if (optimizer.isSatisfiable()) {
			
			System.out.println("SATISFIABLE!");
			int[] model = optimizer.model();
//			lits.clear();
			for (int i : model) {
				String s = Integer.toString(i);
				int x = Integer.parseInt(String.valueOf(s.charAt(s.length()-2)));
				int y = Integer.parseInt(String.valueOf(s.charAt(s.length()-1)));
				
				System.out.println(s);
				if (i > 0 && cells[x][y].isClosed()) {
					System.out.print("FLAG! - ");
					cells[x][y].flag();
				} else if (i < 0 && cells[x][y].isClosed()) {
					System.out.print("SAFE! - ");
					game.select(x, y);
				}
				System.out.println("[" + x + "," + y + "]");
				
//				lits.push(i*-1);
			}
//			pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(2));
//			optimizer = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));
			System.out.println("\n");
			return true;
		}
		if (!optimizer.isSatisfiable()) {
			return false;
		}
		return false;
	}

}
