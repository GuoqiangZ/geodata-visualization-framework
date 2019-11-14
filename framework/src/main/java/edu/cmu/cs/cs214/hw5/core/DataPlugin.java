package edu.cmu.cs.cs214.hw5.core;

import java.util.List;
import java.util.Map;

/**
 * Interface implemented by concrete data plugin to extract data from source into framework.
 */
public interface DataPlugin {
    
    /**
     * Fetch the name of data plugin which is loaded into the framework.
     * 
     * @return name of data plugin.
     */
    String getName();

    /**
     * Plugin should provide a list of UserInputConfig to customize the plugin-specific usage
     * configuration or information (e.g. file path, dataset name..) that user needs to specify.
     * The plugin needs to specify configuration name, input type (TEXT_FIELD, MULTI_SELECTION
     * or SINGLE_SELECTION) and a list of selections if any. The concrete parameters
     * that user specifies can be fetched later when loading data.
     * 
     * @return a list of plugin-specific UserInputConfig.
     */
    List<UserInputConfig> getUserInputConfigs();

    /**
     * Based on a parameter mapping from configuration name to concrete parameters user specifies,
     * client can extract data from source into DataSet object and import into framework. The client 
     * needs to specify a list of column labels, a list of data type (STRING, INTEGER, DOUBLE) 
     * respectively and a list of data entries (an entry is a list of object and stores values in 
     * position corresponding to the column labels).
     * 
     * @param params a parameter mapping from configuration name to concrete parameters user specifies.
     * @return DataSet object containing the data from source.
     */
    DataSet loadData(Map<String, List<String>> params);
}
