
/*
 * Minesweeper.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.TitledBorder;

public class Minesweeper extends JFrame {

	private static final long serialVersionUID = 1L;

	// Password to unlock MineField.java
	private final String PASSWORD = "hello";

	// Swing components (buttons etc.)
	private JButton resetBtn = new JButton("Reset");
	private JButton stopBtn = new JButton("Stop");

	private JButton ptHintBtn = new JButton("Hint");
	private JButton ptAssistBtn = new JButton("Assist");
	private JButton ptSolveBtn = new JButton("Solve");

	private JButton SATHintBtn = new JButton("Hint");
	private JButton SATAssistBtn = new JButton("Assist");
	private JButton SATSolveBtn = new JButton("Solve");
	private JButton SATProbBtn = new JButton("Show Probabilities");

	private JButton fullAutoBtn = new JButton("Full Auto");

	private JLabel movesLbl = new JLabel();
	private JLabel minesLbl = new JLabel();
	private JCheckBoxMenuItem debugCb = new JCheckBoxMenuItem("Debug Mode");
	private JCheckBoxMenuItem strategyCb = new JCheckBoxMenuItem("Use Strategy");
	private JRadioButtonMenuItem diffEasyRb = new JRadioButtonMenuItem("Easy");
	private JRadioButtonMenuItem diffMediumRb = new JRadioButtonMenuItem("Medium");
	private JRadioButtonMenuItem diffHardRb = new JRadioButtonMenuItem("Hard");
	private JMenuItem customGameItem = new JMenuItem("Custom Game");
	private JMenuItem startSimItem = new JMenuItem("Start Simulation");
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menu = new JMenu("File");

	private MineField mineField; // Blackbox minefield
	private int width, height; // Width and Height of board (number of cells =
								// width*height)
	private Cell[][] cells; // 2D array to store all cells
	private List<Cell> hintCells = new ArrayList<Cell>(); //
	private int noOfMines; // Number of mines
	private Board board; // Board instance, where cells appearance is processed
	private boolean debug; // If debug information should be printed to console
	private boolean prob = false;
	private boolean isGameOver; // True if the game has been lost or won
	private int moves = 0; // Number if moves made by the player.
	private int currentGameTime;
	private Timer gameTimer;
	private long startTime;
	private long endTime;
	private int minesLeft;
	private boolean gameWon;

	private BoardSolver solver;

	public Minesweeper(int x, int y, double d) {
		// Cast number of mines down to integer value
		setup(x, y, (int) ((x * y) * d));
	}

	public Minesweeper(int x, int y, int d) {
		setup(x, y, d);
	}

	public Minesweeper(Difficulty diff) {
		int x, y, d;
		switch (diff) {
		case EASY:
			x = 9;
			y = 9;
			d = 10;
			break;
		case MEDIUM:
			x = 16;
			y = 16;
			d = 40;
			break;
		case HARD:
			x = 30;
			y = 16;
			d = 99;
			break;
		default:
			x = 9;
			y = 9;
			d = 10;
			break;
		}
		setup(x, y, d);
	}

	public Minesweeper() {
		this(Difficulty.EASY);
	}

	// For use when no GUI is wanted
	public Minesweeper(Difficulty d, MineField mf) {
		switch (d) {
		case EASY:
			width = 9;
			height = 9;
			noOfMines = 10;
			minesLeft = 10;
			break;
		case MEDIUM:
			width = 16;
			height = 16;
			noOfMines = 40;
			minesLeft = 40;
			break;
		case HARD:
			width = 30;
			height = 16;
			noOfMines = 99;
			minesLeft = 99;
			break;
		default:
			break;
		}
		isGameOver = false;
		moves = 0;
		minesLeft = noOfMines;
		cells = new Cell[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell(i, j);
			}
		}
		gameWon = false;
		// Reset board to a fresh setting
		mineField = mf;
		startTime = System.nanoTime();
	}

	private void setup(int x, int y, int d) {
		width = x;
		height = y;
		noOfMines = d;
		minesLeft = d;

		// Load interface components
		board = new Board(this, x, y);
		loadUI();
		loadFileMenu();
		Container fl = new Container();
		fl.add(board);
		fl.setLayout(new FlowLayout());
		add(fl, BorderLayout.CENTER);

		// Reset board to a fresh setting
		reset();

		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
		startTime = System.nanoTime();
		solver = new BoardSolver(this);
	}

	/**
	 * Load buttons to the JFrame and assign their action listeners.
	 */
	private void loadUI() {
		SolverThreadWrapper[] thead = new SolverThreadWrapper[1];
		Container controlBtns = new Container();
		JPanel ptBtns = new JPanel();
		JPanel SATBtns = new JPanel();
		JPanel stats = new JPanel();
		Container topBar = new Container();
		controlBtns.setLayout(new FlowLayout());
		topBar.setLayout(new BorderLayout());

		TitledBorder statsTitle = new TitledBorder("Stats");
		statsTitle.setTitleJustification(TitledBorder.CENTER);
		stats.setBorder(new TitledBorder(statsTitle));
		stats.setLayout(new FlowLayout());
		stats.add(movesLbl);
		stats.add(minesLbl);
		movesLbl.setText(Integer.toString(moves));
		minesLbl.setText(Integer.toString(minesLeft));

		JLabel timeLbl = new JLabel();
		stats.add(timeLbl);
		// Tick every second
		gameTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentGameTime++;
				if (currentGameTime < 100000) {
					timeLbl.setText("~ Time: " + Integer.toString(currentGameTime));
				} else {
					((Timer) (e.getSource())).stop();
				}
			}
		});
		gameTimer.setInitialDelay(0);
		gameTimer.start();

		TitledBorder ptTitle = new TitledBorder("Pattern Matching");
		ptTitle.setTitleJustification(TitledBorder.CENTER);
		ptBtns.setBorder(ptTitle);
		ptBtns.setLayout(new FlowLayout());
		ptBtns.add(ptSolveBtn);
		ptBtns.add(ptAssistBtn);
		ptBtns.add(ptHintBtn);

		TitledBorder SATtitle = new TitledBorder("SAT");
		SATtitle.setTitleJustification(TitledBorder.CENTER);
		SATBtns.setBorder(SATtitle);
		SATBtns.setLayout(new FlowLayout());
		SATBtns.add(SATHintBtn);
		SATBtns.add(SATAssistBtn);
		SATBtns.add(SATSolveBtn);
		SATBtns.add(SATProbBtn);

		controlBtns.add(ptBtns);
		stopBtn.setEnabled(false);
		controlBtns.add(stopBtn);
		controlBtns.add(fullAutoBtn);

		JButton randCellBtn = new JButton("Rand. Cell");

		controlBtns.add(randCellBtn);
		controlBtns.add(SATBtns);

		topBar.add(stats, BorderLayout.NORTH);
		topBar.add(controlBtns, BorderLayout.SOUTH);

		this.add(topBar, BorderLayout.NORTH);
		this.add(resetBtn, BorderLayout.SOUTH);

		// Add icon to JFrame and Taskbar
		List<Image> icons = new ArrayList<Image>();
		Image img4 = Toolkit.getDefaultToolkit().getImage("resources/bomb64x64.png");
		icons.add(img4);
		this.setIconImages(icons);

		resetBtn.addActionListener(e -> reset());

		randCellBtn.addActionListener(e -> {
			// solver.testSATSovle();
			// solver.selectRandomCell();
			// refresh();
		});

		ptHintBtn.addActionListener(e -> solver.patternMatchHint());

		SATHintBtn.addActionListener(e -> {
			SolverThreadWrapper t1 = new SolverThreadWrapper(this, true, true);
			thead[0] = t1;
			stopBtn.setEnabled(true);
			disableAllBtns();
		});

		ptAssistBtn.addActionListener(e -> solver.patternMatch());

		SATAssistBtn.addActionListener(e -> {
			SolverThreadWrapper t1 = new SolverThreadWrapper(this, false, false, false, true, false);
			thead[0] = t1;
			stopBtn.setEnabled(true);
			disableAllBtns();
		});

		SATProbBtn.addActionListener(e -> {
			SolverThreadWrapper t1 = new SolverThreadWrapper(this);
			thead[0] = t1;
			stopBtn.setEnabled(true);
			disableAllBtns();
		});

		ptSolveBtn.addActionListener(e -> {
			// Perform the assist action until no more safe moves exist
			disableAllBtns();
			while (solver.patternMatch())
				;
			if (!isGameOver) {
				enableAllBtns();
			}
		});

		SATSolveBtn.addActionListener(e -> {
			// Perform the assist action until no more safe moves exist
			disableAllBtns();
			SolverThreadWrapper t1 = new SolverThreadWrapper(this, false, true, false, true, false);
			thead[0] = t1;
			stopBtn.setEnabled(true);
		});

		fullAutoBtn.addActionListener(e -> {
			// Perform the assist action until no more safe moves exist
			disableAllBtns();
			SolverThreadWrapper t1 = new SolverThreadWrapper(this, true, true, true, true, false);
			thead[0] = t1;
			stopBtn.setEnabled(true);
		});

		stopBtn.addActionListener(e -> {
			thead[0].end();
			stopBtn.setEnabled(false);
			enableAllBtns();
		});
	}

	/**
	 * Load the File menu component options and add each option's action listener
	 * code.
	 */
	private void loadFileMenu() {
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Playability settings.");
		menuBar.add(menu);

		ButtonGroup diffRdGroup = new ButtonGroup();

		// When option selected invert the debug variable.
		debugCb.addActionListener(e -> {
			debug = !debug;
			refresh();
		});

		strategyCb.addActionListener(e -> {
			prob = !prob;
		});

		menu.add(debugCb);
		menu.add(strategyCb);
		menu.addSeparator();
		diffEasyRb.setSelected(true);
		diffEasyRb.setEnabled(false);
		diffEasyRb.addActionListener(e -> {
			Minesweeper newGame = new Minesweeper(Difficulty.EASY);
			newGame.getDiffEasyRb().setSelected(true);
			newGame.getDiffEasyRb().setEnabled(false);
			newGame.getDiffMediumRb().setEnabled(true);
			newGame.getDiffHardRb().setEnabled(true);
			newGame.setDebug(debugCb.isSelected());
			newGame.getDebugCB().setSelected(debugCb.isSelected());
			setVisible(false);
			dispose();
		});
		diffRdGroup.add(diffEasyRb);
		menu.add(diffEasyRb);

		diffMediumRb.addActionListener(e -> {
			Minesweeper newGame = new Minesweeper(Difficulty.MEDIUM);
			newGame.getDiffMediumRb().setSelected(true);
			newGame.getDiffEasyRb().setEnabled(true);
			newGame.getDiffMediumRb().setEnabled(false);
			newGame.getDiffHardRb().setEnabled(true);
			newGame.setDebug(debugCb.isSelected());
			newGame.getDebugCB().setSelected(debugCb.isSelected());
			setVisible(false);
			dispose();
		});
		diffRdGroup.add(diffMediumRb);
		menu.add(diffMediumRb);

		diffHardRb.addActionListener(e -> {
			Minesweeper newGame = new Minesweeper(Difficulty.HARD);
			newGame.getDiffHardRb().setSelected(true);
			newGame.getDiffEasyRb().setEnabled(true);
			newGame.getDiffMediumRb().setEnabled(true);
			newGame.getDiffHardRb().setEnabled(false);
			newGame.setDebug(debugCb.isSelected());
			newGame.getDebugCB().setSelected(debugCb.isSelected());
			setVisible(false);
			dispose();
		});
		diffRdGroup.add(diffHardRb);
		menu.add(diffHardRb);

		startSimItem.addActionListener(e -> {
			// solver.SATStratergy();
			// JFrame simWindow = new JFrame("Simulating...");
			// simWindow.add(new JLabel("Simulating..."));
			// simWindow.setSize(new Dimension(100, 100));
			// simWindow.setVisible(true);
			// simWindow.setLocationRelativeTo(null);
			// GameSimulation gameSim = new GameSimulation(5);
			// gameSim.genericSim();
			// simWindow.setVisible(false);
			// simWindow.dispose();
		});
		menu.addSeparator();
		menu.add(startSimItem);

		customGameItem.addActionListener(e -> {
			// Formats to format and parse numbers
			NumberFormat newWidth;
			NumberFormat newHeight;
			NumberFormat newNoOfMines;

			newWidth = NumberFormat.getIntegerInstance();
			newHeight = NumberFormat.getIntegerInstance();
			newNoOfMines = NumberFormat.getIntegerInstance();

			// Fields for data entry
			JFormattedTextField widthField = new JFormattedTextField(newWidth);
			JFormattedTextField heightField = new JFormattedTextField(newHeight);
			JFormattedTextField noOfMinesField = new JFormattedTextField(newNoOfMines);

			widthField.setValue(0);
			heightField.setValue(0);
			noOfMinesField.setValue(0);

			JPanel fieldPane = new JPanel(new GridLayout(0, 1));
			fieldPane.add(widthField);
			fieldPane.add(heightField);
			fieldPane.add(noOfMinesField);

			JFrame frame = new JFrame("Custom Board Test");
			JButton btn = new JButton("Create");
			btn.addActionListener(e2 -> {
				int newX = ((Number) widthField.getValue()).intValue();
				int newY = ((Number) heightField.getValue()).intValue();
				int newMines = ((Number) noOfMinesField.getValue()).intValue();
				Minesweeper newGame = new Minesweeper(newX, newY, newMines);
				newGame.getDiffEasyRb().setEnabled(true);
				newGame.getDiffMediumRb().setEnabled(true);
				newGame.getDiffHardRb().setEnabled(true);
				newGame.setDebug(debugCb.isSelected());
				newGame.getDebugCB().setSelected(debugCb.isSelected());
				frame.setVisible(false);
				frame.dispose();
				setVisible(false);
				dispose();
			});
			fieldPane.add(btn);
			frame.add(fieldPane);
			// Display the window.
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
		menu.add(customGameItem);

		this.setJMenuBar(menuBar);
	}

	/**
	 * Redraw the board, updating all cells appearance and behaviour.
	 */
	public void refresh() {
		board.repaint(10);
	}

	/**
	 * Called when a cell is wished to be cleared. Will not affect flagged cells.
	 * Clears hint cells and
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
	public void select(int x, int y) {
		// Dont perform any behaviour if cell is flagged or game has already been
		// won/lost
		if (cells[x][y].isFlagged() || isGameOver) {
			return;
		}

		// If there are any cells set as hints,
		// reset them back to plain closed cells
		resetHints();

		// Reset probabilies
		resetProbs();

		if (debug) {
			debug(x, y);
		}

		incrementMoves();

		// How many mines are around the cell
		// x and y has to be reversed as MineField.java takes parameters
		// (height, width) not (width, height).
		int cellNum = mineField.uncover(y, x);

		cells[x][y].setNumber(cellNum);
		cells[x][y].open();

		// If there are 0 neighbouring mines then recursively open neighbouring
		// cells
		if (cellNum == 0) {
			clearNeighbours(x, y);
			movesLbl.setText("Moves: " + Integer.toString(moves));
		} else if (cellNum == -1) { // If cell is a mine (-1), game is lost
			cells[x][y].setFail();
			System.out.println("LOST ON CELL " + cells[x][y]);
			endGame();
			JOptionPane.showMessageDialog(null, "              BOOOOM!");
			return;
		}

		if (won()) { // If the game has been beaten (number of closed cells =
						// number of mines)
			// Flag remaining unflagged mines
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (cells[i][j].isBlank()) {
						cells[i][j].flag();
						decrementMines();
					}
				}
			}
			endGame();
			JOptionPane.showMessageDialog(null, "     Congratulations! You won!");
		}
		refresh();
	}

	public void quietSelect(int x, int y) {
		// Dont perform any behaviour if cell is flagged or game has already been
		// won/lost
		if (cells[x][y].isFlagged() || isGameOver) {
			return;
		}

		// How many mines are around the cell
		// x and y has to be reversed as MineField.java takes parameters
		// (height, width) not (width, height).
		int cellNum = mineField.uncover(y, x);

		cells[x][y].setNumber(cellNum);
		cells[x][y].open();

		// If there are 0 neighbouring mines then recursively open neighbouring
		// cells
		if (cellNum == 0) {
			List<Cell> neighbours = getNeighbours(x, y); // Reuse code thats in solver class
			for (Cell c : neighbours) {
				// Only attempt to open closed cells
				if (c.isClosed() && !c.isFlagged()) {
					quietSelect(c.getX(), c.getY());
				}
			}
		} else if (cellNum == -1) { // If cell is a mine (-1), game is lost
			gameWon = false;
			isGameOver = true;
			return;
		}

		if (won()) { // If the game has been beaten (number of closed cells = no of mines)
			endTime = System.nanoTime();
			gameWon = true;
			isGameOver = true;
		}
	}

	public void clearNeighbours(int x, int y) {
		List<Cell> neighbours = getNeighbours(x, y); // Reuse code thats in solver class
		for (Cell c : neighbours) {
			// Only attempt to open closed cells
			if (c.isClosed() && !c.isFlagged()) {
				select(c.getX(), c.getY());
				--moves;
			}
		}
	}

	/**
	 * Called when the game has been won or lost.
	 */
	private void endGame() {
		endTime = System.nanoTime();
		isGameOver = true;
		gameTimer.stop();
		try {
			openAllCells();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		disableAllBtns();
		refresh();
	}

	/**
	 * Generate a fresh board and a new minefield.
	 */
	public void reset() {
		isGameOver = false;
		endTime = 0;
		currentGameTime = 0;
		startTime = 0;
		moves = 0;
		movesLbl.setText("Moves: " + Integer.toString(moves));
		minesLeft = noOfMines;
		minesLbl.setText("~ Mines Left: " + Integer.toString(minesLeft));
		mineField = new MineField(height, width, noOfMines);
		cells = new Cell[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell(i, j);
			}
		}
		enableAllBtns();
		gameTimer.start();
		refresh();
		solver = new BoardSolver(this);
	}

	/**
	 * Opens all remaining closed on the board.
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	private void openAllCells() throws NoSuchAlgorithmException {
		// Unlock the minefield
		mineField.open(PASSWORD);
		// Open/Select every closed cell
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (cells[i][j].isClosed()) {
					// (j, i) as MineField.java takes (height, width), not
					// (width, height)
					int cellNum = mineField.uncover(j, i);
					cells[i][j].setNumber(cellNum);
					cells[i][j].open();
				}
			}
		}
		refresh();
	}

	/**
	 * Check to see if the game as been won.
	 * 
	 * @return if the remaining number of closed cells is equal to the number of
	 *         mines in the minefield.
	 */
	private boolean won() {
		int closedCount = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (cells[i][j].isClosed()) {
					++closedCount;
				}
			}
		}
		// If the number of covered cells left is equal to the number of mines,
		// game is
		// won
		return (closedCount == noOfMines);
	}

	/**
	 * Print information about a cell to console.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
	private void debug(int x, int y) {
		// System.out.println("=======================");
		// System.out.println("Cell info = " + cells[x][y]);
		// System.out.println("Set of neighbors = " + solver.getNeighbours(x, y));
		// System.out.println("Num of uncovered neighbors = " +
		// solver.calcClosedNeighbours(x, y));
		// System.out.println("Num of flagged neighbors = " +
		// solver.calcFlaggedNeighbours(x, y));
		// System.out.println("=======================");
	}

	private List<Cell> getNeighbours(int x, int y) {
		List<Cell> neighbours = new ArrayList<Cell>();
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y + 1; ++j) {
				if (i >= 0 && i < cells.length && j >= 0 && j < cells[i].length && !(i == x && j == y)) {
					neighbours.add(cells[i][j]);
				}
			}
		}
		return neighbours;
	}

	private void showNoMoreMovesDialog() {
		JOptionPane.showMessageDialog(null, "No known safe moves.");
	}

	private void disableAllBtns() {
		ptHintBtn.setEnabled(false);
		ptAssistBtn.setEnabled(false);
		ptSolveBtn.setEnabled(false);
		SATHintBtn.setEnabled(false);
		SATAssistBtn.setEnabled(false);
		SATSolveBtn.setEnabled(false);
		SATProbBtn.setEnabled(false);
		fullAutoBtn.setEnabled(false);
	}

	public void enableAllBtns() {
		ptHintBtn.setEnabled(true);
		ptAssistBtn.setEnabled(true);
		ptSolveBtn.setEnabled(true);
		SATHintBtn.setEnabled(true);
		SATAssistBtn.setEnabled(true);
		SATSolveBtn.setEnabled(true);
		SATProbBtn.setEnabled(true);
		fullAutoBtn.setEnabled(true);
	}

	// checks if (i,j) is within the field (taken from MineField.java)
	// Acknowledgements to Victor Khomenko for programming this check
	public boolean is_good(int i, int j) {
		return i >= 0 && i < cells.length && j >= 0 && j < cells[i].length;
	}

	public void incrementMoves() {
		movesLbl.setText("Moves: " + Integer.toString(++moves));
	}

	public void decrementMines() {
		minesLbl.setText("~ Mines Left: " + Integer.toString(--minesLeft));
	}

	public void incrementMines() {
		minesLbl.setText("~ Mines Left: " + Integer.toString(++minesLeft));
	}

	/* Getters/Setters */

	public Cell getCell(int x, int y) {
		return cells[x][y];
	}

	public boolean isFinished() {
		return isGameOver;
	}

	public List<Cell> getHintCells() {
		return hintCells;
	}

	public void resetHints() {
		for (Cell cell : hintCells) {
			cell.resetHint();
		}
		hintCells.clear();
	}

	public MineField getMineField() {
		return mineField;
	}

	public int getx() {
		return width;
	}

	public int gety() {
		return height;
	}

	public int getNoOfMines() {
		return noOfMines;
	}

	public Cell[][] getCells() {
		return cells;
	}

	public JCheckBoxMenuItem getDebugCB() {
		return debugCb;
	}

	public JRadioButtonMenuItem getDiffEasyRb() {
		return diffEasyRb;
	}

	public JRadioButtonMenuItem getDiffMediumRb() {
		return diffMediumRb;
	}

	public JRadioButtonMenuItem getDiffHardRb() {
		return diffHardRb;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public boolean getDebug() {
		return debug;
	}

	public int getMinesLeft() {
		return minesLeft;
	}

	public void setDebug(boolean value) {
		debug = value;
	}

	public boolean getProb() {
		return prob;
	}

	public boolean isGameWon() {
		return gameWon;
	}

	public JButton getFullAutoBtn() {
		return fullAutoBtn;
	}

	public JButton getStopBtn() {
		return stopBtn;
	}

	public Long getElapsedTime() {
		return endTime - startTime;
	}

	public void resetProbs() {
		for (Cell[] col : cells) {
			for (Cell c : col) {
				c.resetProb();
			}
		}
	}

	public void setGameOver(boolean isGameOver) {
		this.isGameOver = isGameOver;
	}
}
// Sample for loop for copy and paste
// for(int i = 0;i<width;i++) {
// for (int j = 0; j < height; j++) {
// cells[i][j];
// }
// }