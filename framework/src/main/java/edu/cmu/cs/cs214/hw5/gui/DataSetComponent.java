package edu.cmu.cs.cs214.hw5.gui;

import static java.awt.Font.BOLD;
import static javax.security.auth.callback.ConfirmationCallback.YES;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * DataSetComponent shows an imported dataSet as a block with its name
 * on it in the DataSet panel.
 */
class DataSetComponent extends JButton {

    /**
     * Width of component button.
     */
    private static final int BUTTON_WIDTH = 150;
    
    /**
     * Height of component button.
     */
    private static final int BUTTON_HEIGHT = 100;

    private static final Font FONT = new Font(new JLabel("").getFont().getFontName(), BOLD, 20);

    /**
     * Create and initialize a dataSet component.
     * 
     * @param parent geoData Framework Gui.
     * @param name name of dataSet.
     */
    DataSetComponent(GeoDataFrameworkGui parent, String name) {
        super(name);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        setHorizontalTextPosition(SwingConstants.CENTER);
        setFont(FONT);

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

        addActionListener(l -> {
            new DataSetOperationFrame(parent, name).setVisible(true);
        });

        CloseButton button = new CloseButton();
        button.addActionListener(l -> {
            int reply = JOptionPane.showConfirmDialog(this, "Delete this dataset?", name, JOptionPane.YES_NO_OPTION);
            if (reply == YES)
                parent.getCore().deleteDataSet(name);
        });
        add(button);
    }
}
