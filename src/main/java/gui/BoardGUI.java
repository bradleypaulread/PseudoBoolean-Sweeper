package main.java.gui;

import main.java.DisplayState;
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

    public static void refreshCellGUIs() {
        if (game.getGameState() != GameState.RUNNING) {
            for (CellGUI cell : cells) {
                cell.openCell();
            }
            return;
        }
        for (CellGUI cell : cells) {
            if (!cell.isClicked() && cell.getCell().isOpen()) {
                cell.openCell();
            } else if (cell.getCell().isFlagged() && cell.getDisplayState() != DisplayState.FLAG) {
                cell.setDisplayState(DisplayState.FLAG);
                cell.updateCell();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            for (CellGUI cell : cells) {
                if (!cell.getCell().isOpen() && !cell.getCell().isFlagged()) {
                    cell.setBackground(Color.LIGHT_GRAY);
                }
                cell.setEnabled(false);
            }
        } else {
            for (CellGUI cell : cells) {
                if (!cell.getCell().isOpen() && !cell.getCell().isFlagged()) {
                    cell.setBackground(null);
                    cell.setEnabled(true);
                }
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
