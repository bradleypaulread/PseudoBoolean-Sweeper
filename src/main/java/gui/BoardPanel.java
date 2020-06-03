package main.java.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import main.java.*;
import main.java.solvers.ProbabilitySolver;
import org.apache.commons.math3.fraction.BigFraction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class BoardPanel extends JPanel {

    private final BiMap<CellButton, Cell> cellAndBtnMapping;
    private final MineSweeper game;
    private final Set<CellButton> mineHints;
    private final Set<CellButton> safeHints;
    private boolean showProbabilities;

    public BoardPanel(MineSweeper game) {
        super();
        cellAndBtnMapping = HashBiMap.create();
        this.game = game;
        this.mineHints = new HashSet<>();
        this.safeHints = new HashSet<>();
        this.setLayout(new GridLayout(game.getHeight(), game.getWidth()));
        setupGrid();
    }

    public void setShowProbabilities(boolean showProbabilities) {
        this.showProbabilities = showProbabilities;
        if (showProbabilities) {
            showHeatMap(new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines()).getProbabilities());
        }
    }

    public void refreshCellBtns() {
        if (game.getState() != GameState.RUNNING) {
            for (Map.Entry<CellButton, Cell> pair : cellAndBtnMapping.entrySet()) {
                CellButton button = pair.getKey();
                Cell cell = pair.getValue();
                button.setNumber(cell.getNumber());
                button.setDisplayState(DisplayState.OPEN);
                button.setEnabled(false);
            }
        } else {
            if (showProbabilities) {
                showHeatMap(new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines()).getProbabilities());
            }
            for (Map.Entry<CellButton, Cell> pair : cellAndBtnMapping.entrySet()) {
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
            for (CellButton button : cellAndBtnMapping.keySet()) {
                if (button.getDisplayState() == DisplayState.CLOSED) {
                    button.setBackground(button.getDisplayState().colour);
                    button.setEnabled(true);
                }
            }
        } else {
            for (CellButton button : cellAndBtnMapping.keySet()) {
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
                cellAndBtnMapping.put(cellButton, cell);
                this.add(cell.toString(), cellButton);
            }
        }
    }

    private void addLeftClickListener(CellButton button) {
        button.addActionListener(e -> {
            selectButton(button);
            refreshCellBtns();  // for if a 0 was selected
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
                        cellAndBtnMapping.get(button).setState(CellState.CLOSED);
                    } else {
                        button.setEnabled(false);
                        button.setDisplayState(DisplayState.FLAG);
                        cellAndBtnMapping.get(button).setState(CellState.FLAGGED);
                    }
                }
            }
        });
    }

    public void showHeatMap(Map<Cell, BigFraction> probs) {
        if (probs.isEmpty()) {
            return;
        }
        var cellToBtn = cellAndBtnMapping.inverse();
        var bestProb = BigFraction.ONE;
        List<Cell> bestCells = new ArrayList<>();
        for (var pair : probs.entrySet()) {
            Cell cell = pair.getKey();
            BigFraction prob = pair.getValue();
            int probCompare = prob.compareTo(bestProb);
            if (probCompare == -1) {
                bestProb = prob;
                bestCells.clear();
                bestCells.add(cell);
            } else if (probCompare == 0) {
                bestCells.add(cell);
            }
            setCellHeat(cellToBtn.get(cell), prob);
        }
        if (bestProb.compareTo(BigFraction.ZERO) == 0) {
            return;
        }
        bestCells.forEach(cell -> cellToBtn.get(cell).setBackground(new Color(94, 137, 248, 255)));
    }

    private void setCellHeat(CellButton button, BigFraction intensity) {
        final Color[] colours = {
                new Color(202, 250, 162),
                new Color(70, 179, 70),
                new Color(255, 248, 150),
                new Color(255, 127, 127),
                new Color(247, 76, 76),
        };
        Color colour = Color.WHITE;
        if (intensity.compareTo(BigFraction.ZERO) > 0) {
            colour = Color.white;
            for (int i = 0; i < colours.length; i++) {
                colour = colours[i];
                if (intensity.compareTo(new BigFraction(i, colours.length)) <= 0) {
                    break;
                }
            }
        }
        button.setBackground(colour);
        button.setToolTipText("" + intensity.percentageValue() + "...%");
    }

    public boolean knowsHints() {
        return !mineHints.isEmpty() && !safeHints.isEmpty();
    }

    public void setHintCells(Map<Cell, Boolean> knownCells) {
        var cellToBtn = cellAndBtnMapping.inverse();
        for (var pair : knownCells.entrySet()) {
            boolean isMine = pair.getValue();
            CellButton button = cellToBtn.get(pair.getKey());
            if (isMine) {
                mineHints.add(button);
            } else {
                safeHints.add(button);
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
        Cell cell = cellAndBtnMapping.get(button);
        game.openCell(cell.getX(), cell.getY());
        button.setNumber(cell.getNumber());
        button.setDisplayState(DisplayState.OPEN);
        button.setEnabled(false);
    }

}
