package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;

/**
 * NoFlyZone class encompasses all details regarding zo-fly zones, and
 * contains functionality to obtain a no-fly zone as mapbox.geojson Polygon or
 * a JTS locationtech Polygon
 */
public class NoFlyZone {

    private com.mapbox.geojson.Polygon geoJsonPolygon;
    private org.locationtech.jts.geom.Polygon jtsPolygon;

    /**
     * Class constructor specifying the no-fly-zone to create.
     *
     * @param feature the no-fly-zone represented as a feature.
     */
    public NoFlyZone(Feature feature) {
        Geometry geometry = feature.geometry();
        Polygon polygon = (com.mapbox.geojson.Polygon) geometry;
        this.geoJsonPolygon = polygon;
        this.makeJtsPolygon();
    }

    /**
     * @return a GeoJson Polygon representing the no-fly-zone
     * from the given class
     */
    protected com.mapbox.geojson.Polygon getGeojsonPolygon() {
        return this.geoJsonPolygon;
    }


    /**
     * Method to convert the GeoJson Polygon representing the
     * no-fly-zone to a JTS Polygon.
     */
    protected void makeJtsPolygon() {
        List<Point> geoJsonPoints = this.geoJsonPolygon.coordinates().get(0);
        Coordinate[] outerZone = new Coordinate[geoJsonPoints.size()];
        for (int i = 0; i < geoJsonPoints.size(); i++) {
            Point point = geoJsonPoints.get(i);
            Coordinate coordinate = new Coordinate(point.longitude(), point.latitude());
            outerZone[i] = coordinate;
        }
        this.jtsPolygon = Utils.geometryFactory.createPolygon(outerZone);
    }

    /**
     * @return 	a JTS Polygon representing the no-fly-zone
     * 			from the given class
     */
    protected org.locationtech.jts.geom.Polygon getJtsPolygon() {
        return this.jtsPolygon;
    }
}