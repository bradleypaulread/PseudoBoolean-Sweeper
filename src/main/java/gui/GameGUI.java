package main.java.gui;

import main.java.Difficulty;
import main.java.MineSweeper;

import javax.swing.*;
import java.awt.*;

public class GameGUI extends JFrame {

    // This game reference is passed around the gui a lot
    // not great programming but it will do
    private MineSweeper game;
    private BoardGUI boardGUI;

    public GameGUI(MineSweeper game) {
        this.game = game;
        this.boardGUI = new BoardGUI(this.game);
    }

    public static void main(String[] args) {
        System.out.println("Started");
        MineSweeper game = new MineSweeper(9, 9, 10);
        GameGUI gameGUI = new GameGUI(game);
        gameGUI.buildGUI();
    }

    private void resetBoard() {
        this.game = new MineSweeper(Difficulty.BEGINNER);
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

        this.getContentPane().add(topFrame, BorderLayout.NORTH);
        this.getContentPane().add(boardGUI, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        this.setVisible(true);
    }

}
