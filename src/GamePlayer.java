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

    private boolean patternMatch, SAT, strat;

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
        patternMatch = false;
        SAT = false;
        strat = false;
    }

    /**
     * @param patternMatch the patternMatch to set
     */
    public void setSinglePoint(boolean patternMatch) {
        this.patternMatch = patternMatch;
    }

    /**
     * @param sAT the sAT to set
     */
    public void setPB(boolean SAT) {
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
            singlePointSolve();
            return;
        } else if (SAT) {
            PBSolve();
            return;
        } else {
            System.err.println("No sim specs configured.");
            return;
        }
    }

    private void singlePointSolve() {
        SinglePointSolver sp = new SinglePointSolver(game);
        sp.setQuiet();
        startTime = System.nanoTime();
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
        PBSolver pb = new PBSolver(game);
        pb.setQuiet();
        startTime = System.nanoTime();
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
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game);
        sp.setQuiet();
        pb.setQuiet();
        while (!game.isGameOver()) {
            if (!sp.assist() && !pb.assist()) {
                sp.selectRandomCell();
                guessCount++;
            }
        }
        endTime = System.nanoTime();
        gameWon = game.isGameWon();
    }

    private void fullSolve() {
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game);
        ProbabilitySolver prob = new ProbabilitySolver(game);
        sp.setQuiet();
        pb.setQuiet();
        startTime = System.nanoTime();
        while (!game.isGameOver()) {
            if (!sp.assist() && !pb.assist()) {
                prob.makeBestMove();
                guessCount++;
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