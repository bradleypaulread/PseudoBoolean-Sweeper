package main.java.gui;

import main.java.Difficulty;
import main.java.MineSweeper;
import main.java.solvers.MyPBSolver;
import main.java.solvers.ProbabilitySolver;
import main.java.solvers.SinglePointSolver;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameMenuBar extends JMenuBar {

    private final GameFrame gameFrame;

    private JRadioButtonMenuItem easyDiffRb;
    private JRadioButtonMenuItem mediumDiffRb;
    private JRadioButtonMenuItem hardDiffRb;

    private JCheckBoxMenuItem singlePointCb;
    private JCheckBoxMenuItem pseudoBooleanCb;
    private JCheckBoxMenuItem probabilityCb;

    public GameMenuBar(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        var menu = new JMenu("Options");
        initMenuItems();
        addMenusItems(menu);
        addListeners();
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

    private void addListeners() {
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

        singlePointCb.addActionListener(e -> setSolvers());
        pseudoBooleanCb.addActionListener(e -> setSolvers());
        probabilityCb.addActionListener(e -> setSolvers());
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

    private void setSolvers() {
        List<Class> solvers = new ArrayList<>();
        if (singlePointCb.isSelected()) {
            solvers.add(SinglePointSolver.class);
        }
        if (pseudoBooleanCb.isSelected()) {
            solvers.add(MyPBSolver.class);
        }
        if (probabilityCb.isSelected()) {
            solvers.add(ProbabilitySolver.class);
        }
        gameFrame.setSolvers(solvers);
    }
}
