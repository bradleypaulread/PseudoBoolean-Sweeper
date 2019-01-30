
/*
 * Minesweeper.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Minesweeper extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	// Password to unlock the minefield
	private final String password = "hello";

	private MineField mineField;
	private int width, height;
	private Cell[][] cells;
	private List<Cell> hintCells = new ArrayList<Cell>();
	private int noOfMines; // Number of mines
	private Board board;
	private JButton resetBtn = new JButton("Reset");
	private JButton assistBtn = new JButton("Assist");
	private JButton autoBtn = new JButton("Auto");
	private JButton hintBtn = new JButton("Hint");
	private boolean finished;

	public Minesweeper(int x, int y, double d) {
		width = x;
		height = y;
		noOfMines = (int) ((x*y)*d);
		System.out.println(noOfMines);
		cells = new Cell[width][height];
		mineField = new MineField(height, width, noOfMines);
		reset();

		board = new Board(this, x, y);

		Container topBar = new Container();
		topBar.setLayout(new FlowLayout());

		topBar.add(autoBtn);
		topBar.add(assistBtn);
		topBar.add(hintBtn);
		add(topBar, BorderLayout.NORTH);
		// add(assistBtn, BorderLayout.NORTH);

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
				while(assist()) {}
			}
		});

		add(board, BorderLayout.CENTER);
		add(resetBtn, BorderLayout.SOUTH);

		resetBtn.addActionListener(new Actions(this));
		// this.setLocationRelativeTo(null); //center JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setResizable(false);
		//setPreferredSize(new Dimension(1000, 1000));

		pack();
		setVisible(true);
	}

	public Minesweeper(int x, int y, int d) {
		width = x;
		height = y;
		noOfMines = d;
		System.out.println(noOfMines);
		cells = new Cell[width][height];
		mineField = new MineField(height, width, noOfMines);
		reset();

		board = new Board(this, x, y);

		Container topBar = new Container();
		topBar.setLayout(new FlowLayout());

		topBar.add(autoBtn);
		topBar.add(assistBtn);
		topBar.add(hintBtn);
		add(topBar, BorderLayout.NORTH);
		// add(assistBtn, BorderLayout.NORTH);

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
				while(assist()) {}
			}
		});

		add(board, BorderLayout.CENTER);
		add(resetBtn, BorderLayout.SOUTH);

		resetBtn.addActionListener(new Actions(this));
		// this.setLocationRelativeTo(null); //center JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setResizable(false);
		//setPreferredSize(new Dimension(1000, 1000));

		pack();
		setVisible(true);
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

	private void debug(int x, int y) {
		System.out.println("=======================");
		System.out.println("Cell info = " + cells[x][y]);
		System.out.println("Set of neighbors = " + getNeighbours(x, y));
		System.out.println("Num of uncovered neighbors = " + calcClosedNeighbours(x, y));
		System.out.println("Num of flagged neighbors = " + calcFlaggedNeighbours(x, y));
		System.out.println("=======================");
	}

	public void reset() {
		finished = false;

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
		/*
		 * for (int i = 0; i < width; ++i) { cells[i][0].setHint();
		 * hintCells.add(cells[i][0]); }
		 */

		// Find cells that have n surrounding mines and n closed neighbours
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (is_good(i, j)) {
					Cell current = cells[i][j];
					// Only apply logic to open cells with 0 surrounding mines
					if (current.isOpen() && current.getNumber() == 0) {    // Code to add if I want to find mines: (current.getNumber() == 0 || current.getNumber() == calcClosedNeighbours(i, j))
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
		if (!finished) JOptionPane.showMessageDialog(null, "No (more) known safe moves.");
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
					if (current.getNumber() != 0 && current.getNumber() == calcClosedNeighbours(i, j) && current.getNumber() != calcFlaggedNeighbours(i, j)) {
						List<Cell> n = getNeighbours(current); // List of neighbouring cells
						for (Cell c : n) {
							if (c.isClosed()) c.flag();
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
		if (!finished) JOptionPane.showMessageDialog(null, "No (more) known safe moves.");
		return false;
	}

	public void select(int x, int y) {
		if (cells[x][y].isFlagged())
			return;
		for (Cell c : hintCells) {
			c.resetHint();
		}
		hintCells.clear();

		// TODO Remove when product is finished
		debug(x, y);

		// Mines around the cell
		int cellNum = mineField.uncover(y, x); // x and y has to be reversed as MineField.java takes parameters in the order of height then width, not width then height

		cells[x][y].setNumber(cellNum);
		// openCells.add(cells[x][y]);
		cells[x][y].open();
		resetMarks();
		refresh();

		if (cellNum == -1) // If cell is a mine (-1), game is lost
		{
			endGame();
			JOptionPane.showMessageDialog(null, "BOOOOM!");
		} else if (won()) {
			endGame();
			JOptionPane.showMessageDialog(null, "Congratulations! You won!");
		}
	}

	private void endGame() {
		finished = true;
		hintBtn.setEnabled(false);
		assistBtn.setEnabled(false);
		autoBtn.setEnabled(false);
		try {
			openAllCells();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		refresh();
	}

	private void openAllCells() throws NoSuchAlgorithmException {
		mineField.open(password);
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int cellNum = mineField.uncover(j, i); // j then i as MineField.java takes (height, width), not (width, height)
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
		return (obscuredCount == noOfMines); // If the number of covered cells
												// left is equal to the number
												// of mines, game is won
	}

	public void mark(int x, int y) {
		if (cells[x][y].isFlagged())
			cells[x][y].unflag();
		else if (cells[x][y].isClosed())
			cells[x][y].flag();

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

	public boolean isFinished() {
		return finished;
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

	public List<Cell> getNeighbours(Cell c) {
		return getNeighbours(c.getX(), c.getY());
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

	// checks if (i,j) is within the field
	public boolean is_good(int i, int j) {
		return i >= 0 && i < cells.length && j >= 0 && j < cells[i].length;
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

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

}

/*
 * Sample for loop for copy and paste
 * 
 * for (int i = 0; i < width; ++i) { for (int j = 0; j < height; ++j) {
 * cells[i][j] } }
 * 
 */