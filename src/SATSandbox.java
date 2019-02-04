import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

public class SATSandbox {

	public static void main(String[] args) throws ContradictionException, TimeoutException, IOException {

		// PB Plaything
		
		IPBSolver pbSolver = SolverFactory.newDefault();		
		IVecInt lits = new VecInt();
		lits.push(1);
		lits.push(2);
		lits.push(-3);
		lits.push(-4);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.valueOf(1));
		coeffs.push(BigInteger.valueOf(1));
		coeffs.push(BigInteger.valueOf(1));
		coeffs.push(BigInteger.valueOf(1));
		pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(2));
		
		OptToPBSATAdapter optimizer = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));

		while (optimizer.isSatisfiable()) {
			System.out.println("SATISFIABLE!");
			int[] model = optimizer.model();
			lits.clear();
			for (int i : model) {
				System.out.print("" + i + ", ");
				lits.push(i*-1);
			}
			pbSolver.addExactly(lits, coeffs, BigInteger.valueOf(2));
			optimizer = new OptToPBSATAdapter(new PseudoOptDecorator(pbSolver));
			System.out.println("\n");
		}
		if (!optimizer.isSatisfiable()) {
			System.out.println("test");
		}
		
//		ISolver solver = SolverFactory.newDefault();
//		ModelIterator mi = new ModelIterator(solver);
//		solver.setTimeout(3600); // 1 hour timeout
//		Reader reader = new InstanceReader(mi);
//		// filename is given on the command line
//
//		List<int[]> results = new ArrayList<int[]>();
//		// PBSolverClause pb = new PBSolverClause();
//		try {
//			boolean unsat = true;
//			IProblem problem = reader.parseInstance("resources/queens16.cnf");
//			while (problem.isSatisfiable()) {
//				unsat = false;
//				int[] model = problem.model();
//				// do something with each model
//				results.add(model);
//			}
//			System.out.println("\n\n\nNumber of solutions: " + results.size());
//
//			if (unsat) {
//				// do something for unsat case
//				System.out.println("\n\n\nNumber of solutions: " + results.size());
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (ParseFormatException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ContradictionException e) {
//			System.out.println("Unsatisfiable (trivial)!");
//		} catch (TimeoutException e) {
//			System.out.println("Timeout, sorry!");
//		}

		// ISolver solver = SolverFactory.newDefault();
		// solver.setTimeout(3600); // 1 hour timeout
		// Reader reader = new DimacsReader(solver);
		// PrintWriter out = new PrintWriter(System.out, true);
		// // CNF filename is given on the command line
		// try {
		// IProblem problem = reader.parseInstance("src/zebra.cnf");
		// if (problem.isSatisfiable()) {
		// System.out.println("Satisfiable !");
		// reader.decode(problem.model(), out);
		// for (int i = 0; i < problem.model().length; ++i) {
		// if (i%10 == 0) System.out.println();
		// System.out.print(problem.model()[i] + ", ");
		// }
		// } else {
		// System.out.println("Unsatisfiable !");
		// }
		// } catch (FileNotFoundException e) {
		// } catch (ParseFormatException e) {
		// } catch (IOException e) {
		// } catch (ContradictionException e) {
		// System.out.println("Unsatisfiable (trivial)!");
		// } catch (TimeoutException e) {
		// System.out.println("Timeout, sorry!");
		// }

		// DimacToList d = new DimacToList("resources/zebra.cnf");
		//
		// final int MAXVAR = d.getMAX_VARS();
		// final int NBCLAUSES = d.getMAX_CLAUSES();
		// final List<Integer> lits = d.getLits();
		//
		// ISolver solver = SolverFactory.newDefault();
		//
		// // prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT
		// solving
		// solver.newVar(MAXVAR);
		// solver.setExpectedNumberOfClauses(NBCLAUSES);
		// // Feed the solver using Dimacs format, using arrays of int
		// // (best option to avoid dependencies on SAT4J IVecInt)
		//
		// int start = 0;
		// int end = 0;
		// for (int i = 0; i < NBCLAUSES; ++i) {
		//
		// while (lits.get(end) != 0) {
		// ++end;
		// }
		//
		// int[] clause = new int[(end - start)];
		// for (int j = 0; j < clause.length; ++j) {
		// clause[j] = lits.get(start + j);
		// }
		// end += 1;
		// start = end;
		//
		// solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
		// }
		//
		// IProblem problem = solver;
		// if (problem.isSatisfiable()) {
		// System.out.println("SATISFIABLE!");
		// int r = 1;
		// for (int l : problem.model()) {
		// if (r++ % 20 == 0)
		// System.out.println();
		// System.out.print(l + ", ");
		// }
		// System.out.println();
		// } else {
		// System.out.println("UNSATISFIABLE!");
		// }
		// List<int[]> results = new ArrayList<int[]>();
		// IProblem problem = solver;
		// while (problem.isSatisfiable()) {
		// System.out.println("SATISFIABLE!");
		// results.add(problem.model());
		// }
		// System.out.println("\nTotal valid results: " + results.size());

	}

}
