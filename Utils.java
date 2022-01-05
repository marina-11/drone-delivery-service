package uk.ac.ed.inf;

import java.sql.SQLException;
import java.util.*;
import org.javatuples.Pair;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Contains methods that help in the overall construction of the drone control algorithm
 */
public class Utils {

    protected static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Check if the LongLat point is within the drone's confinement area.
     *
     * @param point a Location object that represents a point.
     * @return true if the LongLat point is within the confinement area, false otherwise.
     */
    protected static boolean isConfined(Location point) {
        return (point.getLatitude() > Constants.LATITUDE_SOUTH_BOUNDARY) && (point.getLatitude() < Constants.LATITUDE_NORTH_BOUNDARY)
                && (point.getLongitude() > Constants.LONGITUDE_WEST_BOUNDARY) && (point.getLongitude() < Constants.LONGITUDE_EAST_BOUNDARY);
    }

    /**
     * Calculates the Euclidean distance between 2 locations that takes
     * as a parameter and returns the result.
     *
     * @param p1 a Location object that represents point 1.
     * @param p2 a Location object that represents point 2.
     * @return the Euclidean distance between the two points.
     *
     * @throws IllegalArgumentException if a point is null.
     */
    protected static double getDistance(Location p1, Location p2) throws IllegalArgumentException {
        if (p1 == null || p2 == null) throw new IllegalArgumentException("Points must be not null");
        double x1 = p1.getLongitude();
        double y1 = p1.getLatitude();
        double x2 = p2.getLongitude();
        double y2 = p2.getLatitude();
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }


    /**
     * Checks if the two points given as arguments are close, where in the
     * context of this project, 2 points are 'close to' each other if the
     * distance between them is strictly less than the distance tolerance.
     *
     * @param p1 a Location object that represents point 1.
     * @param p2 a Location object that represents point 2.
     * @return true if the points are close to each other, false otherwise.
     */
    protected static boolean isClose(Location p1, Location p2) {
        return getDistance(p1, p2) < Constants.DISTANCE_TOLERANCE;
    }

    /**
     * Checks if the line starting at 'start' and finishing at
     * 'end' intersects with any of the no fly zones.
     *
     * @param start starting location of the drone's move
     * @param end   ending location of the drone's move
     * @return      true if it intersects, false otherwise
     */
    protected static boolean doesIntersectWithNoFlyZones(Location start, Location end) {
        Coordinate[] coordinates = new Coordinate[] {start.getJtsCoordinate(), end.getJtsCoordinate()};
        LineString line = Utils.geometryFactory.createLineString(coordinates);
        for (NoFlyZone zone : BuildFlightpath.noFlyZones) {
            if (line.intersects(zone.getJtsPolygon())) return true;
        }
        return false;
    }

    /**
     * Returns every possible move the drone could perform
     * from a given location, i.e. moves of an angle that
     * is a multiple of 10, and moves that the endLocation
     * is still in the confined area.
     *
     * @param start  current location of the drone
     * @return       list of possible moves it could perform
     */
    protected static List<Pair<Integer, Location>> getPossibleMoves(Location start) {
        List<Pair<Integer, Location>> possibleMoves = new ArrayList<>();
        for (int angle = 0; angle <= Constants.MAX_ANGLE_ALLOWED; angle += 10) {
            Location endLocation = nextPosition(start, angle);
            Pair<Integer, Location> move = new Pair<>(angle, endLocation);
            possibleMoves.add(move);
        }
        possibleMoves.removeIf(pair -> !isConfined(pair.getValue1()));
        return possibleMoves;
    }


    /**
     * Calculates the longitude and latitude of the position of the
     * drone if it would make a move in the direction of the angle
     * given as a parameter.
     *
     * @param angle
     * @return a Location object that represents the point of the next
     *         position of the drone if it makes a move in the direction
     *         of the angle taken as a parameter.
     *
     * @throws IllegalArgumentException if the angle is (1) a negative number
     *                                  other than the junk value -999 that
     *                                  denotes that the drone is hovering,
     *                                  (2) greater than 350 degrees,
     *                                  (3) and not a multiple of 10.
     */
    protected static Location nextPosition(Location currentPos, int angle) {
        if (angle == Constants.HOVER_ANGLE) {
            return new Location(currentPos.getLongitude(), currentPos.getLatitude());

        } else if (angle >= 0 && angle <= Constants.MAX_ANGLE_ALLOWED && angle % 10 == 0) {
            double angle_rad = Math.toRadians(angle);
            var nextLatitude = currentPos.getLatitude() + Constants.MOVE_LENGTH * Math.sin(angle_rad);
            var nextLongitude = currentPos.getLongitude() + Constants.MOVE_LENGTH * Math.cos(angle_rad);
            Location nextPosition = new Location(nextLongitude, nextLatitude);
            return nextPosition;

        } else {
            throw new IllegalArgumentException("Angles greater than 350 degrees, angles that are not " +
                    "multiples of 10, or negative angles other than -999 to indicate hovering, are invalid.");
        }
    }

    /**
     * Returns the closest landmark in proximity to the location
     * we provide as argument.
     *
     * @param goalLocation
     * @return closest landmark to location provided as an argument
     */
    protected static Landmark getClosestLandmark(Location goalLocation) {
        Landmark closestLandmark = null;
        double minDistance = Double.MAX_VALUE;
        for (Landmark landmark : BuildFlightpath.landmarks) {
            double distanceToLandmark = getDistance(goalLocation, landmark.getLocation());
            if (distanceToLandmark < minDistance) {
                minDistance = distanceToLandmark;
                closestLandmark = landmark;
            }
        }
        return closestLandmark;
    }

    /**
     * Using the possible moves the drone could make, it returns
     * the one which gets closer to the location the drone has as
     * a target.
     *
     * @param startLocation     starting location of the drone
     * @param goalLocation      location the drone needs to get to
     * @param moveNumber		number of the move
     * @param possibleMoves      moves the drone could do without
     * 							 leaving the confined area
     * @return the optimal move as a Move object
     */
    protected static Move getOptimalMove(Location startLocation, Location goalLocation,
                                         Integer moveNumber, List<Pair<Integer, Location>> possibleMoves) {
        System.out.println("We are in getOptimalMove method.");
        Move optimalMove = null;
        var minDistance = Double.MAX_VALUE;
        for (Pair<Integer, Location> move : possibleMoves) {
            var endLocation = move.getValue1();
            //System.out.println("Checking all possible moves from start location..");
            var distanceFromMoveToGoalLoc = getDistance(endLocation, goalLocation);
            if (distanceFromMoveToGoalLoc < minDistance ) {
                minDistance = distanceFromMoveToGoalLoc;
                optimalMove = new Move(startLocation, endLocation, moveNumber, move.getValue0());
            }
        }
        System.out.println("Optimal move from start location to goal location is found");
        assert optimalMove != null;
        return optimalMove;
    }


    /**
     * Calculates the cost of each order including the base cost
     * of 50p for the delivery.
     *
     * @param items a String of variable length which includes the items
     *              in the order.
     * @return an integer which represents the cost of the order in pence.
     */
    protected static int getDeliveryCost(List<Shop> shops, List<String> items) {
        int cost = Constants.DELIVERY_COST;                                //Base cost is 50p for delivery cost.

        for (String item : items) {
            for (Shop shop : shops) {
                for (Shop.ItemAndCost itemAndCost : shop.menu) {
                    if (itemAndCost.item.equals(item)) {
                        cost += itemAndCost.pence;               //Add to the base cost for every item ordered.
                        break;
                    }
                }
            }
        }
        return cost;
    }

    /**
     * Returns the shops that are involved in each order.
     *
     * @param shops all the shops we can have orders from.
     * @param items items that need to be delivered in an order.
     * @return
     */
    protected static List<Shop> getShopsInOrder(List<Shop> shops, List<String> items) {
        List<Shop> shopsInOrder = new ArrayList<>();
        for (String item : items) {
            for (Shop shop : shops) {
                for (Shop.ItemAndCost itemAndCost : shop.menu) {
                    //if the item we need to deliver is in that shop's menu and if the particular
                    //is not already in our shopsInOrder list, then we add it.
                    if (itemAndCost.item.equals(item)) {
                        if (shopsInOrder.isEmpty()) {
                            shopsInOrder.add(shop);
                        } else {
                            //Here we assume that there can't be more than 2 shops in an order as the
                            // restrictions on the delivery scheme say.
                            if (!shopsInOrder.contains(shop)) {
                                shopsInOrder.add(shop);
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (shopsInOrder.size() > 2) {
            System.err.println("Cannot place an order that includes items from more than 2 shops.");
        }
        return shopsInOrder;
    }



    /**
     * Method to calculate the value or metric of each order that is to be used when prioritising which
     * orders are to be delivered first in order to get a high sampled average percentage
     * monetary value. The value is calculated by dividing the total cost of the order by the
     * total Euclidean distance between the locations that need to be visited for that order.
     *
     * @param shops all the shops a customer can order from as a list of Shop objects
     * @param items the items in the particular order we are working with represented
     *              by a list of String
     * @param order the order as an Order object
     * @param currentLocation the current location of the drone
     * @return the cost to total distance ratio value
     *
     * @throws InterruptedException
     */
    protected static double getValue(List<Shop> shops, List<String> items, Order order,
                                     Location currentLocation) throws InterruptedException {
        List<Shop> shopsInOrder = getShopsInOrder(shops, items);
        List<Location> goalLocationsInOrder = getGoalLocationsForOrder(order, shopsInOrder, currentLocation);
        double totalDistanceInOrder = 0;

        if (goalLocationsInOrder.size() == 2) {
            totalDistanceInOrder = getDistance(currentLocation, goalLocationsInOrder.get(0))
                    + getDistance(goalLocationsInOrder.get(0), goalLocationsInOrder.get(1));
        } else if (goalLocationsInOrder.size() == 3) {
            totalDistanceInOrder = getDistance(currentLocation, goalLocationsInOrder.get(0))
                    + getDistance(goalLocationsInOrder.get(0), goalLocationsInOrder.get(1))
                    + getDistance(goalLocationsInOrder.get(1), goalLocationsInOrder.get(2));
        }
        double cost = getDeliveryCost(shopsInOrder, items);
        double value = cost / totalDistanceInOrder;
        return value;
    }


    /**
     * Returns linked list of orders in greedy order given the
     * starting position. In other words, it takes prioritizes
     * the order that has the highest cost to total distance
     * ratio (or value) from the current location. First the
     * ratio is calculated from the initial position, then from
     * the delivery point of the last orderin the linked list.
     *
     *
     * @param shops          all the shops a customer can order
     *                       from as a list of Shop objects
     * @param orders         all the orders placed on that given date
     * @param currentLocation  location from which the drone starts
     * @return  Queue with the orders to execute in order
     * @throws InterruptedException
     */
    protected static Queue<Order> getOrdersInLine(List<Shop> shops, List<Order> orders, Location currentLocation)
            throws InterruptedException, SQLException {
        var ordersInLine = new LinkedList<Order>();

        while (!orders.isEmpty()) {
            Order highestValueOrder = null;
            int highestValueOrderIdx = -1;
            double maxValue = Double.MIN_VALUE;

            for (int i = 0; i < orders.size(); i++) {
                var currentOrder = orders.get(i);
                var itemsInOrder = DatabaseConnection.getItemsInOrder(currentOrder);
                var valueOfCurrentOrder = getValue(shops, itemsInOrder, currentOrder, currentLocation);

                if (valueOfCurrentOrder > maxValue) {
                    maxValue = valueOfCurrentOrder;
                    highestValueOrderIdx = i;
                    highestValueOrder = currentOrder;
                }
            }

            assert highestValueOrder != null;
            currentLocation = highestValueOrder.getDeliveryLocation();
            ordersInLine.add(highestValueOrder);
            orders.remove(highestValueOrderIdx);
        }

        return ordersInLine;
    }



    /**
     * Returns a list of the key-locations that need to be visited
     * when executing a particular order. The locations are indexed
     * in order of priority.
     *
     * @param order the order we are currently considering
     * @param currentLocation the location that the drone is currently at
     *
     * @return the list of Location objects that represent the locations that
     * the drone needs to visit in indexed order to execute the particular order
     *
     * @throws InterruptedException
     */
    protected static List<Location> getGoalLocationsForOrder(Order order, List<Shop> shopsInOrder, Location currentLocation) throws InterruptedException {
        List<Location> goalLocationsForOrder = new ArrayList<>();
        //List<Shop> shopsInOrder = order.getShopsInOrder();
        if (shopsInOrder.size() == 1) {
            goalLocationsForOrder.add(shopsInOrder.get(0).getShopLocation());
        } else {
            Location l0 = shopsInOrder.get(0).getShopLocation();
            double d0 = getDistance(currentLocation, l0);
            Location l1 = shopsInOrder.get(1).getShopLocation();
            double d1 = getDistance(currentLocation, l1);
            if (d1>d0) {
                goalLocationsForOrder.add(shopsInOrder.get(0).getShopLocation());
                goalLocationsForOrder.add(shopsInOrder.get(1).getShopLocation());
            } else {
                goalLocationsForOrder.add(shopsInOrder.get(1).getShopLocation());
                goalLocationsForOrder.add(shopsInOrder.get(0).getShopLocation());
            }
        }
        goalLocationsForOrder.add(order.getDeliveryLocation());
        return goalLocationsForOrder;
    }
}
