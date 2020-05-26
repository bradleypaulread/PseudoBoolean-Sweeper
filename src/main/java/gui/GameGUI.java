package main.java.gui;

import main.java.Cell;
import main.java.MineSweeper;
import main.java.MyPBSolver;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameGUI extends JFrame {

    // This game reference is passed around the gui a lot
    // not great programming but it will do
    private MineSweeper game;
    private BoardGUI boardGUI;

    public GameGUI(MineSweeper game) {
        this.game = game;
        this.boardGUI = new BoardGUI(this.game);
    }

    private void resetBoard() {
        this.game = new MineSweeper(game.getWidth(), game.getHeight(), game.getMines());
        this.boardGUI = new BoardGUI(this.game);
    }

    private void resetGUI() {
        this.getContentPane().remove(((BorderLayout) this.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER));
        this.getContentPane().add(boardGUI, BorderLayout.CENTER);
        this.pack();
    }

    public void buildGUI() {
        JPanel topFrame = new JPanel();
        topFrame.setLayout(new FlowLayout());

        JButton tmpBtn = new JButton("Tmp Btn");
        tmpBtn.addActionListener(e -> {
            resetBoard();
            resetGUI();
        });
        topFrame.add("Temp Btn", tmpBtn);

        JButton pbMines = new JButton("Mines");
        pbMines.addActionListener(e -> {
            MyPBSolver p = new MyPBSolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines());
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
        topFrame.add("See Cells", pbMines);

        JButton solveBtn = new JButton("Solve");
        solveBtn.addActionListener(e -> {
            boolean somethingChanged = true;
            while (somethingChanged) {
                MyPBSolver p = new MyPBSolver(game.getCells(), game.getWidth(), game.getHeight(), game.getMines());
                List<Cell> mines = p.getMineCells();
                List<Cell> safe = p.getSafeCells();
                somethingChanged = false;
                for (Cell cell : mines) {
                    if (!cell.isFlagged()) {
                        somethingChanged = true;
                        cell.setFlagged(true);
                    }
                }

                for (Cell cell : safe) {
                    if (!cell.isOpen()) {
                        somethingChanged = true;
                        game.openCell(cell.getX(), cell.getY());
                    }
                }
                boardGUI.refreshCellGUIs();
            }
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
