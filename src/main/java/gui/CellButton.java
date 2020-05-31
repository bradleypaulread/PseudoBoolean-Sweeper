package main.java.gui;

import main.java.DisplayState;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.util.Optional;

public class CellButton extends JButton {

    private static final int CELL_WIDTH = 50;
    private static final int TEXT_SIZE = CELL_WIDTH / 6;
    private DisplayState displayState;

    public CellButton(String name) {
        this.setPreferredSize(new Dimension(CELL_WIDTH, CELL_WIDTH));
        setDisplayState(DisplayState.CLOSED);
        this.setFont(new Font("", Font.BOLD, (CELL_WIDTH / 3)));
        this.setToolTipText(name);
    }

    public DisplayState getDisplayState() {
        return this.displayState;
    }

    public void setDisplayState(DisplayState displayState) {
        setBackground(displayState.colour);
        this.displayState = displayState;
    }

    // Enforces cells only having integer text (or "X" if int is -1)
    public void setNumber(int number) {
        String text = String.valueOf(number);
        Optional<Color> colour = switch (number) {
            case -1 -> {
                text = "X";
                yield Optional.of(Color.BLACK);
            }
            case 0 -> {
                text = "";
                yield Optional.empty();
            }
            case 1 -> Optional.of(new Color(0, 0, 255));    // Blue
            case 2 -> Optional.of(new Color(0, 130, 0));    // Green
            case 3 -> Optional.of(new Color(255, 0, 0));    // Red
            case 4 -> Optional.of(new Color(0, 0, 132));    // Dark Blue
            case 5 -> Optional.of(new Color(132, 0, 0));    // Burgundy Red
            case 6 -> Optional.of(new Color(0, 130, 132));  // Cyan-ish
            case 7 -> Optional.of(new Color(132, 0, 132));  // Purple
            default -> Optional.of(Color.BLACK);
        };
        if (colour.isPresent()) {
            // change the text colour of a disabled button
            this.setUI(new MetalButtonUI() {
                protected Color getDisabledTextColor() {
                    return colour.get();
                }
            });
        }
        setText(text);
    }
}