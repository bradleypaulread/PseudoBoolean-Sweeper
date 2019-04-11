import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import com.google.gson.Gson;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

public class SinglePointSimulator {

    private final String EASY_PATH = "resources/easyFields.txt";
    private final String MEDIUM_PATH = "resources/mediumFields.txt";
    private final String HARD_PATH = "resources/hardFields.txt";

    private String RESULT_PATH;
    private int noOfGames;
    private StringBuilder resultString;

    public SinglePointSimulator(int noOfSims, String path) {
        this.noOfGames = noOfSims;
        RESULT_PATH = path;
        resultString = new StringBuilder();
    }

    public void startFullSim() throws IOException {
        writeTitle();
        sim(Difficulty.BEGINNER, EASY_PATH);
        sim(Difficulty.INTERMEDIATE, MEDIUM_PATH);
        sim(Difficulty.EXPERT, HARD_PATH);
    }

    public void sim(Difficulty diff, String path) throws IOException {
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int gameCount = 0;
            int winCount = 0;
            int guessCount = 0;
            BigInteger winTimes = BigInteger.ZERO;
            for (String strFieldJson; (strFieldJson = br.readLine()) != null && gameCount < noOfGames;) {
                gameCount++;
                MineField mf = gson.fromJson(strFieldJson, MineField.class);
                long start = System.nanoTime();
                Minesweeper game = new Minesweeper(diff, mf);
                SinglePointSolver solver = new SinglePointSolver(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.assist()) {
                        solver.selectRandomCell();
                        guessCount++;
                    }
                }
                if (game.isGameWon()) {
                    long end = System.nanoTime();
                    long gameTime = end - start;
                    winCount++;
                    winTimes = winTimes.add(BigInteger.valueOf(gameTime));
                }
            }
            writeLine(diff.toString(), winCount, winTimes, guessCount);
        }
    }

    public void writeResults() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(RESULT_PATH)) {
            writer.write(resultString.toString());
        }
    }

    public void writeTitle() {
        resultString.append("difficulty");
        resultString.append(",");
        resultString.append("no. of games");
        resultString.append(",");
        resultString.append("wins");
        resultString.append(",");
        resultString.append("loss");
        resultString.append(",");
        resultString.append("win/loss (%)");
        resultString.append(",");
        resultString.append("avg. time (m/s)");
        resultString.append(",");
        resultString.append("no. of guesses");
        resultString.append(",");
        resultString.append("avg. no. of guesses");
        resultString.append(",");
        resultString.append("\n");
    }

    public void writeLine(String diff, int winCount, BigInteger gameTime, int guessCount) {
        resultString.append(diff);
        resultString.append(",");
        resultString.append(noOfGames);
        resultString.append(",");
        resultString.append(winCount);
        resultString.append(",");
        resultString.append(noOfGames - winCount);
        resultString.append(",");
        Fraction winPercent = new Fraction(winCount, noOfGames);
        resultString.append(winPercent.percentageValue());
        resultString.append(",");
        if (winCount == 0) {
            resultString.append("0");
        } else {
            BigFraction avgTime = new BigFraction(gameTime, BigInteger.valueOf(winCount));
            avgTime = avgTime.divide(BigInteger.valueOf(1000000));
            resultString.append(avgTime.doubleValue());
        }
        resultString.append(",");
        resultString.append(guessCount);
        resultString.append(",");
        if (guessCount == 0) {
            resultString.append("0");
        } else {
            Fraction guessAvg = new Fraction(guessCount, noOfGames);
            resultString.append(guessAvg.doubleValue());
        }
        resultString.append(",");
        resultString.append("\n");
    }

    public static void main(String[] args) throws IOException {
        SinglePointSimulator sim = new SinglePointSimulator(10000,  "resources/test.csv");
        sim.startFullSim();
        sim.writeResults();
        System.out.println("DONE!");
    }
}