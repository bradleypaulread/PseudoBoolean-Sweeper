
public class LaunchGame {

        public static void main(String[] args) {

                int x = 3; // Width of the board
                int y = 3; // Height of the board
                double diff = 0.2; // The difficulty of the game (percent of cells that are mines)
                int mines = 2; // Integer number of mines on the board
                assert diff >= 0.00 && diff < 1.00 && mines >= 0 && mines < (x * y);

                String oneCellMineSeaField = "{\"field\":[[true,false,false,false,false,false,false,false,true],[false,false,false,false,false,false,false,false,false],[false,false,true,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[false,false,false,false,true,false,true,false,false],[false,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[true,true,false,false,false,false,false,false,false],[true,true,false,false,false,false,false,false,true]],\"exploded\":false,\"opened\":false}";
                String lotsCellSafeSeaField = "{\"field\":[[false,false,false,false,false,false,false,false,false],[false,true,false,false,false,false,false,false,false],[false,false,false,false,true,false,false,false,false],[false,false,false,false,false,false,true,false,false],[false,false,false,false,false,false,false,false,false],[true,true,true,true,false,false,false,false,false],[false,false,false,true,false,false,false,false,false],[false,false,false,true,false,false,false,false,false],[false,false,false,true,false,false,false,false,false]],\"exploded\":false,\"opened\":false}";
                String lotsCellMineSeaField = "{\"field\":[[false,false,false,true,false,false,false,false,false],[true,false,true,false,false,false,false,true,true],[false,false,false,true,false,false,false,false,false],[false,false,false,false,false,false,true,false,false],[false,false,false,false,true,false,false,false,false],[true,true,true,true,false,false,false,true,false],[true,true,true,true,false,false,false,false,false],[true,true,true,true,false,false,false,true,false],[true,true,true,true,false,false,true,false,true]],\"exploded\":false,\"opened\":false}";
                String oneCellSafeSeaField = "{\"field\":[[true,true,false,false,true,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,true,false,true],[false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true],[false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,true,false,true,false,false,false,true,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,true,true,true,false,false,false,false,false,false,false,false,false],[false,true,false,true,true,true,false,false,false,false,true,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false],[false,false,false,true,true,true,false,false,false,false,false,true,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false],[false,false,true,false,false,false,false,false,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,true,false,false,true,true,true,true],[false,false,true,false,false,true,false,true,false,false,false,false,false,true,false,false,false,false,false,false,false,true,false,false,true,false,false,false,false,false],[false,false,false,false,true,false,false,true,false,false,false,true,true,true,false,false,false,false,false,true,false,false,false,false,true,false,true,true,false,false],[true,true,false,false,true,false,false,false,false,false,false,true,false,false,false,false,false,false,true,true,true,true,false,false,false,true,false,false,false,true],[false,false,false,true,true,false,false,true,false,false,false,false,false,true,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false],[false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false],[false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,true,false,true,true,false,false,false,true,false,false,false,false,false,false],[false,true,false,true,false,false,false,false,true,false,false,false,true,false,true,true,false,true,false,false,false,false,false,false,true,false,false,false,false,false],[true,true,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false],[false,true,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,true,false,true,false,false]],\"exploded\":false,\"opened\":false}";
                String noSea = "{\"field\":[[false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,true,true,false],[false,false,false,true,false,false,true,false,true,false,false,false,true,false,false,true,false,false,false,false,false,false,false,true,false,true,false,false,false,false],[false,false,true,false,false,false,true,false,true,false,true,true,false,false,true,false,false,false,false,false,false,true,false,true,false,false,false,false,true,true],[false,false,false,false,false,true,false,false,false,true,false,false,false,false,false,true,true,true,true,false,false,false,false,true,false,false,false,true,false,false],[false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,true,false,false,true,false,false,false,false,false,true,false,false,false],[false,true,false,false,true,false,false,true,false,false,true,false,true,false,false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false],[false,true,true,false,false,false,false,true,false,false,false,false,true,false,false,true,false,false,false,false,false,true,false,false,false,false,true,false,true,false],[true,false,false,false,false,false,false,true,false,true,false,false,true,false,true,false,true,true,false,false,false,true,false,false,false,false,false,false,false,false],[false,false,false,true,false,false,false,false,false,true,true,false,false,false,false,false,true,false,true,false,false,false,false,true,false,false,false,false,true,false],[false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false],[false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false],[false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,true,true,true,false,false,false,false,true,false,false,false,false,false,false],[false,true,true,false,true,false,false,false,false,true,false,false,true,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false],[false,false,false,false,true,false,false,false,false,false,false,false,true,false,false,false,true,true,false,true,true,false,false,false,false,true,false,false,false,true],[false,false,false,false,false,true,false,false,false,true,false,true,false,true,false,false,true,true,false,true,false,false,true,false,false,false,false,false,false,false]],\"exploded\":false,\"opened\":false}";      
                String longComp = "{\"field\":[[false,false,true,false,false,false,false,false,true,true,false,false,false,true,false,false,false,false,true,false,false,true,false,false,true,false,false,false,false,false],[false,false,false,false,true,true,true,true,false,true,false,true,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false],[false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,true,false,false,false,true,true,false,true,true,false],[false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,true,false],[false,false,false,false,false,false,false,true,false,true,false,false,false,true,false,false,false,false,false,false,false,false,true,false,true,false,true,false,false,false],[false,false,false,false,false,true,false,false,true,true,false,false,false,false,false,true,false,false,false,false,false,false,true,false,false,true,false,false,false,true],[false,false,false,false,false,false,false,false,true,false,true,false,false,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false],[false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,true,false,true,false,false,false,true,false,false],[false,false,false,false,true,true,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false,true,false,false,true,false,false,false],[false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,true,true,false],[false,false,false,false,true,false,false,true,true,true,false,false,false,false,false,false,true,false,false,false,false,false,true,false,false,false,false,false,true,true],[false,false,false,false,false,false,true,false,false,true,false,false,false,false,false,true,true,false,false,true,true,false,true,false,false,true,false,false,false,true],[false,false,false,false,false,false,false,false,false,false,true,false,false,false,true,false,false,true,true,false,false,true,false,false,false,false,false,false,false,true],[false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,true,false,false,false,false,true,true,false,true,false,false,false,false,true,true],[false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,true,false],[false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,true,false,false,false,false,true,false,true,false,false]],\"exploded\":false,\"opened\":false}";
                String temp = "{\"field\":[[false,false,false,false,false,false,true,true,true,false,false,false,false,false,false,false,false,true,false,false,false,false,true,true,false,false,true,false,false,false],[false,true,false,false,false,true,false,false,false,true,false,false,false,false,false,false,false,true,false,false,true,false,false,false,false,true,false,true,true,false],[true,true,false,false,false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false,false,true],[true,false,false,false,false,true,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false,false,false,true,false],[true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,true,false,false,false,false,true,false,false,false,true,false,false],[false,false,false,false,false,false,false,true,false,false,false,false,false,true,true,false,false,false,false,true,false,false,false,false,true,false,false,true,false,true],[false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,true,false,true,false,false,false],[false,false,false,false,true,false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true],[false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,true],[false,true,true,false,false,false,false,false,true,false,true,false,false,false,false,true,false,false,false,false,false,false,true,false,false,false,false,true,false,false],[false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false],[false,true,false,false,true,false,false,false,false,false,false,false,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false],[false,false,true,false,false,true,false,false,false,false,true,true,false,true,false,true,false,false,false,false,false,false,true,false,false,false,true,true,false,false],[true,true,false,true,false,false,false,false,false,false,false,false,true,false,false,false,true,false,true,false,true,true,false,false,false,false,false,false,false,false],[true,true,false,false,false,false,true,false,false,false,true,true,false,false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false],[true,false,false,true,false,true,false,false,false,true,false,false,false,false,false,false,false,false,false,true,true,true,false,true,false,false,false,false,false,true]],\"exploded\":true,\"opened\":true}";
                String temp2 = "{\"field\":[[false,false,false,false,true,false,false,false,false],[true,false,true,false,false,true,false,false,false],[false,false,false,true,false,false,false,true,false],[true,false,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,false],[false,true,false,false,false,false,false,false,false],[false,false,false,false,false,false,false,false,true],[false,false,false,false,false,false,false,false,false],[true,false,false,false,false,false,false,false,false]],\"exploded\":false,\"opened\":false}";
                
                new Minesweeper(9, 9, 10, temp2);
                // new Minesweeper(30, 16, 99, longComp);
                // new Minesweeper(9, 9, 28, lotsCellMineSeaField);
                // new Minesweeper(9, 9, 10, lotsCellSafeSeaField);
                // new Minesweeper();
                // new Minesweeper(Difficulty.INTERMEDIATE);
                // new Minesweeper(Difficulty.EXPERT);

                // new Minesweeper(x, y, diff); // Constructor for % mines
                // new Minesweeper(x, y, mines); // Constructor for int mines
        }
}