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
		int x = 9;		//Width of the board
		int y = 9;		//Height of the board
		int d = 5;		//The difficulty of the game, the number of mines in the board.

 
		new Minesweeper(x, y, d);
	}
}