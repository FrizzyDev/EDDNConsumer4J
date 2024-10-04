package com.github.frizzy.eddnconsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


/**
 * Pulled from EDDN github tests and modified to work in EDDNConsumer.
 */
public class EddnPump extends Thread {

    /**
     *
     */
    private static final Logger LOGGER = LogManager.getLogger ( );

    /**
     *
     */
    public static final String SCHEMA_KEY = "$schemaRef";

    /**
     *
     */
    public static final String RELAY = "tcp://eddn.edcd.io:9500";

    /**
     * A list of ResponseListeners that should be notified
     * when an EDDN response is received.
     */
    private final List<ResponseListener> listeners;

    /**
     * Boolean flag determining if the loop within the pump thread
     * should run.
     */
    private final AtomicBoolean active = new AtomicBoolean ( true );

    /**
     *
     */
    public EddnPump ( ) {
        listeners = Collections.synchronizedList ( new ArrayList<> ( ) );
    }

    /**
     * Adds the specified ResponseListener to the EddnPump listeners list.
     */
    public void addResponseListener ( final ResponseListener listener ) {
        listeners.add ( listener );
    }

    /**
     * Removes the specified ResponseListener from the EddnPump listeners list.
     */
    @SuppressWarnings ( "unused" )
    public void removeResponseListener ( final ResponseListener listener ) {
        synchronized ( listeners ) {
            listeners.remove ( listener );
        }
    }

    /**
     * Sets the active flag for the pump thread.
     */
    public void setActive ( boolean active ) {
        this.active.set ( active );
        LOGGER.info ( "Set active flag to: {}", active );
    }

    /**
     * Returns if the pump thread should be active or not.
     * Note: If this returns false, it doesn't necessarily mean the thread is
     * not active.
     */
    public synchronized boolean getActive ( ) {
        return active.get ();
    }

    /**
     * Runs the pump thread.
     */
    @Override
    public void run ( ) {
        pump ( );
    }

    /**
     * Connects to the EDDN service and polls for events.
     * Polled events are then wrapped in an ResponseEvent and
     * ResponseListeners are notified of that event.
     */
    public synchronized void pump ( ) {
        ZContext ctx = new ZContext ( );
        ZMQ.Socket client = ctx.createSocket ( ZMQ.SUB );
        client.subscribe ( "".getBytes ( ) );
        client.setReceiveTimeOut ( 30000 );

        client.connect ( RELAY );
        ZMQ.Poller poller = ctx.createPoller ( 2 );
        poller.register ( client, ZMQ.Poller.POLLIN );
        byte[] output = new byte[ 256 * 1024 ];

        while ( active.get () ) {
            LOGGER.info ( "Attempting to receive response" );
            int poll = poller.poll ( 10 );
            if ( poll == ZMQ.Poller.POLLIN ) {
                ZMQ.PollItem item = poller.getItem ( poll );

                if ( poller.pollin ( 0 ) ) {
                    byte[] recv = client.recv ( ZMQ.NOBLOCK );
                    if ( recv.length > 0 ) {
                        // decompress
                        Inflater inflater = new Inflater ( );
                        inflater.setInput ( recv );
                        try {
                            int outlen = inflater.inflate ( output );
                            String outputString = new String ( output, 0, outlen, StandardCharsets.UTF_8 );
                            // outputString contains a json message

                            if ( outputString.contains ( SCHEMA_KEY ) ) {
                                LOGGER.info ( "Got response" );

                                synchronized ( listeners ) {
                                    for ( ResponseListener rl : listeners ) {
                                        rl.responseReceived ( new ResponseEvent ( outputString, LocalDateTime.now ( ).toString ( ) ) );
                                    }
                                }
                            }

                        } catch ( DataFormatException e ) {
                            LOGGER.error ( "An error occurred receiving a response.", e );
                        }
                    }
                }
            }
        }

        boolean disconnected = client.disconnect ( RELAY );
        LOGGER.info ( "Client disconnected status: {}", disconnected  );
    }
}