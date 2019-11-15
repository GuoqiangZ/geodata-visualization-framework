package edu.cmu.cs.cs214.hw5.core;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MultiPolygon represents a set of contours.
 */
public final class MultiPolygon {

    static class ImmutablePoint2D extends Point2D.Double {

        ImmutablePoint2D(double x, double y) {
            super(x, y);
        }
        @Override
        public void setLocation(double x, double y) {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Right bound and left bound of MultiPolygon.
     */
    private final double maxX, minX;

    /**
     * Lower bound and upper bound of MultiPolygon.
     */
    private final double maxY, minY;

    /**
     * List of areas represented points.
     */
    private final List<List<Point2D>> polygons;

    /**
     * Initialize a MultiPolygon object.
     * 
     * @param points a collection of points composing this polygons.
     */
    MultiPolygon(List<List<Point2D>> points) {
        if (points == null)
            throw new NullPointerException();
        this.polygons = points;
        double maxX = Integer.MIN_VALUE, minX = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE, minY = Integer.MAX_VALUE;
        for (List<Point2D> polygon : polygons) {
            for (Point2D point : polygon) {
                maxX = Math.max(maxX, point.getX());
                maxY = Math.max(maxY, point.getY());
                minX = Math.min(minX, point.getX());
                minY = Math.min(minY, point.getY());
            }
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
    public double getMaxX() {
        return maxX;
    }

    /**
     * Return the left bound.
     *
     * @return the left bound of MultiPolygon.
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Return the lower bound.
     *
     * @return the lower bound of MultiPolygon.
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Return the upper bound.
     *
     * @return the upper bound of MultiPolygon.
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Return a list of polygons composing this MultiPolygon. Each polygon is a list of points.
     * 
     * @return a list of polygons composing this MultiPolygon.
     */
    public List<List<Point2D>> getPoints() {
        return polygons.stream().map(ArrayList::new).collect(Collectors.toList());
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
