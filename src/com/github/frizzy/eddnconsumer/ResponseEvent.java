package com.github.frizzy.eddnconsumer;

/**
 * @author Frizzy
 * @version HIP 1000
 * @since HIP 1000
 */
public class ResponseEvent {

    /**
     * The received response.
     */
    private final String response;

    /**
     * The timestamp the event occurred at.
     */
    private final String timeStamp;

    /**
     *
     */
    public ResponseEvent ( final String response, final String timeStamp) {
        this.response = response;
        this.timeStamp = timeStamp;
    }

    /**
     * Returns the response attached to the event.
     */
    public final String getResponse ( ) {
        return response;
    }

    /**
     * Returns the timestamp attached to the event/
     */
    public final String getTimeStamp ( ) {
        return timeStamp;
    }
}
