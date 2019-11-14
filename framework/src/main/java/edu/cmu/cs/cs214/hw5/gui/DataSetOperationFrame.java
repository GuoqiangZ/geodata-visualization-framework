package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.DataType;
import edu.cmu.cs.cs214.hw5.core.DisplayFilterConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * DataSetOperationFrame consists of DataSetInfo Panel (showing the dataSet column name,
 * type), SelectDisplayPlugin Panel(user can select display plugin for plots), DisplayPlugin
 * Panel(user can specify usage-specific configuration parameters), GeoCoding Panel(use can
 * specify configuration parameter of geoCoding transformation), Filter Panel (user can specify
 * configuration parameter of filtering transformation) and Sort Panel (user can specify 
 * configuration parameter of sorting transformation).
 */
class DataSetOperationFrame extends JFrame {

    /**
     * GeoData Framework Gui.
     */
    private final GeoDataFrameworkGui parent;

    /**
     * Name of dataSet.
     */
    private final String dataSetName;

    /**
     * Width of operation frame window.
     */
    private static final int OPERATION_FRAME_WIDTH = 720;
    
    /**
     * Height of operation frame window.
     */
    private static final int OPERATION_FRAME_HEIGHT = 540;

    /**
     * Width of DataSetInfo panel.
     */
    private static final int DATASET_INFO_PANEL_WIDTH = 640;

    /**
     * Height of DataSetInfo panel.
     */
    private static final int DATASET_INFO_PANEL_HEIGHT = 100;

    /**
     * GEOCODE title.
     */
    private static final String GEOCODE = "GeoCode";
    
    /**
     * GEOCODE tip.
     */
    private static final String GEOCODE_TIP = "  Select one or more columns in the dataset that stores address data. " +
            "A new dataset will be created that contains the contours and coordinates of your addresses.";

    /**
     * GEOCODE rigid form title
     */
    private static final String GEOCODE_RIGID_FORM = "Rigid Form";

    /**
     * GEOCODE free form title
     */
    private static final String GEOCODE_FREE_FORM = "Free Form";
    
    /**
     * FILTER title.
     */
    private static final String FILTER = "Filter";

    /**
     * FILTER tip.
     */
    private static final String FILTER_TIP = "  Select column and specify constraints to sort.";

    /**
     * FILTER numeric form title.
     */
    private static final String FILTER_NUMERIC_FORM = "Numeric Form";

    /**
     * FILTER string form title.
     */
    private static final String FILTER_STRING_FORM = "String Form";
    
    /**
     * SORT title.
     */
    private static final String SORT = "Sort";

    /**
     * SORT tip.
     */
    private static final String SORT_TIP = "  Select a column by which you want to sort.";

    /**
     * GRAPH NAME label name.
     */
    private static final String GRAPH_NAME = "Graph Name";

    /**
     * Create and initialize a dataSet Frame.
     * 
     * @param parent geoData Framework Gui.
     * @param dataSetName name of dataSet selected.
     */
    DataSetOperationFrame(GeoDataFrameworkGui parent, String dataSetName) {
        super("DataSet: " + dataSetName);
        this.parent = parent;
        this.dataSetName = dataSetName;

        setLayout(new BorderLayout(0, 0));
        setMinimumSize(new Dimension(OPERATION_FRAME_WIDTH, OPERATION_FRAME_HEIGHT));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Set up upper part of frame
        JPanel upperPanel = createDataSetInfoPanel();
        add(upperPanel, BorderLayout.NORTH);

        // Set up middle part of frame
        JTabbedPane drawOrTransfromTabbedPane = new JTabbedPane();
        add(drawOrTransfromTabbedPane, BorderLayout.CENTER);

        // Set up a panel associated with the 'Display' tab.
        drawOrTransfromTabbedPane.addTab("Display", createSelectDisplayPluginPanel());

        // Set up a tabbed pane associated with the 'Transform' tab.
        JTabbedPane paneTransform = new JTabbedPane();
        drawOrTransfromTabbedPane.addTab("Transform", paneTransform);

        JPanel geoCodePanel = createGeoCodePanel();
        paneTransform.addTab(GEOCODE, geoCodePanel);
        JPanel filterPanel = createFilterPanel();
        paneTransform.addTab(FILTER, filterPanel);
        JPanel sortPanel = createSortPanel();
        paneTransform.addTab(SORT, sortPanel);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Create and return a DataSetInfo panel.
     * 
     * @return DataSetInfo panel.
     */
    private JPanel createDataSetInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(DATASET_INFO_PANEL_WIDTH, DATASET_INFO_PANEL_HEIGHT));

        DataSet dataSet = parent.getCore().getDataSet(dataSetName);

        String[] labels = dataSet.getLabels().toArray(String[]::new);
        String[] types = dataSet.getDataTypes().stream().map(DataType::toString).toArray(String[]::new);

        String[] columnIndex = IntStream.range(0, labels.length).mapToObj(i -> "C" + i).toArray(String[]::new);
        JTable table = new JTable(new String[][] {labels, types}, columnIndex) {
            @Override
            public String getToolTipText( MouseEvent e ) {
                int row = rowAtPoint(e.getPoint());
                int column = columnAtPoint(e.getPoint());

                Object value = getValueAt(row, column);
                if (value == null)
                    return null;
                if (row == 0)
                    return "Label: " + value.toString();
                else
                    return "DataType: " + value.toString();
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JScrollPane pane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(pane, BorderLayout.CENTER);
        
        JLabel countLabel = new JLabel(String.format("Rows: %d, Cols: %d", dataSet.rowCount(), dataSet.colCount()));
        panel.add(countLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create and return a SelectDisplayPlugin panel. User can select display plugin
     * to visualize dataSet in this panel.
     * 
     * @return a SelectDisplayPlugin panel.
     */
    private JPanel createSelectDisplayPluginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        UserInputComponent.SingleSelectionInput displayPluginInput = new UserInputComponent.SingleSelectionInput(
                "Display Plugin", parent.getCore().listAllDisplayPlugins());
        panel.add(displayPluginInput, BorderLayout.NORTH);
        displayPluginInput.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 60));

        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 60));
        panel.add(configPanel, BorderLayout.CENTER);

        displayPluginInput.addActionListener(l -> {
            List<String> selectedPlugin = displayPluginInput.getUserInput();
            if (!selectedPlugin.isEmpty() && selectedPlugin.get(0) != null) {
                createDisplayPluginPanel(selectedPlugin.get(0), configPanel);
            } else {
                configPanel.removeAll();
                configPanel.revalidate();
                configPanel.repaint();
            }
        });

        return panel;
    }

    /**
     * Create a DisplayPlugin control panel.
     * 
     * @param pluginName name of display control plugin.
     * @param container container containing the DisplayPlugin panel.
     */
    private void createDisplayPluginPanel(String pluginName, JPanel container) {
        container.removeAll();

        List<UserInputConfig> configs = parent.getCore().getDisplayPluginConfigs(pluginName, dataSetName);
        UserInputPanel userInputPanel = new UserInputPanel(configs);
        container.add(userInputPanel, BorderLayout.NORTH);

        JPanel centralPanel = new JPanel(new BorderLayout());
        centralPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        container.add(centralPanel, BorderLayout.CENTER);

        UserInputComponent graphNameInput = new UserInputComponent.TextFieldInput(GRAPH_NAME);
        centralPanel.add(graphNameInput, BorderLayout.SOUTH);

        container.revalidate();
        container.repaint();

        container.add(createCancelOkButtonPanel(e -> {
            try {
                Map<String, List<String>> params = userInputPanel.getUserInput();
                String graphName = graphNameInput.getUserInput().get(0);
                List<DisplayFilterConfig> filterConfigs = parent.getCore().createGraph(pluginName, dataSetName, params);
                SwingUtilities.invokeLater(() ->
                        new GraphDisplayingFrame(parent, graphName, pluginName, dataSetName, userInputPanel.getUserInput(), filterConfigs)
                                .setVisible(true));
                this.setVisible(false);
                this.dispose();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }), BorderLayout.SOUTH);
    }

    /**
     * Create a geoCoding control panel.
     * 
     * @return geoCoding control panel.
     */
    private JPanel createGeoCodePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea(GEOCODE_TIP);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        panel.add(textArea, BorderLayout.NORTH);

        JTabbedPane rigidOrFreeFormTabPane =  new JTabbedPane(SwingConstants.LEFT);

        panel.add(rigidOrFreeFormTabPane, BorderLayout.CENTER);

        // Set up a panel associated with with 'Rigid Form' tab which requires address specified by
        // country, state, city, ... .
        UserInputPanel mapRigidFormPanel = new UserInputPanel(
                parent.getCore().getGeoCodeTransformConfigs(dataSetName, false));
        JPanel rigidFormPanelContainer = new JPanel(new BorderLayout());
        rigidFormPanelContainer.add(mapRigidFormPanel, BorderLayout.NORTH);
        rigidFormPanelContainer.add(Box.createGlue(), BorderLayout.CENTER);
        rigidOrFreeFormTabPane.addTab(GEOCODE_RIGID_FORM, rigidFormPanelContainer);

        // Set up a panel associated with with 'Free Form' tab which requires the whole adress
        UserInputPanel mapFreeFormPanel = new UserInputPanel(
                parent.getCore().getGeoCodeTransformConfigs(dataSetName, true));
        JPanel freeFormPanelContainer = new JPanel(new BorderLayout());
        freeFormPanelContainer.add(mapFreeFormPanel, BorderLayout.NORTH);
        freeFormPanelContainer.add(Box.createGlue(), BorderLayout.CENTER);
        rigidOrFreeFormTabPane.addTab(GEOCODE_FREE_FORM, freeFormPanelContainer);

        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
        panel.add(lowerPanel, BorderLayout.SOUTH);

        UserInputComponent resultLabel = new UserInputComponent.TextFieldInput("New Label");
        UserInputComponent newDataSetName = new UserInputComponent.TextFieldInput("New DataSet Name");

        lowerPanel.add(resultLabel);
        lowerPanel.add(newDataSetName);

        lowerPanel.add(createCancelOkButtonPanel(l -> {
            boolean isFreeForm = rigidOrFreeFormTabPane.getSelectedIndex() == 0 ? false : true;
            Map<String, List<String>> params = isFreeForm ? mapFreeFormPanel.getUserInput() : mapRigidFormPanel.getUserInput();

            SwingWorker worker = new SwingWorker<Integer, Integer>() {
                @Override
                protected Integer doInBackground() {
                    try {
                        DataSetOperationFrame.this.setEnabled(false);
                        List<String> unfounded = parent.getCore().geoCodeTransform(dataSetName,
                                newDataSetName.getUserInput().get(0), resultLabel.getUserInput().get(0),params,
                                isFreeForm);
                        if (unfounded.size() > 0) {
                            String s;
                            if (unfounded.size() > 3) {
                                s = String.join("\n", unfounded.subList(0, 3));
                                s += "\n...";
                            } else {
                                s = String.join("%n", unfounded);
                            }

                            String msg = String.format("Some Address(es) Are Not Found:%n" +
                                    "%s%n%nFilter And Save?", s);
                            int res = JOptionPane.showConfirmDialog(DataSetOperationFrame.this, msg, dataSetName, YES_NO_OPTION);
                            if (res == NO_OPTION) {
                                parent.getCore().deleteDataSet(newDataSetName.getUserInput().get(0));
                                DataSetOperationFrame.this.setEnabled(true);
                                return 0;
                            }
                        }
                        DataSetOperationFrame.this.setVisible(false);
                        DataSetOperationFrame.this.dispose();
                    } catch (IllegalArgumentException | IllegalStateException ex) {
                        DataSetOperationFrame.this.setEnabled(true);
                        JOptionPane.showMessageDialog(DataSetOperationFrame.this, ex.getMessage());
                    } catch (Exception ex) {
                        DataSetOperationFrame.this.setEnabled(true);
                        ex.printStackTrace();
                    }
                    return 0;
                }
            };
            worker.execute();

        }));

        return panel;
    }

    /**
     * Create a filtering control panel.
     * 
     * @return filtering control panel.
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea(FILTER_TIP);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        panel.add(textArea, BorderLayout.NORTH);

        JTabbedPane numericOrStringFormTabPane = new JTabbedPane(SwingConstants.LEFT);
        panel.add(numericOrStringFormTabPane, BorderLayout.CENTER);
        
        // create numeric form panel.
        UserInputPanel numericPanel = new UserInputPanel(
                parent.getCore().getFilterConfigs(dataSetName, true));
        JPanel numericFormPanelContainer = new JPanel(new BorderLayout());
        numericFormPanelContainer.add(numericPanel, BorderLayout.NORTH);

        numericFormPanelContainer.add(Box.createHorizontalBox(), BorderLayout.CENTER);

        JLabel tipsLabel = new JLabel();
        numericFormPanelContainer.add(tipsLabel, BorderLayout.SOUTH);

        numericPanel.addActionListener(e -> {
            List<String> cur = numericPanel.getUserInputList().get(0);
            if (cur.isEmpty() || cur.get(0) == null) {
                tipsLabel.setText("");
                return;
            }
            String selected = cur.get(0);
            List<Object> values = parent.getCore().getDataSet(dataSetName).getColumn(selected);
            if (values == null || values.isEmpty())
                return;
            double max = values.stream().mapToDouble(i -> ((Number) i).doubleValue()).max().getAsDouble();
            double min = values.stream().mapToDouble(i -> ((Number) i).doubleValue()).min().getAsDouble();
            tipsLabel.setText(String.format("Min: %s, Max: %s", min, max));
        });

        numericOrStringFormTabPane.addTab(FILTER_NUMERIC_FORM, numericFormPanelContainer);

        // create string form panel.
        StringFormPanel stringFormPanel = new StringFormPanel(
                parent.getCore().getFilterConfigs(dataSetName, false), dataSetName, parent);
        JPanel stringFormPanelContainer = new JPanel(new BorderLayout());
        stringFormPanelContainer.add(stringFormPanel, BorderLayout.NORTH);
        stringFormPanelContainer.add(Box.createGlue(), BorderLayout.CENTER);
        numericOrStringFormTabPane.addTab(FILTER_STRING_FORM, stringFormPanelContainer);
        
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
        panel.add(lowerPanel, BorderLayout.SOUTH);
        UserInputComponent newDataSetName = new UserInputComponent.TextFieldInput("New DataSet Name");
        
        lowerPanel.add(newDataSetName);
        
        lowerPanel.add(createCancelOkButtonPanel(l -> {
            boolean isNumericForm = numericOrStringFormTabPane.getSelectedIndex() == 0 ? true : false;
            Map<String, List<String>> params = isNumericForm ? numericPanel.getUserInput() : stringFormPanel.getUserInput();
            try {
                if (isNumericForm) {
                    parent.getCore().numericFilter(dataSetName, newDataSetName.getUserInput().get(0), params);
                } else {
                    parent.getCore().stringFilter(dataSetName, newDataSetName.getUserInput().get(0), params);
                }
                this.setVisible(false);
                this.dispose();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }));
        
        return panel;
    }

    /**
     * Create a sort control panel.
     * 
     * @return sort control panel.
     */
    private JPanel createSortPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea(SORT_TIP);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        panel.add(textArea, BorderLayout.NORTH);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        panel.add(controlPanel, BorderLayout.CENTER);
        
        UserInputPanel sortPanel = new UserInputPanel(
                parent.getCore().getSortConfigs(dataSetName));
        JPanel sortFormPanelContainer = new JPanel(new BorderLayout());
        sortFormPanelContainer.add(sortPanel, BorderLayout.NORTH);
        sortFormPanelContainer.add(Box.createGlue(), BorderLayout.CENTER);
        controlPanel.add(sortPanel, BorderLayout.NORTH);

        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
        controlPanel.add(lowerPanel, BorderLayout.SOUTH);
        
        UserInputComponent newDataSetName = new UserInputComponent.TextFieldInput("New DataSet Name");
        lowerPanel.add(newDataSetName);

        lowerPanel.add(createCancelOkButtonPanel(l -> {
            Map<String, List<String>> params = sortPanel.getUserInput();
            try {
                parent.getCore().sort(dataSetName, newDataSetName.getUserInput().get(0), params);
                this.setVisible(false);
                this.dispose();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }));
        
        return panel;
    }

    /**
     * Create a panel containing cancel and ok button.
     * 
     * @param okButtonListener action listener of ok button.
     * @return a panel containing cancel and ok button.
     */
    private JPanel createCancelOkButtonPanel(ActionListener okButtonListener) {
        JPanel panel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(l -> {
            this.setVisible(false);
            this.dispose();
        });
        panel.add(cancelButton);

        JButton okButton = new JButton("OK");
        panel.add(okButton);
        okButton.addActionListener(okButtonListener);
        return panel;
    }
}
