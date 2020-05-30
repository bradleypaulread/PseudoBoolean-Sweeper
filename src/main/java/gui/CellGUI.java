package main.java.gui;

import main.java.Cell;
import main.java.CellState;
import main.java.DisplayState;
import main.java.MineSweeper;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CellGUI extends JButton {

    private static int cellWidth = 50;
    private final Cell cell;
    private final MineSweeper game;
    private boolean clicked;
    private DisplayState displayState;

    public CellGUI(Cell cell, MineSweeper game) {
        this.cell = cell;
        this.clicked = false;
        this.setPreferredSize(new Dimension(cellWidth, cellWidth));
        this.game = game;
        this.displayState = DisplayState.NORMAL;
        addActionListeners();
        addMouseListeners();
        this.setFont(new Font("", Font.PLAIN, cellWidth / 6));
        this.setText("" + this.cell);
    }

    public static void setCellWidth(int newCellWidth) {
        cellWidth = newCellWidth;
    }

    public DisplayState getDisplayState() {
        return displayState;
    }

    public void setDisplayState(DisplayState displayState) {
        this.displayState = displayState;
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
                // Do nothing if cell has been clicked
                if (isClicked()) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (cell.getState() == CellState.FLAGGED) {
                        cell.setState(CellState.CLOSED);
                    } else {
                        cell.setState(CellState.FLAGGED);
                    }
                    displayState = displayState != DisplayState.FLAG ? DisplayState.FLAG : DisplayState.NORMAL;
                    updateCell();
                }
            }
        });
    }

    private void clickedCell() {
        System.out.println("Clicked " + this.cell);
        game.openCell(this.cell.getX(), this.cell.getY());  // Go through game so win/loss conditions are checked
        openCell();
        BoardGUI.refreshCellGUIs();  // Refresh whole board as a 0 could have been clicked and opened more cells
    }

    public void openCell() {
        this.clicked = true;
        updateCell();
    }

    private void drawText() {
        this.setFont(new Font("", Font.BOLD, (cellWidth / 3)));
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

    public void updateCell() {
        if (this.displayState == DisplayState.FLAG) {
            this.setEnabled(false);
        } else {
            if (this.clicked) {
                drawText();
            }
            this.setEnabled(!this.clicked);
        }
        this.setBackground(this.displayState.colour);
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

    public Cell getCell() {
        return cell;
    }

    private Color getTextColour() {
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
