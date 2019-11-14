package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.FrameworkListener;
import edu.cmu.cs.cs214.hw5.core.GeoDataFramework;
import static edu.cmu.cs.cs214.hw5.gui.DataPluginInputFrame.Receiver.DATA_PLUGIN;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The framework GUI implementation. This class is responsible for displaying
 * the framework GUI to the screen, and for forwarding events to
 * {@link GeoDataFramework} when GUI-related events are detected (such as button
 * clicks, menu-item clicks, etc.).
 */
public class GeoDataFrameworkGui extends JFrame implements FrameworkListener {

    /**
     * Width of the GUI window.
     */
    private static final int WINDOW_WIDTH = 800;

    /**
     * Height of the GUI window.
     */
    private static final int WINDOW_HEIGHT = 600;

    /**
     * Default JFrame title.
     */
    private static final String DEFAULT_TITLE = "GeoData Visualization Framework";

    /**
     * FILE_MENU title.
     */
    private static final String FILE_MENU_TITLE = "File";

    /**
     * IMPORT_DATA option name.
     */
    private static final String MENU_IMPORT_DATA = "Import...";

    /**
     * MENU_EXIT option name.
     */
    private static final String MENU_EXIT = "Exit";

    /**
     * Outer Tabbed Pane(DataSet or Visualization).
     */
    private final JTabbedPane outerTabbedPane;

    /**
     * Data panel containing imported dataSet.
     */
    private final JPanel dataPanel;

    /**
     * IMPORT_DATA menu.
     */
    private final JMenu importDataMenu;

    /**
     * Framework core.
     */
    private final GeoDataFramework core;

    /**
     * Map from dataSet name to JComponent showing dataSet on panel.
     */
    private final Map<String, JComponent> dataSetMap;

    /**
     * Map from graph name to GraphDisplayingFrame.
     */
    private final Map<String, GraphDisplayingFrame> graphMap;

    /**
     * Initialize the GeoData Framework GUI.
     * 
     * @param core core implementation of GeoData Framework.
     */
    public GeoDataFrameworkGui(GeoDataFramework core) {
        super(DEFAULT_TITLE);

        // Set the framework core instance that the GUI will talk to in response
        // to GUI-related events.
        this.core = core;
        this.graphMap = new HashMap<>();
        this.dataSetMap = new HashMap<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // Set-up the tab pane
        outerTabbedPane = new JTabbedPane();
        add(outerTabbedPane);
        outerTabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 50, 10, 50));

        // Add the frame's panels to the view.
        dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        outerTabbedPane.addTab("DataSet", dataPanel);

        // Set-up the menu bar.
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Add a 'File' menu tile.
        JMenu fileMenu = new JMenu(FILE_MENU_TITLE);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        // Add a 'Import Data' menu item.
        importDataMenu = new JMenu(MENU_IMPORT_DATA);
        importDataMenu.setMnemonic(KeyEvent.VK_N);
        fileMenu.add(importDataMenu);

        // Add a separator between 'Import Data' and 'Exit' menu items.
        fileMenu.addSeparator();

        // Add an 'Exit' menu item.
        JMenuItem exitMenuItem = new JMenuItem(MENU_EXIT);
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener(event -> System.exit(0));
        fileMenu.add(exitMenuItem);
        
        addDataPluginsToMenu();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        core.subscribe(this);
    }

    /**
     * Add all data plugin's name into the menu.
     */
    private void addDataPluginsToMenu() {
        List<String> dataPlugins = core.listAllDataPlugins();
        for (String name : dataPlugins) {
            JMenuItem dataPluginItem = new JMenuItem(name);
            dataPluginItem.addActionListener(
                    e -> new DataPluginInputFrame(
                            this, DATA_PLUGIN, name)
                            .setVisible(true)
            );
            importDataMenu.add(dataPluginItem);
        }
    }

    /**
     * Return the framework core reference.
     * 
     * @return framework core reference.
     */
    GeoDataFramework getCore() {
        return this.core;
    }

    /**
     * Load data from data plugin given its name.
     * 
     * @param pluginName name of plugin from which to load data.
     * @param dataSetName name of dataSet loaded by the plugin.
     * @param userInput a parameter mapping from configuration name to concrete parameters user specifies.
     */
    void loadData(String pluginName, String dataSetName, Map<String, List<String>> userInput) {
        core.loadData(pluginName, dataSetName, userInput);
    }

    /**
     * Handle events when dataSet is loaded into the framework. 
     * 
     * @param dataSetName Name of dataSet loaded into the framework.
     */
    @Override
    public void dataSetLoaded(String dataSetName) {
        JComponent component = new DataSetComponent(this, dataSetName);
        dataPanel.add(component);
        dataSetMap.put(dataSetName, component);
    }

    /**
     * Handle events when a dataSet is deleted in the framework.
     *
     * @param dataSetName name of dataSet.
     */
    @Override
    public void dataSetDeleted(String dataSetName) {
        if (dataSetMap.containsKey(dataSetName))
            dataPanel.remove(dataSetMap.remove(dataSetName));
        dataPanel.revalidate();
        dataPanel.repaint();
    }
}
