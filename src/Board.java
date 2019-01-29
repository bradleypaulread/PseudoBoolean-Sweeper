/*
 * Board.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;
 
public class Board extends JPanel
{
	private static final long serialVersionUID = 1L;
	private Minesweeper game;
	//private MineField mineField;
	private Cell[][] cells;
	private final int cellWidth = 20;
	public Board(Minesweeper m, MineField mf)
	{
		//mineField = mf;
		game = m;
		cells = game.getCells();
 
		addMouseListener(new Actions(game));
 
		setPreferredSize(new Dimension(game.getx() * cellWidth, game.gety() * cellWidth));
	}
	
	public int getCellWidth() {
		return cellWidth;
	}
	
	public void paintComponent(Graphics g)
	{
		cells = game.getCells();
 
		for (int i = 0; i < game.getx(); ++i)
		{
			for (int j = 0; j < game.gety(); ++j)
			{
				Cell current = cells[i][j];
				int posX = i*cellWidth;
				int posY = j*cellWidth;
				if (current.isFlagged())
				{
					if (current.isMine() && game.isFinished())
					{
						g.setColor(Color.ORANGE);
						g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
						g.setColor(Color.BLACK);
 
						g.drawLine(posX, posY, posX + cellWidth, posY + cellWidth);
						g.drawLine(posX, posY + cellWidth, posX + cellWidth, posY);
					}
					else if (game.isFinished())
					{
						g.setColor(Color.GREEN);
						g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
						g.setColor(Color.BLACK);
					}
					else
					{
						g.setColor(Color.YELLOW);
						g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
						g.setColor(Color.BLACK);
					}
				}
				else if (current.isHint()) {
					g.setColor(Color.PINK);
					g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
					g.setColor(Color.BLACK);
				}
				else if (!current.isOpen())
				{
					g.setColor(Color.GRAY);
					g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
					g.setColor(Color.BLACK);
				}
				else if (current.isMine())
				{
					g.setColor(Color.RED);
					g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
					g.setColor(Color.BLACK);
					g.drawLine(posX, posY, posX + cellWidth, posY + cellWidth);
					g.drawLine(posX, posY + cellWidth, posX + cellWidth, posY);
				}
				else
				{
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(posX, posY, posX + cellWidth, posY + cellWidth);
					g.setColor(Color.BLACK);
				}
				if (current.isOpen())
				{
					// Debug
					//System.out.println(i*20 + ", " + j*20);

					// TODO Draw a better 0
					if (current.getNumber() == 0)
					{
						//g.setFont(new Font());
						g.drawString("0", (i*cellWidth + cellWidth/2)-3, (j*cellWidth + cellWidth/2)+5);
					}
					else if (current.getNumber() == 1)
					{
						g.drawLine(posX + 13, posY + 5, posX + 13, posY + 9);	//3
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
					}
					else if (current.getNumber() == 2)
					{
						g.drawLine(posX + 8, posY + 4, posX + 12, posY + 4);	//2
						g.drawLine(posX + 13, posY + 5, posX + 13, posY + 9);	//3
						g.drawLine(posX + 8, posY + 10, posX + 12, posY + 10);	//4
						g.drawLine(posX + 7, posY + 11, posX + 7, posY + 15);	//5
						g.drawLine(posX + 8, posY + 16, posX + 12, posY + 16);	//7
					}
					else if (current.getNumber() == 3)
					{
						g.drawLine(posX + 8, posY + 4, posX + 12, posY + 4);	//2
						g.drawLine(posX + 13, posY + 5, posX + 13, posY + 9);	//3
						g.drawLine(posX + 8, posY + 10, posX + 12, posY + 10);	//4
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
						g.drawLine(posX + 8, posY + 16, posX + 12, posY + 16);	//7
					}
					else if (current.getNumber() == 4)
					{
						g.drawLine(posX + 7, posY + 5, posX + 7, posY + 9);		//1
						g.drawLine(posX + 13, posY + 5, posX + 13, posY + 9);	//3
						g.drawLine(posX + 8, posY + 10, posX + 12, posY + 10);	//4
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
					}
					else if (current.getNumber() == 5)
					{
						g.drawLine(posX + 7, posY + 5, posX + 7, posY + 9);		//1
						g.drawLine(posX + 8, posY + 4, posX + 12, posY + 4);	//2
						g.drawLine(posX + 8, posY + 10, posX + 12, posY + 10);	//4
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
						g.drawLine(posX + 8, posY + 16, posX + 12, posY + 16);	//7
					}
					else if (current.getNumber() == 6)
					{
						g.drawLine(posX + 7, posY + 5, posX + 7, posY + 9);		//1
						g.drawLine(posX + 8, posY + 4, posX + 12, posY + 4);	//2
						g.drawLine(posX + 8, posY + 10, posX + 12, posY + 10);	//4
						g.drawLine(posX + 7, posY + 11, posX + 7, posY + 15);	//5
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
						g.drawLine(posX + 8, posY + 16, posX + 12, posY + 16);	//7
					}
					else if (current.getNumber() == 7)
					{
						g.drawLine(posX + 8, posY + 4, posX + 12, posY + 4);	//2
						g.drawLine(posX + 13, posY + 5, posX + 13, posY + 9);	//3
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
					}
					else if (current.getNumber() == 8)
					{
						g.drawLine(posX + 7, posY + 5, posX + 7, posY + 9);		//1
						g.drawLine(posX + 8, posY + 4, posX + 12, posY + 4);	//2
						g.drawLine(posX + 13, posY + 5, posX + 13, posY + 9);	//3
						g.drawLine(posX + 8, posY + 10, posX + 12, posY + 10);	//4
						g.drawLine(posX + 7, posY + 11, posX + 7, posY + 15);	//5
						g.drawLine(posX + 13, posY + 11, posX + 13, posY + 15);	//6
						g.drawLine(posX + 8, posY + 16, posX + 12, posY + 16);	//7
					}
				}
				g.setColor(Color.BLACK);
				g.drawRect(posX, posY, posX + cellWidth, posY + cellWidth);
			}
		}
	}
}