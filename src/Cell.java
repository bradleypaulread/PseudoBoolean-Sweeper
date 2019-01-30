/*
 * Cell.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */
 
public class Cell
{
	private int x;
	private int y;
	private boolean flagged;
	private boolean open;
	private int number;
	private boolean hint;
 
	public Cell(int x, int y)
	{
		this.x = x;
		this.y = y;
		flagged = false;
		open = false;
		hint = false;
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
 
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (flagged ? 1231 : 1237);
		result = prime * result + (hint ? 1231 : 1237);
		result = prime * result + number;
		result = prime * result + (open ? 1231 : 1237);
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
		if (flagged != other.flagged)
			return false;
		if (hint != other.hint)
			return false;
		if (number != other.number)
			return false;
		if (open != other.open)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
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
	
	public boolean isMine() {
		return (number == -1);
	}
	
	public boolean isHint() {
		return hint;
	}
	
	public void setHint() {
		hint = true;
	}
	
	public void resetHint() {
		hint = false;
	}
	
	public boolean isBlank() {
		return !open && !flagged && !hint;
	}

	@Override
	public String toString() {
		return "Cell [Pos=[" + x + "," + y + "]" + " isMine=" + this.isMine() + ", isFlagged=" + flagged + ", isOpen=" + open + ", isHint=" + isHint() + ", number="
				+ number + "]";
	}
}