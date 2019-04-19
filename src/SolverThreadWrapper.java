import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    @SuppressWarnings("unused")
    private static int threadID = 0;

    private volatile AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet, hint, loop, singlePoint, pb, showProb, strat;

    private volatile Thread thread;

    public SolverThreadWrapper(Minesweeper g) {
        game = g;

        reset();
    }

    public void start() {
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
        this.strat = false;
        this.quiet = false;
        this.loop = false;
        this.hint = false;
        this.singlePoint = false;
        this.pb = false;
        this.showProb = false;
    }

    public void setHint() {
        this.hint = true;
    }

    public void setSinglePoint(boolean singlePoint) {
        this.singlePoint = singlePoint;
    }

    public void setPB(boolean pb) {
        this.pb = pb;
    }

    public void setStrat(boolean strat) {
        this.strat = strat;
    }

    public void setLoop() {
        this.loop = true;
    }

    public void setProb() {
        this.showProb = true;
    }

    @Override
    public void run() {
        if (hint) {
            if (singlePoint && pb && strat) {
                fullHint();
            } else if (singlePoint && pb) {
                singlePointPBHint();
            } else if (pb && strat) {
                pbStratHint();
            } else if (singlePoint && strat) {
                singlePointStratHint();
            } else if (singlePoint) {
                singlePointHint();
            } else if (pb) {
                pbHint();
            } else if (strat) {
                stratHint();
            }
        } else if (showProb) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.displayProb();
        } else if (loop) {
            if (singlePoint && pb && strat) {
                fullSolve();
            } else if (singlePoint && pb) {
                singlePointPBSolve();
            } else if (pb && strat) {
                pbStratSolve();
            } else if (singlePoint && strat) {
                singlePointStratSolve();
            } else if (singlePoint) {
                singlePointSolve();
            } else if (pb) {
                pbSolve();
            } else if (strat) {
                stratSolve();
            }
        } else { // Just Assist
            if (singlePoint && pb && strat) {
                jointStrat();
            } else if (singlePoint && pb) {
                singlePointPB();
            } else if (pb && strat) {
                pbStrat();
            } else if (singlePoint && strat) {
                singlePointStrat();
            } else if (singlePoint) {
                new SinglePointSolver(game).assist();
            } else if (pb) {
                new PBSolver(game, running).assist();
            } else if (strat) {
                new ProbabilitySolver(game, running).assist();
            }
        }
        game.getStopBtn().setEnabled(false);
        if (!game.isGameOver()) {
            game.enableAllBtns();
        }
    }

    private void stratSolve() {
        Thread thisThread = Thread.currentThread();
        ProbabilitySolver prob = new ProbabilitySolver(game, running);
        while (thisThread == thread && running.get() && !game.isGameOver() && prob.assist())
            ;
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
        while (thisThread == thread && running.get() && !game.isGameOver() && pb.assist())
            ;
    }

    private void singlePointPB() {
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game, running);
        if (!sp.assist()) {
            pb.assist();
        }
    }

    private void singlePointPBSolve() {
        Thread thisThread = Thread.currentThread();
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game, running);
        while (thisThread == thread && running.get() && !game.isGameOver() && (sp.assist() || pb.assist()))
            ;
    }

    private void singlePointStrat() {
        SinglePointSolver sp = new SinglePointSolver(game);
        if (!sp.assist()) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.assist();
        }
    }

    private void pbStrat() {
        PBSolver pb = new PBSolver(game, running);
        if (!pb.assist()) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.assist();
        }
    }

    private void jointStrat() {
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game, running);
        if (!sp.assist() && !pb.assist()) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.assist();
        }
    }

    private void singlePointStratSolve() {
        while (!game.isGameOver()) {
            singlePointStrat();
        }
    }

    private void pbStratSolve() {
        while (!game.isGameOver()) {
            pbStrat();
        }
    }

    private void fullSolve() {
        while (!game.isGameOver()) {
            jointStrat();
        }
    }

    private boolean singlePointHint() {
        SinglePointSolver sp = new SinglePointSolver(game);
        return sp.hint();
    }

    private boolean pbHint() {
        PBSolver pb = new PBSolver(game, running);
        return pb.hint();
    }

    private boolean stratHint() {
        ProbabilitySolver prob = new ProbabilitySolver(game, running);
        return prob.hint();
    }

    private void singlePointStratHint() {
        if (!singlePointHint()) {
            stratHint();
        }
    }

    private void pbStratHint() {
        if (!pbHint()) {
            stratHint();
        }
    }

    private void singlePointPBHint() {
        if (!singlePointHint()) {
            pbHint();
        }
    }

    private void fullHint() {
        if (!singlePointHint()) {
            if (!pbHint()) {
                stratHint();
            }
        }
    }

}