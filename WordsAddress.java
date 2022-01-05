package uk.ac.ed.inf;

/**
 * Class to deserialize What3Words location from the
 * server and use it to get its coordinates.
 */
public class WordsAddress {

    String country;
    Square square;

    protected class Square {
        Coordinates southwest;
        Coordinates northeast;
    }

    String nearestPlace;
    Coordinates coordinates;

    protected class Coordinates {
        double lng;
        double lat;
    }

    String words;
    String language;
    String map;
}
