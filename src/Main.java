/*
 * Main.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */
 
public class Main
{
	public static void main (String[] args)
	{
		int x = 80;		//Width of the board
		int y = 35;		//Height of the board
		double d = 0.2;		//The difficulty of the game, percent of cells that are mines
		int mines = 40;
		assert d >= 0.00 && d < 1.00;
 
		new Minesweeper(x, y, d);
		//new Minesweeper(x, y, mines);
	}
}