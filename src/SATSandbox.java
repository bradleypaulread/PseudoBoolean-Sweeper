import java.io.IOException;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class SATSandbox {

	public static void main(String[] args) throws ContradictionException, TimeoutException, IOException {
//		ISolver solver = SolverFactory.newDefault();
//		solver.setTimeout(3600); // 1 hour timeout
//		Reader reader = new DimacsReader(solver);
//		PrintWriter out = new PrintWriter(System.out, true);
//		// CNF filename is given on the command line
//		try {
//			IProblem problem = reader.parseInstance("src/zebra.cnf");
//			if (problem.isSatisfiable()) {
//				System.out.println("Satisfiable !");
//				reader.decode(problem.model(), out);
//				for (int i = 0; i < problem.model().length; ++i) {
//					if (i%10 == 0) System.out.println();
//					System.out.print(problem.model()[i] + ", ");
//				}
//			} else {
//				System.out.println("Unsatisfiable !");
//			}
//		} catch (FileNotFoundException e) {
//		} catch (ParseFormatException e) {
//		} catch (IOException e) {
//		} catch (ContradictionException e) {
//			System.out.println("Unsatisfiable (trivial)!");
//		} catch (TimeoutException e) {
//			System.out.println("Timeout, sorry!");
//		}
		
		DimacToList d = new DimacToList("resources/queens16.cnf");

			final int MAXVAR = d.getMAX_VARS();
			final int NBCLAUSES = d.getMAX_CLAUSES();
			final List<Integer> lits = d.getLits();

			ISolver solver = SolverFactory.newDefault();

			// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
			solver.newVar(MAXVAR);
			solver.setExpectedNumberOfClauses(NBCLAUSES);
			// Feed the solver using Dimacs format, using arrays of int
			// (best option to avoid dependencies on SAT4J IVecInt)

			int start = 0;
			int end = 0;
			for (int i = 0; i < NBCLAUSES; ++i) {

				while (lits.get(end) != 0) {
					++end;
				}

				int[] clause = new int[(end - start)];
				for (int j = 0; j < clause.length; ++j) {
					clause[j] = lits.get(start + j);
				}
				end += 1;
				start = end;

				solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
			}
			IProblem problem = solver;
			if (problem.isSatisfiable()) {
				System.out.println("SATISFIABLE!");
				int r = 1;
				for (int l : problem.model()) {
					if (r++%20 == 0) System.out.println();
					System.out.print(l + ", ");
				}
				System.out.println();
			} else {
				System.out.println("UNSATISFIABLE!");
			}
	}

}
