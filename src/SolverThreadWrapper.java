import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private static int threadID = 0;

    private volatile AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet, hint, loop, patternMatch, SAT, prob, sim, strat, old;

    private volatile Thread thread;
    private BoardSolver solver;

    public SolverThreadWrapper(Minesweeper g) {
        game = g;

        reset();
    }

    /**
     * For use by simulators only.
     */
    public SolverThreadWrapper(Minesweeper g, boolean sim, boolean patternMatch, boolean SAT, boolean prob) {
        game = g;
        this.patternMatch = patternMatch;
        this.SAT = SAT;
        this.prob = prob;
        this.sim = sim;
        thread = new Thread(this, Integer.toString(threadID++));
    }
    
    public void start() {
        this.solver = new BoardSolver(game, running);
        this.thread = new Thread(this);
        this.thread.start();
    }
    
    public void end() {
        running.set(false);
        if (thread != null) thread.interrupt();
        thread = null;
    }

    public void reset() {
        this.sim = false;
        this.strat = false;
        this.old = false;
        this.quiet = false;
        this.loop = false;
        this.hint = false;
        this.patternMatch = false;
        this.SAT = false;
        this.prob = false;
    }

    public void setPatternMatchHint() {
        this.hint = true;
        this.patternMatch = true;
    }

    public void setStrat() {
        this.strat = true;
    }

    public void setSATHint() {
        this.hint = true;
        this.SAT = true;
    }

    public void setLoop() {
        this.loop = true;
    }

    public void setPatternMatchSolve() {
        this.patternMatch = true;
    }
    
    public void setSATSolve() {
        this.SAT = true;
    }
    
    public void setProbSolve() {
        this.prob = true;
    }

    public void setOld() {
        this.old = true;
    }

    @Override
    public void run() {
        if (strat && !hint) {
            solver.setStrat(true);
        }
        if (hint) {
            if (patternMatch && !SAT) {
                solver.patternMatchHint();
            } else if (!patternMatch && SAT) {
                solver.SATHint();
            } else {
                solver.SATHint();
            }
        } else if (prob) {
            if (patternMatch && SAT) {
                fullSolve();
            } else {
                solver.displayProb();
            }
        } else if (sim) {
            if (patternMatch && SAT) {
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternAndSATSolve()) {
                        solver.selectRandomCell();
                    }
                }
            } else if (patternMatch) {
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch()) {
                        solver.selectRandomCell();
                    }
                }
            } else if (SAT) {
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
                    solver.oldSATHint();
                } else {
                    SATSolve();
                }
            }
        } else { // Just Assist
            if (patternMatch && SAT) {
                solver.patternAndSATSolve();
            } else if (patternMatch) {
                solver.patternMatch();
            } else if (SAT) {
                if(!solver.SATSolve()) {
                    if (strat) {
                        solver.temp();
                    }
                }
            }
        }
        game.getStopBtn().setEnabled(false);
        if (!game.isGameOver()) {
            game.enableAllBtns();
        }
    }

    private void patternMatchSolve() {
        Thread thisThread = Thread.currentThread();
        while (thisThread == thread && running.get() && !game.isGameOver() && solver.patternMatch()) {
        }
    }

    private void SATSolve() {
        Thread thisThread = Thread.currentThread();
        while (thisThread == thread && running.get() && !game.isGameOver() && solver.SATSolve()) {
        }
    }

    private void oldSATSolve() {
        Thread thisThread = Thread.currentThread();
        while (thisThread == thread && running.get() && !game.isGameOver() && solver.oldSATSolve()) {
        }
    }

    private void jointSolve() {
        Thread thisThread = Thread.currentThread();
        while (thisThread == thread && running.get() && !game.isGameOver() && solver.patternAndSATSolve()) {
        }
    }

    private void fullSolve() {
        Thread thisThread = Thread.currentThread();
        while (thisThread == thread && running.get() && !game.isGameOver()) {
            solver.fullSolve();
        }
    }

}