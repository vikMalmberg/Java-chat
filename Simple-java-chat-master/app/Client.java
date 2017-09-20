package app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.ImageIcon;

/**
 * Klassen hanterar logiken kring klient för uppkoppling mot server samt
 * meddelanden som skickas till server och tas emot av klient.
 * 
 * @author Mikael, Viktor, Daniel, Fredrik, Emil
 *
 */
public class Client {
	private String username;
	private int port;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Socket socket;
	private ClientUI client;

	/**
	 * Konstruktor tar emot användarnamn från UI samt vilket UI som gäller
	 * @param username
	 * @param client
	 */
	public Client(int port, String username, ClientUI client) {
		this.username = username;
		this.client = client;
		this.port = port;
	}

	/**
	 * Ansluter till server, skapar input/outputs från server Skickar vidare
	 * vårt användarnamn direkt vid anslutning
	 * 
	 * @return
	 */
	public boolean start() {
		try {
			socket = new Socket("127.0.0.1", port);
		} catch (Exception ec) {
		}

		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException eIO) {
		}

		new ServerListener().start(); // Startar tråd för att lyssna på meddelande

		try {
			outputStream.writeObject(username);
		} catch (IOException eIO) {
		}

		return true;
	}

	/**
	 * Skicka meddelanden till server
	 * @param msg
	 */
	void sendMessage(Message msg) {
		try {
			outputStream.writeObject(msg);
			outputStream.flush();
		} catch (IOException e) {
		}
	}

	/**
	 * Tråd som väntar på meddelanden från server och skriver ut dessa till
	 * klient
	 * @author Mikael, Viktor, Daniel, Fredrik, Emil
	 */
	class ServerListener extends Thread {
		ArrayList<String> arr;
		Set<String> set = new HashSet<String>();
		Set<Message> offMsg = new HashSet<Message>();
		ImageIcon image = null;
		Random rand = new Random();
		private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

		public void run() {

			while (true) {
				try {

					Message msg = (Message) inputStream.readObject();

					if (client == null) {
						System.out.println(msg);
						System.out.print("> ");
					} else {

						if (msg.getMessage() != null || msg.getImage() != null) {

							String message = null;
							ImageIcon img = null;

							if (msg.getImage() != null) {
								img = msg.getImage();
							}

							if (msg.getMessage() != null) {
								message = msg.getMessage();
							}

							if (message.contains("USERNAME100")) {
								// Splittar bort USERNAME100
								message = message.split("USERNAME100")[0];
								// Sätter ny HashSet
								set.add(message);
								// ArrayList<String> initieras med HashSet
								arr = new ArrayList<String>(set);
								// Skickar in Array till ClientUI
								client.updateUsers(arr);
								
							} else if (message.contains("updateTheUser")) {
								// Splittar ut updateTheUser
								String split = message.split("updateTheUser")[0];
								// Tar bort kvarvarande sträng
								set.remove(split);
								// Uppdaterar array med nytt set
								arr = new ArrayList<String>(set);
								// Skickar ny array till klient
								client.updateUsers(arr);
							} else {
								// SEND MESSAGE
								if (arr.contains(msg.getRecipient())) {
									String time = df.format(new Date());
									String messageLf = time + " " + message
											+ "\n";
									client.privateAppend(messageLf, img,
											msg.getRecipient(), msg.getSender());

								} 
								else if(!arr.contains(msg.getRecipient())){
									
									offMsg.add(msg);
									
								}
								else{
									String time = df.format(new Date());
									String messageLf = time + " " + message
											+ "\n";
									client.append(messageLf, img);
									img = null;
								}
							}
						}

					}
				} catch (IOException e) {
				}

				catch (ClassNotFoundException e2) {
					System.out.println("Något gick fel!");
				}
			}
		}
	}

}
