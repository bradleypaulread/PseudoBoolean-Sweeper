
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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

public class Minesweeper extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	// Password to unlock MineField.java
	private final String PASSWORD = "hello";

	// Swing components (buttons etc.)
	private JButton resetBtn = new JButton("Reset");
	private JButton assistBtn = new JButton("Assist");
	private JButton autoBtn = new JButton("Auto");
	private JButton fullAutoBtn = new JButton("Full Auto");
	private JButton hintBtn = new JButton("Hint");
	private JButton SATSolveBtn = new JButton("SAT Solve");
	private JLabel movesLbl = new JLabel();
	private JLabel minesLbl = new JLabel();
	private JCheckBoxMenuItem debugCb = new JCheckBoxMenuItem("Debug Output");
	private JRadioButtonMenuItem diffEasyRb = new JRadioButtonMenuItem("Easy");
	private JRadioButtonMenuItem diffMediumRb = new JRadioButtonMenuItem("Medium");
	private JRadioButtonMenuItem diffHardRb = new JRadioButtonMenuItem("Hard");
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
	private boolean isGameOver; // True if the game has been lost or won
	private int moves = 0; // Number if moves made by the player.
	private int minesLeft;

	private BoardSolver solver;

	public Minesweeper(int x, int y, double d) {
		// Cast number of mines down to integer value
		setup(x, y, (int) ((x * y) * d));
	}

	public Minesweeper(int x, int y, int d) {
		setup(x, y, d);
	}

	public Minesweeper() {
		setup(11, 11, 15);
	}

	private void setup(int x, int y, int d) {
		width = x;
		height = y;
		noOfMines = d;
		minesLeft = d;

		// Load interface components
		board = new Board(this, x, y);
		loadButtons();
		loadFileMenu();
		add(board, BorderLayout.CENTER);

		// Reset board to a fresh setting
		reset();

		// this.setLocationRelativeTo(null); //center JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setVisible(true);

		solver = new BoardSolver(this);
	}

	/**
	 * Load buttons to the JFrame and assign their action listeners.
	 */
	private void loadButtons() {
		Container buttons = new Container();
		Container stats = new Container();
		Container topBar = new Container();
		topBar.setLayout(new BorderLayout());

		stats.setLayout(new FlowLayout());
		stats.add(movesLbl);
		stats.add(minesLbl);
		movesLbl.setText(Integer.toString(moves));
		minesLbl.setText(Integer.toString(minesLeft));

		buttons.setLayout(new FlowLayout());
		buttons.add(fullAutoBtn);
		buttons.add(autoBtn);
		buttons.add(SATSolveBtn);
		buttons.add(assistBtn);
		buttons.add(hintBtn);

		topBar.add(stats, BorderLayout.NORTH);
		topBar.add(buttons, BorderLayout.SOUTH);

		this.add(topBar, BorderLayout.NORTH);
		this.add(resetBtn, BorderLayout.SOUTH);

		// Add icon to JFrame and Taskbar
		List<Image> icons = new ArrayList<Image>();
		Image img1 = Toolkit.getDefaultToolkit().getImage("resources/bomb16x16.png");
		Image img2 = Toolkit.getDefaultToolkit().getImage("resources/bomb24x24.png");
		Image img3 = Toolkit.getDefaultToolkit().getImage("resources/bomb32x32.png");
		Image img4 = Toolkit.getDefaultToolkit().getImage("resources/bomb64x64.png");
		icons.add(img1);
		icons.add(img2);
		icons.add(img3);
		icons.add(img4);
		this.setIconImages(icons);

		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		hintBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				solver.genHint();
			}
		});

		assistBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!solver.assist() && !isGameOver()) {
					showNoMoreMovesDialog();
				}
			}
		});

		autoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Perform the assist action until no more safe moves exist
				while (solver.assist())
					;

				int dialogResult = JOptionPane.showConfirmDialog(null,
						"No more known moves available. Would you like to select the 'least dangerous' cell?",
						"No More Known Moves", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					Cell cell = solver.calcCellOdds();
					select(cell.getX(), cell.getY());
				}
			}
		});

		fullAutoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Perform the assist action until no more safe moves exist
				while (!isGameOver) {
					while (solver.assist())
						;
					Cell cell = solver.calcCellOdds();
					select(cell.getX(), cell.getY());
				}
			}
		});

		SATSolveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Find a guarenteed mine/safe cell, if non found (returns false):
				// Ask the user if they would like to select the safest/least dangerous cell
				if (!solver.SATSolve()) {
					int dialogResult = JOptionPane.showConfirmDialog(null,
							"No more known moves available. Would you like to select the 'least dangerous' cell?",
							"No More Known Moves", JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.YES_OPTION) {
						Cell cell = solver.calcCellOdds();
						select(cell.getX(), cell.getY());
					}
				}
			}
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
		debugCb.setMnemonic(KeyEvent.VK_C);

		ButtonGroup diffRdGroup = new ButtonGroup();

		// When option selected invert the debug variable.
		debugCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				debug = !debug;
			}
		});

		menu.add(debugCb);
		menu.addSeparator();
		diffEasyRb.setSelected(true);
		diffEasyRb.setEnabled(false);
		diffEasyRb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Minesweeper newGame = new Minesweeper(11, 11, 15);
				newGame.getDiffEasyRb().setSelected(true);
				newGame.getDiffEasyRb().setEnabled(false);
				newGame.getDiffMediumRb().setEnabled(true);
				newGame.getDiffHardRb().setEnabled(true);
				newGame.getDebugCB().setSelected(debugCb.isSelected());
				setVisible(false);
				dispose();
			}
		});
		diffRdGroup.add(diffEasyRb);
		menu.add(diffEasyRb);

		diffMediumRb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Minesweeper newGame = new Minesweeper(16, 16, 40);
				newGame.getDiffMediumRb().setSelected(true);
				newGame.getDiffEasyRb().setEnabled(true);
				newGame.getDiffMediumRb().setEnabled(false);
				newGame.getDiffHardRb().setEnabled(true);
				newGame.getDebugCB().setSelected(debugCb.isSelected());
				setVisible(false);
				dispose();
			}
		});
		diffRdGroup.add(diffMediumRb);
		menu.add(diffMediumRb);

		diffHardRb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Minesweeper newGame = new Minesweeper(30, 16, 99);
				newGame.getDiffHardRb().setSelected(true);
				newGame.getDiffEasyRb().setEnabled(true);
				newGame.getDiffMediumRb().setEnabled(true);
				newGame.getDiffHardRb().setEnabled(false);
				newGame.getDebugCB().setSelected(debugCb.isSelected());
				setVisible(false);
				dispose();
			}
		});
		diffRdGroup.add(diffHardRb);
		menu.add(diffHardRb);

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
		for (Cell c : hintCells) {
			c.resetHint();
		}
		hintCells.clear();

		if (debug) {
			debug(x, y);
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
			clearNeighbours(x, y);
		} else if (cellNum == -1) { // If cell is a mine (-1), game is lost
			System.out.println("LOST ON CELL " + cells[x][y]);
			endGame();
			JOptionPane.showMessageDialog(null, " BOOOOM!");
			return;
		}

		if (won()) { // If the game has been beaten (number of closed cells =
						// number of mines)
			// Flag remaining unflagged mines
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (cells[i][j].isClosed() && !cells[i][j].isFlagged()) {
						cells[i][j].flag();
						decrementMines();
					}
				}
			}
			endGame();
			JOptionPane.showMessageDialog(null, "Congratulations! You won!");
		}

		refresh();
	}

	public void clearNeighbours(int x, int y) {
		List<Cell> neighbours = solver.getNeighbours(x, y); // Reuse code thats in solver class
		for (Cell c : neighbours) {
			// Only attempt to open closed cells
			if (c.isClosed()) {
				select(c.getX(), c.getY());
			}
		}
	}

	/**
	 * Called when the game has been won or lost.
	 */
	private void endGame() {
		isGameOver = true;
		hintBtn.setEnabled(false);
		assistBtn.setEnabled(false);
		autoBtn.setEnabled(false);
		fullAutoBtn.setEnabled(false);
		SATSolveBtn.setEnabled(false);
		try {
			openAllCells();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		refresh();
	}

	/**
	 * Generate a fresh board and a new minefield.
	 */
	public void reset() {
		isGameOver = false;
		moves = 0;
		hintBtn.setEnabled(true);
		assistBtn.setEnabled(true);
		autoBtn.setEnabled(true);
		fullAutoBtn.setEnabled(true);
		SATSolveBtn.setEnabled(true);
		minesLeft = noOfMines;
		minesLbl.setText("Mines Left: " + Integer.toString(minesLeft));
		mineField = new MineField(height, width, noOfMines);
		cells = new Cell[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell(i, j);
			}
		}
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

	public void incrementMoves() {
		++moves;
	}

	/**
	 * Print information about a cell to console.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
	private void debug(int x, int y) {
		System.out.println("=======================");
		System.out.println("Cell info = " + cells[x][y]);
		System.out.println("Set of neighbors = " + solver.getNeighbours(x, y));
		System.out.println("Num of uncovered neighbors = " + solver.calcClosedNeighbours(x, y));
		System.out.println("Num of flagged neighbors = " + solver.calcFlaggedNeighbours(x, y));
		System.out.println("=======================");
	}

	public void showNoMoreMovesDialog() {
		JOptionPane.showMessageDialog(null, "No known safe moves.");
	}

	// checks if (i,j) is within the field (taken from MineField.java)
	// Acknowledgements to Victor Khomenko for programming this check
	public boolean is_good(int i, int j) {
		return i >= 0 && i < cells.length && j >= 0 && j < cells[i].length;
	}

	public void decrementMines() {
		minesLbl.setText(Integer.toString(--minesLeft));
	}

	public void incrementMines() {
		minesLbl.setText(Integer.toString(++minesLeft));
	}

	/* Getters/Setters */

	public Cell getCell(int x, int y) {
		return cells[x][y];
	}

	public boolean isFinished() {
		return isGameOver;
	}

	public JButton getHintBtn() {
		return hintBtn;
	}

	public JButton getAssistBtn() {
		return assistBtn;
	}

	public JButton getAutoBtn() {
		return autoBtn;
	}

	public List<Cell> getHintCells() {
		return hintCells;
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

	@Override
	public void actionPerformed(ActionEvent arg0) {
	}
}

// Sample for loop for copy and paste
// for(int i = 0;i<width;i++) {
// for (int j = 0; j < height; j++) {
// cells[i][j];
// }
// }