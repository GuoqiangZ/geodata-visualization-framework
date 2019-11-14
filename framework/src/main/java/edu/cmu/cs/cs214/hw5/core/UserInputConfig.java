package edu.cmu.cs.cs214.hw5.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UserInputConfig is provided to let user parameterize the configuration.
 */
public class UserInputConfig {
    
    /**
     * Label name of the configuration.
     */
    private final String name;

    /**
     * Type of the configuration.
     */
    private final UserInputType type;

    /**
     * List of labels which can be selected by user(empty list for TEXT_FIELD type).
     */
    private final List<String> selectionList;

    /**
     * Create and initilize a userInputConfig.
     * 
     * @param name name of configuration.
     * @param type type of configuration.
     * @param selectionList List of labels which can be selected by user((empty list for TEXT_FIELD type).
     */
    public UserInputConfig(String name, UserInputType type, List<String> selectionList) {
        this.name = name;
        this.type = type;
        if (type.equals(UserInputType.TEXT_FIELD))
            this.selectionList = new ArrayList<>();
        else
            this.selectionList = new ArrayList<>(selectionList);
    }

    /**
     * Return label name of this configuration. 
     * 
     * @return label name of this configuration. 
     */
    public String getName() {
        return name;
    }

    /**
     * Return input type of this configuration.
     * 
     * @return input type of this configuration.
     */
    public UserInputType getInputType() {
        return type;
    }

    /**
     * Return a copy of list of labels which can be selected by user((empty list for TEXT_FIELD type).
     * 
     * @return a copy of list of labels which can be selected by user((empty list for TEXT_FIELD type).
     */
    public List<String> getSelectionList() {
        return Collections.unmodifiableList(selectionList);
    }
}
