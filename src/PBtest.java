import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PBtest {

    public static void main(String[] args) {

        /*0  1  2 
        0{?, ?, ?}
        1{2, 2, 0}
        2{?, ?, ?}
        */
        Cell[][] one = new Cell[3][3];

        for (int i = 0; i < one.length; i++) {
            for (int j = 0; j < one[i].length; j++) {
                one[i][j] = new Cell(i, j);
            }
        }
        
        one[0][1].open();
        one[0][1].setNumber(2);
        one[1][1].open();
        one[1][1].setNumber(2);
        one[2][1].open();
        one[2][1].setNumber(0);

        /*0  1  2  3
        0{0, 2, ?, ?}
        1{1, 3, ?, ?}
        2{?, 2, 1, 1}
        */
        Cell[][] two = new Cell[4][3];

        for (int i = 0; i < two.length; i++) {
            for (int j = 0; j < two[i].length; j++) {
                two[i][j] = new Cell(i, j);
            }
        }
        two[0][0].setNumber(0);
        two[0][0].open();

        two[0][1].setNumber(1);
        two[0][1].open();

        two[1][0].setNumber(2);
        two[1][0].open();

        two[1][1].setNumber(3);
        two[1][1].open();

        two[1][2].setNumber(2);
        two[1][2].open();

        two[2][2].setNumber(1);
        two[2][2].open();

        two[3][2].setNumber(1);
        two[3][2].open();

        try {
            BoardSolver b = new BoardSolver();

            Map<Cell, Integer> result = b.solve(two);

            System.out.println(result);

        } catch (ContradictionException | TimeoutException e1) {
        }
    }

}