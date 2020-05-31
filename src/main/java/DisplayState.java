package main.java;

import java.awt.*;

/**
 * An Enum that specifies the hint state of a GUI cell.
 *
 * @author Bradley Read
 * @version 1.0
 * @since 2019-08-30
 */

public enum DisplayState {
    CLOSED(null),
    OPEN(null),
    FLAG(new Color(233, 237, 149)),
    MINE_HINT(new Color(214, 159, 155)),
    SAFE_HINT(new Color(130, 217, 130)),
    ;

    public final Color colour;

    DisplayState(Color colour) {
        this.colour = colour;
    }
}