import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private static int threadID = 0;

    private final AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet, hint, loop, patternMatch, SAT, prob, sim, old;
    Thread thread;

    public SolverThreadWrapper(Minesweeper g) {
        game = g;

        reset();

        thread = new Thread(this, Integer.toString(threadID++));
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
        this.thread.start();
    }
    
    public void end() {
        running.set(false);
        thread.interrupt();
    }

    public void reset() {
        this.sim = false;
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
            if (patternMatch && SAT) {
                fullSolve();
            } else {
                new BoardSolver(game, running).displayProb();
            }
        } else if (sim) {
            if (patternMatch && SAT) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternAndSATSolve()) {
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
                new BoardSolver(game, running).patternAndSATSolve();
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
        while (running.get() && !game.isGameOver() && solver.patternAndSATSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void fullSolve() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver()) {
            if (Thread.interrupted()) {
                break;
            }
            solver.fullSolve();
        }
    }

}