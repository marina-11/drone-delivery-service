package uk.ac.ed.inf;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Parses the command line arguments and outputs the geojson file
 */
public class IO {
    protected static Date date;
    protected static String server_port;
    protected static String database_port;

    /**
     * Uses the command line arguments to set up the date, the starting location,
     * the seed and the port.
     *
     * @param args the arguments to be parsed
     * @throws ArrayIndexOutOfBoundsException if the given arguments
     * 										  are less than 5
     */
    protected static void parseArguments(String[] args) throws ArrayIndexOutOfBoundsException {
        if (args.length < 5) {
            String message = "\n\tNeed 5 command line arguments. Given: " +
                    args.length + " arguments in total";
            throw new ArrayIndexOutOfBoundsException(message);
        }

        date = new Date(args[0], args[1], args[2]);
        server_port = args[3];
        database_port = args[4];
    }

    /**
     * Creates the readings file corresponding to the date given with the command line arguments
     * and writes to it the json string representing the flightpath of the delivery drone.
     *
     * @param stringToWrite string which will be written to the
     * 						readings file
     */
    protected static void writeReadingFile(String stringToWrite) {
        String filename = "drone-" + date.getDay() + "-" + date.getMonth()
                + "-" + date.getYear() + ".geojson";
        try {
            FileWriter writer = new FileWriter(filename);
            writer.append(stringToWrite);
//	    	System.out.println("File " + filename + " created successfully.");
            writer.close();
        } catch (IOException e) {
            System.out.println("File could not be created");
            e.printStackTrace();
        }
    }


}
