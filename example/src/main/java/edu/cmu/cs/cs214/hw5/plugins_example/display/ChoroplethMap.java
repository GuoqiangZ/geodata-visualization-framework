package edu.cmu.cs.cs214.hw5.plugins_example.display;

import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.DataType;
import edu.cmu.cs.cs214.hw5.core.DisplayFilterConfig;
import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.MultiPolygon;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputType;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Light Color.
     */
    private static final int R2 = 27, G2 = 131, B2 = 255;

    /**
     * Light Color.
     */
    private static final int R1 = 1, G1 = 2, B1 = 118;

    private static final int TITLE_HEIGHT = 50;
    private static final int GRAPH_HORIZONTAL_BORDER = 10;
    private static final int GRAPH_VERTICAL_BORDER = 5;
    private static final int BAR_WIDTH = 30;

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

        String areaLabel = pluginParams.get(AREA).get(0);
        List<MultiPolygon> areaColumns = dataSet.getColumn(areaLabel).stream().map(o -> (MultiPolygon) o).collect(Collectors.toList());

        String valueLabel = pluginParams.get(VALUE).get(0);

        List<Double> valueColumn = dataSet.getColumn(valueLabel).stream()
                .map(o -> ((Number) o).doubleValue()).collect(Collectors.toList());

        double maxV = valueColumn.stream().max(Double::compareTo).get();
        double minV = valueColumn.stream().min(Double::compareTo).get();

        double maxX = areaColumns.stream().map(MultiPolygon::getMaxX).max(Double::compareTo).get();
        double minX = areaColumns.stream().map(MultiPolygon::getMinX).min(Double::compareTo).get();
        double maxY = areaColumns.stream().map(MultiPolygon::getMaxY).max(Double::compareTo).get();
        double minY = areaColumns.stream().map(MultiPolygon::getMinY).min(Double::compareTo).get();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(width, height));
        panel.setBackground(new Color(210, 210, 210));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel title = new JLabel(valueLabel, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JLabel(String.format("(Max: %s, Min: %s", maxV, minV), SwingConstants.CENTER), BorderLayout.CENTER);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBorder(BorderFactory.createEmptyBorder(0, GRAPH_HORIZONTAL_BORDER, GRAPH_VERTICAL_BORDER, GRAPH_HORIZONTAL_BORDER));
        graphPanel.setOpaque(false);
        panel.add(graphPanel, BorderLayout.SOUTH);

        int graphWidth = width - 2 * GRAPH_HORIZONTAL_BORDER - BAR_WIDTH;
        int graphHeight = height - GRAPH_VERTICAL_BORDER - TITLE_HEIGHT;
        BufferedImage img = new BufferedImage(graphWidth, graphHeight, TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < valueColumn.size(); i++) {
            double val = (valueColumn.get(i) - minV + 1) / (maxV - minV + 1);
            Color color = new Color(
                    (int) (val * R1 + (1 - val) * R2),
                    (int) (val * G1 + (1 - val) * G2),
                    (int) (val * B1 + (1 - val) * B2));
            for (List<Point2D> points : areaColumns.get(i).getPoints()) {
                Polygon newPolygon = new Polygon(
                        points.stream().mapToInt(p -> (int) ((p.getX() - minX + 1) * graphWidth / (maxX - minX + 1))).toArray(),
                        points.stream().mapToInt(p -> graphHeight - (int) ((p.getY() - minY + 1) * graphHeight / (maxY - minY + 1))).toArray(),
                        points.size()
                );
                g.setColor(Color.WHITE);
                g.drawPolygon(newPolygon);
                g.setColor(color);
                g.fill(newPolygon);
            }
        }
        JLabel graphLabel = new JLabel(new ImageIcon(img));
        graphLabel.setOpaque(true);
        graphLabel.setBackground(Color.WHITE);
        graphPanel.add(graphLabel, BorderLayout.CENTER);


        int barHeight = height - GRAPH_VERTICAL_BORDER - TITLE_HEIGHT;
        img = new BufferedImage(BAR_WIDTH, barHeight, TYPE_INT_RGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, new Color(R1, G1, B1), 0, barHeight, new Color(R2, G2, B2));
        g.setPaint(gp);
        g.fillRect(0, 0, BAR_WIDTH, barHeight);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, BAR_WIDTH, barHeight);

        JLabel barLabel = new JLabel(new ImageIcon(img.getScaledInstance(BAR_WIDTH - 10, barHeight - 10, SCALE_SMOOTH)));
        barLabel.setPreferredSize(new Dimension(30, barHeight));
        barLabel.setOpaque(true);
        barLabel.setBackground(Color.WHITE);
        graphPanel.add(barLabel, BorderLayout.EAST);

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

