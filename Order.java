package uk.ac.ed.inf;
import java.sql.Date;
import java.util.List;


/**
 * Creates an Order object which contains any information we may need for an order
 */
public class Order {
    private String orderNo;
    private Date date;
    private String matricNo;
    private String deliveryLocation;
    private List<Location> goalLocations;
    private List<Shop> shopsInOrder;
    private List<String> itemsToBeDelivered;
    private int cost;

    /**
     * @return unique order number
     */
    protected String getOrderNo() { return this.orderNo; }

    /**
     * @return date that the order has been placed
     */
    protected Date getDate() {return  this.date;}

    /**
     * @return matriculation number of the customer who
     *         placed the order
     */
    protected String getMatricNo() { return this.matricNo; }

    /**
     * @return What3Words address of the delivery location
     */
    protected String get3WordsAddress() { return this.deliveryLocation; }

    /**
     * Returns the delivery location using the field containing
     * the What3Words encoding after parsing it in order to get
     * the location as an instance of our Location class.
     *
     * @return delivery location of the order
     */
    protected Location getDeliveryLocation() {
        WordsAddress.Coordinates deliveryCoordinates = ServerRequest.getWordsAddress(this.deliveryLocation).coordinates;
        double latitude = deliveryCoordinates.lat;
        double longitude = deliveryCoordinates.lng;
        Location location = new Location(longitude, latitude);
        return location;
    }

    /**
     * @return list of the locations the drone needs to visit for the
     *         particular order in priority ranking.
     *         i.e. orderedShops + delivery location
     */
    protected List<Location> getGoalLocations() { return this.goalLocations; }

    /**
     * @return list of the shops the drone needs to visit for the
     *         particular order.
     */
    protected List<Shop> getShopsInOrder() { return this.shopsInOrder; }

    /**
     * @return list of strings of the names of the items that need
     *          to be delivered
     */
    protected List<String> getItemsToBeDelivered() { return this.itemsToBeDelivered; }

    /**
     * @return the cost of the order including the 50p charge for delivery.
     */
    protected int getCost() { return  this.cost; }

    /**
     * @param orderNo unique order number
     */
    protected void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    /**
     * @param date date the order has been placed for
     */
    protected void setDate(Date date) { this.date = date; }

    /**
     * @param matricNo matriculation number of the customer placing the order
     */
    protected void setMatricNo(String matricNo) { this.matricNo = matricNo; }

    /**
     * @param deliveryLocation a What3Words address representing the location
     *                          the order needs to be delivered at.
     */
    protected void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }

    /**
     * @param goalLocations list of Locations representing the locations the drone
     *                      needs to visit in indexed order to complete the order
     */
    protected void setGoalLocations(List<Location> goalLocations) { this.goalLocations = goalLocations; }

    /**
     * @param shopsInOrder the shops that the drone needs to visit to
     *                            collect the items requested.
     */
    protected void setShopsInOrder(List<Shop> shopsInOrder) { this.shopsInOrder = shopsInOrder; }

    /**
     * @param itemsToBeDelivered list of strings representing the items that
     *                           have been ordered
     */
    protected void setItemsToBeDelivered(List<String> itemsToBeDelivered) { this.itemsToBeDelivered = itemsToBeDelivered; }

    /**
     * @param cost the total cost of the order including the 50p charge for delivery
     */
    protected void setCost(int cost) { this.cost = cost; }

}
