package edu.cmu.cs.cs214.hw5.plugins_example.display;

import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.DataType;
import edu.cmu.cs.cs214.hw5.core.DisplayFilterConfig;
import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.MultiPolygon;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * ChoroplethMap is a display plugin which displays GeoDataSet as a choropleth
 * map and renders a JPanel containing the map back to the framework.
 */
public class ChoroplethMap implements DisplayPlugin {
    
    /**
     * Name of the plugin.
     */
    private static final String NAME = "(Example) Choropleth Map";

    /**
     * Area Name configuration label name.
     */
    private static final String AREA_NAME = "Area Name (Required)";

    /**
     * Area configuration label name.
     */
    private static final String AREA = "Area (Required)";

    /**
     * Value configuration label name.
     */
    private static final String VALUE = "Value (Required)";

    /**
     * Time configuration label name.
     */
    private static final String TIME = "Time (Optional)";

    /**
     * Red Color.
     */
    private static final int R1 = 255, G1 = 0, B1 = 0;

    /**
     * Blue Color.
     */
    private static final int R2 = 0, G2 = 0, B2 = 255;

    /**
     * Fetch the name of display plugin which is loaded into the framework.
     *
     * @return name of display plugin.
     */
    @Override
    public String getName() {
        return NAME;
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

        configs.add(new UserInputConfig(AREA_NAME, UserInputType.SINGLE_SELECTION, columnPreview.get(DataType.STRING)));
        configs.add(new UserInputConfig(AREA, UserInputType.SINGLE_SELECTION, columnPreview.get(DataType.POLYGONS)));
        List<String> numericLabels = columnPreview.get(DataType.DOUBLE);
        numericLabels.addAll(columnPreview.get(DataType.INTEGER));
        configs.add(new UserInputConfig(VALUE, UserInputType.SINGLE_SELECTION, numericLabels));
        configs.add(new UserInputConfig(TIME, UserInputType.SINGLE_SELECTION, columnPreview.get(DataType.INTEGER)));

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
        if (!pluginParams.get(TIME).isEmpty() && pluginParams.get(TIME).get(0) != null) {
            configs.add(new DisplayFilterConfig(pluginParams.get(TIME).get(0), UserInputType.SINGLE_SELECTION));
        }
        configs.add(new DisplayFilterConfig(pluginParams.get(AREA_NAME).get(0), UserInputType.MULTI_SELECTION));
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
        if (dataSet.rowCount() == 0)
            return null;
        checkParams(pluginParams);

        int graphWidth = width - 40;
        int graphHeight = height - 10;

        String areaLabel = pluginParams.get(AREA).get(0);
        List<MultiPolygon> areaColumns = dataSet.getColumn(areaLabel).stream().map(o -> (MultiPolygon) o).collect(Collectors.toList());

        String valueLabel = pluginParams.get(VALUE).get(0);

        List<Double> valueColumn = dataSet.getColumn(valueLabel).stream()
                .map(o -> ((Number) o).doubleValue()).collect(Collectors.toList());

        double maxV = valueColumn.stream().max(Double::compareTo).get();
        double minV = valueColumn.stream().min(Double::compareTo).get();

        int maxX = areaColumns.stream().map(MultiPolygon::getMaxX).max(Integer::compareTo).get();
        int minX = areaColumns.stream().map(MultiPolygon::getMinX).min(Integer::compareTo).get();
        int maxY = areaColumns.stream().map(MultiPolygon::getMaxY).max(Integer::compareTo).get();
        int minY = areaColumns.stream().map(MultiPolygon::getMinY).min(Integer::compareTo).get();

        BufferedImage img = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, width, height);
        for (int i = 0; i < valueColumn.size(); i++) {
            double val = (valueColumn.get(i) - minV + 1) / (maxV - minV + 1);
            Color color = new Color(
                    (int) (val * R1 + (1 - val) * R2),
                    (int) (val * G1 + (1 - val) * G2),
                    (int) (val * B1 + (1 - val) * B2));
            for (Polygon polygon : areaColumns.get(i).getPolygons()) {
                Polygon newPolygon = new Polygon(
                        Arrays.stream(polygon.xpoints).map(x -> (x - minX + 1) * graphWidth / (maxX - minX + 1) + 5).toArray(),
                        Arrays.stream(polygon.ypoints).map(y -> (y - minY + 1) * graphHeight / (maxY - minY + 1) + 5).toArray(),
                        polygon.npoints
                );
                g.setColor(Color.BLACK);
                g.drawPolygon(newPolygon);
                g.setColor(color);
                g.fill(newPolygon);
            }
        }

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = 20;
        int h = height - 30;
        GradientPaint gp = new GradientPaint(0, 0, new Color(R1, G1, B1), 0, h, new Color(R2, G2, B2));
        g.setPaint(gp);
        g.fillRect(width - 25, 15, w, h);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        label.setIcon(new ImageIcon(img));

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(label, BorderLayout.CENTER);
        String title = String.format("%s (max = %s, min = %s)", valueLabel, maxV, minV);
        panel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        return panel;
    }
    
    /**
     * Check if there is required field that user does not specify.
     * 
     * @param pluginParams a parameter mapping from configuration name to concrete parameters user specifies.
     */
    private void checkParams(Map<String, List<String>> pluginParams) {
        if (pluginParams.get(AREA).isEmpty() || pluginParams.get(AREA).get(0) == null)
            throw new IllegalArgumentException("Select Area");
        if (pluginParams.get(VALUE).isEmpty() || pluginParams.get(VALUE).get(0) == null)
            throw new IllegalArgumentException("Select Value");
        if (pluginParams.get(AREA_NAME).isEmpty() || pluginParams.get(VALUE).get(0) == null)
            throw new IllegalArgumentException("Select Area Name");
    }

}

