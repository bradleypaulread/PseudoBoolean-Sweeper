package main.java.gui;

import main.java.Cell;
import main.java.CellState;
import main.java.MineSweeper;
import main.java.SolverSwingWorker;
import main.java.solvers.MyPBSolver;
import main.java.solvers.ProbabilitySolver;
import main.java.solvers.Solver;
import org.apache.commons.math3.fraction.BigFraction;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GameFrame extends JFrame {

    // This game reference is passed around the gui a lot
    // not great programming but it will do
    private MineSweeper game;
    private BoardPanel boardPanel;
    private SolverSwingWorker worker;

    public GameFrame(MineSweeper game) {
        this.game = game;
        this.boardPanel = new BoardPanel(this.game);
    }

    private void resetBoard() {
        this.game = new MineSweeper(game.getWidth(), game.getHeight(), game.getMines());
        this.boardPanel = new BoardPanel(this.game);
    }

    private void resetGUI() {
        this.getContentPane().remove(((BorderLayout) this.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.CENTER));
        this.getContentPane().add(boardPanel, BorderLayout.CENTER);
        this.pack();
    }

    public void buildGUI() {
        JPanel topFrame = new JPanel();
        topFrame.setLayout(new FlowLayout());

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            resetBoard();
            resetGUI();
        });
        topFrame.add("Reset Button", resetBtn);

        JButton stopBtn = new JButton("Stop");
        stopBtn.addActionListener(e -> {
            if (worker != null) {
                worker.stop();
            }
        });
        topFrame.add("Stop Button", stopBtn);

        JButton debugPrintKnownCells = new JButton("See Cells");
        debugPrintKnownCells.addActionListener(e -> {
            ProbabilitySolver p = new ProbabilitySolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines());
            List<Cell> mines = p.getMineCells();
            List<Cell> safe = p.getSafeCells();
            System.out.println("Constraints are: ");
            for (String c : p.constraintLog) {
                System.out.println(c);
            }
            System.out.println();
            System.out.println("Mines are: " + mines);
            System.out.println("Safes are: " + safe);
        });
        topFrame.add("See Cells", debugPrintKnownCells);

        JButton solveBtn = new JButton("Solve");
        solveBtn.addActionListener(e -> {
            Solver p = new MyPBSolver(game.getCells(), game.getWidth(),
                    game.getHeight(), game.getMines());

            List<JComponent> disableBtns = List.of(
                    resetBtn,
                    solveBtn,
                    debugPrintKnownCells,
                    boardPanel
            );

            this.worker = new SolverSwingWorker(disableBtns, p, this.game, boardPanel);
            this.worker.execute();
        });
        topFrame.add("Solve", solveBtn);

        JButton hintBtn = new JButton("Hint");
        hintBtn.addActionListener(e -> {
            if (!boardPanel.knowsHints()) {
                Solver p = new MyPBSolver(game.getCells(), game.getWidth(),
                        game.getHeight(), game.getMines());
                boardPanel.setHintCells(p.getKnownCells());
            }
            boardPanel.showHint();
        });
        topFrame.add("Hint Button", hintBtn);

        JButton probabilityBtn = new JButton("Probabilities");
        probabilityBtn.addActionListener(e -> {
            Map<Cell, BigFraction> probs = Map.of(
                    new Cell(0, 0), new BigFraction(1, 5),
                    new Cell(1, 1), new BigFraction(2, 5),
                    new Cell(2, 2), new BigFraction(3, 5),
                    new Cell(3, 3), new BigFraction(4, 5),
                    new Cell(4, 4), new BigFraction(5, 5),
                    new Cell(5, 5), BigFraction.ZERO
            );
            boardPanel.showHeatMap(probs);
        });
        topFrame.add("Probability Button", probabilityBtn);

        this.getContentPane().add(topFrame, BorderLayout.NORTH);
        this.getContentPane().add(boardPanel, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        this.setVisible(true);
    }

}
