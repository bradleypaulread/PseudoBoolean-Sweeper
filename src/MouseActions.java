
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
	private Minesweeper game;
	private Board board;

	public MouseActions(Minesweeper m, Board b) {
		game = m;
		board = b;
	}

	public void mouseClicked(MouseEvent e) {
		// // If mouse 1 is pressed
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();
			if (game.is_good(x, y)) {
				if (e.getClickCount() == 2) { // Double click, clear all neighbours around selected cell
					game.clearNeighbours(x, y);
				}
			}
		}
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
				game.refresh();
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) { // If right click is pressed
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();

			if (game.is_good(x, y) && game.getCell(x, y).isClosed()) {
				if (!game.getCell(x, y).isFlagged()) {
					game.decrementMines();
				} else {
					game.incrementMines();
				}
				game.getCell(x, y).invertFlag();
			}
			//    public void repaint(int x, int y, int width, int height) {
			board.repaint(10);
		}
	}

	public void mouseReleased(MouseEvent e) {
	}
}