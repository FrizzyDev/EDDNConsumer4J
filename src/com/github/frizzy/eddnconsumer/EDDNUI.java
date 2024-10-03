package com.github.frizzy.eddnconsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

/**
 * Controls the UI aspect of EDDNConsumer.
 *
 * @author Frizzy
 * @version HIP 1000
 * @since HIP 1000
 */
public class EDDNUI {

    /**
     *
     */
    private final static Logger LOGGER = LogManager.getLogger ( );

    /**
     * The button values for the JOptionPane dialog.
     */
    private final Object[] buttonValues = { "Stop receiving", "Cancel" };

    /**
     * The String displayed as the title of the JOptionPane dialog.
     */
    private final String dialogTitle = "Receiving EDDN responses";

    /**
     * The dialog option int value used for the JOptionPane dialog.
     */
    private final int dialogOption = JOptionPane.YES_NO_OPTION;

    /**
     * The dialog message type int value used for the JOptionPane dialog.
     */
    private final int dialogType = JOptionPane.PLAIN_MESSAGE;

    /**
     * The EDDN icon displayed in the dialog.
     */
    private Icon dialogIcon;

    /**
     * The container for the message pane.
     */
    private final JPanel messagePaneContainer;

    /**
     * The label that displays total responses received.
     */
    private final JLabel responsesLabel;

    /**
     * The JEditorPane displayed as the message pane within the JOptionPane dialog.
     */
    private final JTextArea dialogMessagePane;

    /**
     * The JScrollPane for the JEditorPane
     */
    private final JScrollPane messagePaneScroller;

    /**
     * Constructs the EDDNUI.
     */
    public EDDNUI ( ) {
        try { //SET LaF to system
            UIManager.setLookAndFeel ( UIManager.getSystemLookAndFeelClassName ( ) );
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e ) {
            LOGGER.error ( "Exception occurred setting UI LaF", e );
        }

        try { //Try to load the dialog icon
            dialogIcon = new ImageIcon ( ImageIO.read ( EDDNUI.class.getResource ( "/com/github/frizzy/eddnconsumer/Resources/16920010.png" ) ) );
        } catch ( IOException e ) {
            LOGGER.error ( "Exception occurred loading icon.", e );
        }

        messagePaneContainer = new JPanel ( );
        responsesLabel = new JLabel ( "Responses Received: 0" ); //Default text
        dialogMessagePane = new JTextArea ( );
        messagePaneScroller = new JScrollPane ( dialogMessagePane );

        messagePaneContainer.setLayout ( new BorderLayout ( ) );
        messagePaneContainer.setMinimumSize ( new Dimension ( 700, 600 ) );
        messagePaneContainer.setPreferredSize ( new Dimension ( 700, 600 ) );
        messagePaneContainer.setSize ( new Dimension ( 700, 600 ) );
        messagePaneContainer.add ( responsesLabel, BorderLayout.PAGE_START );
        messagePaneContainer.add ( messagePaneScroller, BorderLayout.CENTER );

        dialogMessagePane.setLineWrap ( false ); //We want each response to contain into their own line
        dialogMessagePane.setEditable ( false ); //Does not need to be editable
        dialogMessagePane.setForeground ( Color.BLACK );
    }

    /**
     * <p>
     * Shows the file chooser that allows the user to select the outLocation File
     * for the Response Exporter.
     * </p>
     * <br>
     * <p>
     * The JFileChooser is set to Directories only for the file selection mode, to ensure
     * a folder is selected.
     * </p>
     */
    public File showFileChooser ( ) {
        JFileChooser chooser = new JFileChooser ( );
        chooser.setFileSelectionMode ( JFileChooser.DIRECTORIES_ONLY );

        int option = chooser.showOpenDialog ( null );

        if ( option == JFileChooser.APPROVE_OPTION ) {
            return chooser.getSelectedFile ( );
        } else {
            return null;
        }
    }

    /**
     * Shows the JOptionPane dialog and when closed, returns the option value.
     */
    public int showDialog ( ) {
        return JOptionPane.showOptionDialog ( null, messagePaneContainer, dialogTitle, dialogOption, dialogType, dialogIcon, buttonValues, null );
    }

    /**
     * Displays the provided error message.
     */
    public void showErrorDialog ( String message ) {
        JOptionPane.showMessageDialog ( null, message, "Error", JOptionPane.ERROR_MESSAGE );
    }

    /**
     * Displays the done dialog.
     */
    public void showDoneDialog ( ) {
        JOptionPane.showMessageDialog ( null, "EDDNConsumer is all done. Application will now close", "All done!", JOptionPane.INFORMATION_MESSAGE );
    }

    /**
     * Sets the responses label to the new value.
     */
    public void updateReceivedResponses ( final int newValue ) {
        responsesLabel.setText ( "Responses Received: " + newValue );
    }

    /**
     * Appends the provided response to the dialog JTextArea.
     */
    public void addNewResponse ( final String response ) {
        dialogMessagePane.append ( response + "\n" );
    }
}
