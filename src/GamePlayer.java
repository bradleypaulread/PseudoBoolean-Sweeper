import com.google.gson.Gson;

public class GamePlayer implements Runnable {

    private Difficulty gameDifficulty;
    private MineField mineField;
    private String mineFieldBackup;

    private Minesweeper game;
    private BoardSolver solver;

    private long startTime;
    private long endTime;

    private boolean gameWon;
    private int guessCount;

    private boolean patternMatch, SAT, strat;

    public GamePlayer(Difficulty diff, String fieldJson) {
        gameDifficulty = diff;

        reset();

        Gson gson = new Gson();

        mineField = gson.fromJson(fieldJson, MineField.class);
        mineFieldBackup = fieldJson;

        game = new Minesweeper(gameDifficulty, mineField);
        solver = new BoardSolver(game);
        solver.setQuiet();
    }

    public String getFieldBackup() {
        return mineFieldBackup;
    }

    public void reset() {
        patternMatch = false;
        SAT = false;
        strat = false;
    }

    /**
     * @param patternMatch the patternMatch to set
     */
    public void setPatternMatch(boolean patternMatch) {
        this.patternMatch = patternMatch;
    }

    /**
     * @param sAT the sAT to set
     */
    public void setSAT(boolean SAT) {
        this.SAT = SAT;
    }

    /**
     * @param strat the strat to set
     */
    public void setStrat(boolean strat) {
        this.strat = strat;
    }

    @Override
    public void run() {
        if (patternMatch && SAT && strat) {
            fullSolve();
            return;
        } else if (patternMatch && SAT) {
            jointSolve();
            return;
        } else if (patternMatch) {
            patternMatchSolve();
            return;
        } else if (SAT) {
            SATSolve();
            return;
        } else {
            System.err.println("No sim specs configured.");
            return;
        }
    }

    private void patternMatchSolve() {
        startTime = System.nanoTime();
        while (!game.isGameOver()) {
            if (!solver.patternMatch()) {
                solver.selectRandomCell();
                guessCount++;
            }
        }
        endTime = System.nanoTime();
        gameWon = game.isGameWon();
    }

    private void SATSolve() {
        startTime = System.nanoTime();
        while (!game.isGameOver()) {
            if (!solver.old()) {
                solver.selectRandomCell();
                guessCount++;
            }
        }
        endTime = System.nanoTime();
        gameWon = game.isGameWon();
    }

    private void jointSolve() {
        startTime = System.nanoTime();
        while (!game.isGameOver()) {
            if (!solver.patternAndSATSolve()) {
                solver.selectRandomCell();
                guessCount++;
            }
        }
        endTime = System.nanoTime();
        gameWon = game.isGameWon();
    }

    private void fullSolve() {
        guessCount = 0;
        startTime = System.nanoTime();
        while (!game.isGameOver()) {
            solver.fullSolve();
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