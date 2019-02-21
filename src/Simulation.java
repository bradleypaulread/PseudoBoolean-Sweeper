import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.google.gson.Gson;

public class Simulation {

    int noOfSims;
    List<List<MineField>> fields;

    public Simulation() {
        // fields = new ArrayList<>();
    }

    public Simulation(int noOfSims) {
        this.noOfSims = noOfSims;
        reset(noOfSims);

        // for (int init = 0; init < 25; init++) {
        // for (int fieldDiff = 0; fieldDiff < fields.size(); fieldDiff++) {
        // for (int i = 0; i < fields.get(fieldDiff).size(); i++) {
        // Gson gson = new Gson();
        // MineField mineField =
        // gson.fromJson(gson.toJson(fields.get(fieldDiff).get(i)), MineField.class);
        // Minesweeper game = new Minesweeper(9, 9, 10, mineField);
        // BoardSolver solver = new BoardSolver(game);
        // solver.setQuiet();
        // while (!game.isGameOver()) {
        // if (!solver.assist()) {
        // Cell c = solver.selectRandomCell();
        // game.quietSelect(c.getX(), c.getY());
        // }
        // }
        // }
        // }
        // }
    }

    public void startPatternMatchSim() {
        List<Integer> wins = new ArrayList<>();
        wins.add(0);
        wins.add(0);
        wins.add(0);

        List<List<Long>> times = new ArrayList<>();
        times.add(new ArrayList<Long>());
        times.add(new ArrayList<Long>());
        times.add(new ArrayList<Long>());

        for (int fieldDiff = 0; fieldDiff < fields.size(); fieldDiff++) {
            for (int i = 0; i < fields.get(fieldDiff).size(); i++) {
                Gson gson = new Gson();
                MineField mineField = gson.fromJson(gson.toJson(fields.get(fieldDiff).get(i)), MineField.class);
                Minesweeper game = new Minesweeper(9, 9, 10, mineField);
                BoardSolver solver = new BoardSolver(game);
                solver.setQuiet();
                long start = System.nanoTime();
                while (!game.isGameOver()) {
                    if (!solver.assist()) {
                        Cell c = solver.selectRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }

                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    wins.set(fieldDiff, wins.get(fieldDiff) + 1);
                    times.get(fieldDiff).add(end - start);
                }
            }
        }
        List<Float> avgTimes = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            long total = 0;
            int size = times.get(i).size();
            for (int j = 0; j < size; j++) {
                total += times.get(i).get(j);
            }
            float avg = (total / size);
            avgTimes.add(avg);
        }
        writeResults(wins, avgTimes);
        System.out.println("Finished!");
    }

    public void startSATSim() {

    }

    public void startJointSim() {

    }

    public void startJointStratSim() {

    }

    private void writeResults(List<Integer> wins, List<Float> times) {
        try (PrintWriter writer = new PrintWriter(new File("resources/results.csv"))) {
            System.out.println(times);
            StringBuilder sb = new StringBuilder();
            sb.append("difficulty");
            sb.append(',');
            sb.append("no of games");
            sb.append(',');
            sb.append("wins");
            sb.append(',');
            sb.append("loss");
            sb.append(',');
            sb.append("win/loss (%)");
            sb.append(',');
            sb.append("avg time");
            sb.append(',');
            sb.append('\n');

            for (int i = 0; i < wins.size(); i++) {
                String diff = "";
                if (i == 0) {
                    diff = "EASY";
                } else if (i == 1) {
                    diff = "MEDIUM";
                } else {
                    diff = "HARD";
                }
                sb.append(diff);
                sb.append(',');
                sb.append(noOfSims);
                sb.append(',');
                sb.append(wins.get(i));
                sb.append(',');
                sb.append(noOfSims-wins.get(i));
                sb.append(',');
                double percent = (double)wins.get(i)/(double)noOfSims;
                sb.append(percent);
                sb.append(',');
                sb.append(times.get(i).toString());
                sb.append('\n');
            }

            writer.write(sb.toString());

            System.out.println("done!");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    public void reset(int noOfSims) {
        List<MineField> fieldsEasy = new ArrayList<>(noOfSims);
        for (int i = 0; i < noOfSims; i++) {
            fieldsEasy.add(new MineField(9, 9, 10));
        }
        List<MineField> fieldsMedium = new ArrayList<>(noOfSims);
        for (int i = 0; i < noOfSims; i++) {
            fieldsMedium.add(new MineField(16, 16, 40));
        }
        List<MineField> fieldsHard = new ArrayList<>(noOfSims);
        for (int i = 0; i < noOfSims; i++) {
            fieldsHard.add(new MineField(30, 16, 99));
        }
        fields = new ArrayList<>();
        fields.add(fieldsEasy);
        fields.add(fieldsMedium);
        fields.add(fieldsHard);
    }
}