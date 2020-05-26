package main.java.gui;

import main.java.GameState;
import main.java.MineSweeper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BoardGUI extends JPanel {

    private static List<CellGUI> cells;
    private GridLayout grid;

    private static MineSweeper game;

    public BoardGUI(MineSweeper game) {
        super();
        cells = new ArrayList<>();
        this.game = game;
        grid = new GridLayout(game.getHeight(), game.getWidth());
        this.setLayout(grid);
        setupGrid();
        cells.forEach(c -> {
            this.add("" + c.getCell(), c);
        });
    }

    public static void refreshBoard() {
        for (CellGUI cell : cells) {
            if (game.getGameState() != GameState.RUNNING) {
                cell.openCell();
            } else if (!cell.isClicked() && cell.getCell().isOpen()) {
                cell.openCell();
            }
        }
    }

    private void setupGrid() {
        for (int i = 0; i < game.getWidth(); i++) {
            for (int j = 0; j < game.getHeight(); j++) {
                cells.add(new CellGUI(game.getCell(i, j), game));
            }
        }

        Collections.sort(cells, new Comparator<CellGUI>() {
            public int compare(CellGUI a, CellGUI b) {
                int result = Integer.compare(a.getCell().getY(), b.getCell().getY());
                if (result == 0) {
                    result = Integer.compare(a.getCell().getX(), b.getCell().getX());
                }
                return result;
            }
        });
    }

}
