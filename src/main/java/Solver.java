package main.java;

public interface Solver {
    /**
     * Highlights a safe/recommended move to the user.
     *
     * @return True if a hint (one that has not already been hinted) is found. False
     * if no hint or futher hints are found.
     */
    boolean hint();

    /**
     * Probe a safe/recommended cell.
     *
     * @return True if a cell is found and probed. False if no safe cell is found
     * and probed.
     */
    boolean assist();

    /**
     * Probe all safe/recommended cells present on the board. Stop when no more
     * safe/recommended moves can be found. Basically repeatably calls the
     * {@link #assist() assist} method.
     */
    void solve();

//    boolean stop();
}
