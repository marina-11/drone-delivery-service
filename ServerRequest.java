package uk.ac.ed.inf;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.*;
import java.net.URI;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

/**
 * Obtains connection and data from the web server
 */
public class ServerRequest {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * Class constructor which makes sure to parse the
     * arguments given in the command line.
     *
     * @param args arguments from the command line
     */
    public ServerRequest(String[] args) {
        IO.parseArguments(args);
    }

    /**
     * Performs a get request with the given argument as the path connecting to our
     * WebServer in the port provided as a command line argument.
     *
     * Returns the result as a String which can be deserialized.
     *
     * @param path	path to be used for the get Request
     * @return      string containing the result of
     * 	 			the request
     */
    protected static String getRequest(String path) {
        String urlString = Constants.SERVER_PROTOCOL + Constants.MACHINE_NAME + IO.server_port + path;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        String responseBody = "";
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                responseBody = response.body();
            } else if (response.statusCode() == 404){
                System.err.println("Could not find anything");
            } else {
                System.err.println("Unable to connect to " + Constants.MACHINE_NAME+
                        " at port " + IO.server_port + ".");
            }
        } catch (ConnectException e) {
            System.err.println(e);
            System.err.println("Fatal error: Unable to connect to " + Constants.MACHINE_NAME
                    + " at port " + IO.server_port + ".");
            System.err.println("Have you checked the server is running?");
            System.exit(1); // Exit the application
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    /**
     * Returns a list of NoFlyZone members representing
     * each of the no fly zones obtained from the web server.
     *
     * @return list of no fly zones as NoFlyZone objects
     */
    protected List<NoFlyZone> getNoFlyZones() {
        String path = "/buildings/no-fly-zones.geojson";
        String source = getRequest(path);
        FeatureCollection fc = FeatureCollection.fromJson(source);
        List<Feature> features = fc.features();
        List<NoFlyZone> noFlyZones = new ArrayList<>();
        assert features != null;
        for (Feature feature : features) {
            NoFlyZone noFlyZone = new NoFlyZone(feature);
            noFlyZones.add(noFlyZone);
        }
        return noFlyZones;
    }

    /**
     * Returns a list of the 2 landmarks involved around the campus
     * represented by a Landmark object.
     *
     * @return a list of landmarks as Landmark objects.
     */
    protected List<Landmark> getLandmarks() {
        String path = "/buildings/landmarks.geojson";
        String source = getRequest(path);
        FeatureCollection fc = FeatureCollection.fromJson(source);
        List<Feature> features = fc.features();
        List<Landmark> landmarks = new ArrayList<>();
        assert features != null;
        for (Feature feature : features) {
            Landmark landmark = new Landmark(feature);
            landmarks.add(landmark);
        }
        return landmarks;
    }

    /**
     * Returns a list of the shops involved in the delivery scheme
     * obtained from the menus directory in the WebServer.
     *
     * @return a list of the shops involved in the delivery scheme
     *         as Shop objects
     */
    protected List<Shop> getShops() {
        String path = "/menus/menus.json";
        String source = getRequest(path);
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        List<Shop> shops = new Gson().fromJson(source, listType);
        return shops;
    }

    /**
     * Returns an object of the WordsDetails class representing
     * the address of the What3Words string given as argument after
     * deserialising it.
     *
     * @param words What3Words address
     * @return WordsAddress object corresponding to the What3Words address
     *         we got from the server
     */
    protected static WordsAddress getWordsAddress(String words) {
        String[] splittedWords = words.split("\\.");
        String path = "/words/" + splittedWords[0] + "/" + splittedWords[1]
                + "/" + splittedWords[2] + "/" + "details.json";

        String source = getRequest(path);

        WordsAddress address = new Gson().fromJson(source, WordsAddress.class);
        return address;
    }

}
