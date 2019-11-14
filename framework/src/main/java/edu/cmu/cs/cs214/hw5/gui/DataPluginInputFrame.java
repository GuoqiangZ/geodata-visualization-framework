package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.UserInputConfig;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

/**
 * DataPluginInput Frame lets user specify or select usage-specific configuration
 * parameters.
 */
class DataPluginInputFrame extends JFrame {

    /**
     * Receiver type.
     */
    enum Receiver {
        DATA_PLUGIN, DISPLAY_PLUGIN
    }

    /**
     * Width of data plugin input panel.
     */
    private static final int INPUT_PANEL_WIDTH = 480;

    /**
     * Height of data plugin input panel.
     */
    private static final int INPUT_PANEL_HEIGHT = 360;

    /**
     * Create and initialize the data plugin input frame.
     * 
     * @param parent geoData Framework Gui.
     * @param pluginType type of plugin.
     * @param pluginName name of plugin.
     */
    DataPluginInputFrame(GeoDataFrameworkGui parent, Receiver pluginType, String pluginName) {
        super(pluginType.toString() + ": " + pluginName);

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(INPUT_PANEL_WIDTH, INPUT_PANEL_HEIGHT));


        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        upperPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(upperPanel, BorderLayout.NORTH);

        List<UserInputConfig> configSpec = parent.getCore().getDataPluginConfigs(pluginName);
        UserInputPanel userInputPanel = new UserInputPanel(configSpec);

        upperPanel.add(userInputPanel);

        upperPanel.add(new JSeparator());
        add(Box.createVerticalGlue(), BorderLayout.CENTER);

        JPanel lowerPanel = new JPanel();
        add(lowerPanel, BorderLayout.SOUTH);

        JButton cancelButton = new JButton("Cancel");
        lowerPanel.add(cancelButton);
        cancelButton.addActionListener(l -> {
            this.setVisible(false);
            this.dispose();
        });

        JButton okButton = new JButton("OK");
        lowerPanel.add(okButton);
        if (pluginType.equals(Receiver.DATA_PLUGIN)) {
            UserInputComponent dataSetNameInput = new UserInputComponent.TextFieldInput("DataSet Name");
            upperPanel.add(dataSetNameInput);
            okButton.addActionListener(l -> {
                try {
                    parent.loadData(pluginName, dataSetNameInput.getUserInput().get(0), userInputPanel.getUserInput());
                    this.setVisible(false);
                    this.dispose();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }

            });
        }

        pack();
        setLocationRelativeTo(null);
    }
}
