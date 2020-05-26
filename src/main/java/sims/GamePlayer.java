//package main.java.sims;
//
//import com.google.gson.Gson;
//import main.java.*;
//
///**
// * A class that plays a single game following a defined strategy. Stores start
// * time, end time, no. of guesses etc.
// *
// * @author Bradley Read
// * @version 1.0
// * @since 2019-02-25
// */
//public class GamePlayer implements Runnable {
//
//	private final Difficulty gameDifficulty;
//	private final String mineFieldBackup;
//
//	private Minesweeper game;
//
//	private long startTime;
//	private long endTime;
//
//	private boolean gameWon;
//	private int guessCount;
//
//	private boolean singlePoint, PB, strat, firstGuess;
//
//	public GamePlayer(Difficulty diff, String fieldJson) {
//		gameDifficulty = diff;
//
//		reset();
//
//		Gson gson = new Gson();
//
//		MineField mineField = gson.fromJson(fieldJson, MineField.class);
//		mineFieldBackup = fieldJson;
//
//		game = new Minesweeper(gameDifficulty, mineField);
//	}
//
//	@Override
//	public void run() {
//		if (singlePoint && PB && strat) {
//			fullSolve();
//			return;
//		} else if (singlePoint && PB) {
//			jointSolve();
//			return;
//		} else if (singlePoint) {
//			singlePointSolve();
//			return;
//		} else if (PB) {
//			PBSolve();
//			return;
//		} else {
//			System.err.println("No sim specs configured.");
//			return;
//		}
//	}
//
//	private void singlePointSolve() {
//		SinglePointSolver sp;
//
//		if (firstGuess) {
//			boolean opening = false;
//			do {
//				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//				game = new Minesweeper(gameDifficulty, mineField);
//				sp = new SinglePointSolver(game);
//				sp.setQuiet(true);
//				opening = sp.makeFirstGuess();
//				startTime = System.nanoTime();
//			} while (!opening);
//		} else {
//			do {
//				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//				game = new Minesweeper(gameDifficulty, mineField);
//				sp = new SinglePointSolver(game);
//				sp.setQuiet(true);
//				startTime = System.nanoTime();
//				sp.probeRandomCell();
//			} while (game.isGameOver());
//		}
//
//		while (!game.isGameOver()) {
//			if (!sp.assist()) {
//				sp.probeRandomCell();
//				guessCount++;
//			}
//		}
//		endTime = System.nanoTime();
//		gameWon = game.isGameWon();
//	}
//
//	private void PBSolve() {
//		PBSolver pb;
//
//		if (firstGuess) {
//			boolean opening = false;
//			MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//			game = new Minesweeper(gameDifficulty, mineField);
//			pb = new PBSolver(game);
//			pb.setQuiet(true);
//			startTime = System.nanoTime();
//			opening = pb.makeFirstGuess();
//			while (!opening && !game.isGameOver()) {
//				opening = pb.makeFirstGuess();
//				guessCount++;
//			}
//		} else {
//			do {
//				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//				game = new Minesweeper(gameDifficulty, mineField);
//				pb = new PBSolver(game);
//				pb.setQuiet(true);
//				startTime = System.nanoTime();
//				pb.probeRandomCell();
//			} while (game.isGameOver());
//		}
//
//		// First move
//		guessCount++;
//
//		while (!game.isGameOver()) {
//			if (!pb.assist()) {
//				pb.probeRandomCell();
//				guessCount++;
//			}
//		}
//		endTime = System.nanoTime();
//		gameWon = game.isGameWon();
//	}
//
//	private void jointSolve() {
//		SinglePointSolver sp;
//		PBSolver pb;
//
//		if (firstGuess) {
//			boolean opening = false;
//			MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//			game = new Minesweeper(gameDifficulty, mineField);
//			sp = new SinglePointSolver(game);
//			pb = new PBSolver(game);
//			sp.setQuiet(true);
//			pb.setQuiet(true);
//			startTime = System.nanoTime();
//			opening = sp.makeFirstGuess();
//			while (!opening && !game.isGameOver()) {
//				opening = sp.makeFirstGuess();
//				guessCount++;
//			}
//		} else {
//			do {
//				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//				game = new Minesweeper(gameDifficulty, mineField);
//				sp = new SinglePointSolver(game);
//				pb = new PBSolver(game);
//				sp.setQuiet(true);
//				pb.setQuiet(true);
//				startTime = System.nanoTime();
//				sp.probeRandomCell();
//			} while (game.isGameOver());
//		}
//
//		// First move
//		guessCount++;
//
//		while (!game.isGameOver()) {
//			if (!sp.assist()) {
//				if (!pb.assist()) {
//					pb.probeRandomCell();
//					guessCount++;
//				}
//			}
//		}
//		endTime = System.nanoTime();
//		gameWon = game.isGameWon();
//	}
//
//	private void fullSolve() {
//		SinglePointSolver sp;
//		PBSolver pb;
//		ProbabilitySolver prob;
//
//		// No need to check until an opening is created as strat algorithm
//		// will then take over and dictate moves
//		MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
//		game = new Minesweeper(gameDifficulty, mineField);
//		sp = new SinglePointSolver(game);
//		pb = new PBSolver(game);
//		prob = new ProbabilitySolver(game);
//		sp.setQuiet(true);
//		pb.setQuiet(true);
//		prob.setQuiet(true);
//		startTime = System.nanoTime();
//		prob.makeFirstGuess();
//
//		// First move
//		guessCount++;
//
//		while (!game.isGameOver()) {
//			if (!sp.assist()) {
//				if (!pb.assist()) {
//					prob.assist();
//					guessCount++;
//				}
//			}
//		}
//		endTime = System.nanoTime();
//		gameWon = game.isGameWon();
//	}
//
//	/**
//	 * @return the gameDifficulty
//	 */
//	public Difficulty getGameDifficulty() {
//		return gameDifficulty;
//	}
//
//	/**
//	 * @return the startTime
//	 */
//	public long getStartTime() {
//		return startTime;
//	}
//
//	/**
//	 * @return the endTime
//	 */
//	public long getEndTime() {
//		return endTime;
//	}
//
//	/**
//	 * @return total time taken to play game
//	 */
//	public long getElapsedTime() {
//		return (endTime - startTime);
//	}
//
//	/**
//	 * @return the gameWon
//	 */
//	public boolean isGameWon() {
//		return gameWon;
//	}
//
//	/**
//	 * @return the guessCount
//	 */
//	public int getGuessCount() {
//		return guessCount;
//	}
//
//	/**
//	 * @return the game object
//	 */
//	public Minesweeper getGame() {
//		return game;
//	}
//
//	public String getFieldBackup() {
//		return mineFieldBackup;
//	}
//
//	public void reset() {
//		singlePoint = false;
//		PB = false;
//		strat = false;
//		firstGuess = false;
//	}
//
//	public void setFirstGuess(boolean firstGuess) {
//		this.firstGuess = firstGuess;
//	}
//
//	/**
//	 * @param patternMatch the patternMatch to set
//	 */
//	public void setSinglePoint(boolean patternMatch) {
//		this.singlePoint = patternMatch;
//	}
//
//	/**
//	 * @param SAT the SAT to set
//	 */
//	public void setPB(boolean SAT) {
//		this.PB = SAT;
//	}
//
//	/**
//	 * @param strat the strat to set
//	 */
//	public void setStrat(boolean strat) {
//		this.strat = strat;
//	}
//
//}