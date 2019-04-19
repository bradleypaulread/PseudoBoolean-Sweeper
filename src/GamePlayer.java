import com.google.gson.Gson;

public class GamePlayer implements Runnable {

	private Difficulty gameDifficulty;
	private MineField mineField;
	private String mineFieldBackup;

	private Minesweeper game;

	private long startTime;
	private long endTime;

	private boolean gameWon;
	private int guessCount;

	private boolean singlePoint, PB, strat, firstGuess;

	public GamePlayer(Difficulty diff, String fieldJson) {
		gameDifficulty = diff;

		reset();

		Gson gson = new Gson();

		mineField = gson.fromJson(fieldJson, MineField.class);
		mineFieldBackup = fieldJson;

		game = new Minesweeper(gameDifficulty, mineField);
	}

	public String getFieldBackup() {
		return mineFieldBackup;
	}

	public void reset() {
		singlePoint = false;
		PB = false;
		strat = false;
		firstGuess = false;
	}

	public void setFirstGuess(boolean firstGuess) {
		this.firstGuess = firstGuess;
	}

	/**
	 * @param patternMatch the patternMatch to set
	 */
	public void setSinglePoint(boolean patternMatch) {
		this.singlePoint = patternMatch;
	}

	/**
	 * @param sAT the sAT to set
	 */
	public void setPB(boolean SAT) {
		this.PB = SAT;
	}

	/**
	 * @param strat the strat to set
	 */
	public void setStrat(boolean strat) {
		this.strat = strat;
	}

	@Override
	public void run() {
		if (singlePoint && PB && strat) {
			fullSolve();
			return;
		} else if (singlePoint && PB) {
			jointSolve();
			return;
		} else if (singlePoint) {
			singlePointSolve();
			return;
		} else if (PB) {
			PBSolve();
			return;
		} else {
			System.err.println("No sim specs configured.");
			return;
		}
	}

	private void singlePointSolve() {
		SinglePointSolver sp;

		if (firstGuess) {
			boolean opening = false;
			do {
				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
				game = new Minesweeper(gameDifficulty, mineField);
				sp = new SinglePointSolver(game);
				sp.setQuiet();
				opening = sp.makeFirstGuess();
				startTime = System.nanoTime();
			} while (!opening);
		} else {
			do {
				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
				game = new Minesweeper(gameDifficulty, mineField);
				sp = new SinglePointSolver(game);
				sp.setQuiet();
				startTime = System.nanoTime();
				sp.selectRandomCell();
			} while (game.isGameOver());
		}

		while (!game.isGameOver()) {
			if (!sp.assist()) {
				sp.selectRandomCell();
				guessCount++;
			}
		}
		endTime = System.nanoTime();
		gameWon = game.isGameWon();
	}

	private void PBSolve() {
		PBSolver pb;

		if (firstGuess) {
			boolean opening = false;
			MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
			game = new Minesweeper(gameDifficulty, mineField);
			pb = new PBSolver(game);
			pb.setQuiet();
			startTime = System.nanoTime();
			opening = pb.makeFirstGuess();
			while (!opening && !game.isGameOver()) {
				opening = pb.makeFirstGuess();
				guessCount++;
			}
		} else {
			do {
				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
				game = new Minesweeper(gameDifficulty, mineField);
				pb = new PBSolver(game);
				pb.setQuiet();
				startTime = System.nanoTime();
				pb.selectRandomCell();
			} while (game.isGameOver());
		}

		// First move
		guessCount++;

		while (!game.isGameOver()) {
			if (!pb.assist()) {
				pb.selectRandomCell();
				guessCount++;
			}
		}
		endTime = System.nanoTime();
		gameWon = game.isGameWon();
	}

	private void jointSolve() {
		SinglePointSolver sp;
		PBSolver pb;

		if (firstGuess) {
			boolean opening = false;
			MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
			game = new Minesweeper(gameDifficulty, mineField);
			sp = new SinglePointSolver(game);
			pb = new PBSolver(game);
			sp.setQuiet();
			pb.setQuiet();
			startTime = System.nanoTime();
			opening = sp.makeFirstGuess();
			while (!opening && !game.isGameOver()) {
				opening = sp.makeFirstGuess();
				guessCount++;
			}
		} else {
			do {
				MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
				game = new Minesweeper(gameDifficulty, mineField);
				sp = new SinglePointSolver(game);
				pb = new PBSolver(game);
				sp.setQuiet();
				pb.setQuiet();
				startTime = System.nanoTime();
				sp.selectRandomCell();
			} while (game.isGameOver());
		}

		// First move
		guessCount++;

		while (!game.isGameOver()) {
			if (!sp.assist()) {
				if (!pb.assist()) {
					pb.selectRandomCell();
					guessCount++;
				}
			}
		}
		endTime = System.nanoTime();
		gameWon = game.isGameWon();
	}

	private void fullSolve() {
		SinglePointSolver sp;
		PBSolver pb;
		ProbabilitySolver prob;

		do {
			MineField mineField = new Gson().fromJson(mineFieldBackup, MineField.class);
			game = new Minesweeper(gameDifficulty, mineField);
			sp = new SinglePointSolver(game);
			pb = new PBSolver(game);
			prob = new ProbabilitySolver(game);
			sp.setQuiet();
			pb.setQuiet();
			prob.setQuiet();
			startTime = System.nanoTime();
			prob.makeFirstGuess();
		} while (game.isGameOver());

		// First move
		guessCount++;

		while (!game.isGameOver()) {
			if (!sp.assist()) {
				if (!pb.assist()) {
					prob.assist();
					guessCount++;
				}
			}
		}
		endTime = System.nanoTime();
		gameWon = game.isGameWon();
	}

	/**
	 * @return the gameDifficulty
	 */
	public Difficulty getGameDifficulty() {
		return gameDifficulty;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @return total time taken to play game
	 */
	public long getElapsedTime() {
		return (endTime - startTime);
	}

	/**
	 * @return the gameWon
	 */
	public boolean isGameWon() {
		return gameWon;
	}

	/**
	 * @return the guessCount
	 */
	public int getGuessCount() {
		return guessCount;
	}

	public Minesweeper getGame() {
		return game;
	}

}