import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private static int threadID = 0;

    private volatile AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet, hint, loop, singlePoint, pb, prob, strat;

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
        this.prob = false;
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
        this.prob = true;
    }

    @Override
    public void run() {
        if (hint) {
            if (singlePoint && !pb) {
                SinglePointSolver sp = new SinglePointSolver(game);
                sp.hint();
            } else if (!singlePoint && pb) {
                PBSolver pb = new PBSolver(game, running);
                pb.hint();
            }
        } else if (prob) {
                ProbabilitySolver probS = new ProbabilitySolver(game, running);
                probS.displayProb();
        } else if (loop) {
            if (strat) {
                if (singlePoint && pb) {
                    jointStratSolve();
                } else if (singlePoint) {
                    singlePointStratSolve();
                } else if (pb) {
                    pbStratSolve();
                }
            } else if (singlePoint && pb) {
                jointSolve();
            } else if (singlePoint) {
                singlePointSolve();
            } else if (pb) {
                pbSolve();
            }
        } else { // Just Assist
            if (strat) {
                if (singlePoint && pb) {
                    jointStrat();
                } else if (singlePoint) {
                    singlePointStrat();
                } else if (pb) {
                    pbStrat();
                }
            } else if (singlePoint && pb) {
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

    private void singlePointStrat() {
        SinglePointSolver sp = new SinglePointSolver(game);
        if (!sp.assist()) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.makeBestMove();
        }
    }
    
    private void pbStrat() {
        PBSolver pb = new PBSolver(game, running);
        if (!pb.assist()) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.makeBestMove();
        }
    }

    private void jointStrat() {
        SinglePointSolver sp = new SinglePointSolver(game);
        PBSolver pb = new PBSolver(game, running);
        if (!sp.assist() && !pb.assist()) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.makeBestMove();
        }
    }

    private void singlePointStratSolve() {
        while(!game.isGameOver()) {
            singlePointStrat();
        }
    }

    private void pbStratSolve() {
        while(!game.isGameOver()) {
            pbStrat();
        }
    }

    private void jointStratSolve() {
        while(!game.isGameOver()) {
            jointStrat();
        }
    }

}