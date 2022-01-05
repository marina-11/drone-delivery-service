package uk.ac.ed.inf;

/**
 * Contains all global constants.
 */
public class Constants {
    protected static final String MACHINE_NAME = "localhost:";
    protected static final String SERVER_PROTOCOL = "http://";
    protected static final String DB_PROTOCOL = "jdbc:derby://";
    protected static final String DELIVERIES = "DELIVERIES";
    protected static final String FLIGHTPATH = "FLIGHTPATH";
    protected static final double APPLETON_LNG = -3.186874;
    protected static final double APPLETON_LAT = 55.944494;
    protected static final Location START_LOCATION = new Location(APPLETON_LNG, APPLETON_LAT);
    protected static final double LONGITUDE_WEST_BOUNDARY =  -3.192473;
    protected static final double LONGITUDE_EAST_BOUNDARY = -3.184319;
    protected static final double LATITUDE_SOUTH_BOUNDARY = 55.942617;
    protected static final double LATITUDE_NORTH_BOUNDARY = 55.946233;
    protected static final double MOVE_LENGTH = 0.00015;
    protected static final double DISTANCE_TOLERANCE = 0.00015;
    protected static final int MAX_MOVES = 1500;
    protected static final int HOVER_ANGLE = -999;
    protected static final int MAX_ANGLE_ALLOWED = 350;
    protected static final int DELIVERY_COST = 50;
}
