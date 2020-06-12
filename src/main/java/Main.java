package main.java;

import main.java.gui.GameFrame;

public class Main {
    public static void main(String[] args) {
        MineSweeper game = new MineSweeper(Difficulty.BEGINNER);
        GameFrame gui = new GameFrame(game);
        gui.buildGUI();
    }
}