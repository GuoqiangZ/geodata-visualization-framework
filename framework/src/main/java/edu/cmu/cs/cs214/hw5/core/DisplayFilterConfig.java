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
     * Create and initialize a DisplayFilterConfig. Client needs to specify the label
     * name of the configuration and the type.
     * 
     * @param label label name of the configuration.
     * @param transformType type of configuration(FILTER_BY_DISTINCTIVE or FILTER_BY_SELECT).
     */
    public DisplayFilterConfig(String label, UserInputType transformType) {
        this.label = label;
        this.filterType = transformType;
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
}
