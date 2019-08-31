
/**
 * Minesweeper.java
 * 
 * Created by Potrik
 * Last modified: 22/07/13
 * 
 * Heavily modified by Bradley Read
 * Last modified: 02/05/19
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
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

public class Minesweeper extends JFrame {

	private static final long serialVersionUID = 1L;

	// Password to unlock MineField instance
	private final String PASSWORD = "hello";

	// Swing components (buttons etc.)
	private JButton resetBtn;
	private JButton stopBtn;

	private JButton hintBtn;
	private JButton assistBtn;
	private JButton solveBtn;
	private JButton showProbBtn;

	private JLabel movesLbl;
	private JLabel minesLbl;
	private JLabel detailsLbl;

	private JCheckBoxMenuItem debugCb;
	private JRadioButtonMenuItem diffEasyRb;
	private JRadioButtonMenuItem diffMediumRb;
	private JRadioButtonMenuItem diffHardRb;

	private JCheckBoxMenuItem singlePointCb;
	private JCheckBoxMenuItem pbCb;
	private JCheckBoxMenuItem stratCb;

	private JMenuItem customGameItem;
	private JMenuItem changeCellSizeItem;
	private JMenuItem firstGuessItem;

	private JMenuBar menuBar;;
	private JMenu menu;
	private JMenu helpMenu;

	SolverThreadWrapper thread = new SolverThreadWrapper(this);

	private MineField mineField; // Blackbox minefield
	private int width, height; // Width and Height of board (number of cells =
								// width*height)
	private Cell[][] cells; // 2D array to store all cells
	private Set<Cell> hintCells = new HashSet<Cell>(); //
	private int noOfMines; // Number of mines
	private Board board; // Board instance, where cells appearance is processed
	private boolean isGameOver; // True if the game has been lost or won
	private int moves; // Number if moves made by the player.
	private int currentGameTime;	// The current game elapsed time (in seconds)
	private Timer gameTimer;	// Timer that ticks every second
	private int minesLeft;
	private boolean gameWon;

	// If an opening has been found in the game yet (a cell with number 0)
	// used for the first guess function in the file menu
	private boolean opening;	

	/**
	 * Default constructor for Minesweeper class. Initiates the board to beginner
	 * difficulty.
	 */
	public Minesweeper() {
		this(Difficulty.BEGINNER);
	}

	/**
	 * Constructor for Minesweeper class.
	 * 
	 * @param width     boards width. That is the number of cells in a row.
	 * @param height    boards height. That is the number of cells in a column.
	 * @param noOfMines number of mines present on the board. Must be less than the
	 *                  area of the board.
	 */
	public Minesweeper(int width, int height, int noOfMines) {
		setup(width, height, noOfMines);
	}

	/**
	 * Constructor for Minesweeper class that only takes a MineField json string in.
	 */
	public Minesweeper(String mf) {
		final String mfCopy = new String(mf);

		final JSONObject jsonObj = new JSONObject(mfCopy);

		final JSONArray mfJsonArr = jsonObj.getJSONArray("field");
		int mineCount = 0;
		int width = 0;
		int height = mfJsonArr.length();
		for (int i = 0; i < mfJsonArr.length(); i++) {
			final JSONArray row = mfJsonArr.getJSONArray(i);
			width = row.length();
			for (int j = 0; j < row.length(); j++) {
				final Boolean isMine = row.getBoolean(j);
				if (isMine)
					mineCount++;
			}
		}
		Gson gson = new Gson();
		MineField mfObj = gson.fromJson(mf, MineField.class);
		setup(width, height, mineCount, mfObj);
	}

	/**
	 * Constructor for Minesweeper class that uses a percentage difficulty of mines.
	 * Number is casted down to integer amount
	 * 
	 * @param width            boards width. That is the number of cells in a row.
	 * @param height           boards height. That is the number of cells in a
	 *                         column.
	 * @param mineDistribution percent number of mines present on the board. Should
	 *                         be in the form 0.0 <= mineDistribution <= 1.0.
	 */
	public Minesweeper(int width, int height, double mineDistribution) {
		// Cast number of mines down to integer value
		int noOfMines = (int) ((width * height) * mineDistribution);
		setup(width, height, noOfMines);
	}
	
	/**
	 * Constructor for Minesweeper class that uses a pre-specified mine field.
	 * 
	 * @param width            boards width. That is the number of cells in a row.
	 * @param height           boards height. That is the number of cells in a
	 *                         column.
	 * @param noOfMines        number of mines present on the board. Must be less
	 *                         than the area of the board.
	 * @param jsonMineFieldStr string representation of the json representation of a
	 *                         minefield.
	 */
	public Minesweeper(int width, int height, int noOfMines, String jsonMineFieldStr) {
		Gson gson = new Gson();
		MineField mf = gson.fromJson(jsonMineFieldStr, MineField.class);
		setup(width, height, noOfMines, mf);
	}

	/**
	 * Constructor for Minesweeper class that uses difficulty settings (enums). Uses
	 * the classic Minesweeper game difficulty parameters.
	 * 
	 * @param difficulty enum difficulty of the board.
	 */
	public Minesweeper(Difficulty difficulty) {
		setup(difficulty.width, difficulty.height, difficulty.noOfMines);
	}

	/**
	 * Constructor for Minesweeper class that uses a difficulty setting (enum) and a
	 * pre-specified MineField. Uses the classic Minesweeper game difficulty
	 * parameters.
	 * 
	 * @param difficulty enum difficulty of the board.
	 */
	public Minesweeper(Difficulty difficulty, MineField mf) {
		setup(difficulty.width, difficulty.height, difficulty.noOfMines, mf);
	}

	/**
	 * Sets private variables (e.g. widht/height, noOfMines, board, etc.). Also
	 * resets the game values (e.g. game time, is game won, etc.).
	 * 
	 * @param width     boards width. That is the number of cells in a row.
	 * @param height    boards height. That is the number of cells in a column.
	 * @param noOfMines number of mines present on the board. Must be less than the
	 *                  area of the board.
	 */
	private void setup(int width, int height, int noOfMines) {
		this.width = width;
		this.height = height;
		this.noOfMines = noOfMines;
		this.minesLeft = noOfMines;

		// Load interface components
		board = new Board(this, width, height);

		resetGame();
	}

	/**
	 * Same as {@link #setup(int, int, int) setup} method but sets the minefield to
	 * the specficed one afterwards.
	 * 
	 * @param width     boards width. That is the number of cells in a row.
	 * @param height    boards height. That is the number of cells in a column.
	 * @param noOfMines number of mines present on the board. Must be less than the
	 *                  area of the board.
	 * 
	 */
	private void setup(int width, int height, int noOfMines, MineField mf) {
		setup(width, height, noOfMines);
		this.mineField = mf;
	}

	/**
	 * Initialises and displays all the GUI.
	 */
	public void buildGUI() {
		resetBtn = new JButton("Reset");
		stopBtn = new JButton("Stop");

		hintBtn = new JButton("Hint");
		assistBtn = new JButton("Assist");
		solveBtn = new JButton("Solve");
		showProbBtn = new JButton("Show Probabilities");

		movesLbl = new JLabel();
		minesLbl = new JLabel();
		detailsLbl = new JLabel();

		debugCb = new JCheckBoxMenuItem("Debug Mode");
		diffEasyRb = new JRadioButtonMenuItem("Beginner");
		diffMediumRb = new JRadioButtonMenuItem("Intermediate");
		diffHardRb = new JRadioButtonMenuItem("Expert");

		singlePointCb = new JCheckBoxMenuItem("Single Point");
		pbCb = new JCheckBoxMenuItem("Pseudo-Boolean/SAT");
		stratCb = new JCheckBoxMenuItem("Strategy");

		customGameItem = new JMenuItem("Custom Game");
		changeCellSizeItem = new JMenuItem("Change Cell Size");
		firstGuessItem = new JMenuItem("Make First Guess");

		menuBar = new JMenuBar();
		menu = new JMenu("Options");
		helpMenu = new JMenu("Help");

		loadUI();
		loadFileMenu();
		resetUIDetails();
		// Centres minefield
		Container fl = new Container();
		fl.add(board);
		fl.setLayout(new FlowLayout());
		add(fl, BorderLayout.CENTER);

		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
	}

	/**
	 * Load buttons to the JFrame and assign their action listeners.
	 */
	private void loadUI() {
		JPanel controlBtnsPnl = new JPanel();
		JPanel details = new JPanel();
		JPanel stats = new JPanel();

		Container gameStatsAndDetails = new Container();
		gameStatsAndDetails.setLayout(new FlowLayout());

		Container topBar = new Container();
		topBar.setLayout(new BorderLayout());

		Container controlBtns = new Container();
		controlBtns.setLayout(new FlowLayout());

		TitledBorder statsTitle = new TitledBorder("Stats");
		statsTitle.setTitleJustification(TitledBorder.CENTER);
		stats.setBorder(new TitledBorder(statsTitle));
		stats.setLayout(new FlowLayout());
		stats.add(movesLbl);
		stats.add(minesLbl);
		movesLbl.setText(Integer.toString(moves));
		minesLbl.setText(Integer.toString(minesLeft));
		stats.setPreferredSize(new Dimension(250, 50));
		gameStatsAndDetails.add(stats);

		TitledBorder detailsTitle = new TitledBorder("Details");
		detailsTitle.setTitleJustification(TitledBorder.CENTER);
		details.setBorder(new TitledBorder(detailsTitle));
		details.setLayout(new FlowLayout());
		details.add(detailsLbl);
		detailsLbl.setText("...");
		detailsLbl.setHorizontalTextPosition(SwingConstants.CENTER);
		details.setPreferredSize(new Dimension(500, 50));
		gameStatsAndDetails.add(details);

		JLabel timeLbl = new JLabel();
		stats.add(timeLbl);
		// Tick every second
		gameTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentGameTime < 100000) {
					timeLbl.setText("~ Time: " + Integer.toString(currentGameTime));
				} else {
					((Timer) (e.getSource())).stop();
				}
				currentGameTime++;
			}
		});
		gameTimer.setInitialDelay(0);
		gameTimer.start();

		TitledBorder controlBtnsTitle = new TitledBorder("Control Buttons");
		controlBtnsTitle.setTitleJustification(TitledBorder.CENTER);
		controlBtnsPnl.setBorder(controlBtnsTitle);
		controlBtnsPnl.setLayout(new FlowLayout());
		controlBtnsPnl.add(hintBtn);
		controlBtnsPnl.add(assistBtn);
		controlBtnsPnl.add(solveBtn);
		controlBtnsPnl.add(showProbBtn);
		controlBtnsPnl.add(stopBtn);
		stopBtn.setEnabled(false);
		controlBtns.add(controlBtnsPnl);

		topBar.add(gameStatsAndDetails, BorderLayout.NORTH);
		topBar.add(controlBtns, BorderLayout.SOUTH);

		this.add(topBar, BorderLayout.NORTH);
		this.add(resetBtn, BorderLayout.SOUTH);

		// Add icon to JFrame and Taskbar
		List<Image> icons = new ArrayList<Image>();
		Image img4 = Toolkit.getDefaultToolkit().getImage("resources/bomb64x64.png");
		icons.add(img4);
		this.setIconImages(icons);

		resetBtn.addActionListener(e -> {
			resetGame();
			resetUIDetails();
		});

		hintBtn.addActionListener(e -> {
			disableAllBtns();
			thread = new SolverThreadWrapper(this);
			configureSolver(thread);
			thread.setHint();
			thread.start();
			stopBtn.setEnabled(true);
		});

		assistBtn.addActionListener(e -> {
			disableAllBtns();
			thread = new SolverThreadWrapper(this);
			configureSolver(thread);
			thread.start();
			stopBtn.setEnabled(true);
		});

		solveBtn.addActionListener(e -> {
			disableAllBtns();
			thread = new SolverThreadWrapper(this);
			configureSolver(thread);
			thread.setLoop();
			thread.start();
			stopBtn.setEnabled(true);
		});

		showProbBtn.addActionListener(e -> {
			disableAllBtns();
			thread = new SolverThreadWrapper(this);
			thread.setProb();
			thread.start();
			stopBtn.setEnabled(true);
		});

		stopBtn.addActionListener(e -> {
			thread.end();
			stopBtn.setEnabled(false);
			enableAllBtns();
		});
	}

	/**
	 * Load the File menu component options and adds each option's action listener
	 * code.
	 */
	private void loadFileMenu() {
		menu.getAccessibleContext().setAccessibleDescription("Playability settings.");
		menuBar.add(menu);
		helpMenu.getAccessibleContext().setAccessibleDescription("Help/User Guide.");
		menuBar.add(helpMenu);

		JMenuItem helpMenuItem = new JMenuItem("See Help Memu");
		helpMenuItem.addActionListener(e -> {
			String helpText = "The game follows the standard rules of Minesweeper. The aim is to probe all cells that are not mines.\nThe number revealed after probing a cell represents how many mines are touching that cell.\nThe Hint, Assist and Solve buttons can be used if you are stuck to reveal (or probe) known safe/mine cells.\nIf you click the Hint button and a cell's appearance turns to Green, this means that that cell is safe.\nIf the cell changes to Red, then that cell is a mine. You can customise what algorithm(s) these buttons\nuse through the \"Option\" menu. If you want to cancel/stop a solve computation then you can click the Stop button.\nClicking the \"Show Probabilties\" button will change the appearnce of cells, with their colour representing how\ndangerous they are. A low probability is safer. The darker the colour of a cell means that the cell is more likely a mine.\nCells highlighted blue mean that the solver has identified that cell as the best move.\nYou can see the full probabiltiy of a cell via a tooltip when hovering over a cell.";
			JOptionPane.showMessageDialog(this, helpText);
		});
		helpMenu.add(helpMenuItem);

		ButtonGroup diffRdGroup = new ButtonGroup();
		// When option selected invert the debug variable.
		debugCb.addActionListener(e -> {
			refresh();
		});

		menu.add(debugCb);
		menu.addSeparator();
		menu.addSeparator();
		diffEasyRb.setSelected(true);
		diffEasyRb.setEnabled(false);
		diffEasyRb.addActionListener(e -> {
			Minesweeper newGame = new Minesweeper(Difficulty.BEGINNER);
			newGame.copySettings(this);
			setVisible(false);
			dispose();
		});
		diffRdGroup.add(diffEasyRb);
		menu.add(diffEasyRb);

		diffMediumRb.addActionListener(e -> {
			Minesweeper newGame = new Minesweeper(Difficulty.INTERMEDIATE);
			newGame.copySettings(this);
			setVisible(false);
			dispose();
		});
		diffRdGroup.add(diffMediumRb);
		menu.add(diffMediumRb);

		diffHardRb.addActionListener(e -> {
			Minesweeper newGame = new Minesweeper(Difficulty.EXPERT);
			newGame.copySettings(this);
			setVisible(false);
			dispose();
		});
		diffRdGroup.add(diffHardRb);

		menu.add(diffHardRb);
		menu.addSeparator();
		menu.addSeparator();

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
				// Dont do anything is any input is invalid
				if (newX < 1 || newY < 1 || newMines < 0 || newMines > (newX * newY)) {
					return;
				}
				Minesweeper newGame = new Minesweeper(newX, newY, newMines);
				newGame.copySettings(this);
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

		changeCellSizeItem.addActionListener(e -> {
			NumberFormat newSize;

			newSize = NumberFormat.getIntegerInstance();

			// Fields for data entry
			JFormattedTextField newSizeField = new JFormattedTextField(newSize);

			newSizeField.setValue(board.getCellWidth());

			JPanel fieldPane = new JPanel(new GridLayout(0, 1));
			fieldPane.add(newSizeField);

			JFrame frame = new JFrame("Change Cell Size");
			JButton btn = new JButton("Change");
			btn.addActionListener(e2 -> {
				int userSetSize = ((Number) newSizeField.getValue()).intValue();
				System.out.println(userSetSize);
				frame.setVisible(false);
				frame.dispose();
				changeCellSize(userSetSize);
			});
			fieldPane.add(btn);
			frame.add(fieldPane);
			// Display the window.
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
		menu.add(changeCellSizeItem);

		menu.addSeparator();
		menu.addSeparator();
		firstGuessItem.addActionListener(e -> {
			if (!opening) {
				opening = new SinglePointSolver(this).makeFirstGuess();
				if (opening) {
					firstGuessItem.setEnabled(false);
				}
			}
		});
		menu.add(firstGuessItem);
		singlePointCb.setSelected(true);
		menu.add(singlePointCb);
		menu.add(pbCb);
		menu.add(stratCb);

		this.setJMenuBar(menuBar);
	}

	/**
	 * Copies settings (difficulty, strategy, etc.) of one game over to another.
	 * 
	 * @param game game that the settings of are going to be copied.
	 */
	private void copySettings(Minesweeper game) {
		diffEasyRb.setSelected(game.isEasy());
		diffEasyRb.setEnabled(!game.isEasy());
		diffMediumRb.setSelected(game.isMedium());
		diffMediumRb.setEnabled(!game.isMedium());
		diffHardRb.setSelected(game.isHard());
		diffHardRb.setEnabled(!game.isHard());
		debugCb.setSelected(game.isDebug());
		singlePointCb.setSelected(game.isSinglePoint());
		pbCb.setSelected(game.isPb());
		stratCb.setSelected(game.isStrat());
		changeCellSize(game.getBoard().getCellWidth());
	}

	/**
	 * Sets the desired options of the solver depending on what settings the user
	 * has selected.
	 * 
	 * @param solver the solver object that options will be changed.
	 */
	private void configureSolver(SolverThreadWrapper solver) {
		solver.reset();
		boolean doSinglePoint = singlePointCb.isSelected();
		boolean doPB = pbCb.isSelected();
		boolean doStrat = stratCb.isSelected();
		solver.setSinglePoint(doSinglePoint);
		solver.setPB(doPB);
		solver.setStrat(doStrat);
	}

	/**
	 * Redraws the board, updating all cells appearance and behaviour.
	 */
	public void refresh() {
		board.repaint(10);
	}

	/**
	 * Called when a cell is wished to be cleared. Will not affect flagged cells.
	 * Clears hint cells and probabilities. Increments move count. Checks if game is
	 * won or lost.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
	public void probe(int x, int y) {
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

		incrementMoves();

		// How many mines are around the cell
		// x and y has to be reversed as MineField.java takes parameters
		// (height, width) not (width, height).
		int cellNum = mineField.uncover(y, x);

		cells[x][y].setNumber(cellNum);
		cells[x][y].setOpen(true);

		// If there are 0 neighbouring mines then recursively open neighbouring
		// cells
		if (cellNum == 0) {
			if (!opening) {
				firstGuessItem.setEnabled(false);
				opening = true;
			}
			clearNeighbours(x, y);
			movesLbl.setText("Moves: " + Integer.toString(moves));
		} else if (cellNum == -1) { // If cell is a mine (-1), game is lost
			cells[x][y].setFail(true);
			endGame();
			setDetail("Game Lost!!! :( . Lost on Cell " + cells[x][y]);
			return;
		}

		if (won()) { // If the game has been beaten (number of closed cells =
						// number of mines)
			// Flag remaining unflagged mines
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (cells[i][j].isBlank()) {
						cells[i][j].setFlagged(true);
						decrementMines();
					}
				}
			}
			endGame();
			setDetail("GAME WON!!! :)");
		}
		refresh();
	}

	/**
	 * Same as {@link #probe(int, int) probe} but does not update any UI elements.
	 * Used when performing simulations.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
	public void quietProbe(int x, int y) {
		// Dont perform any behaviour if cell is flagged or game has already been
		// won/lost
		if (cells[x][y].isFlagged() || isGameOver) {
			return;
		}
		moves++;
		
		// How many mines are around the cell
		// x and y has to be reversed as MineField.java takes parameters
		// (height, width) not (width, height).
		int cellNum = mineField.uncover(y, x);

		cells[x][y].setNumber(cellNum);
		cells[x][y].setOpen(true);

		// If there are 0 neighbouring mines then recursively open neighbouring
		// cells
		if (cellNum == 0) {
			List<Cell> neighbours = getNeighbours(x, y); // Reuse code thats in solver class
			for (Cell c : neighbours) {
				// Only attempt to open closed cells
				if (c.isClosed() && !c.isFlagged()) {
					quietProbe(c.getX(), c.getY());
					moves--;
				}
			}
		} else if (cellNum == -1) { // If cell is a mine (-1), game is lost
			gameWon = false;
			isGameOver = true;
			return;
		}

		if (won()) { // If the game has been beaten (number of closed cells = no of mines)
			gameWon = true;
			isGameOver = true;
		}
	}

	/**
	 * Changes each cells size.
	 * 
	 * @param size what size the cells should be changed to. Should be more than 0.
	 */
	private void changeCellSize(int size) {
		// Bug where screen would stop adjusting size if tried to set
		// the size as current size (not sure why)
		if (size == board.getCellWidth() || size <= 0) {
			return;
		}
		this.board.setCellSize(size);
		refresh();
		// Repack all components so that JFrame is modelled correctly
		this.pack();
		// Reposition the JFrame into the centre of the screen.
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
	}

	/**
	 * Clear the neightbouring cells around a cell. Used when a cell with number 0
	 * is probed. Originally also used when performing a double click action but
	 * this encounted various bugs so was abandoned.
	 * 
	 * @param x X-axis coordinate of cell which neighbours should be cleared.
	 * @param y Y-axis coordinate of cell which neighbours should be cleared.
	 */
	public void clearNeighbours(int x, int y) {
		List<Cell> neighbours = getNeighbours(x, y); // Reuse code thats in solver class
		for (Cell c : neighbours) {
			// Only attempt to open closed cells
			if (c.isClosed() && !c.isFlagged()) {
				probe(c.getX(), c.getY());
				--moves;
			}
		}
	}

	/**
	 * Called when the game has been won or lost. Changes UI, stops game timer and
	 * "unlocks" the minefield so that all cells true behaviour is revealed (mine,
	 * number, etc.).
	 */
	private void endGame() {
		thread.end();
		isGameOver = true;
		gameTimer.stop();
		try {
			openAllCells(PASSWORD);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		disableAllBtns();
		refresh();
	}

	/**
	 * Resets all the UI elements to their default text.
	 */
	public void resetUIDetails() {
		firstGuessItem.setEnabled(true);
		setDetail("...");
		movesLbl.setText("Moves: " + Integer.toString(moves));
		minesLbl.setText("~ Mines Left: " + Integer.toString(minesLeft));
		enableAllBtns();
		gameTimer.start();
	}

	/**
	 * Generate a fresh board and a new minefield.
	 */
	public void resetGame() {
		thread.end();
		opening = false;
		gameWon = false;
		isGameOver = false;
		currentGameTime = 0;
		moves = 0;

		minesLeft = noOfMines;
		mineField = new MineField(height, width, noOfMines);
		cells = new Cell[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell(i, j);
			}
		}
		refresh();
	}

	/**
	 * Opens all remaining closed on the board.
	 * 
	 * @param password the password that will be used to unlock the minefield.
	 * 
	 * @throws NoSuchAlgorithmException when passed password is incorrect.
	 */
	public void openAllCells(String password) throws NoSuchAlgorithmException {
		// Unlock the minefield with the passed password
		// Throws NoSuchAlgorithmException if password is wrong
		mineField.open(password);

		// Open/probe every remaining closed cell
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Cell current = cells[i][j];
				if (current.isClosed()) {
					// (j, i) as MineField.java takes (height, width), not
					// (width, height)
					int cellNum = mineField.uncover(j, i);
					current.setNumber(cellNum);
					current.setOpen(true);
				}
			}
		}
		// Refeash the appeance of the board, revealing all cells
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
		// the game is won
		return (closedCount == noOfMines);
	}

	/**
	 * Returns a list of of the neighbouring cells around the specified cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
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

	/**
	 * Disables all UI buttons.
	 */
	private void disableAllBtns() {
		firstGuessItem.setEnabled(false);
		hintBtn.setEnabled(false);
		assistBtn.setEnabled(false);
		solveBtn.setEnabled(false);
		showProbBtn.setEnabled(false);
	}

	/**
	 * Enables all UI buttons.
	 */
	public void enableAllBtns() {
		hintBtn.setEnabled(true);
		assistBtn.setEnabled(true);
		solveBtn.setEnabled(true);
		showProbBtn.setEnabled(true);
	}

	// checks if (i,j) is within the field (taken from MineField.java)
	// Acknowledgements to Victor Khomenko for programming this check
	public boolean is_good(int i, int j) {
		return i >= 0 && i < cells.length && j >= 0 && j < cells[i].length;
	}

	/**
	 * Increment moves and update the moves label.
	 */
	public void incrementMoves() {
		movesLbl.setText("Moves: " + Integer.toString(++moves));
	}

	/**
	 * Decrement mines left and update the mines left label.
	 */
	public void decrementMines() {
		minesLbl.setText("~ Mines Left: " + Integer.toString(--minesLeft));
	}

	/**
	 * Increment mines left and update the mines left label.
	 */
	public void incrementMines() {
		minesLbl.setText("~ Mines Left: " + Integer.toString(++minesLeft));
	}

	/* Getters/Setters */
	private Board getBoard() {
		return this.board;
	}

	/**
	 * Returns the specified cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 */
	public Cell getCell(int x, int y) {
		return cells[x][y];
	}

	public Set<Cell> getHintCells() {
		return hintCells;
	}

	/**
	 * Reset all hint cells back to normal cells.
	 */
	public void resetHints() {
		for (Cell cell : hintCells) {
			cell.resetHint();
		}
		hintCells.clear();
	}

	public MineField getMineField() {
		return mineField;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getNoOfMines() {
		return noOfMines;
	}

	public Cell[][] getCells() {
		return cells;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public int getMinesLeft() {
		return minesLeft;
	}

	public boolean isGameWon() {
		return gameWon;
	}

	public JButton getStopBtn() {
		return stopBtn;
	}

	/**
	 * Reset all cells probablities.
	 */
	public void resetProbs() {
		for (Cell[] col : cells) {
			for (Cell c : col) {
				c.resetProb();
				c.resetBestCell();
			}
		}
	}

	public void setGameOver(boolean isGameOver) {
		this.isGameOver = isGameOver;
	}

	public int getNoOfMoves() {
		return moves;
	}

	/**
	 * Sets the detail message in the details label to the specified string.
	 * 
	 * @param detail the detail string to display.
	 */
	public void setDetail(String detail) {
		this.detailsLbl.setText(detail + ".");
	}

	public boolean isEasy() {
		return diffEasyRb.isSelected();
	}

	public boolean isMedium() {
		return diffMediumRb.isSelected();
	}

	public boolean isHard() {
		return diffHardRb.isSelected();
	}

	public boolean isSinglePoint() {
		return singlePointCb.isSelected();
	}

	public boolean isPb() {
		return pbCb.isSelected();
	}

	public boolean isStrat() {
		return stratCb.isSelected();
	}

	public boolean isDebug() {
		return debugCb.isSelected();
	}
}