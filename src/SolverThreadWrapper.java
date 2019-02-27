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
                BoardSolver1 solver = new BoardSolver1(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.jointSolve(running)) {
                        Cell c = solver.selectRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }
            } else if (patternMatch) {
                BoardSolver1 solver = new BoardSolver1(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.patternMatch(running)) {
                        Cell c = solver.selectRandomCell();
                        game.quietSelect(c.getX(), c.getY());
                    }
                }
            } else if (SAT) {
                BoardSolver1 solver = new BoardSolver1(game);
                solver.setQuiet();
                while (!game.isGameOver()) {
                    if (!solver.SATSolve(running)) {
                        Cell c = solver.selectRandomCell();
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
                new BoardSolver1(game).jointSolve(running);
            } else if (patternMatch) {
                new BoardSolver1(game).patternMatch(running);
            } else if (SAT) {
                new BoardSolver1(game).SATSolve(running);
            }
        }
        //java.awt.Toolkit.getDefaultToolkit().beep();
        System.out.println(Thread.currentThread().toString() + " DONE!");
        game.enableAllBtns();
        game.getStopBtn().setEnabled(false);
    }

    public void end() {
        running.set(false);
    }

    private void patternMatch() {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.patternMatch(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void SAT() {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.SATSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void joint() {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.jointSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private void strat() {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.fullSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
    }

}