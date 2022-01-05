package uk.ac.ed.inf;

import java.sql.SQLException;


public class App 
{
    public static void main( String[] args ) throws InterruptedException, SQLException {
        BuildFlightpath.setUp(args);
        BuildFlightpath.buildMap();
    }
}

