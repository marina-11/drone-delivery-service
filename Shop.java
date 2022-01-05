package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * Shop class encompasses all details regarding a shop, including their menus,
 * that have been deserialized from the JSON list menus.json.
 */
public class Shop {
    String name;
    String location;
    ArrayList<ItemAndCost> menu;

    /**
     * ItemAndCost class encompasses all details regarding an
     * item in the menu of a shop, including an item's name and
     * cost. The object has been deserialized from the JSON
     * list 'menu' in menus.json.
     */
    public static class ItemAndCost {
        String item;
        int pence;
    }

    /**
     * Returns the location of the shop using the field
     * containing the What3Words encoding after parsing it
     * in order to get the location as an instance of our
     * Location class.
     *
     * @return location representing the shop's position
     */
    protected Location getShopLocation() {
        WordsAddress.Coordinates shopCoordinates = ServerRequest.getWordsAddress(location).coordinates;
        double latitude = shopCoordinates.lat;
        double longitude = shopCoordinates.lng;
        Location location = new Location(longitude, latitude);
        return location;
    }
}
