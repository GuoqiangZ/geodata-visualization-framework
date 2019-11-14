package edu.cmu.cs.cs214.hw5.core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.tuple.Triple;

import java.awt.Point;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Utility functions of OpenStreetMap API.
 */
class OpenStreetMapClient {
    
    /**
     * Gson used to convert between JSON and Objects.
     */
    private static final Gson GSON = new Gson();

    /**
     * Http Client Object.
     */
    private final HttpClient client;

    private final boolean printStatus;

    OpenStreetMapClient(HttpClient client, boolean printStatus) {
        this.client = client;
        this.printStatus = printStatus;
    }

    private static final String URI_PREFIX =
            "https://nominatim.openstreetmap.org/search?format=json&limit=1&polygon_geojson=1&polygon_threshold=0.5";

    /**
     * Batch Query given an array of GeoAddress.
     * 
     * @param addressArray an array of GeoAddress used for batch query.
     * @param unfounded a set to stored unfound address
     * @return a list of Triple of longitude, latitude and polygons.
     */
    List<Triple<Double, Double, MultiPolygon>> batchQuery(GeoAddress[] addressArray, Set unfounded) {
        List<GeoAddress> addresses = Arrays.stream(addressArray).distinct().collect(Collectors.toList());
        Map<GeoAddress, Triple<Double, Double, MultiPolygon>> res = new ConcurrentHashMap<>();
        addresses.parallelStream().forEach(a -> res.put(a, queryByAdress(a)));
        return Arrays.stream(addressArray).map(a -> {
            if (res.get(a).getLeft() == null) {
                unfounded.add(a);
                return null;
            }
            return res.get(a);
        }).collect(Collectors.toList());
    }

    /**
     * Batch Query given a string representing the address.
     * 
     * @param addressArray a string representing the address used for batch query.
     * @param unfounded a set to stored unfound address
     * @return a list of Triple of longitude, latitude and polygons.
     */
    List<Triple<Double, Double, MultiPolygon>> batchQuery(String[] addressArray, Set unfounded) {
        List<String> addresses = Arrays.stream(addressArray).distinct().collect(Collectors.toList());
        Map<String, Triple<Double, Double, MultiPolygon>> res = new ConcurrentHashMap<>();
        addresses.parallelStream().forEach(a -> res.put(a, queryByAdress(a)));
        return Arrays.stream(addressArray).map(a -> {
            if (res.get(a).getLeft() == null)
                unfounded.add(a);
            return res.get(a);
        }).collect(Collectors.toList());
    }

    /**
     * Query given a GeoAddress.
     * 
     * @param addr GeoAddress representation of address.
     * @return a Triple of longitude, latitude and polygons.
     */
    Triple<Double, Double, MultiPolygon> queryByAdress(GeoAddress addr) {
        StringBuilder sb = new StringBuilder(URI_PREFIX);
        if (addr.getCountry() != null)
            sb.append("&country=" + addr.getCountry());
        if (addr.getState() != null)
            sb.append("&state=" + addr.getState());
        if (addr.getCity() != null)
            sb.append("&city=" + addr.getCity());
        if (addr.getCounty() != null)
            sb.append("&county=" + addr.getCounty());
        if (addr.getStreet() != null)
            sb.append("&street=" + addr.getStreet());
        return queryByUri(sb.toString());

    }

    /**
     * Query given a string representation of address.
     * 
     * @param s a string representation of address.
     * @return a Triple of longitude, latitude and polygons.
     */
    Triple<Double, Double, MultiPolygon> queryByAdress(String s) {
        String uri = URI_PREFIX + "&q=" + s;
        return queryByUri(uri);

    }

    /**
     * Query by using OpenStreetMap API with given uri.
     * 
     * @param uri uri used to query.
     * @return a Triple of longitude, latitude and polygons.
     */
    Triple<Double, Double, MultiPolygon> queryByUri(String uri) {
        uri = uri.replaceAll(" ", "%20");
        System.out.println("Connecting to " + uri + "...: \t");
        String responseBody;
        QueryResult[] results;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(uri)).build();
            responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            results = GSON.fromJson(responseBody, QueryResult[].class);
        } catch (URISyntaxException | IOException | InterruptedException | JsonSyntaxException e) {
            if (printStatus)
                System.out.println("Failed to connect to " + uri);
            throw new IllegalArgumentException(String.format(
                    "Unable to connect to OpenStreetMap. Please Retry. %n%s",
                    URI_PREFIX));
        }
        if (printStatus)
            System.out.println("Connected to " + uri);
        if (results.length == 0) {
            System.out.println("Address Not Found: " + uri.substring(URI_PREFIX.length() + 1));
            return Triple.of(null, null, null);
        }
        GeoGson geojson = results[0].geojson;
        String type = geojson.type;
        List<List<Point>> boundaries = new ArrayList<>();
        if (type.equals("MultiPolygon")) {
            double[][][][] coordinates = GSON.fromJson(geojson.coordinates, double[][][][].class);
            for (double[][][] polygon : coordinates) {
                boundaries.add(parsePolygon(polygon));
            }
        } else if (type.equals("Polygon")) {
            double[][][] polygon = GSON.fromJson(geojson.coordinates, double[][][].class);
            boundaries.add(parsePolygon(polygon));
        }
        if (boundaries.size() == 0)
            boundaries.add(List.of(new Point((int) results[0].lon, (int) results[0].lat)));

        return Triple.of(results[0].lon, results[0].lat, new MultiPolygon(boundaries));
    }

    /**
     * Parse 2-D array representation of polygon to List representation.
     * 
     * @param polygon 2-D array representation of polygon.
     * @return List representation of polygon.
     */
    private static List<Point> parsePolygon(double[][][] polygon) {
        List<Point> res = new ArrayList<>();
        for (double[][] c1 : polygon) {
            for (double[] c : c1) {
                res.add(new Point((int) (c[0] * 10000), - (int) (c[1]) * 10000));
            }
        }
        return res;
    }

    /**
     * Object representing the query result.
     */
    private static class QueryResult {
        /**
         * Longitude.
         */
        private double lon;

        /**
         * Latitude.
         */
        private double lat;
        
        /**
         * GeoGson representation of polygons.
         */
        private GeoGson geojson;
    }

    /**
     * GeoGson representation of polygon.
     */
    private static class GeoGson {
        /**
         * Type of polygon.
         */
        private String type;

        /**
         * Coordinates composing the polygon.
         */
        private JsonElement coordinates;
    }
}
