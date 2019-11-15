package edu.cmu.cs.cs214.hw5.plugins_example.display;

import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.DataType;
import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.DisplayFilterConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputType;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import javax.swing.JPanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * LineChartPlugin is a display plugin which displays GeoDataSet as a line chart
 * renders a JPanel containing chart back to the framework.
 */
public class LineChartPlugin implements DisplayPlugin {
    
    /**
     * Name of the plugin.
     */
    private static final String PLUGIN_NAME = "Line Chart";

    /**
     * X-axis attribute configuration label name.
     */
    private static final String X_AXIS = "X-Axis (Required)";

    /**
     * Y-axis attribute configuration label name.
     */
    private static final String Y_AXIS = "Y-Axis (Required)";

    /**
     * Category configuration label name.
     */
    private static final String CATEGORY = "Category (Required)";

    /**
     * Fetch the name of display plugin which is loaded into the framework.
     *
     * @return name of display plugin.
     */
    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    /**
     * Plugin should provide a list of UserInputConfig to customize the plugin-specific usage
     * configuration or information (e.g. displayed column name, graph name..) that user needs 
     * to specify. The plugin needs to specify configuration name, input type (TEXT_FIELD,
     * MULTI_SELECTION or SINGLE_SELECTION) and a list of selections if any. The
     * user input for these configurations can be received by plugin when drawing graph later.
     *
     * @param columnPreview column preview mapping from column type to a list of column names of this type.
     * @return a list of plugin-specific UserInputConfig.
     */
    @Override
    public List<UserInputConfig> getPluginConfigs(Map<DataType, List<String>> columnPreview) {
        List<UserInputConfig> configs = new ArrayList<>();

        List<String> intLabels = columnPreview.get(DataType.INTEGER);
        List<String> doubleLabels = columnPreview.get(DataType.DOUBLE);
        List<String> stringLabels = columnPreview.get(DataType.STRING);
        List<String> intDoubleLabels = new ArrayList<>(intLabels);
        intDoubleLabels.addAll(doubleLabels);

        configs.add(new UserInputConfig(X_AXIS, UserInputType.SINGLE_SELECTION, intDoubleLabels));
        configs.add(new UserInputConfig(Y_AXIS, UserInputType.MULTI_SELECTION, intDoubleLabels));
        configs.add(new UserInputConfig(CATEGORY, UserInputType.SINGLE_SELECTION, stringLabels));
        return configs;
    }

    /**
     * Plugin can provide a list of DisplayFilterConfig to let user control and filter the data which is being
     * displayed in real-time. The framework will call this method after the user has completed plugin-specific
     * usage configurations and before graph is going to be drawn. The plugin only needs to specify the labels of
     * controllable columns and how the user can control them (choosing a single value or multiple values),
     * so that in later times the framework will handle the user's actions and call the plugin to draw the
     * plot with the data that is already processed.
     *
     * For example, after user specifies column "time" described in previous plugin configs, the plugin can
     * provide the specification that user can select one distinct value in "time" column. Later, when the
     * user select anther "time" value, the framework filters out data with this time value and calls the plugin
     * to draw plot with the dataset.
     *
     * Compared with plugin configs, the former one is column-related, and stays constant during the life of a graph,
     * while the latter is row-related, and user can change these configs any time.
     *
     * @param pluginParams user input for the plugin configurations.
     * @return a list of DisplayFilterConfig specific for this with the dataset
     */
    @Override
    public List<DisplayFilterConfig> getDisplayFilterConfig(Map<String, List<String>> pluginParams) {
        checkParams(pluginParams);
        List<DisplayFilterConfig> configs = new ArrayList<>();
        if (!pluginParams.get(CATEGORY).isEmpty() && pluginParams.get(CATEGORY).get(0) != null) {
            configs.add(new DisplayFilterConfig(pluginParams.get(CATEGORY).get(0), UserInputType.MULTI_SELECTION));
        }
        return configs;
    }

    /**
     * Draw a plot and return a JPanel containing the plot which will be displayed by the framework. The client
     * should draw the plot by querying data value from the given DataSet and displaying selected data based on 
     * the parameter mapping specified by the users.
     *
     * @param dataSet geo-DataSet to be displayed.
     * @param width Width of the JPanel.
     * @param height Height of the JPanel.
     * @param pluginParams a parameter mapping from configuration name to concrete parameters user specifies.
     * @return a JPanel containing the plot.
     */
    @Override
    public JPanel draw(DataSet dataSet, int width, int height, Map<String, List<String>> pluginParams) {
        checkParams(pluginParams);

        String xLabel = pluginParams.get(X_AXIS).get(0);
        List<String> yLabels = pluginParams.get(Y_AXIS);
        final List<List<Double>> yList = new ArrayList<>();
        for (String yLabel : yLabels)
            yList.add(dataSet.getColumn(yLabel).stream().map(v -> ((Number) v).doubleValue()).collect(Collectors.toList()));

        final List<Double> xList;
        xList = dataSet.getColumn(xLabel).stream().map(v -> ((Number) v).doubleValue()).collect(Collectors.toList());

        XYChart chart = new XYChartBuilder().width(width).height(height).build();

        String cateLabel = pluginParams.get(CATEGORY).get(0);
        if (cateLabel != null) {
            List<Object> cateColumn = dataSet.getColumn(cateLabel);
            Map<String, List<Integer>> indices = IntStream.range(0, cateColumn.size()).mapToObj(Integer::valueOf)
                    .collect(Collectors.groupingBy(i -> (String) cateColumn.get(i), Collectors.toList()));
            for (int i = 0; i < yList.size(); i++) {
                for (Map.Entry<String, List<Integer>> entry : indices.entrySet()) {
                    String label = entry.getKey() + ", " + yLabels.get(i);
                    int ii = i;
                    List<Double> y = entry.getValue().stream().map(j -> yList.get(ii).get(j)).collect(Collectors.toList());
                    List<Double> x = entry.getValue().stream().map(j -> xList.get(j)).collect(Collectors.toList());
                    chart.addSeries(label, x, y);
                }
            }
        } else {
            for (int i = 0; i < yList.size(); i++) {
                chart.addSeries(yLabels.get(i), xList, yList.get(i));
            }
        }

        XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
        chart.setXAxisTitle(xLabel);
        chart.setYAxisTitle("Value(s)");
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setLegendBackgroundColor(new Color(255, 255, 255, 122));
        return chartPanel;
    }

    /**
     * Check if there is required field that user does not specify.
     * 
     * @param pluginParams a parameter mapping from configuration name to concrete parameters user specifies.
     */
    private void checkParams(Map<String, List<String>> pluginParams) {
        if (pluginParams.get(X_AXIS).isEmpty() || pluginParams.get(X_AXIS).get(0) == null)
            throw new IllegalArgumentException("Select X Attribute");
        if (pluginParams.get(Y_AXIS).isEmpty() || pluginParams.get(Y_AXIS).get(0) == null)
            throw new IllegalArgumentException("Select Y Attribute");
        if (pluginParams.get(CATEGORY).isEmpty() || pluginParams.get(CATEGORY).get(0) == null)
            throw new IllegalArgumentException("Select Category Column");
    }
}
