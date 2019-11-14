package edu.cmu.cs.cs214.hw5.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A button used to close tab.
 */
class CloseButton extends JButton {

    /**
     * Width of close button.
     */
    private static final int BUTTON_WIDTH = 17;

    /**
     * Height of close button.
     */
    private static final int BUTTON_HEIGHT = 27;

    /**
     * Create and Initialize a close button.
     */
    CloseButton() {
        super("x");
        setFocusable(false);
        setRolloverEnabled(true);
        setForeground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder());
        setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        // Make it transparent
        setContentAreaFilled(false);
        setBorderPainted(false);

        addMouseListener(new MouseAdapter() {
           @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(Color.BLACK);
            }
        });
    }
}
