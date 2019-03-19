import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private static int threadID = 0;

    private final AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean sim = false;
    boolean old = false;
    boolean quiet, hint, loop, patternMatch, SAT, strat, prob;
    Thread thread;

    public SolverThreadWrapper(Minesweeper g) {
        thread = new Thread(this, Integer.toString(threadID++));
        game = g;
        this.quiet = false;
        this.loop = false;
        this.patternMatch = false;
        this.SAT = false;
        this.strat = false;
        this.prob = true;
        thread.start();
    }

    public SolverThreadWrapper(Minesweeper g, boolean quiet, boolean loop, boolean patternMatch, boolean SAT,
            boolean strat) {
        thread = new Thread(this, Integer.toString(threadID++));
        game = g;
        this.quiet = quiet;
        this.loop = loop;
        this.patternMatch = patternMatch;
        this.SAT = SAT;
        this.strat = strat;
        thread.start();
    }

    public SolverThreadWrapper(Minesweeper g, boolean quiet, boolean loop, boolean patternMatch, boolean SAT,
            boolean old, boolean strat) {
        thread = new Thread(this, Integer.toString(threadID++));
        game = g;
        this.quiet = quiet;
        this.loop = loop;
        this.patternMatch = patternMatch;
        this.SAT = SAT;
        this.strat = strat;
        this.old = old;
        thread.start();
    }

    public SolverThreadWrapper(Minesweeper g, boolean hint, boolean SAT) {
        thread = new Thread(this, Integer.toString(threadID++));
        game = g;
        this.hint = hint;
        this.quiet = false;
        this.loop = false;
        this.patternMatch = false;
        this.SAT = SAT;
        this.strat = false;
        thread.start();
    }

    /**
     * For use by simulators only.
     */
    public SolverThreadWrapper(Minesweeper g, boolean sim, boolean patternMatch, boolean SAT, boolean strat) {
        thread = new Thread(this, Integer.toString(threadID++));
        game = g;
        this.patternMatch = patternMatch;
        this.SAT = SAT;
        this.strat = strat;
        this.sim = sim;
        thread.start();
    }

    @Override
    public void run() {
        if (hint) {
            BoardSolver solver = new BoardSolver(game, running);
            if (patternMatch && !SAT) {
                solver.patternMatchHint();
            } else if (!patternMatch && SAT) {
                solver.SATHint();
            } else {
                solver.SATHint();
            }
        } else if (prob) {
            new BoardSolver(game, running).displayProb();
        } else if (sim) {
            if (patternMatch && SAT) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.jointSolve()) {
                        solver.selectRandomCell();
                    }
                }
            } else if (patternMatch) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch()) {
                        solver.selectRandomCell();
                    }
                }
            } else if (SAT) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.SATSolve()) {
                        solver.selectRandomCell();
                    }
                }
            }
        } else if (loop) {
            if (patternMatch && SAT) {
                jointSolve();
            } else if (patternMatch) {
                patternMatchSolve();
            } else if (SAT) {
                if (old) {
                    new BoardSolver(game, running).oldSATHint();
                } else {
                    SATSolve();
                }
            }
        } else { // Just Assist
            if (patternMatch && SAT) {
                new BoardSolver(game, running).jointSolve();
            } else if (patternMatch) {
                new BoardSolver(game, running).patternMatch();
            } else if (SAT) {
                new BoardSolver(game, running).SATSolve();
            }
        }
        end();
        game.getStopBtn().setEnabled(false);
        if (!game.isGameOver()) {
            game.enableAllBtns();
        }
        thread.interrupt();
    }

    public void end() {
        running.set(false);
        thread.interrupt();
    }

    private void patternMatchSolve() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.patternMatch()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void SATSolve() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.SATSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void oldSATSolve() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.oldSATSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void jointSolve() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.jointSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void stratSovle() {
        // BoardSolver solver = new BoardSolver(game, running);
        // while (running.get() && !game.isGameOver() && solver.fullSolve()) {
        // if (Thread.interrupted()) {
        // break;
        // }
        // }
    }

}