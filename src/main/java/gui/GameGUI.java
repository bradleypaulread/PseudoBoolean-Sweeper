package main.java.gui;

import main.java.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameGUI extends JFrame {

    // This game reference is passed around the gui a lot
    // not great programming but it will do
    private MineSweeper game;
    private BoardGUI boardGUI;
    private SolverSwingWorker worker;

    public GameGUI(MineSweeper game) {
        this.game = game;
        this.boardGUI = new BoardGUI(this.game);
    }

    private void resetBoard() {
        this.game = new MineSweeper(game.getWidth(), game.getHeight(), game.getMines());
        this.boardGUI = new BoardGUI(this.game);
    }

    private void resetGUI() {
        this.getContentPane().remove(((BorderLayout) this.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.CENTER));
        this.getContentPane().add(boardGUI, BorderLayout.CENTER);
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
                    boardGUI
            );

            this.worker = new SolverSwingWorker(disableBtns, p, this.game);
            this.worker.execute();
        });
        topFrame.add("Solve", solveBtn);

        this.getContentPane().add(topFrame, BorderLayout.NORTH);
        this.getContentPane().add(boardGUI, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        this.setVisible(true);
    }

}