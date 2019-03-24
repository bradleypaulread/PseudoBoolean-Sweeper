import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.google.gson.Gson;

public class GameSimulator {

    // File locations for storing simulation results
    private final String PATTERN_PATH = "resources/PatternMatching-Results.csv";
    private final String SAT_PATH = "resources/SAT-Results.csv";
    private final String JOINT_PATH = "resources/Joint-Results.csv";
    private final String FULL_PATH = "resources/Full-Results.csv";

    private int noOfSims;
    private ExecutorService pool;
    private List<List<Minesweeper>> gamesPattern;
    private List<List<Minesweeper>> gamesSAT;
    private List<List<Minesweeper>> gamesJoint;

    private Difficulty[] diffs = { Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT };

    public GameSimulator(int noOfSims) {
        this.noOfSims = noOfSims;
        int noOfThreads = Runtime.getRuntime().availableProcessors();
        pool = Executors.newFixedThreadPool(noOfThreads);
        reset(noOfSims);
        warmup();
    }

    public ExecutorService getPool() {
        return pool;
    }

    private void warmup() {
        int limit = noOfSims;
        System.out.println("WARMING UP...");
        for (int init = 0; init < limit; init++) {
            Minesweeper newGame = new Minesweeper(Difficulty.EXPERT, new MineField(16, 30, 99));
            BoardSolver solver = new BoardSolver(newGame);
            solver.setQuiet();
            while (!newGame.isGameOver()) {
                if (!solver.patternMatch()) {
                    solver.selectRandomCell();
                }
            }
        }
    }

    public void startGenericSim() {
        System.out.println("PATTERN SIM");
        startPatternMatchSim();
        System.out.println("SAT SIM");
        startSATSim();
        System.out.println("JOINT SIM");
        startJointSim();
    }

    public void startPatternMatchSim() {
        for (List<Minesweeper> diff : gamesPattern) {
            for (Minesweeper game : diff) {
                pool.execute(new SolverThreadWrapper(game, true, true, false, false));
            }
        }
    }

    public void startSATSim() {
        for (List<Minesweeper> diff : gamesSAT) {
            for (Minesweeper game : diff) {
                pool.execute(new SolverThreadWrapper(game, true, false, true, false));
            }
        }
    }

    public void startJointSim() {
        for (List<Minesweeper> diff : gamesJoint) {
            for (Minesweeper game : diff) {
                pool.execute(new SolverThreadWrapper(game, true, true, true, false));
            }
        }
    }

    public void startJointStratSim() {

    }

    public void calcResults() {
        int[] wins = new int[3];
        List<List<Long>> times = new ArrayList<>();
        times.add(new ArrayList<Long>());
        times.add(new ArrayList<Long>());
        times.add(new ArrayList<Long>());

        for (int i = 0; i < gamesPattern.size(); i++) {
            for (Minesweeper game : gamesPattern.get(i)) {
                if (game.isGameWon()) {
                    wins[i]++;
                }
            }
        }
        writeResults(wins, times, PATTERN_PATH);
        wins = new int[3];
        for (List<Long> list : times) {
            list.clear();
        }
        for (int i = 0; i < gamesSAT.size(); i++) {
            for (Minesweeper game : gamesSAT.get(i)) {
                if (game.isGameWon()) {
                    wins[i]++;
                }
            }
        }
        writeResults(wins, times, SAT_PATH);
        wins = new int[3];
        for (List<Long> list : times) {
            list.clear();
        }
        for (int i = 0; i < gamesJoint.size(); i++) {
            for (Minesweeper game : gamesJoint.get(i)) {
                if (game.isGameWon()) {
                    wins[i]++;
                }
            }
        }
        writeResults(wins, times, JOINT_PATH);
    }

    private void writeResults(int[] wins, List<List<Long>> times, String path) {

        try (PrintWriter writer = new PrintWriter(path)) {
            StringBuilder sb = new StringBuilder();
            sb.append("difficulty");
            sb.append(',');
            sb.append("no. of games");
            sb.append(',');
            sb.append("wins");
            sb.append(',');
            sb.append("loss");
            sb.append(',');
            sb.append("win/loss (%)");
            sb.append(',');
            sb.append("avg. time (m/s)");
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
                long totalTime = 0;
                for (int j = 0; j < times.get(i).size(); j++) {
                    totalTime += times.get(i).get(j);
                }
                long avgTime = times.get(i).size() > 0 ? totalTime / times.get(i).size() : 0;
                sb.append(avgTime / (long) 1e6); // Convert from nano to miliseconds
                sb.append('\n');
            }

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
            fieldsHard.add(new MineField(16, 30, 99));
        }
        List<List<MineField>> fields = new ArrayList<>();
        fields.add(fieldsEasy);
        fields.add(fieldsMedium);
        fields.add(fieldsHard);
        gamesPattern = new ArrayList<>();
        gamesSAT = new ArrayList<>();
        gamesJoint = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            gamesPattern.add(new ArrayList<Minesweeper>());
            gamesSAT.add(new ArrayList<Minesweeper>());
            gamesJoint.add(new ArrayList<Minesweeper>());
        }
        for (int i = 0; i < fields.size(); i++) {
            for (int j = 0; j < fields.get(i).size(); j++) {
                Gson gson = new Gson();
                gamesPattern.get(i).add(
                        new Minesweeper(diffs[i], gson.fromJson(gson.toJson(fields.get(i).get(j)), MineField.class)));
                gamesSAT.get(i).add(
                        new Minesweeper(diffs[i], gson.fromJson(gson.toJson(fields.get(i).get(j)), MineField.class)));
                gamesJoint.get(i).add(
                        new Minesweeper(diffs[i], gson.fromJson(gson.toJson(fields.get(i).get(j)), MineField.class)));
            }
        }
    }
}