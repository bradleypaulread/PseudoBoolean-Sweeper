package main.java.gui;

import main.java.CellState;
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
    private static MineSweeper game;

    public BoardGUI(MineSweeper game) {
        super();
        cells = new ArrayList<>();
        BoardGUI.game = game;
        GridLayout grid = new GridLayout(game.getHeight(), game.getWidth());
        this.setLayout(grid);
        setupGrid();
        cells.forEach(c -> {
            this.add("" + c.getCell(), c);
        });
    }

    public static void refreshCellGUIs() {
        if (game.getState() != GameState.RUNNING) {
            for (CellGUI cell : cells) {
                cell.openCell();
            }
            return;
        }
        for (CellGUI cell : cells) {
            if (!cell.isClicked() && cell.getCell().getState() == CellState.OPEN) {
                cell.openCell();
            } else if (cell.getCell().getState() == CellState.FLAGGED) {
                cell.setDisplayState(DisplayState.FLAG);
                cell.updateCell();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            for (CellGUI cell : cells) {
                if (cell.getCell().getState() == CellState.CLOSED) {
                    cell.setBackground(null);
                    cell.setEnabled(true);
                }
            }
        } else {
            for (CellGUI cell : cells) {
                if (cell.getCell().getState() == CellState.CLOSED) {
                    cell.setBackground(Color.LIGHT_GRAY);
                }
                cell.setEnabled(false);
            }
        }
    }

    private void setupGrid() {
        for (int i = 0; i < game.getWidth(); i++) {
            for (int j = 0; j < game.getHeight(); j++) {
                cells.add(new CellGUI(game.getCell(i, j), game));
            }
        }

        Collections.sort(cells, Comparator.comparingInt((CellGUI a) -> a.getCell().getY())
                .thenComparingInt(a -> a.getCell().getX()));
    }

}
