import java.io.*;

/*
 * Main.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

public class Main {
	public static void main(String[] args) throws IOException {

		GameSimulation sim = new GameSimulation(50);
		System.out.println("PATTERN SIM");
		sim.startPatternMatchSim();
		System.out.println("SAT SIM");
		sim.startSATSim();
		System.out.println("JOINT SIM");
		sim.startJointSim();
		System.out.println("DONE!");

		int x = 6; // Width of the board
		int y = 6; // Height of the board
		double diff = 0.2; // The difficulty of the game (percent of cells that are mines)
		int mines = 3; // Integer number of mines on the board
		assert diff >= 0.00 && diff < 1.00 && mines >= 0 && mines < (x * y);

		// new Minesweeper(x, y, diff); // Constructor for % mines
		// new Minesweeper(x, y, mines); // Constructor for int mines
		//new Minesweeper(Difficulty.HARD);
	}
}
