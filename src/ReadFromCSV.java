import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

public class ReadFromCSV {

    private StringBuilder resultString;

    private Difficulty diff;
    private int noOfGames;
    private int wins;
    private int loss;
    private double winTimes;
    private int guessCount;
    private double avgWinGuessCount;
    private double totalElapsedTime;

    private String path;
    private String name;
    private int fileEntries;

    public ReadFromCSV(String path, String name, int fileEntries) {
        this.path = path;
        this.name = name;
        this.fileEntries = fileEntries;
    }

    public void readResults() {
        for (int i = 0; i < (10000 / fileEntries); i++) {
            String file = path + "from" + (i * fileEntries) + name;
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(file));

                String line;
                br.readLine();
                line = br.readLine();

                String[] row = line.split(",");
                diff = Difficulty.valueOf(row[0]);
                noOfGames += Integer.parseInt(row[1]);
                wins += Integer.parseInt(row[2]);
                loss += Integer.parseInt(row[3]);
                winTimes += Double.parseDouble(row[5]);
                guessCount += Integer.parseInt(row[6]);
                avgWinGuessCount += Double.parseDouble(row[7]);
                totalElapsedTime += Double.parseDouble(row[8]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void writeTheTing() {
        System.out.println(diff);
        System.out.println(noOfGames);
        System.out.println(wins);
        System.out.println(loss);
        Fraction winPercent = new Fraction(wins, noOfGames);
        System.out.println(((double)((double)wins / (double)noOfGames))*100);
        System.out.println(winTimes / (10000/fileEntries));
        System.out.println(guessCount);
        Fraction avgWinGuessCount_transposed = new Fraction(avgWinGuessCount, fileEntries);
        System.out.println(avgWinGuessCount / (double) 20);
        System.out.println(totalElapsedTime);

    }

    public static void main(String[] args) {
        ReadFromCSV data = new ReadFromCSV("resources/SPPB/Hard/", "3Joint-Results.csv", 500);
        data.readResults();
        data.writeTheTing();
    }

}

/*
 * resultString.append("difficulty"); resultString.append("no. of games");
 * resultString.append("wins"); resultString.append("loss");
 * resultString.append("win (%)"); resultString.append("avg. win time (m/s)");
 * resultString.append("no. of guesses");
 * resultString.append("avg. guesses in win");
 * resultString.append("total elapsed time (ms)");
 */