package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import org.locationtech.jts.geom.Coordinate;

/**
 * Creates a Location object and includes method to create a JTS Point from a GeoJson point
 */
public class Location {
    private double longitude;
    private double latitude;
    private Point point;

    /**
     * Class constructor specifying latitude and longitude
     * of the point we want to represent.
     * @param longitude the longitude of the point
     * @param latitude the latitude of the point
     */
    protected Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.point = Point.fromLngLat(this.longitude, this.latitude);
    }

    /**
     * Returns a GeoJson point using the coordinates
     * from the class' fields.
     * @return GeoJson point with the specified latitude
     * 		   and longitude.
     */
    protected Point getPoint() {
        return this.point;
    }

    /**
     * @return latitude of the specified point.
     */
    protected double getLatitude() {
        return this.latitude;
    }

    /**
     * @return longitude of the specified point.
     */
    protected double getLongitude() {
        return this.longitude;
    }

    /**
     * Returns a Coordinate object using the class'
     * fields so that jts-core dependency can be used with it.
     * @return the coordinate of the point with the specified
     * 		   latitude and longitude.
     */
    protected Coordinate getJtsCoordinate() {
        return new Coordinate(this.longitude, this.latitude);
    }


}
