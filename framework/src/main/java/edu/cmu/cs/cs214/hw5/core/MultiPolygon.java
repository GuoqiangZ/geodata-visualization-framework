package edu.cmu.cs.cs214.hw5.core;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

/**
 * MultiPolygon represents a set of contours.
 */
public final class MultiPolygon {
    
    /**
     * Right bound and left bound of MultiPolygon.
     */
    private final int maxX, minX;

    /**
     * Lower bound and upper bound of MultiPolygon.
     */
    private final int maxY, minY;

    /**
     * List of areas represented by Polygon class.
     */
    private final List<Polygon> polygons;

    /**
     * Initialize a MultiPolygon object.
     * 
     * @param points a collection of points composing this polygons.
     */
    MultiPolygon(List<List<Point>> points) {
        this.polygons = new ArrayList<>();
        int maxX = Integer.MIN_VALUE, minX = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE, minY = Integer.MAX_VALUE;
        for (List<Point> points1 : points) {
            for (Point point : points1) {
                maxX = Math.max(maxX, (int) point.getX());
                maxY = Math.max(maxY, (int) point.getY());
                minX = Math.min(minX, (int) point.getX());
                minY = Math.min(minY, (int) point.getY());
            }
            polygons.add(new Polygon(
                    points1.stream().mapToInt(p -> (int) p.getX()).toArray(),
                    points1.stream().mapToInt(p -> (int) p.getY()).toArray(),
                    points1.size()));
        }
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
    }

    /**
     * Return the right bound.
     * 
     * @return the right bound of MultiPolygon.
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Return the left bound.
     *
     * @return the left bound of MultiPolygon.
     */
    public int getMinX() {
        return minX;
    }

    /**
     * Return the lower bound.
     *
     * @return the lower bound of MultiPolygon.
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Return the upper bound.
     *
     * @return the upper bound of MultiPolygon.
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Return a list of polygons composing this MultiPolygon.
     * 
     * @return a list of polygons composing this MultiPolygon.
     */
    public List<Polygon> getPolygons() {
        return new ArrayList<>(polygons);
    }

    /**
     * Return a string representing the polygons.
     * 
     * @return a string representing the polygons.
     */
    @Override
    public String toString() {
        return polygons.toString();
    }
}
