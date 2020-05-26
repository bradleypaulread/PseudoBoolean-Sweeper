package main.java.gui;

import main.java.Cell;
import main.java.MineSweeper;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CellGUI extends JButton {

    private static final Color NORMAL_COLOUR = null;  // new Color(238, 238, 238);
    private static final Color FLAGGED_COLOUR = new Color(233, 237, 149);
    private static final Color HINT_COLOUR = Color.YELLOW;

    private static int cellWidth = 50;
    private Cell cell;
    private boolean clicked;
    private MineSweeper game;
    private boolean flagged;


    public CellGUI(Cell cell, MineSweeper game) {
        this.cell = cell;
        this.flagged = false;
        this.clicked = false;
        this.setPreferredSize(new Dimension(cellWidth, cellWidth));
        this.game = game;
        addActionListeners();
        addMouseListeners();
    }

    public static void setCellWidth(int newCellWidth) {
        cellWidth = newCellWidth;
    }

    private void addMouseListeners() {
        flagCell(this);
    }

    private void addActionListeners() {
        this.addActionListener(e -> {
            clickedCell();
        });
    }

    private void flagCell(CellGUI button) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (isClicked()) {
                        return;
                    }
                    if (flagged) {
                        button.setBackground(NORMAL_COLOUR);
                        flagged = false;
                    } else {
                        flagged = true;
                        button.setBackground(FLAGGED_COLOUR);
                    }
                    button.setEnabled(!flagged);
                }
            }
        });
    }

    private void clickedCell() {
        game.openCell(this.cell.getX(), this.cell.getY());  // Go through game so win/loss conditions are checked
        openCell();
        BoardGUI.refreshBoard();
    }

    public void openCell() {
        this.clicked = true;
        drawText();
        this.setEnabled(false);
    }

    private void drawText() {
        String text = switch (this.cell.getNumber()) {
            case -1 -> "X";
            case 0 -> "";
            default -> "" + this.cell.getNumber();
        };
        this.setUI(new MetalButtonUI() {
            protected Color getDisabledTextColor() {
                return getTextColour();
            }
        });
        this.setText(text);
    }

    @Override
    public Color getForeground() {
        //workaround
        if (!isEnabled()) {
            return getTextColour();
        }
        return super.getForeground();
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public String getReadableCell() {
        return "(" + cell.getX() + ", " + cell.getY() + ")";
    }

    public Cell getCell() {
        return cell;
    }

    public Color getTextColour() {
        return switch (this.cell.getNumber()) {
            case 1 -> new Color(0, 0, 255);    // Blue
            case 2 -> new Color(0, 130, 0);    // Green
            case 3 -> new Color(255, 0, 0);    // Red
            case 4 -> new Color(0, 0, 132);    // Dark Blue
            case 5 -> new Color(132, 0, 0);    // Burgundy Red
            case 6 -> new Color(0, 130, 132);  // Cyan-ish
            case 7 -> new Color(132, 0, 132);  // Purple
            default -> new Color(0, 0, 0);     // Black
        };
    }

}
