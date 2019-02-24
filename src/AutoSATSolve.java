import java.util.concurrent.atomic.AtomicBoolean;

public class AutoSATSolve implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    Thread thread;
    public AutoSATSolve(Minesweeper g) {
        thread = new Thread(this);
        game = g;
        thread.start();
    }

    @Override
    public void run() {
        BoardSolver1 solver = new BoardSolver1(game);
        while (running.get() && !game.isGameOver() && solver.jointSolve(running)) {
            if (Thread.interrupted()) {
                break;
            }
        }
        game.enableAllBtns();
        game.getStopBtn().setEnabled(false);
    }

    public void end() {
        running.set(false);
    }

}