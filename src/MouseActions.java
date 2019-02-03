
/*
 * MouseAction.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseActions implements MouseListener {
	private Minesweeper mine;
	private Board board;

	public MouseActions(Minesweeper m, Board b) {
		mine = m;
		board = b;
	}

	public void mouseClicked(MouseEvent e) {
		// // If mouse 1 is pressed
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();
			if (mine.is_good(x, y)) {
				if (e.getClickCount() == 2) { // Double click, clear all neighbours around selected cell
					for (Cell c : mine.getNeighbours(x, y)) {
						if (c.isClosed()) {
							mine.select(c.getX(), c.getY());
						}
					}
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		// If mouse 1 is pressed
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();
			if (mine.is_good(x, y)) {
				if (mine.getCell(x, y).isClosed()) {
					mine.select(x, y);
				}
				mine.refresh();
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) { // If mouse 2 is pressed
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();

			if (mine.is_good(x, y) && mine.getCell(x, y).isClosed()) {
				if (!mine.getCell(x, y).isFlagged()) {
					mine.decrementMines();
				} else {
					mine.incrementMines();
				}
				mine.getCell(x, y).invertFlag();
			}
			mine.refresh();
		}
	}

	public void mouseReleased(MouseEvent e) {
	}
}