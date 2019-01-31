
/*
 * Minesweeper.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

public class Minesweeper extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	// Password to unlock MineField.java
	private final String PASSWORD = "hello";

	// Swing components (buttons etc.)
	private JButton resetBtn = new JButton("Reset");
	private JButton assistBtn = new JButton("Assist");
	private JButton autoBtn = new JButton("Auto");
	private JButton hintBtn = new JButton("Hint");
	private JCheckBoxMenuItem debugCB = new JCheckBoxMenuItem("Debug Output");
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menu = new JMenu("File");
	
	private MineField mineField; 	// Blackbox minefield
	private int width, height;		// Width and Height of board (number of cells = width*height)
	private Cell[][] cells;		// 2D array to store all cells
	private List<Cell> hintCells = new ArrayList<Cell>();  // 
	private int noOfMines; // Number of mines
	private Board board;	// Board instance, where cells appearance is processed
	private boolean debug = false;	// If debug information should be printed to console
	private boolean isGameOver;		// True if the game has been lost or won
	

	public Minesweeper(int x, int y, double d) {
		width = x;
		height = y;
		noOfMines = (int) ((x * y) * d); // Round down to integer amount of mines
		cells = new Cell[width][height];
		mineField = new MineField(height, width, noOfMines);
		reset();

		// Load interface components
		board = new Board(this, x, y);
		loadButtons();
		loadFileMenu();
		add(board, BorderLayout.CENTER);
		
		// this.setLocationRelativeTo(null); //center JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setVisible(true);
	}

	public Minesweeper(int x, int y, int d) {
		width = x;
		height = y;
		noOfMines = d;
		cells = new Cell[width][height];
		mineField = new MineField(height, width, noOfMines);
		reset();

		// Load interface components
		board = new Board(this, x, y);
		loadButtons();
		loadFileMenu();
		add(board, BorderLayout.CENTER);
		
		// this.setLocationRelativeTo(null); //center JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setVisible(true);
	}

	private void loadButtons() {
		Container topBar = new Container();
		topBar.setLayout(new FlowLayout());
		topBar.add(autoBtn);
		topBar.add(assistBtn);
		topBar.add(hintBtn);
		this.add(topBar, BorderLayout.NORTH);
		this.add(resetBtn, BorderLayout.SOUTH);
		
		resetBtn.addActionListener(new MouseActions(this));
		
		hintBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				genHint();
			}
		});

		assistBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				assist();
			}
		});

		autoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				while (assist()) {
				}
			}
		});
	}

	private void loadFileMenu() {
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Playability settings.");
		menuBar.add(menu);
		debugCB.setMnemonic(KeyEvent.VK_C);

		debugCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				debug = !debug;
			}
		});

		menu.add(debugCB);
		this.setJMenuBar(menuBar);
	}

	private void debug(int x, int y) {
		System.out.println("=======================");
		System.out.println("Cell info = " + cells[x][y]);
		System.out.println("Set of neighbors = " + getNeighbours(x, y));
		System.out.println("Num of uncovered neighbors = " + calcClosedNeighbours(x, y));
		System.out.println("Num of flagged neighbors = " + calcFlaggedNeighbours(x, y));
		System.out.println("=======================");
	}

	public void reset() {
		isGameOver = false;

		mineField = new MineField(height, width, noOfMines);

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				cells[i][j] = new Cell(i, j);
			}
		}
	}

	public void refresh() {
		board.repaint();
	}

	public boolean genHint() {
		// Find cells that have n surrounding mines and n closed neighbours
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (is_good(i, j)) {
					Cell current = cells[i][j];
					// Only apply logic to open cells with 0 surrounding mines
					if (current.isOpen() && current.getNumber() == 0) { // Code to add if I want to find mines:
																		// (current.getNumber() == 0 ||
																		// current.getNumber() ==
																		// calcClosedNeighbours(i, j))
						List<Cell> n = getNeighbours(current); // List of
																// neighbours
						for (int k = 0; k < n.size(); ++k) {
							// If the cell has not been affected by the user (is
							// blank of behaviour)
							if (n.get(k).isBlank()) {
								n.get(k).setHint();
								hintCells.add(n.get(k));
								refresh();
								return true;
							}
						}
					}
				}
			}
		}

		// Find cells that have N surrounding mines but N flagged neighbours
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (is_good(i, j)) {
					Cell current = cells[i][j];
					// Only apply logic to open cells with n surrounding mines
					// and n surrounding flags
					int flagsNo = calcFlaggedNeighbours(i, j);
					if (current.isOpen() && current.getNumber() == flagsNo) {
						List<Cell> n = getNeighbours(current); // List of
																// neighbours
						for (int k = 0; k < n.size(); ++k) {
							// If the cell has not been affected by the user (is
							// blank of behaviour)
							if (n.get(k).isBlank()) {
								n.get(k).setHint();
								hintCells.add(n.get(k));
								refresh();
								return true;
							}
						}
					}
				}
			}
		}
		if (!isGameOver) {
			JOptionPane.showMessageDialog(null, "No known safe moves.");
		}
		return false;
	}

	private boolean assist() {
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (is_good(i, j)) {
					Cell current = cells[i][j];
					// Only apply logic to open cells with 0 surrounding mines
					if (current.isOpen() && current.getNumber() == 0) {
						List<Cell> n = getNeighbours(current); // List of neighbouring cells
						for (int k = 0; k < n.size(); ++k) {
							// If the cell has not been affected by the user (is blank of behaviour)
							if (n.get(k).isClosed() && !n.get(k).isFlagged()) {
								select(n.get(k).getX(), n.get(k).getY());
								return true;
							}
						}
					}
					if (current.getNumber() != 0 && current.getNumber() == calcClosedNeighbours(i, j)
							&& current.getNumber() != calcFlaggedNeighbours(i, j)) {
						List<Cell> n = getNeighbours(current); // List of neighbouring cells
						for (Cell c : n) {
							if (c.isClosed())
								c.flag();
						}
						refresh();
						return true;
					}
				}
			}
		}
		// Find cells that have N surrounding mines but N flagged neighbours
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (is_good(i, j)) {
					Cell current = cells[i][j];
					// Only apply logic to open cells with n surrounding mines
					// and n surrounding flags
					int flagsNo = calcFlaggedNeighbours(i, j);
					if (current.isOpen() && current.getNumber() != 0 && current.getNumber() == flagsNo) {
						List<Cell> n = getNeighbours(current); // List of neighbours
						for (int k = 0; k < n.size(); ++k) {
							// If the cell has not been affected by the user (is
							// blank of behaviour)
							if (n.get(k).isClosed() && !n.get(k).isFlagged()) {
								select(n.get(k).getX(), n.get(k).getY());
								return true;
							}
						}
					}
				}
			}
		}
		if (!isGameOver) {
			JOptionPane.showMessageDialog(null, "No known safe moves.");
		}
		return false;
	}

	public void select(int x, int y) {
		// Dont perform any behaviour if cell is flagged
		if (cells[x][y].isFlagged()) {
			return;
		}

		// If there are any cells set as hints, 
		//		reset them back to plain closed cells
		for (Cell c : hintCells) {
			c.resetHint();
		}
		hintCells.clear();

		if (debug) {
			debug(x, y);
		}

		// Mines around the cell
		// x and y has to be reversed as MineField.java takes parameters in the
		//	 order of height then width, not width then height
		int cellNum = mineField.uncover(y, x); 

		cells[x][y].setNumber(cellNum);
		// openCells.add(cells[x][y]);
		cells[x][y].open();

		// If there are 0 neighbouring mines then recursivly open neighbouring cells
		if (cellNum == 0) {
			List<Cell> neighbours = getNeighbours(x, y);
			for (Cell c : neighbours) {
				if (c.isClosed()) {
					select(c.getX(), c.getY());
				}
			}
		}

		resetMarks();
		refresh();

		// If cell is a mine (-1), game is lost
		if (cellNum == -1) {
			endGame();
			JOptionPane.showMessageDialog(null, "BOOOOM!");
		// If the game has been beaten (number of closed cells = number of mines)
		} else if (won()) {
			endGame();
			JOptionPane.showMessageDialog(null, "Congratulations! You won!");
		}
	}

	private void endGame() {
		isGameOver = true;
		hintBtn.setEnabled(false);
		assistBtn.setEnabled(false);
		autoBtn.setEnabled(false);
		try {
			openAllCells();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		refresh();
	}

	private void openAllCells() throws NoSuchAlgorithmException {
		// Unlock the minefield 
		mineField.open(PASSWORD);
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				// (j, i) as MineField.java takes (height, width), not (width, height)
				int cellNum = mineField.uncover(j, i); 
				cells[i][j].setNumber(cellNum);
				cells[i][j].open();
			}
		}
		refresh();
	}

	private boolean won() {
		int obscuredCount = 0;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (cells[i][j].isClosed()) {
					++obscuredCount;
				}
			}
		}
		// If the number of covered cells left is equal to the number of mines, game is won
		return (obscuredCount == noOfMines); 
	}

	public void mark(int x, int y) {
		if (cells[x][y].isFlagged()) {
			cells[x][y].unflag();
		} else if (cells[x][y].isClosed()) {
			cells[x][y].flag();
		}
		resetMarks();
	}

	private void resetMarks() {
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (cells[i][j].isOpen())
					cells[i][j].unflag();
			}
		}
	}

	public int calcClosedNeighbours(int x, int y) {
		int count = 0;
		// for loop to count how many closed cells are around a cell
		List<Cell> neighbours = getNeighbours(x, y);
		for (Cell c : neighbours) {
			if (c.isClosed())
				++count;
		}
		return count;
	}

	public int calcFlaggedNeighbours(int x, int y) {
		int count = 0;
		// for loop to count how many flagged cells are around a cell
		List<Cell> neighbours = getNeighbours(x, y);
		for (Cell c : neighbours) {
			if (c.isFlagged())
				++count;
		}
		return count;
	}

	public List<Cell> getNeighbours(int x, int y) {
		List<Cell> neighbours = new ArrayList<Cell>();
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y + 1; ++j) {
				if (is_good(i, j) && !(i == x && j == y)) {
					neighbours.add(cells[i][j]);
				}
			}
		}
		return neighbours;
	}

	public List<Cell> getAllOpenCells() {
		List<Cell> cells = new ArrayList<Cell>();
		for (Cell[] col : this.cells) {
			for (Cell c : col) {
				if (c.isOpen())
					cells.add(c);
			}
		}
		return cells;
	}

	/*
	 * Getters/Setters
	 */

	public List<Cell> getNeighbours(Cell c) {
		return getNeighbours(c.getX(), c.getY());
	}
	
	// checks if (i,j) is within the field
	public boolean is_good(int i, int j) {
		return i >= 0 && i < cells.length && j >= 0 && j < cells[i].length;
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

	public Cell[][] getCells() {
		return cells;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

//Sample for loop for copy and paste
//	for(int i = 0;i<width;++i) {
//		for (int j = 0; j < height; ++j) {
//			cells[i][j]; 
//		} 
//	}
