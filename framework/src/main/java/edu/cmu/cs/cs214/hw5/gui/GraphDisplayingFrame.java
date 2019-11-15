package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.DisplayFilterConfig;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * GraphDisplayingFrame displays the drawn graph in the panel.
 */
class GraphDisplayingFrame extends JFrame {

    private static final int DISPLAYING_FRAME_WIDTH = 720;
    private static final int DISPLAYING_FRAME_HEIGHT = 540;

    /**
     * JPanel containing the plotted graph.
     */
    private final JPanel graphContainer;

    private final GeoDataFrameworkGui parent;

    private final String dataSetName;

    private final String pluginName;

    private final Map<String, List<String>> pluginParams;

    private final List<DisplayFilterConfig> filterConfigs;

    private final UserInputPanel filterPanel;

    private final JScrollPane graphPane;

    /**
     * Create and initialize a graph displaying panel.
     *
     * @param parent geoData Framework Gui.
     * @param graphName name of graph.
     * @param pluginName name of plugin.
     * @param dataSetName name of dataSet.
     * @param pluginParams plugin configs.
     * @param filterConfigs a list of plugin-specific DisplayFilterConfig.
     */
    GraphDisplayingFrame(GeoDataFrameworkGui parent, String graphName,
                         String pluginName, String dataSetName, Map<String, List<String>> pluginParams,
                         List<DisplayFilterConfig> filterConfigs) {
        super(graphName);
        this.parent = parent;
        this.dataSetName = dataSetName;
        this.pluginName = pluginName;
        this.filterConfigs = filterConfigs;
        this.pluginParams = pluginParams;

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(DISPLAYING_FRAME_WIDTH, DISPLAYING_FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        filterPanel = new UserInputPanel(parent.getCore().convertTransformConfigs(dataSetName, filterConfigs));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        add(filterPanel, BorderLayout.NORTH);
        filterPanel.addActionListener(e -> refreshGraph());

        graphContainer = new JPanel();
        graphPane = new JScrollPane(graphContainer);
        add(graphPane, BorderLayout.CENTER);
        refreshGraph();
        pack();
    }

    private void refreshGraph() {
        List<List<String>> userInputString = filterPanel.getUserInputList();
        List<Pair<DisplayFilterConfig, List<String>>> filterInput = IntStream.range(0, userInputString.size())
                .mapToObj(i -> Pair.of(filterConfigs.get(i), userInputString.get(i)))
                .collect(Collectors.toList());
        setDisplayedGraph(
                parent.getCore().drawGraph(pluginName, dataSetName, pluginParams, filterInput)
        );
    }

    /**
     * Add a panel into graph container and repaint the graph container panel.
     * @param panel panel containing plotted graph.
     */
    void setDisplayedGraph(JPanel panel) {
        graphContainer.removeAll();
        if (panel != null)
            graphContainer.add(panel);
        graphPane.revalidate();
        graphPane.repaint();
    }
}
