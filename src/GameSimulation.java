import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.google.gson.Gson;

public class GameSimulation {

    // File locations for storing simulation results
    private final String PATTERN_PATH = "resources/PatternMatching-Results.csv";
    private final String SAT_PATH = "resources/SAT-Results.csv";
    private final String JOINT_PATH = "resources/Joint-Results.csv";
    private final String FULL_PATH = "resources/Full-Results.csv";

    private int noOfSims;
    private List<List<MineField>> fields;

    public GameSimulation() {
        // fields = new ArrayList<>();
    }

    public GameSimulation(int noOfSims) {
        this.noOfSims = noOfSims;
        reset(noOfSims);
        warmup(noOfSims);
    }

    private void warmup(int noOfSims) {
        int limit = 0;
        if (noOfSims < 200) {
            limit = noOfSims;
        } else if (noOfSims >= 200 && noOfSims < 400) {
            limit = noOfSims / (noOfSims / 10);
        } else {
            limit = noOfSims / 200;
        }
        for (int init = 0; init < limit; init++) {
            for (int fieldDiff = 0; fieldDiff < fields.size(); fieldDiff++) {
                for (int i = 0; i < fields.get(fieldDiff).size(); i++) {
                    Gson gson = new Gson();
                    MineField mineField = gson.fromJson(gson.toJson(fields.get(fieldDiff).get(i)), MineField.class);
                    Minesweeper game = new Minesweeper(9, 9, 10, mineField);
                    BoardSolver solver = new BoardSolver(game);
                    solver.setQuiet();
                    while (!game.isGameOver()) {
                        if (!solver.patternMatch()) {
                            Cell c = solver.selectRandomCell();
                            game.quietSelect(c.getX(), c.getY());
                        }
                    }
                }
            }
        }
    }

    public void startPatternMatchSim() {
        int noOfDiff = fields.size();
        int[] wins = new int[noOfDiff];

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
                    if (!solver.patternMatch()) {
                        Cell c = solver.selectRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }

                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    wins[fieldDiff] =  wins[fieldDiff] + 1;
                    times.get(fieldDiff).add(end - start);
                }
            }
        }

        long[] avgTimes = new long[noOfDiff];
        for (int i = 0; i < times.size(); i++) {
            long total = 0;
            int size = times.get(i).size();
            for (int j = 0; j < size; j++) {
                total += times.get(i).get(j);
            }
            long avg = (total / size);
            avgTimes[i] = avg;
        }
        writeResults(wins, avgTimes, PATTERN_PATH);
        System.out.println("Finished!");
    }

    public void startSATSim() {
        int noOfDiff = fields.size();
        int[] wins = new int[noOfDiff];

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
                    if (!solver.patternMatch()) {
                        Cell c = solver.selectRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }

                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    wins[fieldDiff] =  wins[fieldDiff] + 1;
                    times.get(fieldDiff).add(end - start);
                }
            }
        }

        long[] avgTimes = new long[noOfDiff];
        for (int i = 0; i < times.size(); i++) {
            long total = 0;
            int size = times.get(i).size();
            for (int j = 0; j < size; j++) {
                total += times.get(i).get(j);
            }
            long avg = (total / size);
            avgTimes[i] = avg;
        }
        writeResults(wins, avgTimes, SAT_PATH);
        System.out.println("Finished!");
    }

    public void startJointSim() {
        int noOfDiff = fields.size();
        int[] wins = new int[noOfDiff];

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
                    if (!solver.patternMatch()) {
                        Cell c = solver.selectRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }

                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    wins[fieldDiff] =  wins[fieldDiff] + 1;
                    times.get(fieldDiff).add(end - start);
                }
            }
        }

        long[] avgTimes = new long[noOfDiff];
        for (int i = 0; i < times.size(); i++) {
            long total = 0;
            int size = times.get(i).size();
            for (int j = 0; j < size; j++) {
                total += times.get(i).get(j);
            }
            long avg = (total / size);
            avgTimes[i] = avg;
        }
        writeResults(wins, avgTimes, JOINT_PATH);
        System.out.println("Finished!");
    }

    public void startJointStratSim() {

    }

    private void writeResults(int[] wins, long[] times, String path) {
        try (PrintWriter writer = new PrintWriter(new File(path))) {
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

            for (int i = 0; i < wins.length; i++) {
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
                sb.append(wins[i]);
                sb.append(',');
                sb.append(noOfSims - wins[i]);
                sb.append(',');
                double percent = (double) wins[i] / (double) noOfSims;
                sb.append(percent);
                sb.append(',');
                sb.append(times[i]);
                sb.append('\n');
            }
            sb.append("AVG.");
            sb.append(',');
            sb.append(noOfSims);
            sb.append(',');
            int totalWins = 0;
            for (int i : wins) {
                totalWins += i;
            }
            int avgWin = totalWins / wins.length;
            sb.append(avgWin);
            sb.append(',');
            int totalLoss = 0;
            for (int i : wins) {
                totalLoss += noOfSims - i;
            }
            int avgLoss = totalLoss / wins.length;
            sb.append(avgLoss);
            sb.append(',');
            double avgWinPercent = (double) avgWin / (double) noOfSims;
            sb.append(avgWinPercent);
            sb.append(',');
            long totalTime = 0;
            for (long i : times) {
                totalTime += i;
            }
            long avgTime = totalTime / times.length;            
            sb.append(avgTime);
            sb.append(',');
            sb.append('\n');

            writer.write(sb.toString());

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