package edu.cmu.cs.cs214.hw5.plugins_example.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Utility class for World Bank API.
 */
class WorldBankDataWebUtils {

    /**
     * PageInfo of result.
     */
    static class PageInfo {
        
        /**
         * Current page number.
         */
        private int page;
        
        /**
         * Total page number.
         */
        private int pages;

        /**
         * Return current page number.
         * 
         * @return current page number.
         */
        public int getCurrentPage() {
            return page;
        }

        /**
         * Return total page number.
         * 
         * @return total page number.
         */
        public int getTotalPages() {
            return pages;
        }
    }

    /**
     * Class storing country information
     */
    static class CountryInfo {
        /**
         * Country name.
         */
        private String name;

        /**
         * Country code.
         */
        private String iso2Code;

        /**
         * Json structured region of this country.
         */
        private JsonObject region;

        /**
         * Return country name.
         * 
         * @return country name.
         */
        public String getName() {
            return name;
        }

        /**
         * Return country code.
         * 
         * @return country code.
         */ 
        public String getIso2Code() {
            return iso2Code;
        }

        /**
         * Return Json structured region.
         * 
         * @return Json structured region.
         */
        public JsonObject getRegion() {
            return region;
        }
    }

    /**
     * Class Storing the statistic.
     */
    static class Statistic {
        /**
         * Json structured indicator.
         */
        private JsonObject indicator;

        /**
         * Json structured country.
         */
        private JsonObject country;

        /**
         * Date.
         */
        private Integer date;

        /**
         * value of the statistic.
         */
        private Double value;

        /**
         * Return Json structured indicator.
         * 
         * @return Json structured indicator.
         */
        public JsonObject getIndicator() {
            return indicator;
        }

        /**
         * Return Json structured country.
         * 
         * @return Json structured country.
         */
        public JsonObject getCountry() {
            return country;
        }

        /**
         * Return Date.
         * 
         * @return Date.
         */
        public Integer getDate() {
            return date;
        }

        /**
         * Return value of statistic.
         * 
         * @return value of statistic.
         */
        public Double getValue() {
            return value;
        }
    }

    /**
     * Client for WorldBankData API.
     */
    static class WorldBankDataClient {
        
        /**
         * Http Client for WorldBankData API connection.
         */
        private final HttpClient client;

        /**
         * Gson used to convert between JSON and Objects.
         */
        private final Gson gson;

        /**
         * Initialize a client for WorldBankData API.
         */
        WorldBankDataClient() {
            client = HttpClient.newHttpClient();
            gson = new Gson();
        }

        /**
         * Retrieve data by WorldBankData API.
         * 
         * @param uri uri used for retrieving data.
         * @return Json representation of data.
         * @throws URISyntaxException thrown if there is error in retrieving World Bank data.
         * @throws IOException thrown if there is error in retrieving World Bank data.
         * @throws InterruptedException thrown if there is error in retrieving World Bank data.
         */
        Queue<JsonElement> retrieveData(String uri) throws URISyntaxException, IOException, InterruptedException {
            Queue<JsonElement> res = new LinkedList<>();
            int pageNo = 1;
            while (true) {
                String u = String.format("%s&page=%s", uri, pageNo);
                HttpRequest request = HttpRequest.newBuilder(new URI(u)).build();
                System.out.println("Connecting to " + u + "...: \t");
                String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                System.out.println("Connected: " + u + "...: \t");
                int totalPages = -1;
                try {
                    JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
                    res.add(jsonArray.get(1));
                    totalPages = gson.fromJson(jsonArray.get(0), WorldBankDataWebUtils.PageInfo.class).getTotalPages();
                } catch (Exception ex) {
                    System.out.println(String.format("Malformed response: {%50s...}", response));
                }
                if (++pageNo > totalPages)
                    return res;
            }
        }
    }
}
