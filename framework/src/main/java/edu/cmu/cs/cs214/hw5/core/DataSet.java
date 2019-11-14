package edu.cmu.cs.cs214.hw5.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * DataSet is an immutable class that abstracts and stores the geo-data set in the framework.
 * DataPlugin clients convert their data source into DataSet and import it into framework which
 * can be further used by displayPlugin clients.
 *
 * The DataSet consists of a list of labels (column name), a list of data types respective
 * to the column and data entity. The data in each column is of the same type, and it doesn't
 * allow NULL value.
 */
public class DataSet {
    
    /**
     * Column labels of the data set.
     */
    private final List<String> labels;

    /**
     * Data types of the data set.
     */
    private final List<DataType> dataTypes;

    /**
     * Data entity of the data set(stored by row).
     */
    private final List<List<Object>> data;

    /**
     * Number of columns in the data set.
     */
    private final int colCount;

    /**
     * Number of rows in the data set.
     */
    private final int rowCount;

    /**
     * Error message shown when label is empty.
     */
    private static final String EMPTY_LABEL_MSG = "Label Cannot Be Empty";

    /**
     * Error message shown when data type is empty.
     */
    private static final String EMPTY_DATA_TYPE_MSG = "Data Types Cannot Be Empty";

    /**
     * Error message shown when dataSet size is invalid.
     */
    private static final String INVALID_DATASET_SIZE_MSG = "Invalid Size";

    /**
     * Error message shown when size of row is invalid.
     */
    private static final String INVALID_SIZE_AT_ROW_MSG = "Invalid Size At Row ";

    /**
     * Error message shown when data type is invalid.
     */
    private static final String INVALID_TYPE_MSG = "Invalid Data Type At Row %d, Col %d. Expected: %s, Actual: %s";

    /**
     * Create and Initialize a DataSet object.
     * 
     * @param inLabels list of column labels.
     * @param inDataTypes list of data types.
     * @param values data entity stored as a list of rows (a row represented by a list of objects).
     */
    public DataSet(List<String> inLabels, List<DataType> inDataTypes, List<List<Object>> values) {
        this.labels = new ArrayList<>(inLabels);
        if (!checkEmptyLabels())
            throw new IllegalArgumentException(EMPTY_LABEL_MSG);
        this.dataTypes = new ArrayList<>(inDataTypes);
        if (!checkEmptyTypes())
            throw new IllegalArgumentException(EMPTY_DATA_TYPE_MSG);
        if (inDataTypes.size() != inLabels.size()) {
            throw new IllegalArgumentException(INVALID_DATASET_SIZE_MSG);
        }
        colCount = inDataTypes.size();

        data = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = new ArrayList<>(values.get(i));
            if (row.size() != colCount)
                throw new IllegalArgumentException(INVALID_SIZE_AT_ROW_MSG + i);
            for (int j = 0; j < colCount; j++) {
                if (!dataTypes.get(j).checkType(row.get(j)))
                    throw new IllegalArgumentException(String.format(
                            INVALID_TYPE_MSG, i, j, dataTypes.get(j), row.get(j)));
            }
            data.add(row);
        }
        rowCount = data.size();
        resolveDuplicateLabels();
    }

    /**
     * Check if there are empty labels.
     * 
     * @return true if no label is empty and false otherwise.
     */
    private boolean checkEmptyLabels() {
        for (String s : labels) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if there are empty types.
     * 
     * @return true if no type is empty and false otherwise.
     */
    private boolean checkEmptyTypes() {
        for (DataType type : dataTypes) {
            if (type == null)
                return false;
        }
        return true;
    }

    /**
     * Add * to duplicate labels
     */
    private void resolveDuplicateLabels() {
        Set<String> existed = new HashSet<>();
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            while (!existed.add(label)) {
                label += "*";
            }
            labels.set(i, label);
        }
    }

    /**
     * Return data Object at given row and column number.
     * 
     * @param row row number.
     * @param col column number.
     * @return data Object at given row and column number.
     */
    public Object getCell(int row, int col) {
        return data.get(row).get(col);
    }

    /**
     * Return label name of given column.
     * 
     * @param col column number.
     * @return label name of given column.
     */
    public String getLabel(int col) {
        return labels.get(col);
    }

    /**
     * Return a copy of the list of labels.
     * 
     * @return a copy of the list of labels.
     */
    public List<String> getLabels() {
        return new ArrayList<>(labels);
    }

    /**
     * Return a copy of the list of data types.
     * 
     * @return a copy of the list of data types.
     */
    public List<DataType> getDataTypes() {
        return new ArrayList<>(dataTypes);
    }

    /**
     * Return a copy of the list of data objects at given row.
     * 
     * @param row row number.
     * @return a copy of the list of data objects at given row.
     */
    public List<Object> getRow(int row) {
        return new ArrayList<>(data.get(row));
    }

    /**
     * Return a copy of the list of data objects at given column.
     * 
     * @param col column number.
     * @return a copy of the list of data objects at given column.
     */
    public List<Object> getColumn(int col) {
        return IntStream.range(0, rowCount).mapToObj(i -> data.get(i).get(col)).collect(Collectors.toList());
    }

    /**
     * Return a copy of the list of data objects given the label of column.
     * 
     * @param label label of column.
     * @return a copy of the list of data objects given the label of column.
     */
    public List<Object> getColumn(String label) {
        int index = labels.indexOf(label);
        if (index < 0)
            return null;
        return getColumn(index);
    }
    
    /**
     * Return row number of this data set.
     * 
     * @return row number of this data set.
     */
    public int rowCount() {
        return rowCount;
    }

    /**
     * Return column number of this data set.
     * 
     * @return column number of this data set.
     */
    public int colCount() {
        return colCount;
    }

    /**
     * Return a column preview mapping from column type to a list of column names of this type.
     *
     * @return column preview mapping from column type to a list of column names of this type.
     */
    public Map<DataType, List<String>> getColumnPreview() {
        Map<DataType, List<String>> columnPreview = new HashMap<>();

        columnPreview.put(DataType.STRING, labelsOfType(DataType.STRING));
        columnPreview.put(DataType.INTEGER, labelsOfType(DataType.INTEGER));
        columnPreview.put(DataType.DOUBLE, labelsOfType(DataType.DOUBLE));
        columnPreview.put(DataType.POLYGONS, labelsOfType(DataType.POLYGONS));

        return columnPreview;
    }

    /**
     * Return a list of labels of columns with the given type.
     * 
     * @param type data type.
     * @return a list of labels of columns with the given type.
     */
    public List<String> labelsOfType(DataType type) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < colCount; i++) {
            if (dataTypes.get(i).equals(type))
                list.add(labels.get(i));
        }
        return list;
    }

    /**
     * Return a copy of this data set.
     * 
     * @return a copy of this data set.
     */
    List<List<Object>> toLists() {
        List<List<Object>> list = new ArrayList<>();
        for (List<Object> row : data) {
            list.add(new ArrayList<>(row));
        }
        return list;
    }
}
