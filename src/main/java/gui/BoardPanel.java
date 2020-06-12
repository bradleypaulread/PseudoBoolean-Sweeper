package main.java.gui;

import main.java.*;
import main.java.solvers.ProbabilitySolver;
import org.apache.commons.math3.fraction.BigFraction;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BoardPanel extends JPanel {

    private static final Color[] HEAT_MAP_COLOURS = {
            new Color(202, 250, 162),
            new Color(70, 179, 70),
            new Color(255, 248, 150),
            new Color(255, 127, 127),
            new Color(247, 76, 76),
    };
    private static final Color BEST_CELL_COLOUR = new Color(94, 137, 248, 255);

    private final Map<Cell, CellButton> cellAndBtnMapping;
    private final MineSweeper game;
    private final List<Cell> unsetCellHints;  // list for cells that are not yet set to hints in the gui
    private final Set<Cell> bestProbCells;
    private final GameStatsPanel gameStats;
    private Map<Cell, Boolean> cellHints;
    private boolean showProbabilities;

    public BoardPanel(MineSweeper game, GameStatsPanel gameStats) {
        super();
        bestProbCells = new HashSet<>();
        cellAndBtnMapping = new HashMap<>();
        this.gameStats = gameStats;
        this.game = game;
        this.cellHints = new HashMap<>();
        this.unsetCellHints = new ArrayList<>();
        this.setLayout(new GridLayout(game.getHeight(), game.getWidth()));
        setupGrid();
    }

    private void setupGrid() {
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                Cell cell = game.getCell(j, i);
                CellButton button = new CellButton(cell.toString());
                addLeftClickListener(button, cell);
                addRightClickListener(button, cell);
                cellAndBtnMapping.put(cell, button);
                this.add(cell.toString(), button);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        resetHints();
        if (enabled) {
            for (var pair : cellAndBtnMapping.entrySet()) {
                var cell = pair.getKey();
                var button = pair.getValue();
                if (cell.getState() == CellState.CLOSED) {
                    if (!showProbabilities) {
                        button.setDisplayState(DisplayState.CLOSED);
                    }
                    button.setEnabled(true);
                }
            }
        } else {
            cellAndBtnMapping.values().forEach(button -> {
                if (button.getDisplayState() == DisplayState.CLOSED) {
                    button.setBackground(Color.LIGHT_GRAY);
                }
                button.setEnabled(false);
            });
            if (showProbabilities) {
                showHeatMap(new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines()).getProbabilities());
            }
        }
    }

    public CellButton getButtonFromCell(Cell cell) {
        return cellAndBtnMapping.get(cell);
    }

    private void addLeftClickListener(CellButton button, Cell cell) {
        button.addActionListener(click -> {
            selectButton(button, cell);
            gameStats.setMoves(gameStats.getMoves() + 1);
        });
    }

    public void selectButton(CellButton button, Cell cell) {
        resetHints();
        game.openCell(cell.getX(), cell.getY());
        int num = cell.getNumber();
        openCellButton(button, num);
        if (game.getState() != GameState.RUNNING) {
            endGame();
            if (game.getState() == GameState.LOST) {
                button.setUI(new MetalButtonUI() {
                    protected Color getDisabledTextColor() {
                        return Color.WHITE;
                    }
                });
            }
        } else if (num == 0) {  // if an opening was made, collect opening cells and update them
            clearOpening(cell);
        }
        // Todo: could speed up visualisation if all 0% cells were selected first
        //  before redoing heatmap
        if (showProbabilities && game.getState() == GameState.RUNNING) {
            showHeatMap(new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines()).getProbabilities());
        }
    }

    public void flagButton(CellButton button, Cell cell) {
        int newMinesLeft = gameStats.getMinesLeft();
        var displayState = button.getDisplayState() == DisplayState.FLAG ? DisplayState.CLOSED : DisplayState.FLAG;
        var cellState = button.getDisplayState() == DisplayState.FLAG ? CellState.CLOSED : CellState.FLAGGED;
        if (displayState == DisplayState.FLAG) {
            newMinesLeft -= 1;
        } else {
            newMinesLeft += 1;
        }
        gameStats.setMinesLeft(newMinesLeft);
        cell.setState(cellState);
        button.setDisplayState(displayState);
        button.setEnabled(displayState == DisplayState.CLOSED);
    }

    private void addRightClickListener(CellButton button, Cell cell) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // only execute on right click
                if (SwingUtilities.isRightMouseButton(e)) {
                    var state = button.getDisplayState();
                    if (state == DisplayState.OPEN) {
                        return;
                    }
                    flagButton(button, cell);
                }
            }
        });
    }

    private void openCellButton(CellButton button, int num) {
        button.setDisplayState(DisplayState.OPEN);
        button.setNumber(num);
        button.setEnabled(false);
    }

    private void clearOpening(Cell origin) {
        Set<Cell> checkedCells = new HashSet<>();
        checkedCells.add(origin);

        Predicate<Cell> openPredicate = cell -> cell.getState() == CellState.OPEN;

        var neighbours = game.getBoard().getNeighbours(origin.getX(), origin.getY()).stream()
                .filter(openPredicate)
                .collect(Collectors.toList());

        while (!neighbours.isEmpty()) {
            List<Cell> openNeighbours = new ArrayList<>();
            for (Cell cell : neighbours) {
                if (checkedCells.contains(cell)) {
                    continue;
                }
                int num = cell.getNumber();
                CellButton button = cellAndBtnMapping.get(cell);
                openCellButton(button, num);
                checkedCells.add(cell);
                if (num == 0) {
                    openNeighbours.addAll(game.getBoard().getNeighbours(cell.getX(), cell.getY()));
                }
            }
            neighbours.clear();
            neighbours = openNeighbours.stream()
                    .filter(openPredicate)
                    .collect(Collectors.toList());
        }
    }

    private void endGame() {
        gameStats.stopTimer();
        openAllCellButtons();
    }

    private void openAllCellButtons() {
        for (var pair : cellAndBtnMapping.entrySet()) {
            var cell = pair.getKey();
            var button = pair.getValue();
            int num = cell.getNumber();
            if (cell.isMine()) {
                if (button.getDisplayState() == DisplayState.FLAG || game.getState() == GameState.WON) {
                    button.setDisplayState(DisplayState.FLAG);
                    button.setText(CellButton.FLAGGED_MINE_TEXT);
                } else {
                    button.setBackground(new Color(255, 127, 127));
                    button.setText(CellButton.MINE_TEXT);
                }
            } else {
                openCellButton(button, num);
                button.setDisplayState(DisplayState.OPEN);
            }
            button.setEnabled(false);
        }
    }

    public boolean knowsHints() {
        return !cellHints.isEmpty();
    }

    public void setHintCells(Map<Cell, Boolean> knownCells) {
        resetHints();
        this.cellHints = knownCells;
        this.unsetCellHints.addAll(cellHints.keySet());
    }

    public void showHint() {
        while (!unsetCellHints.isEmpty()) {
            Cell cell = unsetCellHints.remove(unsetCellHints.size() - 1);
            boolean isMine = cellHints.get(cell);
            CellButton button = cellAndBtnMapping.get(cell);
            if (isMine) {
                // dont show hint if cell is already flagged
                if (button.getDisplayState() == DisplayState.FLAG) {
                    continue;
                }
                button.setDisplayState(DisplayState.MINE_HINT);
            } else {
                button.setDisplayState(DisplayState.SAFE_HINT);
            }
            break;
        }
    }

    private void resetHints() {
        this.cellHints.keySet().forEach(cell -> {
            DisplayState state = switch (cell.getState()) {
                case OPEN -> DisplayState.OPEN;
                case FLAGGED -> DisplayState.FLAG;
                default -> DisplayState.CLOSED;
            };
            cellAndBtnMapping.get(cell).setDisplayState(state);
        });
        this.unsetCellHints.clear();
        this.cellHints.clear();
    }

    public void setShowProbabilities(boolean showProbabilities) {
        this.showProbabilities = showProbabilities;
        if (showProbabilities) {
            showHeatMap(new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines()).getProbabilities());
        } else {
            normaliseAllCellButtons();
        }
    }

    private void normaliseAllCellButtons() {
        for (var pair : cellAndBtnMapping.entrySet()) {
            Cell cell = pair.getKey();
            CellButton button = pair.getValue();
            DisplayState state = switch (cell.getState()) {
                case OPEN -> DisplayState.OPEN;
                case FLAGGED -> DisplayState.FLAG;
                default -> DisplayState.CLOSED;
            };
            button.setDisplayState(state);
            button.setToolTipText(button.getName());
        }
    }

    public void showHeatMap(Map<Cell, BigFraction> probs) {
        if (probs.isEmpty()) {
            return;
        }
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
            setCellHeat(cellAndBtnMapping.get(cell), prob);
        }
        if (bestProb.compareTo(BigFraction.ZERO) == 0) {
            return;
        }
        bestProbCells.forEach(cell -> cellAndBtnMapping.get(cell).setBackground(BEST_CELL_COLOUR));
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

}