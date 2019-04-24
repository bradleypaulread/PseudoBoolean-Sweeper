import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.gson.Gson;

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

public class StrategySimulator {

	private final String EASY_PATH = "resources/easyFields.txt";
	private final String MEDIUM_PATH = "resources/mediumFields.txt";
	private final String HARD_PATH = "resources/hardFields.txt";

	private final String PATTERN_NAME = "PatternMatching-Results.csv";
	private final String PATTERN_NAME_FIRSTGUESS = "PatternMatchingFirstGuess-Results.csv";
	private final String PB_NAME = "SAT-Results.csv";
	private final String PB_NAME_FIRSTGUESS = "SATFirstGuess-Results.csv";
	private final String JOINT_NAME = "Joint-Results.csv";
	private final String JOINT_NAME_FIRSTGUESS = "JointFirstGuess-Results.csv";
	private final String FULL_NAME = "Full-Results.csv";
	private int startLine;

	private String RESULT_DIR;
	private int noOfGames;
	private StringBuilder resultString;
	private boolean firstGuess;

	private int winCount;
	private int guessCount;
	private int winGuessCount;
	private BigInteger winTimes = BigInteger.ZERO;
	private BigInteger totalTime = BigInteger.ZERO;

	public StrategySimulator(int noOfSims, String path) throws IOException, InterruptedException {
		this.noOfGames = noOfSims;
		RESULT_DIR = path;
		resultString = new StringBuilder();
		firstGuess = false;
		this.startLine = 0;
	}

	public StrategySimulator(int noOfSims, String path, int startLine) throws IOException, InterruptedException {
		this.noOfGames = noOfSims;
		RESULT_DIR = path;
		resultString = new StringBuilder();
		firstGuess = false;
		this.startLine = startLine;
	}

	public void resetScores() {
		winCount = 0;
		guessCount = 0;
		winGuessCount = 0;
		totalTime = BigInteger.ZERO;
		winTimes = BigInteger.ZERO;
	}

	// Start a simulation of games being solved using SinglePoint algorithm
	// No need to perfrom multithreading as quick enough asynchronous
	public void startSPSim() {
		try {
			writeTitle();
			System.out.println("Pattern Match Easy");
			playSinglePoint(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("Pattern Match Medium");
			playSinglePoint(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("Pattern Match Hard");
			playSinglePoint(Difficulty.EXPERT, HARD_PATH);
			writeResults(PATTERN_NAME);
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Start a simulation of games being solved using SinglePoint and FirstGuess
	// algorithm
	// No need to perfrom multithreading as quick enough asynchronous
	public void startSPFirstGuessSim() {
		try {
			this.firstGuess = true;
			writeTitle();
			System.out.println("Pattern Match (FG) Easy");
			playSinglePoint(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("Pattern Match (FG) Medium");
			playSinglePoint(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("Pattern Match (FG) Hard");
			playSinglePoint(Difficulty.EXPERT, HARD_PATH);
			writeResults(PATTERN_NAME_FIRSTGUESS);
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Start a simulation of games being solved using PB algorithm
	public void startPBSim() {
		try {
			writeTitle();
			System.out.println("PB Easy");
			playPB(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("PB Medium");
			playPB(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("PB Hard");
			playPB(Difficulty.EXPERT, HARD_PATH);
			resetResults();
			resetScores();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Start a simulation of games being solved using PB and FirstGuess algorithm
	public void startPBFirstGuessSim() {
		this.firstGuess = true;
		try {
			writeTitle();
			System.out.println("PB Easy (FG)");
			playPB(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("PB Medium (FG)");
			playPB(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("PB Hard (FG)");
			playPB(Difficulty.EXPERT, HARD_PATH);
			writeResults(PB_NAME_FIRSTGUESS);
			resetResults();
			resetScores();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Start a simulation of games being solved using SinglePoint and PB algorithm
	public void startJointSim() {
		try {
			writeTitle();
			System.out.println("SP+PB Easy");
			playSinglePointPB(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("SP+PB Medium");
			playSinglePointPB(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("SP+PB Hard");
			playSinglePointPB(Difficulty.EXPERT, HARD_PATH);
			writeResults(JOINT_NAME);
			resetScores();
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Start a simulation of games being solved using SinglePoint, PB and FirstGuess
	// algorithm
	public void startJointFirstGuessSim() {
		this.firstGuess = true;
		try {
			writeTitle();
			System.out.println("SP+PB Easy (FG)");
			playSinglePointPB(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("SP+PB Medium (FG)");
			playSinglePointPB(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("SP+PB Hard (FG)");
			playSinglePointPB(Difficulty.EXPERT, HARD_PATH);
			writeResults(JOINT_NAME_FIRSTGUESS);
			resetScores();
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startFullSim() {
		try {
			writeTitle();
			System.out.println("SP+PB+Strat Easy");
			playFull(Difficulty.BEGINNER, EASY_PATH);
			System.out.println("SP+PB+Strat Medium");
			playFull(Difficulty.INTERMEDIATE, MEDIUM_PATH);
			System.out.println("SP+PB+Strat Hard");
			playFull(Difficulty.EXPERT, HARD_PATH);
			writeResults(FULL_NAME);
			resetResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void playSinglePoint(Difficulty diff, String path) throws IOException {
		int winCount = 0;
		BigInteger winTimes = BigInteger.ZERO;
		int guessCount = 0;
		int winGuessCount = 0;
		BigInteger totalTime = BigInteger.ZERO;
		int gamesLostOnFirstMove = 0;

		int count = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String fieldJson; (fieldJson = br.readLine()) != null && count < noOfGames;) {
				int currentGuessCount = 0;
				SinglePointSolver sp;
				Minesweeper game;
				long startTime;
				if (firstGuess) {
					boolean opening = false;
					MineField mineField = new Gson().fromJson(fieldJson, MineField.class);
					game = new Minesweeper(diff, mineField);
					sp = new SinglePointSolver(game);
					sp.setQuiet();
					startTime = System.nanoTime();
					opening = sp.makeFirstGuess();
					while (!opening && !game.isGameOver()) {
						opening = sp.makeFirstGuess();
						currentGuessCount++;
					}
				} else {
					do {
						MineField mineField = new Gson().fromJson(fieldJson, MineField.class);
						game = new Minesweeper(diff, mineField);
						sp = new SinglePointSolver(game);
						sp.setQuiet();
						startTime = System.nanoTime();
						sp.selectRandomCell();
					} while (game.isGameOver());
				}
				currentGuessCount++; // First move
				while (!game.isGameOver()) {
					if (!sp.assist()) {
						sp.selectRandomCell();
						currentGuessCount++;
					}
				}
				long endTime = System.nanoTime();
				long elapsedTime = endTime - startTime;

				if (game.getNoOfMoves() == 1 && !game.isGameWon()) {
					gamesLostOnFirstMove++;
				} else {
					totalTime = totalTime.add(BigInteger.valueOf(elapsedTime));
					guessCount += currentGuessCount;
					if (game.isGameWon()) {
						winCount++;
						winGuessCount += currentGuessCount;
						winTimes = winTimes.add(BigInteger.valueOf(elapsedTime));
					}
				}
				count++;
				System.out.println(count);
			}
		}
		if (firstGuess) {
			writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime, gamesLostOnFirstMove);
		} else {
			writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime);
		}
	}

	public void playPB(Difficulty diff, String path) throws IOException {
		int gamesLostOnFirstMove = 0;
		int noOfThreads = Runtime.getRuntime().availableProcessors();
		int batch;
		batch = calcPoolSize(diff);
		List<GamePlayer> games = new ArrayList<>();
		int lineCount = 0;
		while (lineCount < noOfGames) {
			// ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);
			ExecutorService pool = Executors.newCachedThreadPool();

			try (BufferedReader br = new BufferedReader(new FileReader(path))) {

				Stream<String> fields = br.lines().skip(lineCount + startLine);
				int limit = (batch > (noOfGames - lineCount)) ? (noOfGames - lineCount) : batch;
				fields = fields.limit(limit);
				Iterator<String> it = fields.iterator();
				while (it.hasNext()) {
					String fieldJson = it.next();
					lineCount++;
					GamePlayer player = new GamePlayer(diff, fieldJson);
					player.setPB(true);
					player.setFirstGuess(firstGuess);
					games.add(player);
				}
			}

			for (GamePlayer game : games) {
				pool.execute(game);
			}

			pool.shutdown();
			int print = 0;
			while (!pool.isTerminated()) {
				if (print % 20 == 0)
					System.out.println(pool);
				print++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			for (GamePlayer gameSim : games) {
				if (gameSim.getGame().getNoOfMoves() == 1 && !gameSim.isGameWon()) {
					gamesLostOnFirstMove++;
				} else {
					totalTime = totalTime.add(BigInteger.valueOf(gameSim.getElapsedTime()));
					guessCount += gameSim.getGuessCount();
					if (gameSim.isGameWon()) {
						winCount++;
						winGuessCount += gameSim.getGuessCount();
						winTimes = winTimes.add(BigInteger.valueOf(gameSim.getElapsedTime()));
					}
				}
			}
			games.clear();
		}
		if (firstGuess) {
			writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime, gamesLostOnFirstMove);
		} else {
			writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime);
		}
	}

	public void playSinglePointPB(Difficulty diff, String path) throws IOException {
		int gamesLostOnFirstMove = 0;
		int noOfThreads = Runtime.getRuntime().availableProcessors();
		int batch;
		batch = calcPoolSize(diff);
		List<GamePlayer> games = new ArrayList<>();
		int lineCount = 0;
		while (lineCount < noOfGames) {
			// ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);
			ExecutorService pool = Executors.newCachedThreadPool();

			try (BufferedReader br = new BufferedReader(new FileReader(path))) {
				Stream<String> fields = br.lines().skip(lineCount + startLine);
				int limit = (batch > (noOfGames - lineCount)) ? (noOfGames - lineCount) : batch;
				fields = fields.limit(limit);
				Iterator<String> it = fields.iterator();
				while (it.hasNext()) {
					String fieldJson = it.next();
					lineCount++;
					GamePlayer player = new GamePlayer(diff, fieldJson);
					player.setSinglePoint(true);
					player.setPB(true);
					player.setFirstGuess(firstGuess);
					games.add(player);
				}
			}

			for (GamePlayer game : games) {
				pool.execute(game);
			}

			pool.shutdown();
			int print = 0;
			while (!pool.isTerminated()) {
				if (print % 20 == 0)
					System.out.println(pool);
				print++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			for (GamePlayer gameSim : games) {
				if (gameSim.getGame().getNoOfMoves() == 1 && !gameSim.isGameWon()) {
					gamesLostOnFirstMove++;
				} else {
					totalTime = totalTime.add(BigInteger.valueOf(gameSim.getElapsedTime()));
					guessCount += gameSim.getGuessCount();
					if (gameSim.isGameWon()) {
						winCount++;
						winGuessCount += gameSim.getGuessCount();
						winTimes = winTimes.add(BigInteger.valueOf(gameSim.getElapsedTime()));
					}
				}
			}
			games.clear();
		}
		writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime, gamesLostOnFirstMove);
	}

	public void playFull(Difficulty diff, String path) throws IOException {
		int gamesLostOnFirstMove = 0;
		int noOfThreads = Runtime.getRuntime().availableProcessors();
		int batch;
		batch = calcPoolSize(diff);
		List<GamePlayer> games = new ArrayList<>();
		int lineCount = 0;
		while (lineCount < noOfGames) {
			// ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);
			ExecutorService pool = Executors.newCachedThreadPool();

			try (BufferedReader br = new BufferedReader(new FileReader(path))) {
				Stream<String> fields = br.lines().skip(lineCount + this.startLine);
				int limit = (batch > (noOfGames - lineCount)) ? (noOfGames - lineCount) : batch;
				fields = fields.limit(limit);
				Iterator<String> it = fields.iterator();
				while (it.hasNext()) {
					String fieldJson = it.next();
					lineCount++;
					GamePlayer player = new GamePlayer(diff, fieldJson);
					player.setSinglePoint(true);
					player.setPB(true);
					player.setStrat(true);
					player.setFirstGuess(firstGuess);
					games.add(player);
				}
			}

			for (GamePlayer game : games) {
				pool.execute(game);
			}

			pool.shutdown();
			int print = 0;
			while (!pool.isTerminated()) {
				if (print % 20 == 0)
					System.out.println(pool);
				print++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (GamePlayer gameSim : games) {
				if (gameSim.getGame().getNoOfMoves() == 1 && !gameSim.isGameWon()) {
					gamesLostOnFirstMove++;
				} else {
					totalTime = totalTime.add(BigInteger.valueOf(gameSim.getElapsedTime()));
					guessCount += gameSim.getGuessCount();
					if (gameSim.isGameWon()) {
						winCount++;
						winGuessCount += gameSim.getGuessCount();
						winTimes = winTimes.add(BigInteger.valueOf(gameSim.getElapsedTime()));
					}
				}
			}
			games.clear();
		}
		if (firstGuess) {
			writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime, gamesLostOnFirstMove);
		} else {
			writeLine(diff.toString(), winCount, winTimes, guessCount, winGuessCount, totalTime);
		}
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
		resultString.append("avg. guesses in win");
		resultString.append(",");
		resultString.append("total elapsed time (ms)");
		resultString.append(",");
		resultString.append("\n");
	}

	public void writeLine(String diff, int winCount, BigInteger gameTime, int guessCount, int winGuessCount,
			BigInteger totalElapsedTime) {
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
			// Convert from nano to ms
			avgTime = avgTime.divide(BigInteger.valueOf(1000000));
			resultString.append(avgTime.doubleValue());
		}
		resultString.append(",");
		resultString.append(guessCount);
		resultString.append(",");
		if (winGuessCount == 0) {
			resultString.append("0");
		} else {
			Fraction avgWinGuesses = new Fraction(winGuessCount, winCount);
			resultString.append(avgWinGuesses.doubleValue());
		}
		resultString.append(",");
		if (winCount == 0) {
			resultString.append("0");
		} else {
			// Convert from nano to ms
			BigFraction elapsedTime = new BigFraction(totalElapsedTime, BigInteger.valueOf(1000000));
			resultString.append(elapsedTime.doubleValue());
		}
		resultString.append(",");
		resultString.append("\n");
	}

	public void writeLine(String diff, int winCount, BigInteger gameTime, int guessCount, int winGuessCount,
			BigInteger totalElapsedTime, int gamesLostOnFirstMove) {
		resultString.append(diff);
		resultString.append(",");
		int playedGames = noOfGames - gamesLostOnFirstMove;
		resultString.append(playedGames);
		resultString.append(",");
		resultString.append(winCount);
		resultString.append(",");
		resultString.append(playedGames - winCount);
		resultString.append(",");
		if (winCount == 0) {
			resultString.append("0");
		} else {
			Fraction winPercent = new Fraction(winCount, playedGames);
			resultString.append(winPercent.percentageValue());
		}
		resultString.append(",");
		if (winCount == 0) {
			resultString.append("0");
		} else {
			BigFraction avgTime = new BigFraction(gameTime, BigInteger.valueOf(winCount));
			// Convert from nano to ms
			avgTime = avgTime.divide(BigInteger.valueOf(1000000));
			resultString.append(avgTime.doubleValue());
		}
		resultString.append(",");
		resultString.append(guessCount);
		resultString.append(",");
		if (winGuessCount == 0) {
			resultString.append("0");
		} else {
			Fraction avgWinGuesses = new Fraction(winGuessCount, winCount);
			resultString.append(avgWinGuesses.doubleValue());
		}
		resultString.append(",");
		if (winCount == 0) {
			resultString.append("0");
		} else {
			// Convert from nano to ms
			BigFraction elapsedTime = new BigFraction(totalElapsedTime, BigInteger.valueOf(1000000));
			resultString.append(elapsedTime.doubleValue());
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

	private int calcPoolSize(Difficulty diff) {
		int batch;
		switch (diff) {
		case BEGINNER:
			batch = 500;
			break;
		case INTERMEDIATE:
			batch = 200;
			break;
		case EXPERT:
			batch = Runtime.getRuntime().availableProcessors();
			break;
		default:
			batch = Runtime.getRuntime().availableProcessors();
			break;
		}
		return batch;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// Example usage
		StrategySimulator s = new StrategySimulator(10000, "resources/PB/");
		s.startPBFirstGuessSim();
	}
}