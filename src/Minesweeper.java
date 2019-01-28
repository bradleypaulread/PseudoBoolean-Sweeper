
/*
 * Minesweeper.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
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
	private MineField mineField;
	private int width, height;
	private Cell[][] cells;
	private List<Cell> hintCells = new ArrayList<Cell>();
	private int noOfMines; // Number of mines
	private Board board;
	private JButton resetBtn;
	private JButton hintBtn;
	private boolean finished;
	int test = 0;

	public Minesweeper(int x, int y, int d) {
		width = x;
		height = y;
		noOfMines = d;
		cells = new Cell[width][height];
		mineField = new MineField(height, width, noOfMines);
		reset();

		board = new Board(this, mineField);
		resetBtn = new JButton("Reset");
		hintBtn = new JButton("Hint");

		add(hintBtn, BorderLayout.NORTH);

		hintBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				genHint();
				System.out.println("Set of hints = " + hintCells);
				// board.repaint();
			}
		});

		add(board, BorderLayout.CENTER);
		add(resetBtn, BorderLayout.SOUTH);

		resetBtn.addActionListener(new Actions(this));
		// this.setLocationRelativeTo(null); //center JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setResizable(false);
		// setPreferredSize(new Dimension(1000, 1000));

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

	public void genHint() {
		/*
		 * for (int i = 0; i < width; ++i) { cells[i][0].setHint();
		 * hintCells.add(cells[i][0]); }
		 */
		
		// Find cells that have 0 surrounding mines but closed neighbours
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if (is_good(i, j)) {
					Cell current = cells[i][j];
					if (current.isOpen() && current.getNumber() == 0) {
						List<Cell> n = getNeighbours(current);
						for (int k = 0; k < n.size(); ++k) {
							if (n.get(k).isClosed() && !n.get(k).isHint() && !n.get(k).isFlagged()) {
								n.get(k).setHint();
								hintCells.add(n.get(k));
								refresh();
								return;
							}
						}
					}
				}
			}
		}
		/*for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				// If the number of neighbouring flagged cells is the same as
				// the surrounding mines then other cells are safe
				if (cells[i][j].isOpen() && cells[i][j].getNumber() != 0
						&& cells[i][j].getNumber() == calcFlaggedNeighbours(i, j)) {
					List<Cell> neighbours = getNeighbours(i, j);
					for (int k = 0; k < neighbours.size(); ++k) {
						Cell current = neighbours.get(k);
						if (!current.isFlagged() && !hintCells.contains(current) && current.getNumber() != 0 && current.isClosed()) {
							current.setHint();
							hintCells.add(current);
							refresh();
							return;
						}
					}
				}
			}
		}*/
		JOptionPane.showMessageDialog(null, "No known safe moves.");
	}

	public void select(int x, int y) {
		if (cells[x][y].isFlagged())
			return;
		for (Cell c : hintCells) {
			c.resetHint();
		}
		hintCells.clear();
		
		debug(x, y);
		
		// Mines around the cell
		int cellNum = mineField.uncover(x, y);

		cells[x][y].setNumber(cellNum);
		// openCells.add(cells[x][y]);
		cells[x][y].open();
		resetMarks();
		refresh();

		if (cellNum == -1) // If cell is a mine (-1), game is lost
		{
			loose();
		} else if (won()) {
			win();
		}
	}

	private void loose() {
		finished = true;
		try {
			mineField.open("hello");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		refresh();
		JOptionPane.showMessageDialog(null, "BOOOOM!");
		reset();
	}

	private void win() {
		finished = true;
		for (int i = 0; i < width; ++i)
			try {
				mineField.open("hello");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		refresh();
		JOptionPane.showMessageDialog(null, "Congratulations! You won!");
		reset();
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
		assert is_good(x, y);
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y + 1; ++j) {
				if ((i != x || j != y) && is_good(i, j)) {
					if (cells[i][j].isClosed()) {
						++count;
					}
				}
			}
		}
		return count;
	}

	public int calcFlaggedNeighbours(int x, int y) {
		int count = 0;
		// for loop to count how many flagged cells are around a cell
		assert is_good(x, y);
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y + 1; ++j) {
				if ((i != x || j != y) && is_good(i, j)) {
					if (cells[i][j].isFlagged()) {
						++count;
					}
				}
			}
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

}

/*
 * Sample for loop for copy and paste
 * 
 * for (int i = 0; i < width; ++i) { for (int j = 0; j < height; ++j) {
 * cells[i][j] } }
 * 
 */