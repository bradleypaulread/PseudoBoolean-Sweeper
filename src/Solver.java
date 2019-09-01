import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.PBSolver;


/**
 * An abstract class for a board solver. Contains many helper methods. 
 * 
 * @author Bradley Read
 * @version 1.0
 * @since 2019-03-11
 */
public abstract class Solver {

	protected boolean quiet; // Should the solver make changes to GUI
	protected boolean madeFirstGuess; // Has a first guess function been called yet

	protected PBSolver solver;
	
	protected Minesweeper game;
	protected Cell[][] cells;
	protected AtomicBoolean running; // Should the solver be running

	/**
	 * Constructor for solver. Using this constructor means that the solver can not
	 * be stopped.
	 * 
     * @param game the game that the solver is going to perform moves on.
	 */
	public Solver(Minesweeper game) {
		running = new AtomicBoolean(true);
		quiet = false;
		madeFirstGuess = false;
		this.game = game;
		cells = game.getCells();
		solver = SolverFactory.newDefault();
	}

	/**
	 * Constuctor for sovler. The solver can be stopped by changing the passed
	 * running boolean to false.
	 * 
     * @param game the game that the solver is going to perform moves on.
	 * @param running the value that controls whether the solver should continue/stop solving.
	 */
	public Solver(Minesweeper game, AtomicBoolean running) {
		this.running = running;
		quiet = false;
		madeFirstGuess = false;
		this.game = game;
		cells = game.getCells();
		solver = SolverFactory.newDefault();
	}

	/**
	 * Highlights a safe/recommended move to the user.
	 * 
	 * @return True if a hint (one that has not already been hinted) is found. False
	 *         if no hint or futher hints are found.
	 */
	public abstract boolean hint();

	/**
	 * Probe a safe/recommended cell.
	 * 
	 * @return True if a cell is found and probed. False if no safe cell is found
	 *         and probed.
	 */
	public abstract boolean assist();

	/**
	 * Probe all safe/recommended cells present on the board. Stop when no more
	 * safe/recommended moves can be found. Basically repeatably calls the
	 * {@link #assist() assist} method.
	 */
	public abstract void solve();

	/**
	 * Return the cell on the board that has the best probability of revealing an
	 * opening. Calculations can be found at
	 * "http://datagenetics.com/blog/june12012/index.html". Order of Logic: Corner
	 * cell, else edge cell, else centre cell.
	 * 
	 * @return Cell that has best chance of revealing an opening (a cell with number
	 *         0).
	 */
	public Cell getFirstGuess() {
		cells = game.getCells();

		Cell cellToProbe;
		List<Cell> cornerCells = getClosedCornerCells();

		if (cornerCells.isEmpty()) {
			List<Cell> edgeCells = getClosedEdgeCells();
			if (!edgeCells.isEmpty()) {
				cellToProbe = getRandomCell(edgeCells);
			} else {
				cellToProbe = getRandomCell(getClosedCentreCells());
			}
		} else {
			cellToProbe = getRandomCell(cornerCells);
		}

		return cellToProbe;
	}

	/**
	 * Probe the cell with best best probability of revealing an opening. See
	 * {@link #getFirstGuess()}.
	 * 
	 * @return True if an opening was found or if an opening has previously been
	 *         found. False if no opening was found.
	 */
	public boolean makeFirstGuess() {
		if (game.isGameOver()) {
			return false;
		}
		if (madeFirstGuess) {
			return true;
		}
		cells = game.getCells();
		int openCellsCount = getLandCells().size();
		Cell cellToProbe = getFirstGuess();
		if (quiet) {
			game.quietProbe(cellToProbe.getX(), cellToProbe.getY());
		} else {
			game.probe(cellToProbe.getX(), cellToProbe.getY());
		}
		int newOpenCellsCount = getLandCells().size();

		// If the difference between before and after open cells is more than 1
		// then an opening was found
		boolean foundOpening = (newOpenCellsCount - openCellsCount) > 1;

		madeFirstGuess = foundOpening;

		return foundOpening;
	}

	/**
	 * Probe a corner cell. Intended to be used when performing simulations.
	 * 
	 * @return True if a corner cell is probed and revealed an opening (a cell with
	 *         number 0). False otherwise.
	 */
	public boolean makeFirstGuessCorner() {
		if (game.isGameOver()) {
			return false;
		}
		if (madeFirstGuess) {
			return true;
		}
		cells = game.getCells();
		List<Cell> cornerCells = getClosedCornerCells();

		int openCellsCount = getLandCells().size();

		Cell cellToProbe = getRandomCell(cornerCells);
		game.quietProbe(cellToProbe.getX(), cellToProbe.getY());

		int newOpenCellsCount = getLandCells().size();

		boolean foundOpening = (newOpenCellsCount - openCellsCount) > 1;

		return foundOpening;
	}

	/**
	 * Probe an edge cell. Intended to be used when performing simulations.
	 * 
	 * @return True if an edge cell is probed and revealed an opening (a cell with
	 *         number 0). False otherwise.
	 */
	public boolean makeFirstGuessEdge() {
		if (game.isGameOver()) {
			return false;
		}
		if (madeFirstGuess) {
			return true;
		}
		cells = game.getCells();
		List<Cell> edgeCells = getClosedEdgeCells();

		int openCellsCount = getLandCells().size();

		Cell cellToProbe = getRandomCell(edgeCells);
		game.quietProbe(cellToProbe.getX(), cellToProbe.getY());

		int newOpenCellsCount = getLandCells().size();

		boolean foundOpening = (newOpenCellsCount - openCellsCount) > 1;
		return foundOpening;
	}

	/**
	 * Probe an centre cell. Intended to be used when performing simulations.
	 * 
	 * @return True if an centre cell is probed and revealed an opening (a cell with
	 *         number 0). False otherwise.
	 */
	public boolean makeFirstGuessCentre() {
		if (game.isGameOver()) {
			return false;
		}
		if (madeFirstGuess) {
			return true;
		}
		cells = game.getCells();
		List<Cell> centreCells = getClosedCentreCells();

		int openCellsCount = getLandCells().size();

		Cell cellToProbe = getRandomCell(centreCells);
		game.quietProbe(cellToProbe.getX(), cellToProbe.getY());

		int newOpenCellsCount = getLandCells().size();

		boolean foundOpening = (newOpenCellsCount - openCellsCount) > 1;
		return foundOpening;
	}

	/**
	 * Get a list of all closed centre cells. Note: a centre cell is a cell that
	 * does not touch any of the edges of the board.
	 * 
	 * @return a list of all closed centre cells.
	 */
	private List<Cell> getClosedCentreCells() {
		int width = cells.length;
		int height = cells[0].length;
		List<Cell> centreCells = new ArrayList<>();
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				Cell c = cells[i][j];
				if (c.isClosed()) {
					centreCells.add(c);
				}
			}
		}
		return centreCells;
	}

	/**
	 * Get a list of all closed corner cells.
	 * 
	 * @return a list of all the closed corner cells.
	 */
	private List<Cell> getClosedCornerCells() {
		int width = cells.length;
		int height = cells[0].length;
		List<Cell> cornerCells = new ArrayList<>();
		if (cells[0][0].isClosed()) {
			cornerCells.add(cells[0][0]);
		}
		if (cells[width - 1][0].isClosed()) {
			cornerCells.add(cells[width - 1][0]);
		}
		if (cells[0][height - 1].isClosed()) {
			cornerCells.add(cells[0][height - 1]);
		}
		if (cells[width - 1][height - 1].isClosed()) {
			cornerCells.add(cells[width - 1][height - 1]);
		}
		return cornerCells;
	}

	/**
	 * Get a list of all closed edge cells. Note: an edge cell is a cell that is on
	 * the border of the board. DOES NOT INCLUDE CORNER CELLS.
	 * 
	 * @return a list of all the closed edge cells.
	 */
	public List<Cell> getClosedEdgeCells() {
		cells = game.getCells();
		List<Cell> borderCells = new ArrayList<>();
		int width = cells.length;
		int height = cells[0].length;
		for (int i = 1; i < width - 1; i++) {
			Cell c = cells[i][0];
			if (c.isClosed()) {
				borderCells.add(c);
			}
			c = cells[i][height - 1];
			if (c.isClosed()) {
				borderCells.add(c);
			}
		}
		for (int i = 1; i < height - 1; i++) {
			Cell c = cells[0][i];
			if (c.isClosed()) {
				borderCells.add(c);
			}
			c = cells[width - 1][i];
			if (c.isClosed()) {
				borderCells.add(c);
			}
		}
		return borderCells;
	}

	/**
	 * When passed a cell and a board, create a unique identifier (a single integer)
	 * for that cell. To be used for creating litrals
	 * 
	 * @param c     Cell to encode.
	 * @param board Board the cell is present in, used to get the width of the
	 *              board.
	 * @return Unique integer identifier for given cell.
	 */
	protected int encodeCellId(Cell c) {
		return (c.getY() * cells.length + c.getX()) + 1;
	}

	/**
	 * When passed an identity, decode and return the cell it is referring to.
	 * 
	 * @param id    Unique encoded identity id.
	 * @param board Board the cell would be present in, used to get the width of the
	 *              board.
	 * @return Cell that the id refers to.
	 */
	public Cell decodeCellId(int id) {
		int posId = id < 0 ? id * -1 : id;
		if (posId > ((cells[0].length - 1) * cells.length + (cells.length - 1)) + 1) {
			return null;
		}
		int x = (posId - 1) % cells.length;
		int y = ((posId - 1) - x) / cells.length;
		return cells[x][y];
	}

	/**
	 * Encodes a literal so that it does not colide with any cell literals.
	 * 
	 * @param i the ith literal wanting to be encoded.
	 * 
	 * @return an encoded literal.
	 */
	protected int encodeLit(int i) {
		return (cells[0].length * cells.length) + cells.length + i;
	}

	@SuppressWarnings("unused")
	private Cell decodeLit(int lit) {
		int posLit = lit < 0 ? lit * -1 : lit;
		if (posLit > (cells[0].length - 1) * cells.length + (cells.length - 1) + 2) {
			return null;
		}
		int x = (posLit - 1) % cells.length;
		int y = ((posLit - 1) - x) / cells.length;
		return cells[x][y];
	}

	/**
	 * Return the neighbouring cells around a cell.
	 * 
	 * @param cell the cell whoses neighbours will be returned.
	 * 
	 * @return a list of the neighbouring cells of the passed cell.
	 */
	public List<Cell> getNeighbours(Cell cell) {
		return getNeighbours(cell.getX(), cell.getY());
	}

	/**
	 * Return the neighbouring cells around a cell.
	 * 
	 * @param x the cell's X coord whoses neighbours will be returned.
	 * @param y the cell's Y coord whoses neighbours will be returned.]
	 * 
	 * @return a list of the neighbouring cells of the passed cell.
	 */
	public List<Cell> getNeighbours(int x, int y) {
		cells = game.getCells();
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
	 * Return a list of all the games land cells. Note: a land cell is a cell that
	 * has been probed (is open).
	 * 
	 * @return a list of cells that are classed as land cells.
	 */
	public List<Cell> getLandCells() {
		List<Cell> landCells = new ArrayList<>();
		for (Cell col[] : cells) {
			for (Cell c : col) {
				if (c.isOpen()) {
					landCells.add(c);
				}
			}
		}
		return landCells;
	}

	/**
	 * Return a list of all the games sea cells. Note: a sea cell is a cell that has
	 * not been probed and does not touch an open cell.
	 * 
	 * @return a list of cells that are classed as sea cells.
	 */
	public List<Cell> getSeaCells() {
		List<Cell> sea = new ArrayList<>();
		Set<Cell> shoreClosed = new HashSet<>(getClosedShoreCells());
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isClosed() && !shoreClosed.contains(current)) {
					sea.add(current);
				}
			}
		}
		return sea;
	}

	/**
	 * Return a list of all the games closed shore cells. Note: a "closed" shore
	 * cell is a cell that has not been probed (is closed) and touches both a land
	 * cell and a sea cell.
	 * 
	 * @return a list of cells that are classed as closed shore cells.
	 */
	public List<Cell> getClosedShoreCells() {
		List<Cell> shoreClosed = new ArrayList<>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isClosed()) {
					List<Cell> neighbours = getNeighbours(current);
					int noOfNeighbours = neighbours.size();
					neighbours.removeIf(c -> c.isOpen());
					int noOfClosedNeighbours = neighbours.size();
					if (noOfClosedNeighbours < noOfNeighbours) {
						shoreClosed.add(current);
					}
				}
			}
		}
		return shoreClosed;
	}

	/**
	 * Return a list of all the games open sore cells. Note: a "open" shore cell is
	 * a cell that has has been probed (is open) and touches a closed cell.
	 * 
	 * @return a list of cells that are classsed as open shore cells.
	 */
	public List<Cell> getOpenShoreCells() {
		List<Cell> shoreOpen = new ArrayList<>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell current = cells[i][j];
				if (current.isOpen()) {
					List<Cell> neighbours = getNeighbours(current);
					neighbours.removeIf(cell -> cell.isOpen());
					if (!neighbours.isEmpty()) {
						shoreOpen.add(current);
					}
				}
			}
		}
		return shoreOpen;
	}

	/**
	 * Count the amount of flagged cells are around a cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 * 
	 * @return the number of flagged neighbouring cells.
	 */
	public int calcFlaggedNeighbours(int x, int y) {
		int flagCount = 0;
		// count how many flagged cells are around a cell
		List<Cell> neighbours = getNeighbours(x, y);
		for (Cell c : neighbours) {
			if (c.isFlagged()) {
				++flagCount;
			}
		}
		return flagCount;
	}

	/**
	 * Count the amount of closed cells are around a cell.
	 * 
	 * @param x X-axis coordinate of cell.
	 * @param y Y-axis coordinate of cell.
	 * 
	 * @return the number of closed neighbouring cells.
	 */
	public int calcClosedNeighbours(int x, int y) {
		int closedCount = 0;
		// for loop to count how many closed cells are around a cell
		List<Cell> neighbours = getNeighbours(x, y);
		for (Cell c : neighbours) {
			if (c.isClosed()) {
				++closedCount;
			}
		}
		return closedCount;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	/**
	 * Probe a random cell on the board.
	 */
	public void probeRandomCell() {
		cells = game.getCells();
		List<Cell> sea = getSeaCells();
		List<Cell> shoreClosed = getClosedShoreCells();
		sea.addAll(shoreClosed);
		sea.removeIf(c -> !c.isBlank());

		if (!sea.isEmpty()) {
			Cell selectedCell = sea.get(new Random().nextInt(sea.size()));
			game.quietProbe(selectedCell.getX(), selectedCell.getY());
		} else {
			game.setGameOver(true);
		}
	}

	/**
	 * Get a random cell from a list of cells. Returns null if passed cellList if
	 * empty.
	 * 
	 * @param cellList the list of cells the select a random cell from.
	 * 
	 * @return a random cell. Returns null if passed cellList if empty.
	 */
	public Cell getRandomCell(List<Cell> cellList) {
		if (!cellList.isEmpty()) {
			Cell selectedCell = cellList.get(new Random().nextInt(cellList.size()));
			return selectedCell;
		}
		return null;
	}

	public void stopSolver() {
		solver.stop();
		solver.reset();
	}
}