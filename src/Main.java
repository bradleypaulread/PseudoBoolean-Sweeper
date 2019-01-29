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
		int x = 20;		//Width of the board
		int y = 20;		//Height of the board
		double d = 0.25;		//The difficulty of the game, percent of cells that are mines
		
		assert d >= 0.00 && d < 1.00;
 
		new Minesweeper(x, y, d);
	}
}