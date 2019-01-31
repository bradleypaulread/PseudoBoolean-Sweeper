
/*
 * Board.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class Board extends JPanel {
	private static final long serialVersionUID = 1L;
	private Minesweeper game;
	private Cell[][] cells;

	// Width of a cell
	private final int CELL_WIDTH = 20;

	public Board(Minesweeper m, int width, int height) {
		game = m;
		cells = game.getCells();
		addMouseListener(new MouseActions(game));
		setPreferredSize(new Dimension(width * CELL_WIDTH, height * CELL_WIDTH));
	}

	public int getCellWidth() {
		return CELL_WIDTH;
	}

	public void paintComponent(Graphics g) {
		// Update cell array
		cells = game.getCells();

		for (int i = 0; i < game.getx(); ++i) {
			for (int j = 0; j < game.gety(); ++j) {
				Cell current = cells[i][j];

				// Coord position of cell
				int posX = i * CELL_WIDTH;
				int posY = j * CELL_WIDTH;
				
				// For flagged cells
				if (current.isFlagged()) {
					if (game.isFinished()) {
						// If the game has finished and a mine has been correctly flagged
						//		set colour to Orange with a 'X' pattern
						if (current.isMine()) {
							g.setColor(Color.ORANGE);
							g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
							g.setColor(Color.BLACK);
							g.drawLine(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
							g.drawLine(posX, posY + CELL_WIDTH, posX + CELL_WIDTH, posY);
						// Otherwise incorrect flag so set cell to plain Red
						} else {
							g.setColor(Color.RED);
							g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
							g.setColor(Color.BLACK);
						}
					// If cell was flagged but game has not finished
					//		set colour to Yellow
					} else {
						g.setColor(Color.YELLOW);
						g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
						g.setColor(Color.BLACK);
					}
				// If cell is marked as a hint
				//		set colour to Pink
				} else if (current.isHint()) {
					g.setColor(Color.PINK);
					g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
					g.setColor(Color.BLACK);
				// If cell has not been clicked
				//		set colour to Gray
				} else if (current.isClosed()) {
					g.setColor(Color.GRAY);
					g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
					g.setColor(Color.BLACK);
				// If the cell is a mine (number is -1)
				//		set colour to Red with a 'X' pattern
				} else if (current.isMine()) {
					g.setColor(Color.RED);
					g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
					g.setColor(Color.BLACK);
					g.drawLine(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
					g.drawLine(posX, posY + CELL_WIDTH, posX + CELL_WIDTH, posY);
				// If cell is open
				//		set colour to Light Grey and paint cell number
				} else {
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
					g.setColor(Color.BLACK);
					g.setFont(new Font("Name", Font.PLAIN, 16));
					// Switch statement for printing a cells number (surrounding mines amount)
					switch (current.getNumber()) {
					default:
						break;
					// case 0:
					// 	g.drawString("0", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
					// 	break;
					case 1:
						g.drawString("1", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 2:
						g.drawString("2", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 3:
						g.drawString("3", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 4:
						g.drawString("4", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 5:
						g.drawString("5", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 6:
						g.drawString("6", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 7:
						g.drawString("7", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					case 8:
						g.drawString("8", (i * CELL_WIDTH + CELL_WIDTH / 2) - 4, (j * CELL_WIDTH + CELL_WIDTH / 2) + 6);
						break;
					}
				}
				// Draw grid
				g.setColor(Color.BLACK);
				g.drawRect(posX, posY, posX + CELL_WIDTH, posY + CELL_WIDTH);
			}
		}
	}
}