import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet;
    boolean loop;
    boolean patternMatch;
    boolean SAT;
    boolean strat;
    Thread thread;

    public SolverThreadWrapper(Minesweeper g, boolean quiet, boolean loop, boolean patternMatch, boolean SAT, boolean strat) {
        thread = new Thread(this);
        game = g;

        this.quiet = quiet;
        this.loop = loop;
        this.patternMatch = patternMatch;
        this.SAT = SAT;
        this.strat = strat;
        thread.start();
    }

    @Override
    public void run() {
        if (loop) {
            if (patternMatch && SAT) {
                joint(quiet);
            } else if (patternMatch) {
                patternMatch(quiet);
            } else if (SAT) {
                SAT(quiet);
            }
        } else {
            if (patternMatch && SAT) {
                new BoardSolver1(game).jointSolve(running);
            } else if (patternMatch) {
                new BoardSolver1(game).patternMatch(running);
            } else if (SAT) {
                new BoardSolver1(game).SATSolve(running);
            }
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
        game.enableAllBtns();
        game.getStopBtn().setEnabled(false);
    }

    public void end() {
        running.set(false);
    }

    private void patternMatch(boolean quiet) {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.patternMatch(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void SAT(boolean quiet) {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.SATSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void joint(boolean quiet) {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.jointSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void strat(boolean quiet) {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.fullSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    
    
}