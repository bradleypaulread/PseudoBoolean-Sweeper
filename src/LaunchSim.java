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
		int noOfSims = 25;
		if (args.length > 0) {
			noOfSims = Integer.valueOf(args[0]);
		}
		GameSimulation sim = new GameSimulation(noOfSims);
		System.out.println("SAT SIM");
		sim.startSATSim();
		System.out.println("JOINT SIM");
		sim.startJointSim();
		System.out.println("PATTERN SIM");
		sim.startPatternMatchSim();
		System.out.println("DONE!");
	}

}
