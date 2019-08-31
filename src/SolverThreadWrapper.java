import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    @SuppressWarnings("unused")
    private static int threadID = 0;

    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private Minesweeper game;
    private boolean hint;
    private boolean loop;
    private boolean singlePoint;
    private boolean pb;
    private boolean showProb;
    private boolean strat;

    private volatile Thread thread;

    /**
     * List of the solvers to use. If a new solver is created simply add it into the
     * {@link #selectSolvers() selectSovlers method}.
     */
    private List<Solver> solvers;

    public SolverThreadWrapper(Minesweeper g) {
        game = g;
        solvers = new ArrayList<>();
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
        this.loop = false;
        this.hint = false;
        this.singlePoint = false;
        this.pb = false;
        this.showProb = false;
    }

    private void selectSolvers() {
        solvers.clear();
        if (singlePoint) {
            solvers.add(new SinglePointSolver(game));
        }
        if (pb) {
            solvers.add(new PBSolver(game, running));
        }
        if (strat) {
            solvers.add(new ProbabilitySolver(game, running));
        }
    }

    @Override
    public void run() {
        if (showProb) {
            ProbabilitySolver probS = new ProbabilitySolver(game, running);
            probS.displayProb();
        } else {
            selectSolvers();
            if (hint) {
                for (Solver solver : solvers) {
                    if (solver.hint()) {
                        break;
                    }
                }
            } else if (loop) { // Continuous solves
                Thread thisThread = Thread.currentThread();
                while (thisThread == thread && running.get() && !game.isGameOver()) {
                    boolean didBreak = false;
                    for (Solver solver : solvers) {
                        if (!running.get() || solver.assist()) {
                            didBreak = true;
                            break;
                        }
                    }
                    // If not a single move was made by any solver then no moves could be found so
                    // stop looping
                    if (!didBreak) {
                        break;
                    }
                }
            } else { // Just a single assist
                for (Solver solver : solvers) {
                    if (!running.get() || solver.assist()) {
                        break;
                    }
                }
            }
        }
        game.getStopBtn().setEnabled(false);
        if (!game.isGameOver()) {
            game.enableAllBtns();
        }
    }

    public void setHint(boolean hint) {
        this.hint = hint;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setPb(boolean pb) {
        this.pb = pb;
    }

    public void setShowProb(boolean showProb) {
        this.showProb = showProb;
    }

    public void setSinglePoint(boolean singlePoint) {
        this.singlePoint = singlePoint;
    }

    public void setStrat(boolean strat) {
        this.strat = strat;
    }
}