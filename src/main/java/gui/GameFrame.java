package main.java.gui;

import main.java.MineSweeper;
import main.java.SolverSwingWorker;
import main.java.solvers.MyPBSolver;
import main.java.solvers.SinglePointSolver;
import main.java.solvers.Solver;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameFrame extends JFrame {

    private final List<Class> solvers;
    private final GameStatsPanel gameStats;
    private final JButton resetBtn;
    private final JButton hintBtn;
    private final JButton assistBtn;
    private final JButton solveBtn;
    private final JButton stopBtn;
    private final JCheckBox probabilityCheckBox;
    // This game reference is passed around the gui a lot
    // not great programming but it will do
    private MineSweeper game;
    private SolverSwingWorker worker;
    private BoardPanel boardPanel;

    private final List<JComponent> disableBtns;

    public GameFrame(MineSweeper game) {
        this.game = game;
        gameStats = new GameStatsPanel(game.getMines());
        this.boardPanel = new BoardPanel(this.game, gameStats);
        this.resetBtn = new JButton("Reset");
        this.hintBtn = new JButton("Hint");
        this.assistBtn = new JButton("Assist");
        this.solveBtn = new JButton("Solve");
        this.stopBtn = new JButton("Stop");
        this.probabilityCheckBox = new JCheckBox("Probabilities");
        disableBtns = List.of(
                boardPanel,
                resetBtn,
                hintBtn,
                assistBtn,
                solveBtn,
                probabilityCheckBox
        );
        solvers = new ArrayList<>();
        solvers.add(SinglePointSolver.class);
    }

    public void setSolvers(List<Class> solvers) {
        this.solvers.clear();
        this.solvers.addAll(solvers);
    }

    public void setGame(MineSweeper newGame) {
        this.game = newGame;
    }

    public void resetBoard() {
        this.game = new MineSweeper(game.getWidth(), game.getHeight(), game.getMines());
        gameStats.reset(game.getMines());
        this.boardPanel = new BoardPanel(this.game, gameStats);
        this.boardPanel.setShowProbabilities(probabilityCheckBox.isSelected());
    }

    public void resetGUI() {
        this.getContentPane().remove(((BorderLayout) this.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.CENTER));
        this.getContentPane().add(boardPanel, BorderLayout.CENTER);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
    }

    public void buildGUI() {
        this.setJMenuBar(new GameMenuBar(this));

        addButtonListeners();

        JPanel topFrame = new JPanel();
        topFrame.setLayout(new FlowLayout());

        topFrame.add(gameStats);
        topFrame.add("Hint Button", hintBtn);
        topFrame.add("Assist Button", assistBtn);
        topFrame.add("Solve Button", solveBtn);
        topFrame.add("Stop Button", stopBtn);
        topFrame.add("Probability Button", probabilityCheckBox);

        this.getContentPane().add(topFrame, BorderLayout.NORTH);
        this.getContentPane().add(boardPanel, BorderLayout.CENTER);
        this.getContentPane().add(resetBtn, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        this.setVisible(true);
    }

    private void addButtonListeners() {
        hintBtn.addActionListener(e -> {
            if (!boardPanel.knowsHints()) {
                // just use a pb solver rather than incremental solvers
                Solver p = new MyPBSolver(game.getCells(), game.getWidth(),
                        game.getHeight(), game.getMines());
                boardPanel.setHintCells(p.getKnownCells());
            }
            boardPanel.showHint();
        });

        assistBtn.addActionListener(e -> {
            this.worker = new SolverSwingWorker.Builder(game)
                    .disableComponents(disableBtns)
                    .withBoardPanel(boardPanel)
                    .withSolvers(solvers)
                    .setLoop(false)
                    .build();

            this.probabilityCheckBox.setSelected(false);
            boardPanel.setShowProbabilities(false);
            this.worker.execute();
        });

        solveBtn.addActionListener(e -> {
            this.worker = new SolverSwingWorker.Builder(game)
                    .disableComponents(disableBtns)
                    .withBoardPanel(boardPanel)
                    .withSolvers(solvers)
                    .setLoop(true)
                    .build();

            this.probabilityCheckBox.setSelected(false);
            boardPanel.setShowProbabilities(false);
            this.worker.execute();
        });

        stopBtn.addActionListener(e -> {
            if (worker != null) {
                worker.stop();
                disableBtns.forEach(b -> b.setEnabled(true));
            }
        });

        resetBtn.addActionListener(e -> {
            resetBoard();
            resetGUI();
        });

        probabilityCheckBox.addActionListener(e -> boardPanel.setShowProbabilities(probabilityCheckBox.isSelected()));
    }

}
