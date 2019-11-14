package edu.cmu.cs.cs214.hw5.data;

import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.DataType;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * FileReaderPlugin is a DataPlugin which extracts dataSet from files(e.g. csv, tsv..) and
 * imports the dataSet into the framework.
 */
public class FileReaderPlugin implements DataPlugin {
    
    /**
     * Name of the plugin.
     */
    private static final String NAME = "File Reader (Example)";

    /**
     * File Path configuration label name.
     */
    private static final String FILE_PATH = "File Path";

    /**
     * Delimiter configuration label name.
     */
    private static final String DELIMITER = "Delimiter";

    /**
     * Fetch the name of data plugin which is loaded into the framework.
     *
     * @return name of data plugin.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Plugin should provide a list of UserInputConfig to customize the plugin-specific usage
     * configuration or information (e.g. file path, dataset name..) that user needs to specify.
     * The plugin needs to specify configuration name, input type (TEXT_FIELD, MULTI_SELECTION
     * or SINGLE_SELECTION) and a list of selections if any. The concrete parameters
     * that user specifies can be fetched later when loading data.
     *
     * @return a list of plugin-specific UserInputConfig.
     */
    @Override
    public List<UserInputConfig> getUserInputConfigs() {
        List<UserInputConfig> options = new ArrayList<>();
        options.add(new UserInputConfig(FILE_PATH, UserInputType.TEXT_FIELD, new ArrayList<>()));
        options.add(new UserInputConfig(DELIMITER, UserInputType.TEXT_FIELD, new ArrayList<>()));
        return options;
    }

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
    @Override
    public DataSet loadData(Map<String, List<String>> params) {
        String path = params.get(FILE_PATH).get(0);
        String delimiter = params.get(DELIMITER).get(0);

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(path));
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe.getMessage());
        }

        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Please specify a column line in the file");
        }

        String columnLine = scanner.nextLine();
        String[] columnNames = columnLine.split(delimiter);
        int columnNum = columnNames.length;
        List<String> labels = new ArrayList<>();
        for (String columnName: columnNames) {
            labels.add(columnName);
        }

        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Please specify a column value type line in the file");
        }

        String typeLine = scanner.nextLine();
        String[] types = typeLine.split(delimiter);
        List<DataType> dataTypes = new ArrayList<>();
        for (String type: types) {
            switch (type) {
                case "String":
                    dataTypes.add(DataType.STRING);
                    break;
                case "Integer":
                    dataTypes.add(DataType.INTEGER);
                    break;
                case "Double":
                    dataTypes.add(DataType.DOUBLE);
                    break;
                default:
                    break;
            }
        }
                
        List<List<Object>> data = new ArrayList<>();
        while(scanner.hasNextLine()) {
            List<Object> row = new ArrayList<>();
            String[] valuesInLine = scanner.nextLine().split(delimiter);
            for (int i = 0; i < columnNum; i++) {
                switch (dataTypes.get(i)) {
                    case STRING:
                        row.add(valuesInLine[i]);
                        break;
                    case INTEGER:
                        row.add(Integer.parseInt(valuesInLine[i]));
                        break;
                    case DOUBLE:
                        row.add(Double.parseDouble(valuesInLine[i]));
                        break;
                    default:
                        break;
                }
            }
            data.add(row);
        }
        
        DataSet dataSet = new DataSet(labels, dataTypes, data);
        return dataSet;
    }
}