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
		int noOfSims = 500;
		if (args.length > 0) {
			noOfSims = Integer.valueOf(args[0]);
		}
		GameSimulation sim = new GameSimulation(noOfSims);
		ExecutorService pool = sim.getPool();
		sim.genericSim();
		pool.shutdown();
		while (!pool.isTerminated()) {
			try {
				System.out.println(pool);
				Thread.sleep(10000);
			} catch (Exception e) {
			}
		}
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
		}
		sim.calcResults();
		System.out.println("\n\n\nDONE!!!!!");
	}

}
