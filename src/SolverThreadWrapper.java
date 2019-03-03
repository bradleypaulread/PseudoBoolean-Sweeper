import java.util.concurrent.atomic.AtomicBoolean;

public class SolverThreadWrapper implements Runnable {

    private static int threadID = 0;

    private final AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean quiet;
    boolean sim = false;
    boolean loop;
    boolean patternMatch;
    boolean SAT;
    boolean strat;
    Thread thread;

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
        if (sim) {
            if (patternMatch && SAT) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.jointSolve()) {
                        Cell c = solver.getRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }
            } else if (patternMatch) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch()) {
                        Cell c = solver.getRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }
            } else if (SAT) {
                BoardSolver solver = new BoardSolver(game, running);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.SATSolve()) {
                        Cell c = solver.getRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }
            }
        } else if (loop) {
            if (patternMatch && SAT) {
                joint();
            } else if (patternMatch) {
                patternMatch();
            } else if (SAT) {
                SAT();
            }
        } else {
            if (patternMatch && SAT) {
                new BoardSolver(game, running).jointSolve();
            } else if (patternMatch) {
                new BoardSolver(game, running).patternMatch();
            } else if (SAT) {
                new BoardSolver(game, running).SATSolve();
            }
        }
        //java.awt.Toolkit.getDefaultToolkit().beep();
        //System.out.println(Thread.currentThread().toString() + " DONE!");
        game.enableAllBtns();
        game.getStopBtn().setEnabled(false);
    }

    public void end() {
        running.set(false);
    }

    private void patternMatch() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.patternMatch()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void SAT() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.SATSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void joint() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.jointSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void strat() {
        BoardSolver solver = new BoardSolver(game, running);
        while (running.get() && !game.isGameOver() && solver.fullSolve()) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

}