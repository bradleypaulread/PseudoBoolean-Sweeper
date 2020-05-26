package main.java;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A blackbox minefield implementation. Distributes mines randomly and uniformly
 * and the interface only allows access to the mines data structur in a black
 * box mannar.
 * 
 * Created and supplied by Victor Khomenko, School of Computing, University of
 * Newcastle.
 * 
 * @author Victor Khomenko
 * @version 1.0
 * @since 2014-08-17
 */
public class MineField {
	private boolean[][] field;
	private boolean exploded = false;
	private boolean opened = false;

	// checks if (i,j) is within the field
	private boolean is_good(int i, int j) {
		return i >= 0 && i < field.length && j >= 0 && j < field[i].length;
	}

	// 0<height<100 is the height of the mine field
	// 0<width<100 is the width of the mine field
	// 0<=number_of_mines<=height*width is the number of mines
	// Constructs the mine field with the given dimensions and
	// plants the given number of mines
	public MineField(int height, int width, int number_of_mines) {
		assert height > 0;
		assert width > 0;
		assert number_of_mines >= 0 && number_of_mines <= height * width;

		// field's entries are initialised to false by default
		field = new boolean[height][width];
		// plant mines as follows:
		// iterate through the squares of the minefield,
		// and plant a mine in a particular square with the
		// probability m/n, where m is the number of unplanted mines
		// and n is the number of squares still to be considered
		// (including the current one);
		// the resulting distribution is close to uniform
		Random gen = new Random(); // random number generator
		int remaining_positions = height * width; // number of positions that have not been considered yet
		for (int i = 0; i < height; ++i) {
			for (int j = 0; j < width; ++j) {
				if (gen.nextInt(remaining_positions) < number_of_mines) {
					field[i][j] = true;
					--number_of_mines;
				}
				--remaining_positions;
			}
		}
	}

	// 0<=h<height and 0<=w<width are the coordinates;
	// return value: if there is no mine at the square (h,w) then the number of
	// mines around this square is returned (this number is in the range 0..8);
	// otherwise -1 is returned, and the class should never be called again
	// (its behaviour becomes undefined)
	public int uncover(int h, int w) {
		assert is_good(h, w);
		if (!opened) {
			assert !exploded;
			// in case the assertions are switched off, hang if exploded
			while (exploded)
				;
		}

		// check if exploded this time
		if (field[h][w]) {
			exploded = true;
			return -1;
		}

		// count the number of mines around this cell;
		// note that field[h][w] is false, so we can count it
		assert !field[h][w];
		int counter = 0;
		for (int i = h - 1; i <= h + 1; ++i) {
			for (int j = w - 1; j <= w + 1; ++j) {
				if (is_good(i, j) && field[i][j])
					++counter;
			}
		}
		return counter;
	}

	// check the supplied password; if it is correct, the object becomes "opened",
	// and the uncover method stops checking the exploded flag;
	// this is useful for showing mine positions at the end of the game;
	// the password is "hello", but during the demo it will change
	public boolean open(String password) throws NoSuchAlgorithmException {
		// check the password: compute its crypto-hash and compare with the expected
		// crypto-hash
		byte[] crypto_hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
		// convert crypto_hash to a hexadecimal String
		StringBuffer crypto_hex = new StringBuffer();
		for (byte b : crypto_hash) {
			int unsigned_b = 0xFF & b;
			if (unsigned_b < 16)
				crypto_hex.append('0');
			crypto_hex.append(Integer.toHexString(unsigned_b));
		}
		// compare with the cryptohash of the correct password
		String expected_hash = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
		if (!expected_hash.contentEquals(crypto_hex))
			return false;
		// if the password is correct, make the object "opened"
		opened = true;
		return true;
	}

	// public static void main(String[] args) throws NoSuchAlgorithmException{
	// main.java.MineField mf=new main.java.MineField(10,15,20);
	// for(int i=0;i<mf.field.length;++i){
	// System.out.println();
	// for(int j=0;j<mf.field[i].length;++j){
	// System.out.print(mf.field[i][j] ? "1" : "0");
	// }
	// }
	// System.out.print(mf.open("oops"));
	// System.out.print(mf.open("hello"));
	// }
}