
/*
 * Board.java
 * 
 * Created by Potrik
 * Last modified: 07.22.13
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Actions implements ActionListener, MouseListener {
	private Minesweeper mine;

	public Actions(Minesweeper m) {
		mine = m;
	}

	public void actionPerformed(ActionEvent e) {
		mine.getHintBtn().setEnabled(true);
		mine.reset();
		mine.refresh();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 2) {	// Double click, clear all neighbours around selected cell
				for(int i=(e.getX()/20)-1;i<=(e.getX()/20)+1;++i){
					for(int j=(e.getY()/20)-1;j<=(e.getY()/20)+1;++j){
						if (mine.is_good(i, j)) mine.select(i, j);
					}
				}
			} else {
				int x = e.getX() / 20;
				int y = e.getY() / 20;

				mine.select(x, y);
				System.out.println(mine.getCells()[x][y]);
			}
		}

		if (e.getButton() == MouseEvent.BUTTON3) {
			int x = e.getX() / 20;
			int y = e.getY() / 20;

			mine.mark(x, y);
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