package edu.cmu.cs.cs214.hw5.core;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.swing.JPanel;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core implementation of GeoData Framework.
 */
public class GeoDataFramework {
    /**
     * An array of address label.
     */
    private static final String[] ADDRESS_LABEL = {"Country", "State", "City", "County", "Street"};

    /**
     * Width of plot display window.
     */
    private static final int DISPLAY_WINDOW_WIDTH = 600;

    /**
     * Height of plot display window.
     */
    private static final int DISPLAY_WINDOW_HEIGHT = 450;

    /**
     * Map from dataPlugin name to dataPlugin.
     */
    private final Map<String, DataPlugin> dataPluginMap;

    /**
     * Map from displayPlugin name to displayPlugin.
     */
    private final Map<String, DisplayPlugin> displayPluginMap;

    /**
     * Map from dataSet name to dataSet.
     */
    private final Map<String, DataSet> dataSetMap;

    /**
     * Set of framework listeners.
     */
    private final Set<FrameworkListener> listeners;

    /**
     * Open Street Map Client
     */
    private OpenStreetMapClient openStreetMapClient;

    /**
     * Initialize the framework by default.
     */
    public GeoDataFramework() {
        dataPluginMap = new LinkedHashMap<>();
        displayPluginMap = new LinkedHashMap<>();
        dataSetMap = new HashMap<>();
        listeners = new LinkedHashSet<>();
        openStreetMapClient = new OpenStreetMapClient(HttpClient.newBuilder().build(), true);
    }

    /**
     * Initialize the framework by providing a http client.
     * @param httpClient http client.
     * @param printStatus print status.
     */
    public GeoDataFramework(HttpClient httpClient, boolean printStatus) {
        dataPluginMap = new LinkedHashMap<>();
        displayPluginMap = new LinkedHashMap<>();
        dataSetMap = new HashMap<>();
        listeners = new LinkedHashSet<>();
        openStreetMapClient = new OpenStreetMapClient(httpClient, printStatus);
    }

    /**
     * Subscribe the framework listener to the framework.
     *
     * @param listener framework listener.
     * @return return true if the listener is subscribed.
     * @throws NullPointerException thrown when the listener passed in is a null pointer.
     */
    public boolean subscribe(FrameworkListener listener) {
        if (listener == null)
            throw new NullPointerException();
        listeners.add(listener);
        return true;
    }

    /**
     * Register data plugin into the framework.
     *
     * @param plugin data plugin.
     */
    public void registerDataPlugin(DataPlugin plugin) {
        dataPluginMap.put(plugin.getName(), plugin);
        System.out.println("Loaded data plugin: " + plugin.getName());
    }

    /**
     * Register display plugin into the framework.
     *
     * @param plugin display plugin.
     */
    public void registerDisplayPlugin(DisplayPlugin plugin) {
        displayPluginMap.put(plugin.getName(), plugin);
        System.out.println("Loaded display plugin: " + plugin.getName());
    }

    /**
     * Return a list of names of all data plugins.
     *
     * @return a list of names of all data plugins.
     */
    public List<String> listAllDataPlugins() {
        return new ArrayList<>(dataPluginMap.keySet());
    }

    /**
     * Return a list of names of all display plugins.
     *
     * @return a list of names of all display plugins.
     */
    public List<String> listAllDisplayPlugins() {
        return new ArrayList<>(displayPluginMap.keySet());
    }

    /**
     * Return a dataSet by name.
     *
     * @param dataSetName name of wanted dataSet.
     * @return dataSet queried.
     */
    public DataSet getDataSet(String dataSetName) {
        return dataSetMap.get(dataSetName);
    }

    /**
     * Return a list of UserInputConfig specific to the data plugin of given name.
     *
     * @param pluginName name of the data plugin.
     * @return a list of UserInputConfig specific to the data plugin of given name.
     */
    public List<UserInputConfig> getDataPluginConfigs(String pluginName) {
        return dataPluginMap.get(pluginName).getUserInputConfigs();
    }

    /**
     * Load data from data plugin.
     *
     * @param pluginName name of data plugin.
     * @param dataSetName name of dataSet.
     * @param params a parameter mapping from configuration name to concrete parameters user specifies.
     */
    public void loadData(String pluginName, String dataSetName, Map<String, List<String>> params) {
        checkNewDataSetName(dataSetName);
        dataSetMap.put(dataSetName, dataPluginMap.get(pluginName).loadData(params));
        listeners.forEach(l -> l.dataSetLoaded(dataSetName));
    }

    /**
     * Check if a new data set can be added.
     * @param dataSetName name of new data set name;
     */
    private void checkNewDataSetName(String dataSetName) {
        if (dataSetMap.containsKey(dataSetName))
            throw new IllegalArgumentException("Duplicate Name");
        else if (dataSetName.isBlank()) {
            throw new IllegalArgumentException("Empty Name");
        }
    }

    /**
     * Delete the dataSet of the given name.
     *
     * @param name name of the dataSet to be deleted.
     */
    public void deleteDataSet(String name) {
        if (dataSetMap.containsKey(name)) {
            dataSetMap.remove(name);
            listeners.forEach(l -> l.dataSetDeleted(name));
        }
    }

    /**
     * Return a list of UserInputConfig specific to the display plugin of given name.
     *
     * @param pluginName name of the data plugin.
     * @param dataSetName name of the dataSet.
     * @return a list od UserInputConfig specific to the display plugin of given name.
     */
    public List<UserInputConfig> getDisplayPluginConfigs(String pluginName, String dataSetName) {
        return displayPluginMap.get(pluginName).getPluginConfigs(dataSetMap.get(dataSetName).getColumnPreview());
    }

    /**
     * Create a graph in the framework.
     *
     * @param pluginName name of plugin.
     * @param dataSetName name of dataSet.
     * @param pluginParams a parameter mapping from configuration name to concrete parameters user specifies.
     * @return  a list of display filter configs.
     */
    public List<DisplayFilterConfig> createGraph(String pluginName, String dataSetName,
                                                 Map<String, List<String>> pluginParams) {
        DisplayPlugin plugin = displayPluginMap.get(pluginName);
        return plugin.getDisplayFilterConfig(pluginParams);
    }

    /**
     * Draw a graph in the framework.
     *
     * @param pluginName name of plugin.
     * @param dataSetName name of dataSet.
     * @param pluginParams a parameter mapping from configuration name to concrete parameters user specifies.
     * @param transformParams a list of pairs of DisplayFilterConfig and a list of values selected by user.
     * @return  a panel drawn by display plugin.
     */
    public JPanel drawGraph(String pluginName, String dataSetName,
                            Map<String, List<String>> pluginParams,
                            List<Pair<DisplayFilterConfig, List<String>>> transformParams) {
        DisplayPlugin plugin = displayPluginMap.get(pluginName);
        DataSet dataSet = dataSetMap.get(dataSetName);

        if (!transformParams.isEmpty()) {
            Transformation transform = new Transformation(dataSet);
            for (Pair<DisplayFilterConfig, List<String>> param : transformParams) {
                DisplayFilterConfig config = param.getLeft();
                List<String> values = param.getRight();
                transform.filter(config.getLabel(), values);
            }
            dataSet = transform.toDataSet();
        }
        return plugin.draw(dataSet, DISPLAY_WINDOW_WIDTH, DISPLAY_WINDOW_HEIGHT, pluginParams);
    }

    /**
     * Return a list of UserInputConfig specific to GeoCoding.
     *
     * @param dataSetName name of the dataSet to perform GeoCoding on.
     * @param isFreeForm whether the configuration will be displayed on free form or not.
     * @return a list of UserInputConfig specific to GeoCoding.
     */
    public List<UserInputConfig> getGeoCodeTransformConfigs(String dataSetName, boolean isFreeForm) {
        DataSet dataSet = dataSetMap.get(dataSetName);
        if (dataSet == null)
            throw new IllegalArgumentException("DataSet does not exist");
        List<UserInputConfig> configList = new ArrayList<>();

        List<String> stringColumnLabels = dataSet.labelsOfType(DataType.STRING);

        if (isFreeForm) {
            configList.add(new UserInputConfig("Address", UserInputType.SINGLE_SELECTION, stringColumnLabels));
        } else {
            for (String param : ADDRESS_LABEL)
                configList.add(new UserInputConfig(param, UserInputType.SINGLE_SELECTION, stringColumnLabels));
        }

        return configList;
    }

    /**
     * Transform dataSet by geoCoding.
     *
     * @param origDataSetName name of the original dataSet.
     * @param newDataSetName name of the new dataSet.
     * @param newLabel new name of the labels.
     * @param params a parameter mapping from configuration name to concrete parameters user specifies.
     * @param isFreeForm whether the configuration if from free form or not.
     * @return a list of strings of addresses that geo-information cannot be founded.
     */
    public List<String> geoCodeTransform(String origDataSetName, String newDataSetName, String newLabel,
                                         Map<String, List<String>> params, boolean isFreeForm) {
        if (newDataSetName == null || newDataSetName.isBlank())
            throw new IllegalArgumentException("Empty DataSet Name");
        if (newLabel == null || newLabel.isBlank())
            throw new IllegalArgumentException("Empty Label");
        if (dataSetMap.containsKey(newDataSetName))
            throw new IllegalArgumentException("Duplicate Name");

        DataSet origDataSet = dataSetMap.get(origDataSetName);
        if (origDataSet == null) {
            throw new IllegalArgumentException("DataSet Not Found");
        }

        Set<Object> unfounded = new HashSet<>();
        List<Triple<Double, Double, MultiPolygon>> queryResult = null;
        if (isFreeForm) {
            String columnLabel = params.get("Address").get(0);
            if (columnLabel == null)
                throw new IllegalArgumentException("Choose The Address Column");
            String[] addressArray = origDataSet.getColumn(columnLabel).stream().map(Object::toString).toArray(String[]::new);
            queryResult = openStreetMapClient.batchQuery(addressArray, unfounded);
        } else {
            GeoAddress[] addressArray = new GeoAddress[origDataSet.rowCount()];

            List<String> labels = origDataSet.getLabels();
            Integer[] indexes = Arrays.stream(ADDRESS_LABEL)
                    .map(params::get).map(s -> s == null || s.isEmpty() ? -1 : labels.indexOf(s.get(0))).toArray(Integer[]::new);
            for (int i = 0; i < origDataSet.rowCount(); i++) {
                addressArray[i] = new GeoAddress(
                        indexes[0] < 0 ? null : origDataSet.getCell(i, indexes[0]).toString(),
                        indexes[1] < 0 ? null : origDataSet.getCell(i, indexes[1]).toString(),
                        indexes[2] < 0 ? null : origDataSet.getCell(i, indexes[2]).toString(),
                        indexes[3] < 0 ? null : origDataSet.getCell(i, indexes[3]).toString(),
                        indexes[4] < 0 ? null : origDataSet.getCell(i, indexes[4]).toString()
                );
            }
            queryResult = openStreetMapClient.batchQuery(addressArray, unfounded);
        }

        List<List<Object>> data = origDataSet.toLists();
        List<String> labels = new ArrayList<>(origDataSet.getLabels());
        List<DataType> types = new ArrayList<>(origDataSet.getDataTypes());

        for (int i = 0; i < origDataSet.rowCount(); i++) {
            if (queryResult.get(i) == null) {
                continue;
            }
            data.get(i).add(queryResult.get(i).getLeft());
            data.get(i).add(queryResult.get(i).getMiddle());
            data.get(i).add(queryResult.get(i).getRight());
        }
        data.removeIf(r -> r.size() == origDataSet.colCount());

        labels.add(newLabel + " (longitude)");
        types.add(DataType.DOUBLE);
        labels.add(newLabel + " (latitude)");
        types.add(DataType.DOUBLE);
        labels.add(newLabel + " (contour)");
        types.add(DataType.POLYGONS);

        dataSetMap.put(newDataSetName, new DataSet(labels, types, data));
        listeners.forEach(l -> l.dataSetLoaded(newDataSetName));
        List<String> unfoundedList = unfounded.stream().map(Object::toString).collect(Collectors.toList());
        return unfoundedList;
    }

    /**
     * Return a list of UserInputConfig for user to choose which values to filter with.
     *
     * @param dataSetName name of dataSet.
     * @param displayFilterConfigs a list of TransformConfigs mapping from configuration name to transform type.
     * @return a list of UserInputConfig for user to choose which values to filter with.
     */
    public List<UserInputConfig> convertTransformConfigs(String dataSetName, List<DisplayFilterConfig> displayFilterConfigs) {
        DataSet dataSet = dataSetMap.get(dataSetName);
        List<UserInputConfig> res = new ArrayList<>();
        for (DisplayFilterConfig controlConfig : displayFilterConfigs) {
            List<Object> column = dataSet.getColumn(controlConfig.getLabel());
            switch (controlConfig.getFilterType()) {
                case SINGLE_SELECTION:
                case MULTI_SELECTION:
                    res.add(new UserInputConfig(controlConfig.getLabel(), controlConfig.getFilterType(), column.stream()
                            .filter(Objects::nonNull).distinct()
                            .map(Object::toString).collect(Collectors.toList())));
                    break;
                default:
                    break;
            }
        }
        return res;
    }

    /**
     * Return a list of UserInputConfig specific to filter function.
     *
     * @param dataSetName name of dataSet to be filtered.
     * @param isNumericForm whether the configuration will display on numeric form or not.
     * @return a list of UserInputConfig specific to filter function.
     */
    public List<UserInputConfig> getFilterConfigs(String dataSetName, boolean isNumericForm) {
        DataSet dataSet = dataSetMap.get(dataSetName);
        if (dataSet == null)
            throw new IllegalArgumentException("DataSet does not exist");
        List<UserInputConfig> configList = new ArrayList<>();

        if (isNumericForm) {
            List<String> intColumnLabels = dataSet.labelsOfType(DataType.INTEGER);
            List<String> doubleColumnLabels = dataSet.labelsOfType(DataType.DOUBLE);
            List<String> intDoubleColumnLabels = new ArrayList<>(intColumnLabels);
            intDoubleColumnLabels.addAll(doubleColumnLabels);

            configList.add(new UserInputConfig("Column Name", UserInputType.SINGLE_SELECTION, intDoubleColumnLabels));
            configList.add(new UserInputConfig("Operator", UserInputType.SINGLE_SELECTION, Arrays.asList(">", ">=", "=", "<=", "<", "!=")));
            configList.add(new UserInputConfig("Value", UserInputType.TEXT_FIELD, new ArrayList<>()));
        } else {
            List<String> strLabels = dataSet.labelsOfType(DataType.STRING);
            configList.add(new UserInputConfig("Column Name", UserInputType.SINGLE_SELECTION, strLabels));
        }

        return configList;
    }

    /**
     * Return a list of UserInputConfig specific to sort function.
     *
     * @param dataSetName name of dataSet to be sorted.
     * @return a list of UserInputConfig specific to sort function.
     */
    public List<UserInputConfig> getSortConfigs(String dataSetName) {
        DataSet dataSet = dataSetMap.get(dataSetName);
        if (dataSet == null)
            throw new IllegalArgumentException("DataSet does not exist");
        List<UserInputConfig> configList = new ArrayList<>();
        List<String> labels = dataSet.getLabels();
        configList.add(new UserInputConfig("Sort By", UserInputType.SINGLE_SELECTION, labels));
        return configList;
    }

    /**
     * Filter the original dataSet and create a new DataSet based on the parameter mapping specified by user. 
     * (This only supports numeric value filtering)
     *
     * @param origDataSetName name of original dataSet.
     * @param newDataSetName name of new dataSet.
     * @param params parameter mapping from configuration name to a list of values that user specifies.
     */
    public void numericFilter(String origDataSetName, String newDataSetName, Map<String, List<String>> params) {
        checkNewDataSetName(newDataSetName);
        DataSet originDataSet = dataSetMap.get(origDataSetName);
        Transformation origin = new Transformation(originDataSet);
        String label = params.get("Column Name").get(0);
        String operator = params.get("Operator").get(0);
        String value = params.get("Value").get(0);

        Transformation newData = origin.filter(label, operator, value);
        DataSet newDataSet = newData.toDataSet();
        dataSetMap.put(newDataSetName, newDataSet);
        listeners.forEach(l -> l.dataSetLoaded(newDataSetName));
    }

    /**
     * Filter the original dataSet and create a new DataSet based on the parameter mapping specified by user. 
     * (This only supports string value filtering)
     *
     * @param origDataSetName name of original dataSet.
     * @param newDataSetName name of new dataSet.
     * @param params parameter mapping from configuration name to a list of values that user specifies.
     */
    public void stringFilter(String origDataSetName, String newDataSetName, Map<String, List<String>> params) {
        if (params.get("Column Name").isEmpty())
            throw new IllegalArgumentException("Select The Column To Filter");
        checkNewDataSetName(newDataSetName);
        DataSet originDataSet = dataSetMap.get(origDataSetName);
        Transformation origin = new Transformation(originDataSet);
        String label = params.get("Column Name").get(0);
        List<String> values = params.get("Values");
        Transformation newData = origin.filter(label, values);
        DataSet newDataSet = newData.toDataSet();
        dataSetMap.put(newDataSetName, newDataSet);
        listeners.forEach(l -> l.dataSetLoaded(newDataSetName));
    }

    /**
     * Sort the dataSet and create a new DataSet based on the parameter mapping specified by user.
     *
     * @param origDataSetName name of original dataSet.
     * @param newDataSetName name of new dataSet.
     * @param params parameter mapping from configuration name to a list of values that user specifies.
     */
    public void sort(String origDataSetName, String newDataSetName, Map<String, List<String>> params) {
        if (params.get("Sort By").isEmpty())
            throw new IllegalArgumentException("Select The Column To Sort");
        checkNewDataSetName(newDataSetName);
        DataSet originDataSet = dataSetMap.get(origDataSetName);
        Transformation origin = new Transformation(originDataSet);
        String label = params.get("Sort By").get(0);

        Transformation newData = origin.sort(label);

        DataSet newDataSet = newData.toDataSet();
        dataSetMap.put(newDataSetName, newDataSet);
        listeners.forEach(l -> l.dataSetLoaded(newDataSetName));
    }
}
