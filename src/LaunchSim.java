import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * Main.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

public class LaunchSim {
	public static void main(String[] args) {
		int consoleNum = 0;
		int noOfSims = 5000;
		System.out.println("Started");
		if (args.length > 0) {
			if (Integer.valueOf(args[0]) > 0) {
				noOfSims = Integer.valueOf(args[0]);
			}
		}
		long simStart = System.nanoTime();
		System.out.println("Start Setup");
		GameSimulator sim = new GameSimulator(noOfSims);
		System.out.println("Fin. Setup");
		ExecutorService pool = sim.getPool();
		sim.startGenericSim();
		pool.shutdown();
		while (!pool.isTerminated()) {
			try {
				System.out.println(
						"==============================================================================================================================================");
				System.out.println(++consoleNum + ":" + pool);
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
			}
		}
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
		}
		long simEnd = System.nanoTime()-simStart;
		System.out.println("SIM TIME: " + (simEnd / 1e9) + "s");
		sim.calcResults();
		System.out.println("\n\n\nDONE!!!!!");
	}
}
