package app;

import java.io.Serializable;

import javax.swing.ImageIcon;

/**
 * Klassen sköter våra meddelanden. Meddelande instansieras med konstruktor
 * @author mikaelhorvath
 *
 */
public class Message implements Serializable {
	static final int USER = 0, MESSAGE = 1, LOGOUT = 2;
	private int type;
	private String message;
	private ImageIcon image = null; // Ska implementeras i denna klass!
	private String recipient;
	private String sender;
	private String[] recipients;
	
	Message(int type, String message, ImageIcon image, String recipient, String sender) {
		this.type = type;
		this.message = message;
		this.image = image;
		this.recipient = recipient;
		this.sender = sender;
	}
	
	/**
	 * Hämta typ
	 * @return type
	 */
	int getType() {
		return type;
	}
	
	/**
	 * Hämta meddelande
	 * @return message
	 */
	String getMessage() {
		return message;
	}
	
	/**
	 * Hämta mottagare
	 * @return recipient
	 */
	String getRecipient(){
		return recipient;
	}
	
	/**
	 * Hämta avsändare
	 * @return sender
	 */
	String getSender(){
		return sender;
	}
	
	/**
	 * Hämta bild
	 * @return image
	 */
	ImageIcon getImage() {
		return image;
	}

}
