package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StringFormPanel is a control panel to let user select string typed filtering 
 * rule. The rows of data which are selected by user will be displayed in the 
 * graph later.
 */
class StringFormPanel extends JPanel {
    
    /**
     * Component where user can specify the column to filter on.
     */
    private UserInputComponent columnComponent;

    /**
     * Component where user can specify the values to keep after filtering.
     */
    private UserInputComponent valueComponent;

    /**
     * COLUMN_NAME configuration labels.
     */
    private static final String COLUMN_NAME = "Column Name";

    /**
     * VALUES configuration labels.
     */
    private static final String VALUES = "Values";

    /**
     * Create and initialize a string form panel.
     * 
     * @param configs a list of UserInputConfig containing user-specific configuration information.
     * @param dataSetName name of dataSet.
     * @param parent geoData Framework Gui.
     */
    StringFormPanel(List<UserInputConfig> configs, String dataSetName, GeoDataFrameworkGui parent) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.columnComponent = new UserInputComponent.SingleSelectionInput(COLUMN_NAME, configs.get(0).getSelectionList());
        this.valueComponent = new UserInputComponent.MultiSelectionInput(VALUES, new ArrayList<>());
        add(this.columnComponent);
        add(this.valueComponent);

        this.columnComponent.addActionListener(l -> {
            List<String> selectedPlugin = this.columnComponent.getUserInput();
            if (!selectedPlugin.isEmpty() && selectedPlugin.get(0) != null) {
                DataSet dataSet = parent.getCore().getDataSet(dataSetName);
                String userLabel = this.getUserInput().get(COLUMN_NAME).get(0);
                List<Object> column = dataSet.getColumn(userLabel);
                List<Object> distinctValues = column.stream().distinct().collect(Collectors.toList());
                List<String> strDistinctValues = new ArrayList<>();
                for (Object distinctValue: distinctValues) {
                    strDistinctValues.add(String.valueOf(distinctValue));
                }
                UserInputComponent userInputComponent = new UserInputComponent.MultiSelectionInput(VALUES, strDistinctValues);
                remove(this.valueComponent);
                this.valueComponent = userInputComponent;
                this.add(this.valueComponent);
                this.valueComponent.setVisible(true);
                revalidate();
                repaint();
            } else {
                this.valueComponent.setVisible(false);
            }
        });
        this.valueComponent.setVisible(false);
    }

    /**
     * Return a mapping from configuration label names to user inputs.
     * @return mapping from configuration label names to user inputs.
     */
    Map<String, List<String>> getUserInput() {
        Map<String, List<String>> res = new HashMap<>();
        res.put(COLUMN_NAME, columnComponent.getUserInput());
        res.put(VALUES, valueComponent.getUserInput());
        return res;
    }
}
