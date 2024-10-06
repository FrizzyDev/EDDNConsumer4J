package com.github.frizzy.eddnconsumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * <p>
 * Exports EDDN responses to a file within a specified location.
 * </p>
 * <br>
 * <p>
 * ResponseExporter is intended to be used from within a thread, so responses from EDDN can be
 * both inserted and removed from a Queue at different times.
 * </p>
 *
 * @author Frizzy
 * @version HIP 1000
 * @since HIP 1000
 */
public class ResponseExporter {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger ( );

    /**
     * The directory the response files are stored.
     */
    private final File outLocation;

    /**
     * The gson json parser to determine the message event within the
     * json response.
     */
    private final Gson parser;

    /**
     * Total exported responses.
     */
    private int totalExported = 0;

    /**
     * Constructs the ResponseExporter. Throws an IllegalArgumentException if the outLocation
     * File is not a directory or does not exist.
     */
    public ResponseExporter ( File outLocation ) throws IllegalArgumentException {
        if ( !outLocation.isDirectory ( ) ) {
            throw new IllegalArgumentException ( "File outLocation is not a directory" );
        }
        if ( !outLocation.exists ( ) ) {
            throw new IllegalArgumentException ( "File outLocation does not exist." );
        }

        this.outLocation = outLocation;
        LOGGER.info ( "outLocation set to: {}" , outLocation );
        this.parser = new Gson ( );
    }

    /**
     * Exports the provided EDDN response, provided it contains a message and the message
     * contains the event type.
     *
     * @return The path of the exported json file.
     *
     * @throws IOException Thrown if createNewFile() or Files.writeString() fails.
     */
    public synchronized String export ( final String response ) throws IOException {

        JsonObject object = parser.fromJson ( response , JsonObject.class );
        JsonObject message = object.getAsJsonObject ( "message" );

        if ( message.has ( "event" ) ) {
            String event = message.get ( "event" ).getAsString ( );
            LOGGER.info ( "Message object contains event: {}" , event );

            File outFile = new File ( outLocation + File.separator + event + "-" + System.nanoTime ( ) + ".json" );

            if ( !outFile.exists ( ) ) {
                boolean created = outFile.createNewFile ( );

                if ( created ) {
                    /*
                     * Using Files.writeString instead of parser.toJson because for some reason
                     * outputting the json object via gson cuts off the output in the file.
                     */
                    Files.writeString ( outFile.toPath ( ) , response );
                    totalExported++;
                    return outFile.getAbsolutePath ( );
                } else {
                    LOGGER.warn ( "outFile: {} was not created" , outFile );
                }
            }
        }


        return "No export";
    }

    /**
     * Returns the total amount of responses exported to files.
     */
    public int getTotalExported ( ) {
        return totalExported;
    }
}
