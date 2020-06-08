package main.java;

import main.java.gui.GameFrame;

/**
 * A launcher class for a game of PseudoBoolean-Sweeper.
 *
 * @author Bradley Read
 * @version 1.0
 * @since 2019-08-30
 */
public class GameLauncher {
    public static void main(String[] args) {
        MineSweeper game = new MineSweeper(Difficulty.BEGINNER);
        GameFrame gui = new GameFrame(game);
        gui.buildGUI();
    }
}