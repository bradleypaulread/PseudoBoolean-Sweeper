/**
 * An Enum that specifies the dificulty of a game.
 * 
 * @author Bradley Read
 * @version 1.0
 * @since 2019-08-30
 */

public enum Difficulty {
    BEGINNER(9, 9, 10), INTERMEDIATE(16, 16, 40), EXPERT(30, 16, 99);

    public final int width;
    public final int height;
    public final int noOfMines;

    /**
     * Constructor for a Difficulty.
     * 
     * @param width number of columns
     * @param height number of rows
     * @param noOfMines number for mines present in this difficulty
     */
    Difficulty(int width, int height, int noOfMines) {
        this.width = width;
        this.height = height;
        this.noOfMines = noOfMines;
    }

}