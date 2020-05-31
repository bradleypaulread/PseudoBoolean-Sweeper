package main.java.gui;

import main.java.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class BoardPanel extends JPanel {

    private final Map<CellButton, Cell> btnToCell;
    private final MineSweeper game;
    private final Set<CellButton> mineHints;
    private final Set<CellButton> safeHints;

    public BoardPanel(MineSweeper game) {
        super();
        btnToCell = new HashMap<>();
        this.game = game;
        this.mineHints = new HashSet<>();
        this.safeHints = new HashSet<>();
        this.setLayout(new GridLayout(game.getHeight(), game.getWidth()));
        setupGrid();
    }

    public void refreshCellBtns() {
        if (game.getState() != GameState.RUNNING) {
            for (Map.Entry<CellButton, Cell> pair : btnToCell.entrySet()) {
                CellButton button = pair.getKey();
                Cell cell = pair.getValue();
                button.setNumber(cell.getNumber());
                button.setDisplayState(DisplayState.OPEN);
                button.setEnabled(false);
            }
        } else {
            for (Map.Entry<CellButton, Cell> pair : btnToCell.entrySet()) {
                CellButton button = pair.getKey();
                Cell cell = pair.getValue();
                if (cell.getState() == CellState.OPEN &&
                        button.getDisplayState() == DisplayState.CLOSED) {
                    button.setDisplayState(DisplayState.OPEN);
                    button.setNumber(cell.getNumber());
                    button.setEnabled(false);
                } else if (cell.getState() == CellState.FLAGGED) {
                    button.setDisplayState(DisplayState.FLAG);
                }
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        resetHints();
        if (enabled) {
            for (CellButton button : btnToCell.keySet()) {
                if (button.getDisplayState() == DisplayState.CLOSED) {
                    button.setBackground(button.getDisplayState().colour);
                    button.setEnabled(true);
                }
            }
        } else {
            for (CellButton button : btnToCell.keySet()) {
                // make non clicked cells a difference colour
                if (button.getDisplayState() == DisplayState.CLOSED) {
                    button.setBackground(Color.LIGHT_GRAY);
                }
                button.setEnabled(false);
            }
        }
    }

    private void setupGrid() {
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                Cell cell = game.getCell(j, i);
                CellButton cellButton = new CellButton(cell.toString());
                addLeftClickListener(cellButton);
                addRightClickListener(cellButton);
                btnToCell.put(cellButton, cell);
                this.add(cell.toString(), cellButton);
            }
        }
    }

    private void addLeftClickListener(CellButton button) {
        button.addActionListener(e -> {
            selectButton(button);
            refreshCellBtns();  // if a 0 was selected
        });
    }

    private void addRightClickListener(CellButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DisplayState state = button.getDisplayState();
                if (state == DisplayState.OPEN) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (state == DisplayState.FLAG) {
                        button.setEnabled(true);
                        button.setDisplayState(DisplayState.CLOSED);
                        btnToCell.get(button).setState(CellState.CLOSED);
                    } else {
                        button.setEnabled(false);
                        button.setDisplayState(DisplayState.FLAG);
                        btnToCell.get(button).setState(CellState.FLAGGED);
                    }
                }
            }
        });
    }

    private Optional<CellButton> findButtonWithCell(Cell cell) {
        for (Map.Entry<CellButton, Cell> pair : btnToCell.entrySet()) {
            CellButton button = pair.getKey();
            Cell otherCell = pair.getValue();
            if (cell.equals(otherCell)) {
                return Optional.of(button);
            }
        }
        return Optional.empty();
    }

    public boolean knowsHints() {
        return !mineHints.isEmpty() && !safeHints.isEmpty();
    }

    public void setHintCells(Map<Cell, Boolean> knownCells) {
        for (var pair : knownCells.entrySet()) {
            Optional<CellButton> button = findButtonWithCell(pair.getKey());
            if (button.isEmpty()) {
                return;
            }
            boolean isMine = pair.getValue();
            if (isMine) {
                mineHints.add(button.get());
            } else {
                safeHints.add(button.get());
            }
        }
    }

    public void showHint() {
        for (CellButton button : mineHints) {
            DisplayState state = button.getDisplayState();
            if (state != DisplayState.MINE_HINT && state != DisplayState.FLAG) {
                button.setDisplayState(DisplayState.MINE_HINT);
                return;
            }
        }
        for (CellButton button : safeHints) {
            if (button.getDisplayState() != DisplayState.SAFE_HINT) {
                button.setDisplayState(DisplayState.SAFE_HINT);
                return;
            }
        }
    }

    private void resetHints() {
        this.mineHints.forEach(e -> e.setDisplayState(DisplayState.CLOSED));
        this.safeHints.forEach(e -> e.setDisplayState(DisplayState.CLOSED));
        this.mineHints.clear();
        this.safeHints.clear();
    }

    public void selectButton(CellButton button) {
        resetHints();
        Cell cell = btnToCell.get(button);
        game.openCell(cell.getX(), cell.getY());
        button.setNumber(cell.getNumber());
        button.setDisplayState(DisplayState.OPEN);
        button.setEnabled(false);
    }

}
