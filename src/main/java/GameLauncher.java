package main.java;

import main.java.gui.GameGUI;

/**
 * A launcher class for a game of PseudoBoolean-Sweeper.
 *
 * @author Bradley Read
 * @version 1.0
 * @since 2019-08-30
 */
public class GameLauncher {
    public static void main(String[] args) {
        int x = 118; // Width of the board
        int y = 58; // Height of the board
        double diff = 0.2; // The difficulty of the game (percent of cells that are mines)
        int mines = 1412; // Integer number of mines on the board

        // Example of the JSON format needed to create a main.java.MineField
        // Note: no dimensions or number of mines stored (just essentially a 2D array of
        // booleans; true = mine)
        // dimensions and mine count need to be passed manually
        String mineFieldJson = "{\"field\":[[true,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[false,false,true,false,false,false,true,false,false],[false,false,false,true,false,false,false,false,true],[false,false,false,false,false,true,false,false,true],[false,false,false,false,false,false,false,false,false],[false,false,false,false,false,true,false,false,false],[false,false,false,false,false,false,true,false,false],[false,false,false,false,false,false,false,false,true]],\"exploded\":false,\"opened\":false}";

        // new main.java.Minesweeper(mineFieldJson); // Default beginner board

        System.out.println("Started");
        MineSweeper game = new MineSweeper(Difficulty.EXPERT); // Default beginner board
        GameGUI gui = new GameGUI(game);
        gui.buildGUI();

        // Example of how load a game with a pre-generated main.java.MineField JSON
        // new main.java.Minesweeper(9, 9, 10, mineFieldJson);
    }
}