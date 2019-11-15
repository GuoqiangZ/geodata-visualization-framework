package edu.cmu.cs.cs214.hw5.plugins_example.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.DataSet;
import edu.cmu.cs.cs214.hw5.core.DataType;
import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputType;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WorldBankDataPlugin is a DataPlugin which extracts data using World Bank API
 * and imports the dataSet into the framework.
 */
public class WorldBankDataPlugin implements DataPlugin {

    /**
     * Name of the plugin.
     */
    private static final String NAME = "World Bank Open Data";

    /**
     * Country configuration label name.
     */
    private static final String COUNTRY = "Country / District";

    /**
     * Topic configuration label name.
     */
    private static final String TOPIC = "Topic";

    /**
     * Start Year configuration label name.
     */
    private static final String START_YEAR_LABEL = "Start Year";

    /**
     * End Year configuration label name.
     */
    private static final String END_YEAR_LABEL = "End Year";

    /**
     * Mapping from topic string to API-specific topic path.
     */
    private static final Map<String, String> TOPIC_PATH_MAP = Map.of(
            "CO2 emissions (metric tons per capita)", "EN.ATM.CO2E.PC",
            "Forest area (% of land area)", "AG.LND.FRST.ZS",
            "GDP (current US$)", "NY.GDP.MKTP.CD",
            "Life expectancy at birth, total (years)", "SP.DYN.LE00.IN",
            "Population, total", "SP.POP.TOTL",
            "School enrollment, primary (% gross)", "SE.PRM.ENRR"
    );

    /**
     * Gson used to convert between JSON and Objects.
     */
    private final Gson gson = new Gson();

    /**
     * Client of World Bank API.
     */
    private final WorldBankDataWebUtils.WorldBankDataClient client = new WorldBankDataWebUtils.WorldBankDataClient();

    /**
     * Map of country name to code.
     */
    private final Map<String, String> countryNameCodeMap;

    /**
     * List of year range.
     */
    private final List<String> yearRange;

    /**
     * Create and initialize the worldBankDataPlugin.
     */
    public WorldBankDataPlugin() {
        countryNameCodeMap = new TreeMap<>();
        yearRange = new ArrayList<>();
        try {
            loadCountryFromWorldBank();
        } catch (Exception e) {
            throw new IllegalStateException("Error in retrieving World Bank data");
        }
        for (int i = 2019; i >= 1960; i--)
            yearRange.add(String.valueOf(i));
    }

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
        options.add(new UserInputConfig(COUNTRY, UserInputType.MULTI_SELECTION, new ArrayList<>(countryNameCodeMap.keySet())));
        options.add(new UserInputConfig(TOPIC, UserInputType.MULTI_SELECTION, new ArrayList<>(TOPIC_PATH_MAP.keySet())));
        options.add(new UserInputConfig(START_YEAR_LABEL, UserInputType.SINGLE_SELECTION, Collections.unmodifiableList(yearRange)));
        options.add(new UserInputConfig(END_YEAR_LABEL, UserInputType.SINGLE_SELECTION, Collections.unmodifiableList(yearRange)));
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
        // Check start year and end year.
        String startYear = params.get(START_YEAR_LABEL).get(0);
        String endYear = params.get(END_YEAR_LABEL).get(0);
        if (startYear == null || endYear == null)
            throw new IllegalArgumentException("Choose year first");
        if (startYear.compareTo(endYear) > 0)
            throw new IllegalArgumentException("Start year must be smaller than end year");

        // Check if countries are selected.
        List<String> countryList = params.get(COUNTRY);
        if (countryList.isEmpty())
            throw new IllegalArgumentException("Choose countries first.");
        String countryCodes = String.join(";",
                countryList.stream().map(countryNameCodeMap::get).collect(Collectors.toList()));

        // Check if topics are selected.
        List<String> topicList = params.get(TOPIC);
        if (topicList.isEmpty())
            throw new IllegalArgumentException("Choose topics first.");

        try {
            return loadDataFromWorldBank(countryList, topicList, startYear, endYear);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("Error in retrieving World Bank data");
        }
    }

    /**
     * Load data by using World Bank API.
     * 
     * @param countryNames country names.
     * @param topicList list of topics queried.
     * @param startYear start year.
     * @param endYear end year.
     * @return DataSet extracted from World Bank.
     * @throws InterruptedException thrown if there is error in retrieving World Bank data.
     * @throws IOException thrown if there is error in retrieving World Bank data.
     * @throws URISyntaxException thrown if there is error in retrieving World Bank data.
     */
    private DataSet loadDataFromWorldBank(List<String> countryNames, List<String> topicList, String startYear, String endYear)
            throws InterruptedException, IOException, URISyntaxException {
        // Retrive data from world bank.
        Map<Pair<String, Integer>, Double[]> map = new ConcurrentHashMap<>();

        countryNames.stream().parallel().forEach(countryName -> {
            String countryCode = countryNameCodeMap.get(countryName);
            for (int j = 0; j < topicList.size(); j++) {
                String topicPath = TOPIC_PATH_MAP.get(topicList.get(j));
                String uri = String.format(
                        "http://api.worldbank.org/v2/country/%s/indicator/%s?format=json&date=%s:%s",
                        countryCode, topicPath, startYear, endYear);
                try {
                    Queue<JsonElement> elements = client.retrieveData(uri);
                    while (!elements.isEmpty()) {
                        WorldBankDataWebUtils.Statistic[] statistics = gson.fromJson(
                                elements.poll(), WorldBankDataWebUtils.Statistic[].class);
                        if (statistics == null)
                            continue;
                        for (WorldBankDataWebUtils.Statistic statistic : statistics) {
                            Double value = statistic.getValue();
                            if (value == null)
                                continue;
                            int year = statistic.getDate();

                            Pair<String, Integer> key = Pair.of(countryName, year);
                            map.putIfAbsent(key, new Double[topicList.size()]);
                            map.get(key)[j] = value;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        List<List<Object>> data = new ArrayList<>();
        for (Map.Entry<Pair<String, Integer>, Double[]> entry : map.entrySet()) {
            List<Object> row = new ArrayList<>();
            row.add(entry.getKey().getLeft());
            row.add(entry.getKey().getRight());
            for (Double d : entry.getValue()) {
                row.add(d == null ? 0 : d);
            }
            data.add(row);
        }

        List<String> labels = new ArrayList<>();
        List<DataType> types = new ArrayList<>();
        labels.add("Country");
        types.add(DataType.STRING);
        labels.add("Year");
        types.add(DataType.INTEGER);
        for (int i = 0; i < topicList.size(); i++) {
            labels.add(topicList.get(i));
            types.add(DataType.DOUBLE);
        }
        return new DataSet(labels, types, data);
    }

    /**
     * Load country name and code from World Bank API.
     * 
     * @throws URISyntaxException thrown if there is error in retrieving World Bank data.
     * @throws IOException thrown if there is error in retrieving World Bank data.
     * @throws InterruptedException thrown if there is error in retrieving World Bank data.
     */
    private void loadCountryFromWorldBank() throws URISyntaxException, IOException,
            InterruptedException {
        Queue<JsonElement> elements = client.retrieveData("https://api.worldbank.org/v2/country?format=json");

        while (!elements.isEmpty()) {
            WorldBankDataWebUtils.CountryInfo[] countryInfoArray = gson.fromJson(elements.poll(),
                    WorldBankDataWebUtils.CountryInfo[].class);
            if (countryInfoArray != null && countryInfoArray.length > 0) {
                for (WorldBankDataWebUtils.CountryInfo info : countryInfoArray) {
                    if (!"Aggregates".equals(info.getRegion().get("value").getAsString()))
                        countryNameCodeMap.put(info.getName(), info.getIso2Code());
                }
            }
        }
    }
}
