/**
 * A class that stores a cell's attributes. (E.g. its number, if it has been
 * flagged, if its a mine, its probability etc.).
 * 
 * @author Bradley Read
 * @version 1.0
 * @since 2019-01-28
 */
public class Cell {
	// ID of cell (cell coordinates)
	private int x, y;

	// Surrounding mines
	private int number;

	// Cell behaviour
	private boolean flagged; // Cell was flagged by user
	private boolean open; // Cell was probed by user
	private boolean safeHint; // Cell is hinted as being safe
	private boolean mineHint; // Cell is hinted as being a mine
	private boolean fail; // Cell was the one that lost the user the game
	private boolean marked; // Used to help deduce moves during a game
	private Double prob; // Percentage of cell being a mine
	private boolean bestCell; // If cell has the lowest prob of being a mine

	/**
	 * Constructor for Cell class.
	 * 
	 * @param x cell's x-axis position
	 * @param y cell's y-axis position
	 */
	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
		flagged = false;
		open = false;
		safeHint = false;
		mineHint = false;
		fail = false;
		prob = null;
	}

	/**
	 * @return the bestCell
	 */
	public boolean isBestCell() {
		return bestCell;
	}

	public void resetBestCell() {
		this.bestCell = false;
	}

	/**
	 * @return the probability of the cell being a mine. In the form 0.0 <= prob <=
	 *         1.0
	 */
	public Double getProb() {
		return prob;
	}

	/**
	 * @param prob the prob to set
	 */
	public void setProb(double prob) {
		this.prob = prob;
	}

	public void resetProb() {
		this.prob = null;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setNumber(int i) {
		number = i;
	}

	public boolean isFlagged() {
		return flagged;
	}

	public boolean isCellThatLost() {
		return fail;
	}

	public boolean isOpen() {
		return open;
	}

	public boolean isClosed() {
		return !open;
	}

	public int getNumber() {
		return number;
	}

	/**
	 * @return if number is -1 the cell is a mine
	 */
	public boolean isMine() {
		return (number == -1);
	}

	public void resetHint() {
		safeHint = false;
		mineHint = false;
	}

	public boolean isSafeHint() {
		return safeHint;
	}

	public boolean isMineHint() {
		return mineHint;
	}

	public boolean isHint() {
		return safeHint || mineHint;
	}

	/**
	 * @return If the cell has default values.
	 */
	public boolean isBlank() {
		return !open && !flagged;
	}

	@Override
	public String toString() {
		return "[" + x + "," + y + "]";
	}

	/**
	 * Flag/Unflag cell.
	 */
	public void invertFlag() {
		flagged = !flagged;
	}

	public boolean isMarked() {
		return marked;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @param flagged the flagged to set
	 */
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	/**
	 * @param open the open to set
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * @param safeHint the safeHint to set
	 */
	public void setSafeHint(boolean safeHint) {
		this.safeHint = safeHint;
	}

	/**
	 * @param mineHint the mineHint to set
	 */
	public void setMineHint(boolean mineHint) {
		this.mineHint = mineHint;
	}

	/**
	 * @return the fail
	 */
	public boolean isFail() {
		return fail;
	}

	/**
	 * @param fail the fail to set
	 */
	public void setFail(boolean fail) {
		this.fail = fail;
	}

	/**
	 * Marks the cell as blue, debugging use only
	 *
	 * @param marked the marked to set
	 */
	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	/**
	 * @param prob the prob to set
	 */
	public void setProb(Double prob) {
		this.prob = prob;
	}

	/**
	 * @param bestCell the bestCell to set
	 */
	public void setBestCell(boolean bestCell) {
		this.bestCell = bestCell;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (bestCell ? 1231 : 1237);
		result = prime * result + (fail ? 1231 : 1237);
		result = prime * result + (flagged ? 1231 : 1237);
		result = prime * result + (marked ? 1231 : 1237);
		result = prime * result + (mineHint ? 1231 : 1237);
		result = prime * result + number;
		result = prime * result + (open ? 1231 : 1237);
		result = prime * result + (safeHint ? 1231 : 1237);
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		if (bestCell != other.bestCell)
			return false;
		if (fail != other.fail)
			return false;
		if (flagged != other.flagged)
			return false;
		if (marked != other.marked)
			return false;
		if (mineHint != other.mineHint)
			return false;
		if (number != other.number)
			return false;
		if (open != other.open)
			return false;
		if (safeHint != other.safeHint)
			return false;
		if (x != other.x)
			return false;
        return y == other.y;
    }
}