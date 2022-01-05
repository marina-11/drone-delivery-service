package uk.ac.ed.inf;

/**
 * Creates Move objects that hold important imformation
 */
public class Move {

    private Location start;
    private Location end;
    private int moveNumber;
    private int angle;
    private Order associatedOrder = null;

    /**
     * Class constructor to build a move from the given
     * arguments.
     *
     * @param start	  the start location of the move
     * @param end	  the end location achieved by the drone
     * 				  and that move
     * @param number  the move number
     * @param angle   the angle / direction used by the drone
     * 				  to move from start to end
     */
    protected Move(Location start, Location end, int number, int angle) {
        this.start = start;
        this.end = end;
        this.moveNumber = number;
        this.angle = angle;
    }

    /**
     * @return angle used by drone (as an int)
     */
    protected int getAngle() { return this.angle; }

    /**
     * @return number of the drone's move
     */
    protected int getMoveNumber() { return this.moveNumber; }

    /**
     * @return location from which the drone started
     * 		   in this move
     */
    protected Location getStartLocation() { return this.start; }

    /**
     * @return location of the drone when move ended.
     */
    protected Location getEndLocation() { return this.end; }

    /**
     * @return the order associated to that move. i.e. the order
     *         the drone was collecting or delivering while executing
     *         the move. This field is to be ignored in the moves
     *         while the drone is returning back to the start position.
     */
    protected Order getAssociatedOrder() { return this.associatedOrder; }

    /**
     * @param order that the drone is delivering or collecting
     *              when executing the move.
     */
    protected void setAssociatedOrder(Order order) { this.associatedOrder = order; }

    /**
     * @param moveNo the move number of the move.
     */
    protected void setMoveNumber(int moveNo) { this.moveNumber = moveNo; }
}
