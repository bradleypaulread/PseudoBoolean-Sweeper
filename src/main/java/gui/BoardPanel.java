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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BoardPanel extends JPanel {

    private static final Color[] HEAT_MAP_COLOURS = {
            new Color(202, 250, 162),
            new Color(70, 179, 70),
            new Color(255, 248, 150),
            new Color(255, 127, 127),
            new Color(247, 76, 76),
    };
    private static final Color BEST_CELL_COLOUR = new Color(94, 137, 248, 255);

    private final BiMap<CellButton, Cell> cellAndBtnMapping;
    private final MineSweeper game;
    private final Set<CellButton> mineHints;
    private final Set<CellButton> safeHints;
    private final Set<Cell> bestProbCells;
    private final GameStatsPanel gameStats;
    private Map<Cell, BigFraction> probabilityCache;
    private boolean showProbabilities;

    public BoardPanel(MineSweeper game, GameStatsPanel gameStats) {
        super();
        bestProbCells = new HashSet<>();
        probabilityCache = new HashMap<>();
        cellAndBtnMapping = HashBiMap.create();
        this.gameStats = gameStats;
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
        } else {
            refreshAllCellBtns();
        }
    }

    public void refreshCellBtn(Cell cell, CellButton button) {
        if (game.getState() != GameState.RUNNING) {
            openAllCells();
            return;
        }
        if (cell.getState() == CellState.FLAGGED) {
            button.setDisplayState(DisplayState.FLAG);
        } else if (button.getDisplayState() == DisplayState.CLOSED && showProbabilities) {
            setCellHeat(button, probabilityCache.get(cell));
            if (bestProbCells.contains(cell) && probabilityCache.get(cell).compareTo(BigFraction.ZERO) != 0) {
                button.setBackground(BEST_CELL_COLOUR);
            }
        } else if (button.getDisplayState() == DisplayState.CLOSED) {
            button.setBackground(DisplayState.CLOSED.colour);
        }
    }

    private void openAllCells() {
        for (Map.Entry<CellButton, Cell> pair : cellAndBtnMapping.entrySet()) {
            var button = pair.getKey();
            var cell = pair.getValue();
            game.openCell(cell.getX(), cell.getY());
            button.setNumber(cell.getNumber());

            if (cell.isMine()) {
                if (button.getDisplayState() != DisplayState.FLAG) {
                    if (game.getState() == GameState.WON) {
                        button.setText(CellButton.MINE_TEXT);
                        button.setDisplayState(DisplayState.FLAG);
                    } else {
                        button.setBackground(new Color(255, 127, 127));
                        button.setText(CellButton.FLAGGED_MINE_TEXT);
                    }
                } else {
                    button.setText("✔");
                }
            } else {
                button.setDisplayState(DisplayState.OPEN);
            }

            button.setEnabled(false);
            button.setToolTipText(button.getName());
        }
    }

    public void refreshAllCellBtns() {
        int flags = 0;
        if (game.getState() != GameState.RUNNING) {
            openAllCells();
        } else {
            if (showProbabilities) {
                showHeatMap(new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines()).getProbabilities());
            }
            for (var pair : cellAndBtnMapping.entrySet()) {
                refreshCellBtn(pair.getValue(), pair.getKey());
                if (pair.getValue().getState() == CellState.FLAGGED) {
                    flags += 1;
                }
            }
            gameStats.setMinesLeft(game.getMines() - flags);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        resetHints();
        if (enabled) {
            for (var pair : cellAndBtnMapping.entrySet()) {
                var button = pair.getKey();
                var cell = pair.getValue();
                if (button.getDisplayState() == DisplayState.CLOSED) {
                    refreshCellBtn(cell, button);
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
            boolean opening = selectButton(button);
            if (!opening && !showProbabilities) {
                refreshCellBtn(cellAndBtnMapping.get(button), button);
                return;
            }
            if (opening) {
                refreshOpenCellBtns();  // for if a 0 was selected
            }
            if (showProbabilities) {
                refreshAllCellBtns();
            }
        });
    }

    private void addRightClickListener(CellButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DisplayState state = button.getDisplayState();
                if (state == DisplayState.OPEN || game.getState() != GameState.RUNNING) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (state == DisplayState.FLAG) {
                        gameStats.setMinesLeft(gameStats.getMinesLeft() + 1);
                        cellAndBtnMapping.get(button).setState(CellState.CLOSED);
                        button.setDisplayState(DisplayState.CLOSED);
                        button.setEnabled(true);
                    } else {
                        gameStats.setMinesLeft(gameStats.getMinesLeft() - 1);
                        cellAndBtnMapping.get(button).setState(CellState.FLAGGED);
                        button.setDisplayState(DisplayState.FLAG);
                        button.setEnabled(false);
                    }
                    refreshCellBtn(cellAndBtnMapping.get(button), button);
                }
            }
        });
    }

    public void refreshOpenCellBtns() {
        for (var pair : cellAndBtnMapping.entrySet()) {
            var button = pair.getKey();
            var cell = pair.getValue();
            if (cell.getState() == CellState.OPEN &&
                    button.getDisplayState() == DisplayState.CLOSED) {
                button.setDisplayState(DisplayState.OPEN);
                button.setNumber(cell.getNumber());
                button.setEnabled(false);
            }
        }
    }

    public void showHeatMap(Map<Cell, BigFraction> probs) {
        if (probs.isEmpty()) {
            return;
        }
        probabilityCache = probs;
        var cellToBtn = cellAndBtnMapping.inverse();
        var bestProb = BigFraction.ONE;
        for (var pair : probs.entrySet()) {
            Cell cell = pair.getKey();
            BigFraction prob = pair.getValue();
            int probCompare = prob.compareTo(bestProb);
            if (probCompare < 0) {
                bestProb = prob;
                bestProbCells.clear();
                bestProbCells.add(cell);
            } else if (probCompare == 0) {
                bestProbCells.add(cell);
            }
            setCellHeat(cellToBtn.get(cell), prob);
        }
        if (bestProb.compareTo(BigFraction.ZERO) == 0) {
            return;
        }
        bestProbCells.forEach(cell -> cellToBtn.get(cell).setBackground(BEST_CELL_COLOUR));
    }

    private void setCellHeat(CellButton button, BigFraction intensity) {
        Color colour = Color.WHITE;
        if (intensity.compareTo(BigFraction.ZERO) > 0) {
            for (int i = 0; i < HEAT_MAP_COLOURS.length; i++) {
                colour = HEAT_MAP_COLOURS[i];
                if (intensity.compareTo(new BigFraction(i, HEAT_MAP_COLOURS.length)) <= 0) {
                    break;
                }
            }
        }
        button.setBackground(colour);
        button.setToolTipText("" + intensity.percentageValue() + "%");
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
        this.mineHints.forEach(button -> {
            DisplayState state = switch (cellAndBtnMapping.get(button).getState()) {
                case OPEN -> DisplayState.OPEN;
                case FLAGGED -> DisplayState.FLAG;
                default -> DisplayState.CLOSED;
            };
            button.setDisplayState(state);
        });
        this.safeHints.forEach(button -> {
            DisplayState state = switch (cellAndBtnMapping.get(button).getState()) {
                case OPEN -> DisplayState.OPEN;
                case FLAGGED -> DisplayState.FLAG;
                default -> DisplayState.CLOSED;
            };
            button.setDisplayState(state);
        });
        this.mineHints.clear();
        this.safeHints.clear();
    }

    // return true if an opening occurred (if a 0 was selected)
    public boolean selectButton(CellButton button) {
        gameStats.setMoves(gameStats.getMoves() + 1);
        resetHints();
        Cell cell = cellAndBtnMapping.get(button);
        game.openCell(cell.getX(), cell.getY());
        boolean opening = cell.getNumber() == 0;
        button.setNumber(cell.getNumber());
        button.setDisplayState(DisplayState.OPEN);
        button.setEnabled(false);
        return opening;
    }

}
