package edu.cmu.cs.cs214.hw5.core;

import javax.swing.JPanel;
import java.util.List;
import java.util.Map;

/**
 * Interface implemented by concrete display plugin to display any DataSet imported
 * into framework.
 */
public interface DisplayPlugin {
    
    /**
     * Fetch the name of display plugin which is loaded into the framework.
     * 
     * @return name of display plugin.
     */
    String getName();

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
    List<UserInputConfig> getPluginConfigs(Map<DataType, List<String>> columnPreview);

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
    List<DisplayFilterConfig> getDisplayFilterConfig(Map<String, List<String>> pluginParams);

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
    JPanel draw(DataSet dataSet, int width, int height, Map<String, List<String>> pluginParams);
}
