package edu.cmu.cs.cs214.hw5.core;

/**
 * FrameworkListener interface. The class implementing this interface will be notified upon
 * events happening in framework(dataLoaded, graphCreated, graphDrawn and dataSetSelected).
 */
public interface FrameworkListener {
    
    /**
     * Handle events when dataSet is loaded into the framework.
     * 
     * @param dataSetName Name of dataSet loaded into the framework.
     */
    void dataSetLoaded(String dataSetName);
    
    /**
     * Handle events when a dataSet is deleted in the framework.
     * 
     * @param dataSetName name of dataSet.
     */
    void dataSetDeleted(String dataSetName);
}
