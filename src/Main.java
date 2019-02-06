import java.io.IOException;

/*
 * Main.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */
 
public class Main
{
	public static void main (String[] args) throws IOException
	{
		// int count = 0;
		// int val = 0;
		// int[][] cells = new int[4][3];

		// for (int i = 0; i < cells.length; i++) {
		// 	for (int j = 0; j < cells[i].length; j++) {
		// 		cells[i][j] = val;
		// 		++count;
		// 		val = (val + 1) * 3;
		// 	}
		// }

		// System.out.println("Contents Before:");
		// for (int i = 0; i < cells.length; i++) {
		// 	for (int j = 0; j < cells[i].length; j++) {
		// 		System.out.print("" + cells[i][j] + ", ");

		// 	}
		// }
		// int[] ids = new int[count];
		// int idIdx = 0;
		// System.out.println("\nIds:");
		// for (int i = 0; i < cells.length; i++) {
		// 	for (int j = 0; j < cells[i].length; j++) {
		// 		ids[idIdx] = j * cells.length + i;
		// 		System.out.print("" + ids[idIdx] + ", ");
		// 		++idIdx;
		// 	}
		// }

		// System.out.println("\nContents After:");
		// for (int i = 0; i < ids.length; i++) {
		// 	int x = ids[i] % cells.length;
		// 	int y = (ids[i] - x) /cells.length;
		// 	System.out.print("" + cells[x][y] + ", ");
		// }

		int x = 11;		// Width of the board
		int y = 11;		// Height of the board
		double d = 0.2;	// The difficulty of the game (percent of cells that are mines)
		int mines = 13;	// Integer number of mines on the board
		assert d >= 0.00 && d < 1.00 && mines >= 0 && mines < (x*y);
 
		new Minesweeper(x, y, d);	// Constructor for % mines
		//new Minesweeper(x, y, mines); // Constructor for int mines
	}
}