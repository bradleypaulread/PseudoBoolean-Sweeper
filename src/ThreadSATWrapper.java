import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadSATWrapper implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(true);
    Minesweeper game;
    boolean pattern;
    Thread thread;
    
    public ThreadSATWrapper(Minesweeper g, boolean patternMatch) {
        thread = new Thread(this);
        game = g;
        this.pattern = patternMatch;
        thread.start();
    }

    @Override
    public void run() {
    	System.out.println("Test");
        BoardSolver1 solver = new BoardSolver1(game);
        if (pattern) {
	        while (running.get() && !game.isGameOver() && solver.jointSolve(running)) {
	            if (Thread.interrupted()) {
	                break;
	            }
	        }
        } else {
        	while (running.get() && !game.isGameOver() && solver.SATSolve(running)) {
	            if (Thread.interrupted()) {
	                break;
	            }
	        }
        }
        game.enableAllBtns();
        game.getStopBtn().setEnabled(false);
    }

    public void end() {
        running.set(false);
    }

}