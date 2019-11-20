package edu.cmu.cs.cs214.hw5.core;

/**
 * DisplayFilterConfig is provided to let user parameterize the configuration specific
 * to the transformation operations.
 */
public class DisplayFilterConfig {
    
    /**
     * Label of the configuration.
     */
    private final String label;

    /**
     * Type of the configuration.
     */
    private final UserInputType filterType;

    /**
     * Sort method. True is to sortASC ascending, false is sortASC descending.
     */
    private final Boolean sortMethod;

    /**
     * Create and initialize a DisplayFilterConfig. Client needs to specify the label
     * name of the configuration and the type.
     *
     * The dataset will not be sorted by this column by default;
     * 
     * @param label label name of the configuration.
     * @param transformType type of configuration(FILTER_BY_DISTINCTIVE or FILTER_BY_SELECT).
     */
    public DisplayFilterConfig(String label, UserInputType transformType) {
        this(label, transformType, null);
    }

    /**
     * Create and initialize a DisplayFilterConfig. Client needs to specify the label
     * name of the configuration, the type and sort order if needed.
     *
     * If sortOrder is not null, the dataset will be sorted by this column. If sortOrder is true, the dataset will
     * be sorted in ascending order by this column. If sortOrder is false, the dataset will be sorted in descending
     * order by this column.
     *
     * @param label label name of the configuration.
     * @param transformType type of configuration(FILTER_BY_DISTINCTIVE or FILTER_BY_SELECT).
     * @param sortOrder sort order.
     */
    public DisplayFilterConfig(String label, UserInputType transformType, Boolean sortOrder) {
        this.label = label;
        this.filterType = transformType;
        this.sortMethod = sortOrder;
    }

    /**
     * Return the label name of the configuration.
     * 
     * @return label name of the configuration.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the filter type of the configuration.
     * 
     * @return filter type of the configuration.
     */
    public UserInputType getFilterType() {
        return filterType;
    }

    /**
     * Return the sort order of the configuration.
     *
     * @return sort order type of the configuration.
     */
    public Boolean getSortOrder() {
        return sortMethod;
    }
}
