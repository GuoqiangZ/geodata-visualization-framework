package edu.cmu.cs.cs214.hw5.core;

import java.util.Map;

/**
 * DataType enumerates all the possible data types that can be stored in DataSet.
 * It includes INTEGER, DOUBLE, STRING and POLYGONS.
 */
public enum DataType {
    INTEGER, DOUBLE, STRING, POLYGONS;

    /**
     * Mapping from DataType to class type.
     */
    private static Map<DataType, Class> map = Map.of(
            INTEGER, Integer.class,
            DOUBLE, Double.class,
            STRING, String.class,
            POLYGONS, MultiPolygon.class);

    /**
     * Check if the given object has this data type.
     * 
     * @param o object to be checked.
     * @return true if the object is this type and false otherwise.
     */
    boolean checkType(Object o) {
        return map.get(this).isInstance(o);
    }
}
