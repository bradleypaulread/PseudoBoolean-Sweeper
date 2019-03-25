
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
import java.awt.event.MouseMotionListener;

public class MouseActions implements MouseListener {
	private Minesweeper game;
	private Board board;

	public MouseActions(Minesweeper m, Board b) {
		game = m;
		board = b;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		// If left click is pressed
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();
			if (game.is_good(x, y)) {
				if (game.getCell(x, y).isClosed()) {
					game.select(x, y);
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) { // If right click is pressed
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();

			if (game.is_good(x, y) && game.getCell(x, y).isClosed()) {
				if (e.isControlDown()) {
					game.getCell(x, y).mark();
				} else {
					if (!game.getCell(x, y).isFlagged()) {
						game.decrementMines();
					} else {
						game.incrementMines();
					}
					game.getCell(x, y).invertFlag();
					game.resetHints();
					game.resetProbs();
				}
				game.refresh();
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

}