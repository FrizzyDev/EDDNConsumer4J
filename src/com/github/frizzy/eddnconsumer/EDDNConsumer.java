package com.github.frizzy.eddnconsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Frizzy
 * @version HIP 1000
 * @since HIP 1000
 */
public class EDDNConsumer {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger ( );

    /**
     * Exports responses to files.
     */
    private ResponseExporter exporter;

    /**
     * The UI portion of EDDNConsumer.
     */
    private final EDDNUI ui;

    /**
     * The EddnPump creates a zeromq socket and
     * receives EDDN responses.
     */
    private final EddnPump pump;

    /**
     * The queue storing the EDDN responses.
     */
    private final LinkedBlockingQueue< String > queue;

    /**
     *
     */
    private final AtomicBoolean active = new AtomicBoolean ( true );

    /**
     * Initializes EDDNConsumer.
     */
    private EDDNConsumer ( ) {
        ui = new EDDNUI ( );
        File outLocation = selectLocation ( );

        if ( outLocation != null ) {
            exporter = new ResponseExporter ( outLocation );
        } else {
            LOGGER.error ( "File outLocation is null." );
            ui.showErrorDialog ( "File outLocation is null. \n EDDNConsumer will close." );
            System.exit ( -1 );
        }

        pump = new EddnPump ( );
        queue = new LinkedBlockingQueue<> ( );

        startConsumer ( );
    }

    /**
     *
     */
    private File selectLocation ( ) {
        File outLocation = ui.showFileChooser ( );

        if ( outLocation == null ) {
            ui.showErrorDialog ( "File outLocation cannot be null. Please select a folder." );
            selectLocation ( );
        }

        return outLocation;
    }

    /**
     * Starts the EDDNConsumer. A ResponseListener is created to listen
     * for ResponseEvents from EddnPump and both the ResponseExporter and
     * EddnPump threads are started.
     */
    private void startConsumer ( ) {

        ResponseListener listener = new ResponseListener ( ) {
            int received = 0; // Number of received responses

            @Override
            public void responseReceived ( ResponseEvent re ) {
                LOGGER.info ( "Received response" );
                String response = re.getResponse ( );

                queue.add ( response );
                received++;

                SwingUtilities.invokeLater ( ( ) -> {
                    ui.addNewResponse ( response );
                    ui.updateReceivedResponses ( received );
                } );
            }
        };
        pump.addResponseListener ( listener );

        Thread thread = new Thread ( ( ) -> {
            while ( active.get ( ) ) {
                //LOGGER.info ( "Polled" );

                try {
                    final String polled = queue.take ( );
                    try {
                        String exportedPath = exporter.export ( polled );

                        if ( !exportedPath.equals ( "No export" ) ) {//Double-checking to make sure the export was successful
                            SwingUtilities.invokeLater ( ( ) -> {
                                ui.updateExported ( "Exported: " + exportedPath );
                            } );
                        } else {
                            System.out.println ( "Returned No export" );
                        }

                        if ( queue.isEmpty () ) {
                            System.out.println ( "Queue is empty. You can stop the process." );
                        }

                    } catch ( IOException e ) {
                        LOGGER.error ( "An error occurred exporting a response." , e );
                        ui.showErrorDialog ( "An error occurred exporting a EDDN response." );
                    }

                } catch ( InterruptedException e ) {
                    LOGGER.error ( "Exception" , e );
                    ui.showErrorDialog ( "An InterruptedException occurred polling the queue. \n Exiting application." );
                    System.exit ( -1 );
                }

//                final boolean active = pump.getActive ( );
//
//                LOGGER.info ( "Pump state: {}. Now finishing up the exporting process." , active );
//                if ( queue.isEmpty ( ) && !active ) {
//                    /*
//                     * If pump is not active, that means YES_OPTION in the dialog
//                     * was pressed. We need to exit as soon as the queue is empty and
//                     * the exporter call finished.
//                     */
//                    LOGGER.info ( "Queue is empty and pump turned off, Exiting application." );
//                    SwingUtilities.invokeLater ( ( ) -> {
//                        ui.showDoneDialog ( exporter.getTotalExported ( ) );
//                        System.exit ( 0 );
//                    } );
//                }
            }
        } );

        pump.start ( );
        thread.start ( );


        SwingUtilities.invokeLater ( ( ) -> {
            int option = ui.showDialog ( );

            if ( option == JOptionPane.YES_OPTION ) {
                pump.setActive ( false );
            } else {
                System.exit ( 0 );
            }
        } );
    }

    /**
     * Starts EDDNConsumer.
     */
    public static void main ( String[] args ) {
        new EDDNConsumer ( );
    }
}
