
public class LaunchGame {

	public static void main(String[] args) {
        
        int x = 95; // Width of the board
        int y = 40; // Height of the board
        double diff = 0.2; // The difficulty of the game (percent of cells that are mines)
        int mines = 3; // Integer number of mines on the board
        assert diff >= 0.00 && diff < 1.00 && mines >= 0 && mines < (x * y);
        
        // new Minesweeper(x, y, diff); // Constructor for % mines
		new Minesweeper();
        // new Minesweeper(x, y, mines); // Constructor for int mines
	}

}
