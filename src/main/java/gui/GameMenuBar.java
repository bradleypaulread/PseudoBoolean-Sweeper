package main.java.gui;

import main.java.Difficulty;
import main.java.MineSweeper;

import javax.swing.*;

public class GameMenuBar extends JMenuBar {

    private final GameFrame gameFrame;
    private final BoardPanel boardPanel;

    private JRadioButtonMenuItem easyDiffRb;
    private JRadioButtonMenuItem mediumDiffRb;
    private JRadioButtonMenuItem hardDiffRb;

    private JCheckBoxMenuItem singlePointCb;
    private JCheckBoxMenuItem pseudoBooleanCb;
    private JCheckBoxMenuItem probabilityCb;

    public GameMenuBar(GameFrame gameFrame, BoardPanel boardPanel) {
        this.gameFrame = gameFrame;
        this.boardPanel = boardPanel;
        var menu = new JMenu("Options");
        initMenuItems();
        addMenusItems(menu);
        addListeners(menu);
        this.add(menu);
        this.setVisible(true);
    }

    private void initMenuItems() {
        easyDiffRb = new JRadioButtonMenuItem("Easy");
        easyDiffRb.setSelected(true);
        easyDiffRb.setEnabled(false);
        mediumDiffRb = new JRadioButtonMenuItem("Medium");
        mediumDiffRb.setSelected(false);
        hardDiffRb = new JRadioButtonMenuItem("Hard");
        hardDiffRb.setSelected(false);

        singlePointCb = new JCheckBoxMenuItem("Single Point");
        singlePointCb.setSelected(true);
        pseudoBooleanCb = new JCheckBoxMenuItem("Pseudo-Boolean");
        pseudoBooleanCb.setSelected(false);
        probabilityCb = new JCheckBoxMenuItem("Probability");
        probabilityCb.setSelected(false);
    }

    private void addMenusItems(JMenu menu) {
        menu.addSeparator();
        ButtonGroup diffRdGroup = new ButtonGroup();
        diffRdGroup.add(easyDiffRb);
        diffRdGroup.add(mediumDiffRb);
        diffRdGroup.add(hardDiffRb);
        menu.add(easyDiffRb);
        menu.add(mediumDiffRb);
        menu.add(hardDiffRb);

        menu.addSeparator();

        menu.add(singlePointCb);
        menu.add(pseudoBooleanCb);
        menu.add(probabilityCb);

        menu.addSeparator();

    }

    private void addListeners(JMenu menu) {
        easyDiffRb.addActionListener(e -> {
            easyDiffRb.setEnabled(false);
            mediumDiffRb.setEnabled(true);
            hardDiffRb.setEnabled(true);
            changeGameDifficulty();
        });
        mediumDiffRb.addActionListener(e -> {
            easyDiffRb.setEnabled(true);
            mediumDiffRb.setEnabled(false);
            hardDiffRb.setEnabled(true);
            changeGameDifficulty();
        });
        hardDiffRb.addActionListener(e -> {
            easyDiffRb.setEnabled(true);
            mediumDiffRb.setEnabled(true);
            hardDiffRb.setEnabled(false);
            changeGameDifficulty();
        });
    }

    private void changeGameDifficulty() {
        Difficulty diff;
        if (easyDiffRb.isSelected()) {
            diff = Difficulty.BEGINNER;
        } else if (mediumDiffRb.isSelected()) {
            diff = Difficulty.INTERMEDIATE;
        } else {
            diff = Difficulty.EXPERT;
        }
        MineSweeper newGame = new MineSweeper(diff);
        gameFrame.setGame(newGame);
        gameFrame.resetBoard();
        gameFrame.resetGUI();
    }
}
