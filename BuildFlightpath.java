package uk.ac.ed.inf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.mapbox.geojson.*;
import org.javatuples.Pair;

/**
 * Includes the main functionality for the drone control algorithm and controls the movements of the drone
 */
public class BuildFlightpath {

    //List of te no-fly zones
    protected static List<NoFlyZone> noFlyZones;
    //List of the Landmarks
    protected static List<Landmark> landmarks;
    //List of all the shops.
    protected static List<Shop> shops;
    //List of all the orders in the date we are considering.
    protected static List<Order> orders;

    //Moves the drone is considering.
    protected static List<Move> moves = new ArrayList<>();
    //Moves the drone is considering to get to the starting
    // position from the last order's delivery point.
    protected static List<Move> movesToStart = new ArrayList<>();
    //GeoJson points the drone is considering.
    protected static List<Point> points = new ArrayList<>();
    //GeoJson points the drone is considering from last
    // order's delivery location to starting position.
    protected static List<Point> pointsToStart = new ArrayList<>();

    //The starting location of the drone when it considers a
    // different goal location, ie. the starting location
    // when moving between places.
    protected static Location startingPlace;

    //The landmark to be used as as intermediate step for
    // the drone in order to go around the no-fly zones
    protected static Landmark landmarkToBeUsed;;

    //list of orders already completed by the drone.
    private static final ArrayList<Order> completedOrders = new ArrayList<>();
    //GeoJson points the drone is committed to.
    private static final List<Point> pointsCommitted = new ArrayList<>();
    //Moves the drone has committed to.
    private static final List<Move> chosenMoves = new ArrayList<>();

    private static ServerRequest server;
    private static DatabaseConnection database;

    protected static int totalNumberOfOrders;
    protected static double monetaryValueOfAllOrders;

    //dummy move number to pass to getOptimalMove method.
    protected static int dummyMoveNo = 5;
    //Number (No.) of moves made while moving from one key-location to another.
    protected static int betweenPlacesMoveNo;
    //Number of moves the drone is actually making / committing to.
    private static int moveNo;
    //Number of moves needed to get to the starting position from
    // the last order's delivery point.
    private static int moveNoNeededToStart;
    //Number of moves needed to complete a given order.
    private static int moveNoNeeded;

    //Current (actual) location of the drone.
    private static Location currentLocation;
    //Hypothetical location of the drone when considering different optimal moves.
    protected static Location pretendCurrentLocation;

    //Queue of orders in order of execution. Ordered using cost to distance ratio
    private static Queue<Order> ordersInLine;
    //Order we are currently executing.
    private static Order currentOrder;
    //Shops drone needs to visit in current order.
    private static List<Shop> shopsInOrder;

    //The shops the drone needs to visit in order of closest proximity from current location
    //private static List<Shop> orderedShops;

    //Key-locations that the drone must visit for current order lined first to last.
    protected static List<Location> goalLocationsForOrder;

    protected static List<Landmark> visitedLandmarks = new ArrayList<>();


    /**
     * Sets up the server, the database and we save the data
     * retrieved from them in variables. We set up the order
     * that is going to be executed first
     *
     * @param args command line arguments
     *
     * @throws InterruptedException
     * @throws SQLException
     */
    protected static void setUp(String[] args) throws InterruptedException, SQLException {
		System.out.println("Setting up server...");
        server = new ServerRequest(args);
        System.out.println("Server set up");
        System.out.println("Setting up database connection...");
        database = new DatabaseConnection(args);
        System.out.println("Database connection set up");
        System.out.println("Checking if deliveries or flightpath tables already exist");
        //Create 'deliveries' and 'flightpath' tables
        DatabaseConnection.createTables();
        System.out.println("Database tables created");

        noFlyZones = server.getNoFlyZones();
        landmarks = server.getLandmarks();
        shops = server.getShops();
        System.out.println("Shops, no-fly zones and landmarks have been been obtained from server");

        currentLocation = Constants.START_LOCATION;
        pointsCommitted.add(currentLocation.getPoint());

        orders = database.getOrders(IO.date);
        totalNumberOfOrders = orders.size();
        monetaryValueOfAllOrders = getMonetaryValue(orders);
        for (Order order  : orders) {
            shopsInOrder = Utils.getShopsInOrder(shops, DatabaseConnection.getItemsInOrder(order));
            order.setCost(Utils.getDeliveryCost(shopsInOrder, DatabaseConnection.getItemsInOrder(order)));
        }
        System.out.println("Orders for the date requested have been obtained from the database");
        System.out.println("Total cost for each order has been calculated");

        ordersInLine = Utils.getOrdersInLine(shops, orders, currentLocation);
        System.out.println("Number of orders in queue initially: " + ordersInLine.size());
        System.out.println("Orders have been queued in order of maximum cost / distance from drone's current location");
        currentOrder = ordersInLine.poll();
        prepareOrder(currentOrder, currentLocation);


        landmarkToBeUsed = Utils.getClosestLandmark(currentLocation);
    }

    protected static void prepareOrder(Order order, Location currLocation) throws SQLException, InterruptedException {
        assert order != null;
        shopsInOrder = Utils.getShopsInOrder(shops, DatabaseConnection.getItemsInOrder(order));
        //orderedShops = Utils.getOrderedShops(shopsInOrder, currentLocation);
        order.setShopsInOrder(shopsInOrder);
        goalLocationsForOrder = Utils.getGoalLocationsForOrder(order, shopsInOrder, currLocation);
        currentOrder.setGoalLocations(goalLocationsForOrder);
        currentOrder.setItemsToBeDelivered(DatabaseConnection.getItemsInOrder(order));
    }

    protected static void printOrdersInLine(Queue<Order> orders, Location currentLocation) throws SQLException, InterruptedException {
        while (!orders.isEmpty()) {
            Order order = orders.poll();
            shopsInOrder = Utils.getShopsInOrder(shops, DatabaseConnection.getItemsInOrder(order));
            //orderedShops = Utils.getOrderedShops(shopsInOrder, currentLocation);
            List<String> shopsList = new ArrayList<>();
            for (Shop shop : shopsInOrder) {
                shopsList.add(shop.toString());
            }
            currentLocation = order.getDeliveryLocation();
            System.out.println("OrderNo: " + order.getOrderNo());
            System.out.println("Shops: " + shopsList);
            System.out.println("Delivery Location: " + order.get3WordsAddress());
            System.out.println();
        }
    }

    /**
     * Adds a hovering move to the list of non-final moves and the
     * point of the ending location of that move to the list of non-final
     * points of the drone's flightpath. Increases by 1 the betweenPlacesMoveNo
     * counter and the moveNoNeeded counter
     *
     * @param currLocation the drone's location
     */
    protected static void makeHoverMove(Location currLocation) {
        Move hoverMove = new Move(currLocation, currLocation, dummyMoveNo, Constants.HOVER_ANGLE);
        hoverMove.setAssociatedOrder(currentOrder);
        moves.add(hoverMove);
        points.add(hoverMove.getEndLocation().getPoint());
        betweenPlacesMoveNo++;
        moveNoNeeded++;
    }

    /**
     * Returns the most optimal move for the drone to take so
     * that it gets closer to the next goal location taking into
     * consideration the confined area.
     *
     * @return most optimal move
     */
    protected static Move makeGreedyMove(Location goalLocation, int moveNumber) {
        List<Pair<Integer, Location>> possibleMoves = Utils.getPossibleMoves(pretendCurrentLocation);
        Move optimalMove = Utils.getOptimalMove(pretendCurrentLocation, goalLocation, moveNumber, possibleMoves);
        return optimalMove;
    }

    protected static void commitRoute(int procedureCode) {
        if(procedureCode==0) {
            pointsCommitted.addAll(points);
            chosenMoves.addAll(moves);
            moveNo = moveNo + moveNoNeeded;
            //System.out.println("Move number is updated.");
            completedOrders.add(currentOrder);
            currentLocation = chosenMoves.get(chosenMoves.size() - 1).getEndLocation();
            pointsToStart.clear();
            movesToStart.clear();
            points.clear();
            moves.clear();
        } else if (procedureCode==1) {
            pointsCommitted.addAll(pointsToStart);
            chosenMoves.addAll(movesToStart);
            moveNo = moveNo + moveNoNeededToStart;
            currentLocation = Constants.START_LOCATION;
        }
    }

    /**
     * Decides if the drone should execute the next order or
     * go back to the starting position depending on the number
     * of moves needed to execute the next order and go to the
     * starting position after executing it.
     * Depending on the procedure.route chosen, different moves
     * and points are finalised and added to the lists of the drone's
     * committed points and moves.
     * If the order is executed and the ordersInLine queue is still not
     * empty we get the next order and call inspectOrder.
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    protected static void makeNextProcedure() throws SQLException, InterruptedException {
        System.out.println("We are in the makeNextProcedure method.");
        if (moveNo + moveNoNeeded + moveNoNeededToStart <= Constants.MAX_MOVES) {
            //We have enough moves left to commit to making the next order
            commitRoute(0);
            if (!ordersInLine.isEmpty()) {
                currentOrder = ordersInLine.poll();
                prepareOrder(currentOrder, currentLocation);
                inspectOrder();
            } else {
                System.out.println("We have completed all orders for the day so we are moving to the start location.");
                commitRoute(1);
                System.out.println("Percentage monetary value = 1.0");
            }
        } else {
            System.out.println("There are not enough moves left to carry out the next order in line " +
                    "so we are moving to the start location.");
            commitRoute(1);
            System.out.println("Percentage Monetary value = " + getMonetaryValue(completedOrders) / monetaryValueOfAllOrders);
        }
    }


    /**
     * Method to get a list of hypothetical moves that the drone could perform.
     * This method is necessary to see if the drone is able to execute an order
     * before its battery runs out, and to see if the recommended route for the
     * drone when moving between 2 key-locations of an order runs into a no fly
     * zone in which case this method can find an alternative route using the
     * landmarks and zeroing the betweenPlacesMoveNo counter.
     *
     * @param startLocation the first key-location,such as a delivery point or a shop
     * @param goalLocation the goal location the drone needs to get to
     * @param procedureCode a code that shows what kind of procedure the drone is about
     *                      to execute. This can only take values of 0, 1, or 2
     */
    protected static void moveBetweenPlaces(Location startLocation, Location goalLocation, int procedureCode) {
        betweenPlacesMoveNo = 0;
        pretendCurrentLocation = startLocation;

        while (true) {
            System.out.println(pretendCurrentLocation);
            Move optimalMove = makeGreedyMove(goalLocation, dummyMoveNo);
            System.out.println("Checking if optimal move intersects no-fly zones.");
            if (Utils.doesIntersectWithNoFlyZones(optimalMove.getStartLocation(), optimalMove.getEndLocation())) {
                System.out.println("Optimal move does intersect perimeter of no-fly zone.");
                System.out.println("The moves used from previous checkpoint to the intersection is: " + betweenPlacesMoveNo);
                int movesRemoved = 0;
                while (movesRemoved < betweenPlacesMoveNo) {
                    moves.remove(moves.size() - 1);
                    movesRemoved++;
                }
                System.out.println("Moves from checkpoint to intersection have been removed.");
                int pointsRemoved = 0;
                while (pointsRemoved < betweenPlacesMoveNo) {
                    points.remove(points.size() - 1);
                    pointsRemoved++;
                }
                if (procedureCode==3) {
                    landmarks.remove(landmarkToBeUsed);
                }
                if (!landmarks.isEmpty()) {
                    landmarkToBeUsed = Utils.getClosestLandmark(goalLocation);
                    moveBetweenPlaces(startingPlace, landmarkToBeUsed.getLocation(), 3);
                    moveBetweenPlaces(startingPlace, goalLocation, 3);
                } else {
                    System.err.println("Landmarks are not useful for this task!");
                    System.exit(1);
                }

                moveNoNeeded = moveNoNeeded - betweenPlacesMoveNo;
                break;
            } else {
                pretendCurrentLocation = optimalMove.getEndLocation();
                optimalMove.setAssociatedOrder(currentOrder);
                if (procedureCode == 0 || procedureCode == 3) {
                    moves.add(optimalMove);
                    points.add(optimalMove.getEndLocation().getPoint());
                    moveNoNeeded++;
                    if (landmarks.size() != 2) {
                        landmarks = server.getLandmarks();
                    }
                } else {
                    movesToStart.add(optimalMove);
                    pointsToStart.add(optimalMove.getEndLocation().getPoint());
                    moveNoNeededToStart++;
                }
                betweenPlacesMoveNo++;
                double distanceToGoal = Utils.getDistance(pretendCurrentLocation, goalLocation);
                System.out.println(distanceToGoal);
                if (Utils.isClose(pretendCurrentLocation, goalLocation)) {
                    System.out.println("Pretend-current-location is close enough so we break out of " +
                            "the while loop to the next goal location in the for loop");
                    //if (!goalLocation.equals(landmarkToBeUsed.getLocation()) && procedureCode!=1) {
                    if (!goalLocation.equals(landmarkToBeUsed.getLocation())) {
                        makeHoverMove(pretendCurrentLocation);
                    }
                    startingPlace = pretendCurrentLocation;
                    break;
                }
            }
        }
    }

    /**
     * Sets the moveNoNeeded counter to 0 and inspects an order
     * by hypothetically carrying out the order by calling the
     * moveBetweenPlaces method with the procedure code 0, thus
     * increasing the moveNoNeeded counter while moving between
     * places. When we got the optimum route that executes the
     * order we then call gettingToStartPosition method.
     *
     * @throws InterruptedException
     * @throws SQLException
     */
    protected static void inspectOrder() throws InterruptedException, SQLException {
        moveNoNeeded = 0;
        if (currentOrder !=null) {
            System.out.println("next order in not null");
            pretendCurrentLocation = currentLocation;
            startingPlace = currentLocation;
            System.out.println("Pretend-current-location set to our current location");
            List<Location> orderStops = currentOrder.getGoalLocations();

            //System.out.println("Goal locations: " + orderStops);
            for (Location goalLocation : orderStops) {
                System.out.println("Location: " + goalLocation);
                moveBetweenPlaces(startingPlace, goalLocation,0);
            }
            System.out.println("End of inspecting order. Now moving to pretend-getting to start position.");
            gettingToStartPosition();
        }
    }

    /**
     * Sets the moveNoNeededToStart counter to zero and then
     * inspects the route to the starting position from the
     * drone's current position by calling the moveBetweenPlaces
     * method using procedure code 1 so that the counter
     * moveNoNeededToStart is increased.
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    protected static void gettingToStartPosition() throws SQLException, InterruptedException {
        moveNoNeededToStart = 0;
        pretendCurrentLocation = startingPlace;
        moveBetweenPlaces(startingPlace, Constants.START_LOCATION,1);

        System.out.println("End of gettingToStartPosition. Now moving to committing to the next procedure.");
        makeNextProcedure();
    }



    /**
     * Returns the corresponding GeoJson map taking into account the points
     * through which the drone has gone through so that we can render the
     * path as a LineString.
     *
     * @return json string of the corresponding map
     */
    protected static String createGeojsonMap() {
        LineString lineString = LineString.fromLngLats(pointsCommitted);
        List<Feature> features = new ArrayList<>();
        Feature line = Feature.fromGeometry((Geometry) lineString);
        features.add(line);

        FeatureCollection fc = FeatureCollection.fromFeatures(features);
        String jsonString = fc.toJson();
        return jsonString;
    }

    /**
     * Updates the database tables using the data stores in
     * the lists completedOrders and chosenMoves.
     *
     * @throws SQLException
     */
    protected static void updateTables() throws SQLException {
        DatabaseConnection.insertDeliveries(completedOrders);
        DatabaseConnection.insertMoves(chosenMoves);
    }

    /**
     * Calculates the monetary value, i.e. the total cost
     * of the orders given as an argument in pence.
     *
     * @param orders orders for which we want to get the total
     *               monetary value of.
     * @return the total monetary value of the orders in the list
     *         in pence as an integer.
     */
    protected static double getMonetaryValue(List<Order> orders) {
        double total = 0.0;
        for (Order order : orders) {
            total += order.getCost();
        }
        return total;
    }

    /**
     * Method which completes the map executing the orders,
     * creating a json string of the points the drone has
     * travelled to, writing that string to a geojson file,
     * and updating the 'deliveries' and 'flightpath' tables
     * in the database.
     *
     * @throws InterruptedException
     * @throws SQLException
     */
    protected static void buildMap() throws InterruptedException, SQLException {
        //printOrdersInLine(ordersInLine, currentLocation);
        inspectOrder();
		for (int i = 0; i < chosenMoves.size(); i++) {
		    chosenMoves.get(i).setMoveNumber(i);
        }
        String jsonString = createGeojsonMap();
        IO.writeReadingFile(jsonString);
        updateTables();
        //double percentageMonetaryValue = getMonetaryValue(completedOrders) / monetaryValueOfAllOrders;
        //System.out.println("Percentage monetary value: " + percentageMonetaryValue);
        System.out.println("Date: " + currentOrder.getDate().toString());

    }

}
