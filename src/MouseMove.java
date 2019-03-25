
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
import java.awt.event.MouseMotionListener;

public class MouseMove implements MouseMotionListener {
	private Minesweeper game;
	private Board board;

	int currentX;
	int currentY;
	int cellWidth;

	public MouseMove(Minesweeper m, Board b) {
		game = m;
		board = b;
		cellWidth = board.getWidth();
		currentX = -1;
		currentY = -1;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX() / board.getCellWidth();
		int y = e.getY() / board.getCellWidth();
		if (x != currentX || y != currentY) {
			if (game.is_good(x, y)) {
				currentX = x;
				currentY = y;
				Cell cell = game.getCell(x, y);
				if (cell.isOpen()) {
					return;
				}
				if (cell.getProb() != null) {
					game.setDetail("Cell " + cell + " has a prob. of " + cell.getProb());
				}
			}
		}
	}

}