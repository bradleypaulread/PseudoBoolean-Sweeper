
/*
 * MouseAction.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseActions implements ActionListener, MouseListener {
	private Minesweeper mine;

	public MouseActions(Minesweeper m) {
		mine = m;
	}

	/**
	 * When reset button is clicked, reset board and enable buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		mine.getHintBtn().setEnabled(true);
		mine.getAssistBtn().setEnabled(true);
		mine.getAutoBtn().setEnabled(true);
		mine.reset();
		mine.refresh();
	}

	public void mouseClicked(MouseEvent e) {
		// If mouse 1 is pressed
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / 20;
			int y = e.getY() / 20;
			if (e.getClickCount() == 2) { // Double click, clear all neighbours around selected cell
				for (Cell c : mine.getNeighbours(x, y)) {
					mine.select(c.getX(), c.getY());
				}
			} else if (mine.is_good(x, y) && mine.getCell(x, y).isClosed()) {
				mine.select(x, y);
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) { // If mouse 2 is pressed
			int x = e.getX() / 20;
			int y = e.getY() / 20;

			if (mine.is_good(x, y) && mine.getCell(x, y).isClosed()) {
				mine.getCell(x, y).invertFlag();
			}
		}

		mine.refresh();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}