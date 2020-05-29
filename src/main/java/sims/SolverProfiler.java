package main.java.sims;

import main.java.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolverProfiler {

    private final List<Class> SOLVER_CLASS_LIST = Arrays.asList(
            //        SinglePointSolver.class,
            MyPBSolver.class,
            ProbabilitySolver.class
    );

    public static void main(String[] args) throws NoSuchMethodException {
        new SolverProfiler().startSim(100);
    }

    private void startSim(int howMany) {
        List<boolean[][]> fields = generateRandomFields(howMany, Difficulty.EXPERT);
        List<Double> times = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            MineSweeper game1 = new MineSweeper(Difficulty.EXPERT);
//            MineSweeper game2 = new MineSweeper(9, 9, 10);
            replaceField(game1, fields.get(i));
//            replaceField(game2, fields.get(i));

            MyPBSolver pbSolver = new MyPBSolver(game1.getCells(), game1.getWidth(),
                    game1.getHeight(), game1.getMines());

//            ProbabilitySolver probSolver = new ProbabilitySolver(game2.getCells(),
//                    game2.getWidth(), game2.getHeight(), game2.getMines());

            GamePlayer player1 = new GamePlayer(game1, pbSolver);
//            GamePlayer player2 = new GamePlayer(game2, probSolver);

            player1.play();
//            player2.play();

//            System.out.println("PB\tvs.\tProbability");
//            System.out.println(player1.getElapsedTime() + "\t" + player2.getElapsedTime());
            times.add(player1.getElapsedTime());
        }

        double avg = times.stream()
                .mapToDouble(e -> e)
                .average()
                .getAsDouble();

        System.out.println(avg / 1000000000);
    }

    private void replaceField(MineSweeper game, boolean[][] newField) {
        try {
            Board board = game.getBoard();
            MineField mineField = board.getField();
            Field field = mineField.getClass().getDeclaredField("field");
            field.setAccessible(true);
            field.set(mineField, newField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean[][] deepCopy(boolean[][] matrix) {
        return java.util.Arrays.stream(matrix)
                .map(el -> el.clone())
                .toArray($ -> matrix.clone());
    }

    private List<boolean[][]> generateRandomFields(int howMany,
                                                   Difficulty diff) {
        List<boolean[][]> randomFields = new ArrayList<>();

        for (int i = 0; i < howMany; i++) {
            MineField mineField = new MineField(diff.height, diff.width,
                    diff.mines);
            boolean[][] field = null;
            try {
                Field f = mineField.getClass().getDeclaredField("field");
                f.setAccessible(true);
                field = (boolean[][]) f.get(mineField);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (field != null) {
                randomFields.add(field);
            }
        }
        return randomFields;
    }
}
