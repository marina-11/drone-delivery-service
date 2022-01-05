package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Obtains connection and data from the database
 */
public class DatabaseConnection {

    private static String jdbcString = Constants.DB_PROTOCOL + Constants.MACHINE_NAME + IO.database_port + "/derbyDB";
    private static Statement statement;

    /**
     * Class constructor which makes sure to parse the
     * arguments given in the command line so that we
     * get the port to the database we want to access
     * and the date we want to get the orders for.
     *
     * @param args arguments from the command line
     */
    public DatabaseConnection(String[] args) { IO.parseArguments(args); }

    /**
     * Gets and returns the connection to the database specified by the
     * jdbcString string.
     * @return connection to specified database
     * @throws SQLException
     */
    protected static Connection getConn() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcString);
        try {
        // Create a statement object that we can use for running various
        // SQL statement commands against the database.
            statement = conn.createStatement();
        } catch (SQLException e) {
            System.err.println(e);
            System.err.println("Fatal error: Unable to connect to database at port "
                    + IO.database_port + " of " + IO.server_port + ".");
            System.err.println("Have you checked the database is running?");
            System.exit(1); // Exit the application
        }
        return conn;
    }


    /**
     * Returns the orders placed on the date given as a parameter that
     * are stored on the database table called 'orders'.
     *
     * @param date
     * @return list of Order objects representing the orders placed on the
     *         date given as a parameter.
     * @throws SQLException
     */
    protected List<Order> getOrders(Date date) throws SQLException {
        final String orderQuery = "select * from orders where deliveryDate=(?)";
        PreparedStatement psOrderQuery = getConn().prepareStatement(orderQuery);
        psOrderQuery.setString(1, date.getYear()+"-"+date.getMonth()+"-"+date.getDay());

        List<Order> orderList = new ArrayList<>();
        ResultSet rs = psOrderQuery.executeQuery();
        while (rs.next()) {
            Order order = new Order();
            order.setDate(rs.getDate("deliveryDate"));
            order.setOrderNo(rs.getString("orderNo"));
            order.setMatricNo(rs.getString("customer"));
            order.setDeliveryLocation(rs.getString("deliverTo"));
            orderList.add(order);
        }
        return orderList;
    }

    /**
     * Returns a list of Strings that represent the items that have been
     * ordered through the specific order that has been given as a parameter.
     * @param order
     * @return a list of Strings that represent the items ordered
     * @throws SQLException
     */
    protected static List<String> getItemsInOrder(Order order) throws SQLException {
        final String itemsQuery = "select * from orderDetails where orderNo=(?)";
        PreparedStatement psItemsQuery = getConn().prepareStatement(itemsQuery);
        psItemsQuery.setString(1, order.getOrderNo());

        List<String> itemsList = new ArrayList<>();
        ResultSet rs = psItemsQuery.executeQuery();
        while (rs.next()) {
            String item = rs.getString("item");
            itemsList.add(item);
        }
        return itemsList;
    }

    /**
     * Method to check if the tables we want to create already exist.
     * If they already exist, we drop them, else we do nothing.
     * The tables we want to create are 'deliveries' and 'flightpath'.
     *
     * @throws SQLException
     */
    protected static void dropTablesIfExist() throws SQLException {
        DatabaseMetaData databaseMetadata = getConn().getMetaData();
        ResultSet resultSetDeliveries =  databaseMetadata.getTables(null, null, Constants.DELIVERIES, null);
        ResultSet resultSetFlightpath =  databaseMetadata.getTables(null, null, Constants.FLIGHTPATH, null);
        // If the resultSet is not empty then the table exists, so we can drop it
        if (resultSetDeliveries.next()) {
            statement.execute("drop table deliveries");
            System.out.println("Table deliveries exists and have been dropped");
        }
        if (resultSetFlightpath.next()) {
            statement.execute("drop table flightpath");
            System.out.println("Table flightpath exists and have been dropped");
        }
    }

    /**
     * Method to create the tables 'deliveries' and 'flightpath'.
     * Calls the dropTableIfItExists method to make sure that the
     * tables can be safely created.
     *
     * @throws SQLException
     */
    protected static void createTables() throws SQLException {
        dropTablesIfExist();

        statement.execute(
            "create table deliveries(" +
                "orderNo char(8), " +
                "deliveredTo varchar(19), " +
                "costInPence int)");

        statement.execute(
            "create table flightpath(" +
                "orderNo char(8), " +
                "fromLongitude double, " +
                "fromLatitude double, " +
                "angle integer, " +
                "toLongitude double, " +
                "toLatitude double)");

    }

    /**
     * Method to insert deliveries made by the drone into
     * the 'deliveries' table on the database.
     * @param deliveriesMade a list of Order objects of the orders
     *                       that have been successfully delivered
     *                       on that date.
     * @throws SQLException
     */
    protected static void insertDeliveries(List<Order> deliveriesMade) throws SQLException {
        PreparedStatement psDelivery = getConn().prepareStatement(
                "insert into deliveries values (?, ?, ?)");
        for (Order order : deliveriesMade) {
            psDelivery.setString(1, order.getOrderNo());
            psDelivery.setString(2, order.get3WordsAddress());
            psDelivery.setInt(3, order.getCost());
            psDelivery.execute();
        }
    }

    /**
     * Method to insert the moves made by the drone into the
     * 'flightpath' table on the database.
     * @param moves List of Move objects that represents all the moves
     *              that the drone has made on that given date.
     * @throws SQLException
     */
    protected static void insertMoves(List<Move> moves) throws SQLException {
        PreparedStatement psMove = getConn().prepareStatement(
                "insert into flightpath values (?, ?, ?, ?, ?, ?)");
        for (Move m : moves) {
            psMove.setString(1, m.getAssociatedOrder().getOrderNo());
            psMove.setDouble(2, m.getStartLocation().getLongitude());
            psMove.setDouble(3, m.getStartLocation().getLatitude());
            psMove.setInt(4, m.getAngle());
            psMove.setDouble(5, m.getEndLocation().getLongitude());
            psMove.setDouble(6, m.getEndLocation().getLatitude());
            psMove.execute();
        }

    }
}
