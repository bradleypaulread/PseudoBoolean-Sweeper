/*
 * Cell.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */
 
public class Cell {
	// ID of cell (cell coordinates)
	private int x, y;

	// Surrounding mines
	private int number;

	// Cell behaviour
	private boolean flagged;
	private boolean open;
	private boolean safeHint;
	private boolean mineHint;
	private boolean fail;
	private boolean marked;
	private Double prob; // Percentage of cell being a mine
	private boolean bestCell; // If cell has the lowest prob of being a mine

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

	/**
	 * @param bestCell the bestCell to set
	 */
	public void setBestCell() {
		this.bestCell = true;
	}

	public void resetBestCell() {
		this.bestCell = false;
	}

	/**
	 * @return the prob
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
	
	/**
	 * @param prob the prob to set
	 */
	public void resetProb() {
		this.prob = null;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
 
	public void flag()
	{
		flagged = true;
	}
 
	public void unflag()
	{
		flagged = false;
	}
 
	public void open()
	{
		open = true;
	}
 
	public void setNumber(int i)
	{
		number = i;
	}
 
	public boolean isFlagged()
	{
		return flagged;
	}

	public void setFail() {
		fail = true;
	}

	public boolean isCellThatLost() {
		return fail;
	}
 
	public boolean isOpen()
	{
		return open;
	}
	
	public boolean isClosed()
	{
		return !open;
	}
 
	public int getNumber()
	{
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
	
	public void setSafeHint() {
		safeHint = true;
	}
	
	public boolean isMineHint() {
		return mineHint;
	}
	
	public void setMineHint() {
		mineHint = true;
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

	public void mark() {
		marked = !marked;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fail ? 1231 : 1237);
		result = prime * result + (flagged ? 1231 : 1237);
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
		if (fail != other.fail)
			return false;
		if (flagged != other.flagged)
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
		if (y != other.y)
			return false;
		return true;
	}
}