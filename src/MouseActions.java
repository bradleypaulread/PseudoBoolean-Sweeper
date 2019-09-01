import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A class that manages interaction to the board (cells) with the cursor.
 * 
 * @author Bradley Read
 * @version 1.0
 * @since 2019-08-30
 */
public class MouseActions implements MouseListener {
	
	private Minesweeper game;
	private Board board;

	public MouseActions(Minesweeper game, Board board) {
		this.game = game;
		this.board = board;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		board.resetBounds();
	}

	public void mousePressed(MouseEvent e) {
		// If left click is pressed
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();
			if (game.is_good(x, y)) {
				if (game.getCell(x, y).isClosed()) {
					game.probe(x, y);
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) { // If right click is pressed
			int x = e.getX() / board.getCellWidth();
			int y = e.getY() / board.getCellWidth();

			if (game.is_good(x, y) && game.getCell(x, y).isClosed()) {
				if (e.isControlDown()) {
					game.getCell(x, y).setMarked(true);
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