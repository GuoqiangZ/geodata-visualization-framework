package edu.cmu.cs.cs214.hw5.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transformation represents the transformation state of a dataSet which can be later
 * converted into a new transformation object by transformation or a new DataSet.
 */
class Transformation {
    
    private static final String LABEL_NOT_FOUND_MSG = "Label Not Found";
    
    private static final String UNKNOWN_OPERATOR_MSG = "Unknown Operator";

    /**
     * List of label names of dataSet in transformation.
     */
    private List<String> labels;

    /**
     * List of data types of dataSet in transformation.
     */
    private List<DataType> dataTypes;

    /**
     * Data entity of dataSet in transformation.
     */
    private List<List<Object>> data;

    /**
     * Stream storing the dataSet.
     */
    private Stream<List<Object>> rowStream;

    /**
     * Predicate for operator ">".
     */
    private static final Predicate<Integer> LARGER = i -> i > 0;

    /**
     * Predicate for operator ">=".
     */
    private static final Predicate<Integer> LARGER_OR_EQUAL = i -> i >= 0;

    /**
     * Predicate for operator "=".
     */
    private static final Predicate<Integer> EQUAL = i -> i == 0;

    /**
     * Predicate for operator "<=".
     */
    private static final Predicate<Integer> SMALLER_OR_EQUAL = i -> i <= 0;

    /**
     * Predicate for operator "<".
     */
    private static final Predicate<Integer> SMALLER = i -> i < 0;

    /**
     * Predicate for operator "!=".
     */
    private static final Predicate<Integer> UNEQUAL = i -> i != 0;

    /**
     * Map from string representation of operators to predicates.
     */
    private static final Map<String, Predicate<Integer>> PREDICATE_MAP = Map.of(
            ">", LARGER, ">=", LARGER_OR_EQUAL, "=", EQUAL, "<=", SMALLER_OR_EQUAL,
            "<", SMALLER, "!=", UNEQUAL
    );

    /**
     * Comparator for double typed data.
     */
    private static final Comparator<Object> DOUBLE_COMPARATOR = (d1, d2) -> {
        return Double.compare((Double) d1, (Double) d2);
    };

    /**
     * Comparator for integer typed data.
     */
    private static final Comparator<Object> INTEGER_COMPARATOR = (d1, d2) -> {
        return Integer.compare((Integer) d1, (Integer) d2);
    };

    /**
     * Comparator for string typed data.
     */
    private static final Comparator<Object> STRING_COMPARATOR = (d1, d2) -> {
        return ((String) d1).compareTo((String) d2);
    };

    private static final Map<DataType, Comparator<Object>> COMPARATOR_MAP = Map.of(
            DataType.DOUBLE, DOUBLE_COMPARATOR,
            DataType.INTEGER, INTEGER_COMPARATOR,
            DataType.STRING, STRING_COMPARATOR
    );

    /**
     * Initialize a Transformation object ready for performing transformation by dataSet.
     *
     * @param dataSet dataSet to be transformed.
     */
    Transformation(DataSet dataSet) {
        data = dataSet.toLists();
        dataTypes = new ArrayList<>(dataSet.getDataTypes());
        labels = new ArrayList<>(dataSet.getLabels());
        rowStream = data.stream();
    }

    /**
     * Filter dataSet by numeric typed columns selected by user.
     *
     * @param label label name of column to be filtered.
     * @param operator operator in the filtering rule.
     * @param value value in the filtering rule.
     * @return a new Transformation object after applying filtering rule.
     */
    Transformation filter(String label, String operator, String value) {
        int colIdx = labels.indexOf(label);
        if (colIdx < 0)
            throw new IllegalArgumentException(LABEL_NOT_FOUND_MSG);

        Comparator<Object> comparator = COMPARATOR_MAP.get(dataTypes.get(colIdx));
        if (comparator == null)
            throw new IllegalArgumentException("Filter Operation Doesn't Support This Data Type: "
                    + dataTypes.get(colIdx));

        final Predicate<Integer> predicate = PREDICATE_MAP.get(operator);
        if (predicate == null)
            throw new IllegalArgumentException(UNKNOWN_OPERATOR_MSG);

        switch (dataTypes.get(colIdx)) {
            case INTEGER:
                rowStream = rowStream.filter(row -> predicate.test(comparator.compare(row.get(colIdx), Integer.valueOf(value))));
                break;
            case DOUBLE:
                rowStream = rowStream.filter(row -> predicate.test(comparator.compare(row.get(colIdx), Double.valueOf(value))));
                break;
            case STRING:
                rowStream = rowStream.filter(row -> predicate.test(comparator.compare(row.get(colIdx), value)));
                break;
            default:
                break;
        }

        return this;
    }

    /**
     * Filter dataSet by string typed columns selected by user.
     *
     * @param label label name of column to be filtered.
     * @param values value in column that will remain specified in the filtering rule.
     * @return a new Transformation object after applying filtering rule.
     */
    Transformation filter(String label, List<String> values) {
        int colIdx = labels.indexOf(label);
        if (colIdx < 0)
            throw new IllegalArgumentException(LABEL_NOT_FOUND_MSG);

        final Set<Object> set;

        switch (dataTypes.get(colIdx)) {
            case INTEGER:
                set = values.stream().map(Integer::valueOf).collect(Collectors.toSet());
                break;
            case DOUBLE:
                set = values.stream().map(Double::valueOf).collect(Collectors.toSet());
                break;
            case STRING:
                set = new HashSet<>(values);
                break;
            default:
                throw new IllegalArgumentException("Filter Operation Doesn't Support This Data Type: "
                        + dataTypes.get(colIdx));
        }
        rowStream = rowStream.filter(r -> set.contains(r.get(colIdx)));
        return this;
    }

    /**
     * Sort the dataSet by specified column in ascending order.
     *
     * @param label label name of the specified column.
     * @return a new Transformation object after applying sorting.
     */
    Transformation sort(String label) {
        return sort(label, true);
    }

    /**
     * Sort the dataSet by specified column in ascending or descending order.
     *
     * @param label label name of the specified column.
     * @param ascOrder true if sorting in ascending order, false if in descending order.
     * @return a new Transformation object after applying sorting.
     */
    Transformation sort(String label, boolean ascOrder) {
        int colIdx = labels.indexOf(label);
        if (colIdx < 0)
            throw new IllegalArgumentException(LABEL_NOT_FOUND_MSG);

        Comparator comparator = COMPARATOR_MAP.get(dataTypes.get(colIdx));
        if (comparator == null)
            throw new IllegalArgumentException("Sort Operation Doesn't Support This Data Type: " + dataTypes.get(colIdx));
        if (ascOrder) {
            rowStream = rowStream.sorted((row1, row2) -> comparator.compare(row1.get(colIdx), row2.get(colIdx)));
        } else {
            Comparator reversed = comparator.reversed();
            rowStream = rowStream.sorted((row1, row2) -> reversed.compare(row1.get(colIdx), row2.get(colIdx)));
        }
        return this;
    }

    /**
     * Return a new DataSet converted from the transformation object.
     *
     * @return a new DataSet converted from the transformation object.
     */
    DataSet toDataSet() {
        List<List<Object>> newData = rowStream.collect(Collectors.toList());
        return new DataSet(labels, dataTypes, newData);
    }

}
