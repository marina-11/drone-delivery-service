package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

/**
 * Creates a Landmark object
 */
public class Landmark {
    private Point point;
    private Location location;

    /**
     * Class constructor specifying the landmarks.
     * @param feature the landmark represented as a feature.
     */
    public Landmark(Feature feature) {
        Geometry geometry = feature.geometry();
        Point point = (Point) geometry;
        this.point = point;
        assert this.point != null;
        this.location = new Location(this.point.coordinates().get(0), this.point.coordinates().get(1));
    }

    /**
     * @return location representing the landmark's position.
     */
    protected Location getLocation() { return this.location; }

    /**
     * @return 	a GeoJson Point representing the landmark.
     */
    protected Point getPoint() {
        return this.point;
    }
}
