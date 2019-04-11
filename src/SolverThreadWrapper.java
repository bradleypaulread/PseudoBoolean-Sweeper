import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private static int threadID = 0;

    private volatile AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet, hint, loop, singlePoint, pb, prob, sim, strat, old;

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
        this.singlePoint = patternMatch;
        this.pb = SAT;
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
        if (thread != null)
            thread.interrupt();
        thread = null;
    }

    public void reset() {
        this.sim = false;
        this.strat = false;
        this.old = false;
        this.quiet = false;
        this.loop = false;
        this.hint = false;
        this.singlePoint = false;
        this.pb = false;
        this.prob = false;
    }

    public void setPatternMatchHint() {
        this.hint = true;
        this.singlePoint = true;
    }

    public void setStrat() {
        this.strat = true;
    }

    public void setSATHint() {
        this.hint = true;
        this.pb = true;
    }

    public void setLoop() {
        this.loop = true;
    }

    public void setPatternMatchSolve() {
        this.singlePoint = true;
    }

    public void setSATSolve() {
        this.pb = true;
    }

    public void setProb() {
        this.prob = true;
    }

    @Override
    public void run() {
        if (strat && !hint) {
            solver.setStrat(true);
        }
        if (hint) {
            if (singlePoint && !pb) {
                SinglePointSolver sp = new SinglePointSolver(game);
                sp.hint();
            } else if (!singlePoint && pb) {
                PBSolver pb = new PBSolver(game, running);
                pb.hint();
            } else {
                PBSolver pb = new PBSolver(game, running);
                pb.hint();
            }
        } else if (prob) {
            if (singlePoint && pb) {
                fullSolve();
            } else {
                ProbabilitySolver probS = new ProbabilitySolver(game, running);
                probS.displayProb();
            }
        } else if (loop) {
            if (singlePoint && pb) {
                jointSolve();
            } else if (singlePoint) {
                singlePointSolve();
            } else if (pb) {
                pbSolve();
            }
        } else { // Just Assist
            if (singlePoint && pb) {
                SinglePointSolver sp = new SinglePointSolver(game);
                PBSolver pb = new PBSolver(game, running);
                if (!sp.assist()) {
                    pb.assist();
                }
            } else if (singlePoint) {
                SinglePointSolver sp = new SinglePointSolver(game);
                sp.assist();
            } else if (pb) {
                PBSolver pb = new PBSolver(game, running);
                pb.assist();
            }
        }
        game.getStopBtn().setEnabled(false);
        if (!game.isGameOver()) {
            game.enableAllBtns();
        }
    }

    private void singlePointSolve() {
        Thread thisThread = Thread.currentThread();
        SinglePointSolver sp = new SinglePointSolver(game);
        while (thisThread == thread && running.get() && !game.isGameOver() && sp.assist())
            ;
    }

    private void pbSolve() {
        Thread thisThread = Thread.currentThread();
        PBSolver pb = new PBSolver(game, running);
        while (thisThread == thread && running.get() && !game.isGameOver() && pb.assist()) {
        }
    }

    private void jointSolve() {
        Thread thisThread = Thread.currentThread();
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game, running);
        while (thisThread == thread && running.get() && !game.isGameOver() && (sp.assist() || pb.assist()))
            ;
    }

    private void fullSolve() {
        Thread thisThread = Thread.currentThread();
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game, running);
        while (thisThread == thread && running.get() && !game.isGameOver()) {
            if (!sp.assist()) {
                if (!pb.assist()) {
                    ProbabilitySolver probS = new ProbabilitySolver(game, running);
                    probS.makeBestMove();
                }
            }
        }
    }

}