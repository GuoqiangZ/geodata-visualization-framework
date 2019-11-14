package edu.cmu.cs.cs214.hw5;

import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.GeoDataFramework;
import edu.cmu.cs.cs214.hw5.gui.GeoDataFrameworkGui;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry point to launch the framework.
 */
public class Main {

    /**
     * Entry point to launch the framework.
     * 
     * @param args command line argument.
     */
    public static void main(String[] args) {
        GeoDataFramework core = new GeoDataFramework();

        List<DataPlugin> dataPlugins = loadDataPlugins();
        List<DisplayPlugin> displayPlugins = loadDisplayPlugins();

        dataPlugins.forEach(core::registerDataPlugin);
        displayPlugins.forEach(core::registerDisplayPlugin);

        SwingUtilities.invokeLater(() -> new GeoDataFrameworkGui(core));
    }

    /**
     * Load data plugins listed in META-INF/services/...
     *
     * @return List of instantiated plugins
     */
    private static List<DataPlugin> loadDataPlugins() {
        ServiceLoader<DataPlugin> plugins = ServiceLoader.load(DataPlugin.class);
        List<DataPlugin> result = new ArrayList<>();
        for (DataPlugin plugin : plugins) {
            result.add(plugin);
        }
        return result;
    }

    /**
     * Load display plugins listed in META-INF/services/...
     *
     * @return List of instantiated plugins
     */
    private static List<DisplayPlugin> loadDisplayPlugins() {
        ServiceLoader<DisplayPlugin> plugins = ServiceLoader.load(DisplayPlugin.class);
        List<DisplayPlugin> result = new ArrayList<>();
        for (DisplayPlugin plugin : plugins) {
            result.add(plugin);
        }
        return result;
    }
}
