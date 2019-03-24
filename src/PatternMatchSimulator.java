import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

public class PatternMatchSimulator {

    private final String EASY_PATH = "resources/easyFields.txt";
    private final String MEDIUM_PATH = "resources/mediumFields.txt";
    private final String HARD_PATH = "resources/hardFields.txt";

    private String RESULT_PATH;
    private int noOfGames;
    private StringBuilder resultString;

    public PatternMatchSimulator(int noOfSims, String path) {
        this.noOfGames = noOfSims;
        RESULT_PATH = path;
        resultString = new StringBuilder();
    }

    public void start() throws IOException {
        writeTitle();
        easySim();
        medSim();
        hardSim();
    }

    public void easySim() throws IOException {
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader(EASY_PATH))) {
            int count = 0;
            int winCount = 0;
            BigInteger winTimes = BigInteger.ZERO;
            for (String mineFieldStr; (mineFieldStr = br.readLine()) != null && count < noOfGames;) {
                count++;
                MineField mf = gson.fromJson(mineFieldStr, MineField.class);
                long start = System.nanoTime();
                Minesweeper game = new Minesweeper(Difficulty.BEGINNER, mf);
                BoardSolver solver = new BoardSolver(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch()) {
                        solver.selectRandomCell();
                    }
                }
                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    long gameTime = end - start;
                    winCount++;
                    winTimes = winTimes.add(BigInteger.valueOf(gameTime));
                }
            }
            writeLine("Beginner", winCount, winTimes);
        }
    }

    public void medSim() throws IOException {
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader(MEDIUM_PATH))) {
            int count = 0;
            int winCount = 0;
            BigInteger winTimes = BigInteger.ZERO;
            for (String mineFieldStr; (mineFieldStr = br.readLine()) != null && count < noOfGames;) {
                count++;
                MineField mf = gson.fromJson(mineFieldStr, MineField.class);
                long start = System.nanoTime();
                Minesweeper game = new Minesweeper(Difficulty.INTERMEDIATE, mf);
                BoardSolver solver = new BoardSolver(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch()) {
                        solver.selectRandomCell();
                    }
                }
                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    long gameTime = end - start;
                    winCount++;
                    winTimes = winTimes.add(BigInteger.valueOf(gameTime));
                }
            }
            writeLine("Medium", winCount, winTimes);
        }
    }

    public void hardSim() throws IOException {
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader(HARD_PATH))) {
            int count = 0;
            int winCount = 0;
            BigInteger winTimes = BigInteger.ZERO;
            for (String mineFieldStr; (mineFieldStr = br.readLine()) != null && count < noOfGames;) {
                count++;
                MineField mf = gson.fromJson(mineFieldStr, MineField.class);
                long start = System.nanoTime();
                Minesweeper game = new Minesweeper(Difficulty.EXPERT, mf);
                BoardSolver solver = new BoardSolver(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch()) {
                        solver.selectRandomCell();
                    }
                }
                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    long gameTime = end - start;
                    winCount++;
                    winTimes = winTimes.add(BigInteger.valueOf(gameTime));
                }
            }
            writeLine("Expert", winCount, winTimes);
        }
    }

    public void writeResults() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(RESULT_PATH)) {
            writer.write(resultString.toString());
        }
    }

    public void writeTitle() {
        resultString.append("difficulty");
        resultString.append(',');
        resultString.append("no. of games");
        resultString.append(',');
        resultString.append("wins");
        resultString.append(',');
        resultString.append("loss");
        resultString.append(',');
        resultString.append("win/loss (%)");
        resultString.append(',');
        resultString.append("avg. time (m/s)");
        resultString.append(',');
        resultString.append('\n');
    }

    public void writeLine(String diff, int winCount, BigInteger gameTime) {
        resultString.append(diff);
        resultString.append(",");
        resultString.append(noOfGames);
        resultString.append(",");
        resultString.append(winCount);
        resultString.append(",");
        resultString.append(noOfGames - winCount);
        resultString.append(",");
        Fraction winPercent = new Fraction(winCount, noOfGames);
        resultString.append(winPercent.doubleValue() * 100);
        resultString.append(",");
        if (winCount == 0) {
            resultString.append("0");
        } else {
            BigFraction avgTime = new BigFraction(gameTime, BigInteger.valueOf(winCount));
            avgTime = avgTime.divide(BigInteger.valueOf(1000000));
            resultString.append(avgTime.doubleValue());
        }
        resultString.append(",");
        resultString.append("\n");
    }

    public static void main(String[] args) throws IOException {
        PatternMatchSimulator sim = new PatternMatchSimulator(10000,  "resources/test2.csv");
        sim.start();
        sim.writeResults();
        sim = new PatternMatchSimulator(10000,  "resources/test3.csv");
        sim.start();
        sim.writeResults();
        System.out.println("DONE!");
    }
}