
/*
 * Board.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 * 
 * Heavily modified by Bradley Read
 * Last modified: @date
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import javax.swing.JPanel;

public class Board extends JPanel {
	private static final long serialVersionUID = 1L;
	private Minesweeper game;
	private Cell[][] cells;
	// Width of a cell
	private final int CELL_WIDTH = 40;

	public Board(Minesweeper m, int width, int height) {
		game = m;
		cells = game.getCells();
		addMouseListener(new MouseActions(game, this));
		setPreferredSize(new Dimension(width * CELL_WIDTH, height * CELL_WIDTH));
	}

	public int getCellWidth() {
		return CELL_WIDTH;
	}

	public void paintComponent(Graphics g) {
		// Update cell array
		cells = game.getCells();
		// Clear canvas
		super.paintComponent(g);
		for (int i = 0; i < game.getx(); ++i) {
			for (int j = 0; j < game.gety(); ++j) {
				Cell current = cells[i][j];

				// Coord position of cell
				int posX = i * CELL_WIDTH;
				int posY = j * CELL_WIDTH;

				// For flagged cells
				if (current.isFlagged()) {
					if (game.isFinished()) {
						// If the game has finished and a mine has been
						// correctly flagged
						// set colour to Orange with a 'X' pattern
						if (current.isMine()) {
							g.setColor(new Color(233, 237, 149));
							drawCell(g, posX, posY);
							g.setColor(Color.BLACK);
							g.drawLine(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
							g.drawLine(posX, posY + CELL_WIDTH, posX + CELL_WIDTH, posY);
							// Otherwise incorrect flag so set cell to plain Red
						} else {
							g.setColor(new Color(255, 159, 155));
							drawCell(g, posX, posY);
							g.setColor(Color.BLACK);
						}
						// If cell was flagged but game has not finished
						// set colour to Yellow
					} else {
						g.setColor(new Color(233, 237, 149));
						drawCell(g, posX, posY);
						g.setColor(Color.BLACK);
					}
					// If cell is marked as a hint
					// set colour to Pink
				} else if (current.isMarked()) {
					g.setColor(new Color(142, 185, 255));
					drawCell(g, posX, posY);
					g.setColor(Color.BLACK);
				} else if (current.isSafeHint()) {
					g.setColor(new Color(130, 217, 130));
					drawCell(g, posX, posY);
					g.setColor(Color.BLACK);
				} else if (current.isMineHint()) {
					g.setColor(new Color(214, 159, 155));
					drawCell(g, posX, posY);
					g.setColor(Color.BLACK);
					// If cell has not been clicked
					// set colour to Gray
				} else if (current.isClosed()) {
					g.setColor(Color.GRAY);
					drawCell(g, posX, posY);
					g.setColor(Color.BLACK);
					// If the cell is a mine (number is -1)
					// set colour to Red with a 'X' pattern
				} else if (current.isMine()) {
					if (current.isCellThatLost()) {
						g.setColor(new Color(51, 51, 51));
					} else {
						g.setColor(new Color(255, 159, 155));
					}
					drawCell(g, posX, posY);
					g.setColor(Color.BLACK);
					g.drawLine(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
					g.drawLine(posX, posY + CELL_WIDTH, posX + CELL_WIDTH, posY);
					// If cell is open
					// set colour to Light Grey and paint cell number
				} else {
					g.setColor(Color.LIGHT_GRAY);
					drawCell(g, posX, posY);
					drawCellNumber(current.getNumber(), g, i, j);
				}
			}
		}
		g.setColor(Color.BLACK);
		// Draw Vertical grid lines
		for (int i = 0; i < game.gety() + 1; ++i) {
			g.drawLine(0, i * CELL_WIDTH, game.getx() * CELL_WIDTH, i * CELL_WIDTH);
		}
		// Draw Horizontal grid lines
		for (int i = 0; i < game.getx() + 1; ++i) {
			g.drawLine(i * CELL_WIDTH, 0, i * CELL_WIDTH, game.gety() * CELL_WIDTH);
		}
	}

	private void drawCell(Graphics g, int x, int y) {
		if (cells[x / CELL_WIDTH][y / CELL_WIDTH].getProb() != null) {
			double cellProb = cells[x / CELL_WIDTH][y / CELL_WIDTH].getProb();
			if (!(cellProb == 1.0 && cells[x / CELL_WIDTH][y / CELL_WIDTH].isFlagged())) {
				int redness = (int) (255 * cellProb);
				g.setColor(new Color(255, 255 - redness, 255 - redness));
				g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
				g.setColor(Color.BLACK);
				g.setFont(new Font("", Font.PLAIN, (int) (CELL_WIDTH / 4)));
				g.drawString(String.format("%.2f", cellProb * 100.00) + "%", x + 2, y + ((int) (CELL_WIDTH / 3)));
			} else {
				g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
			}
		} else {
			g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
		}
		if (game.getDebug()) {
			g.setFont(new Font("", Font.BOLD, (int) (CELL_WIDTH / 3)));
			g.setColor(Color.BLACK);
			g.drawString("" + (x / CELL_WIDTH) + "," + (y / CELL_WIDTH), x + 2, y + ((int) (CELL_WIDTH / 3)));
			game.refresh();
		}
	}

	private void drawCellNumber(int num, Graphics g, int x, int y) {
		g.setFont(new Font("", Font.PLAIN, (int) (CELL_WIDTH / 1.5)));
		switch (num) {
		case 1: // Blue
			g.setColor(new Color(0, 0, 255));
			break;
		case 2: // Green
			g.setColor(new Color(0, 130, 0));
			break;
		case 3: // Red
			g.setColor(new Color(255, 0, 0));
			break;
		case 4: // Dark Blue
			g.setColor(new Color(0, 0, 132));
			break;
		case 5: // Burgundy Red
			g.setColor(new Color(132, 0, 0));
			break;
		case 6: // Cyan-ish
			g.setColor(new Color(0, 130, 132));
			break;
		case 7: // Purple
			g.setColor(new Color(132, 0, 132));
			break;
		case 8: // Black
			g.setColor(new Color(0, 0, 0));
			break;
		default: // Dont do anything for other numbers (including 0)
			return;
		}
		g.drawString(Integer.toString(num), (x * CELL_WIDTH + CELL_WIDTH / 2) - (CELL_WIDTH / 6),
				(y * CELL_WIDTH + CELL_WIDTH / 2) + (CELL_WIDTH / 5));
	}
}
