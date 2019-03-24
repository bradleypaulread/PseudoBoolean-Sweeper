import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

/*
 * Main.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

public class LaunchSim {

	private final String EASY_PATH = "resources/easyFields.txt";
	private final String MEDIUM_PATH = "resources/mediumFields.txt";
	private final String HARD_PATH = "resources/hardFields.txt";

	private final String PATTERN_NAME = "PatternMatching-Results.csv";
	private final String SAT_NAME = "SAT-Results.csv";
	private final String JOINT_NAME = "Joint-Results.csv";
	private final String FULL_NAME = "Full-Results.csv";

	private String RESULT_DIR;
	private int noOfGames;
	private StringBuilder resultString;

	public LaunchSim(int noOfSims, String path) throws IOException, InterruptedException {
		this.noOfGames = noOfSims;
		RESULT_DIR = path;
		resultString = new StringBuilder();
	}

	public void startPTSim() {
		try {
			writeTitle();
			System.out.println("Pattern Match Easy");
			playerPatternMatch(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("Pattern Match Medium");
			playerPatternMatch(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("Pattern Match Hard");
			playerPatternMatch(Difficulty.EXPERT, HARD_PATH);
			writeResults(PATTERN_NAME);
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startJointSim() {
		try {
			writeTitle();
			System.out.println("Pattern Match + SAT Easy");
			playerPatternMatchSAT(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("Pattern Match + SAT Medium");
			playerPatternMatchSAT(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("Pattern Match + SAT Hard");
			playerPatternMatchSAT(Difficulty.EXPERT, HARD_PATH);
			writeResults(JOINT_NAME);
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startFullSim() {
		try {
			writeTitle();
			System.out.println("Full Easy");
			playerFull(Difficulty.BEGINNER, EASY_PATH);
			writeResults(FULL_NAME);
			resetResults();
			System.out.println("Full Medium");
			playerFull(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			writeResults(FULL_NAME);
			resetResults();
			System.out.println("Full Hard");
			playerFull(Difficulty.EXPERT, HARD_PATH);
			writeResults(FULL_NAME);
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void playerPatternMatch(Difficulty diff, String path) throws IOException {
		int noOfThreads = Runtime.getRuntime().availableProcessors();

		ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);

		List<GamePlayer> games = new ArrayList<>(noOfGames);
		int limit = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String fieldJson; (fieldJson = br.readLine()) != null && limit < noOfGames;) {
				limit++;
				GamePlayer player = new GamePlayer(diff, fieldJson);
				player.setPatternMatch(true);
				games.add(player);
			}
		}

		for (GamePlayer game : games) {
			pool.execute(game);
		}

		pool.shutdown();

		while (!pool.isTerminated()) {
			System.out.println(pool);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int winCount = 0;
		int guessCount = 0;
		BigInteger winTimes = BigInteger.ZERO;
		for (GamePlayer gameSim : games) {
			if (gameSim.isGameWon()) {
				winCount++;
				guessCount += gameSim.getGuessCount();
				winTimes = winTimes.add(BigInteger.valueOf(gameSim.getElapsedTime()));
			}
		}
		writeLine(diff.toString(), winCount, winTimes, guessCount);
		games.clear();
	}

	public void playerPatternMatchSAT(Difficulty diff, String path) throws IOException {
		int noOfThreads = Runtime.getRuntime().availableProcessors();

		ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);

		List<GamePlayer> games = new ArrayList<>(noOfGames);
		int limit = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String fieldJson; (fieldJson = br.readLine()) != null && limit < noOfGames;) {
				limit++;
				GamePlayer player = new GamePlayer(diff, fieldJson);
				player.setPatternMatch(true);
				player.setSAT(true);
				games.add(player);
			}
		}

		for (GamePlayer game : games) {
			pool.execute(game);
		}

		pool.shutdown();
		int consoleNum = 0;
		while (!pool.isTerminated()) {
			System.out.println(consoleNum++ + " - " + pool);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int winCount = 0;
		int guessCount = 0;
		BigInteger winTimes = BigInteger.ZERO;
		for (GamePlayer gameSim : games) {
			if (gameSim.isGameWon()) {
				winCount++;
				guessCount += gameSim.getGuessCount();
				winTimes = winTimes.add(BigInteger.valueOf(gameSim.getElapsedTime()));
			}
		}
		writeLine(diff.toString(), winCount, winTimes, guessCount);
		games.clear();
	}

	public void playerFull(Difficulty diff, String path) throws IOException {
		int noOfThreads = Runtime.getRuntime().availableProcessors();

		ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);

		List<GamePlayer> games = new ArrayList<>(noOfGames);
		int limit = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String fieldJson; (fieldJson = br.readLine()) != null && limit < noOfGames;) {
				limit++;
				GamePlayer player = new GamePlayer(diff, fieldJson);
				player.setPatternMatch(true);
				player.setSAT(true);
				player.setStrat(true);
				games.add(player);
			}
		}

		for (GamePlayer game : games) {
			pool.execute(game);
		}

		pool.shutdown();
		int consoleNum = 0;
		while (!pool.isTerminated()) {
			System.out.println(consoleNum++ + " - " + pool);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int winCount = 0;
		int guessCount = 0;
		BigInteger winTimes = BigInteger.ZERO;
		for (GamePlayer gameSim : games) {
			if (gameSim.isGameWon()) {
				winCount++;
				guessCount += gameSim.getGuessCount();
				winTimes = winTimes.add(BigInteger.valueOf(gameSim.getElapsedTime()));
			}
		}
		writeLine(diff.toString(), winCount, winTimes, guessCount);
		games.clear();
	}

	public void writeResults(String fileName) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(RESULT_DIR + fileName)) {
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
		resultString.append("win (%)");
		resultString.append(",");
		resultString.append("avg. win time (m/s)");
		resultString.append(",");
		resultString.append("no. of guesses");
		resultString.append(",");
		resultString.append("avg. guesses per win");
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
			Fraction guessAvg = new Fraction(guessCount, winCount);
			resultString.append(guessAvg.doubleValue());
		}
		resultString.append(",");
		resultString.append("\n");
	}

	public void resetResults() {
		resultString = new StringBuilder();
	}

	public void setPath(String path) {
		this.RESULT_DIR = path;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		LaunchSim s = new LaunchSim(10000, "resources/");
		s.startPTSim();

		System.out.println("\n\n\nDONE!!!!!");

		// Gson gson = new Gson();

		// PrintWriter writer;
		// try {
		// writer = new PrintWriter("resources/easyFields.txt", "UTF-8");
		// for (int i = 0; i < 10000; i++) {
		// writer.println(gson.toJson(new MineField(9, 9, 10)));
		// }
		// writer.close();
		// writer = new PrintWriter("resources/mediumFields.txt", "UTF-8");
		// for (int i = 0; i < 10000; i++) {
		// writer.println(gson.toJson(new MineField(16, 16, 40)));
		// }
		// writer.close();
		// writer = new PrintWriter("resources/hardFields.txt", "UTF-8");
		// for (int i = 0; i < 10000; i++) {
		// writer.println(gson.toJson(new MineField(16, 30, 99)));
		// }
		// writer.close();
		// } catch (FileNotFoundException | UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }

		// GameSimulator player = new GameSimulator(Difficulty.BEGINNER,
		// "{\"field\":[[false,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[true,true,false,false,false,false,false,false,false],[true,true,false,false,false,false,false,false,false],[true,true,true,false,false,false,false,false,false],[true,true,true,false,false,false,false,false,false]],\"exploded\":false,\"opened\":false}");
		// new LaunchSim();
	}
}
