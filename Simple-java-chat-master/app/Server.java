package app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.ImageIcon;

/**
 * Server hanterar logik för uppkoppling till server. Servern startas med hjälp
 * av ServerUI där du kan koppla upp servern samt koppla ner denna.
 * @author Mikael, Viktor, Daniel, Fredrik, Emil
 *
 */
public class Server {
	private static int uniqueId; // ID för klient
	private ArrayList<ClientHandle> clientList;
	private ServerUI gui;
	private int port;
	private boolean keepOn;
	private Logger log;
	private FileHandler fh;
	private SimpleFormatter format;

	/**
	 * Konstruktor tar emot port och server-ui. Tidsformat skapas redan i
	 * konstruktor samt instansiering av ny ArrayList.
	 * @param port
	 * @param gui
	 */
	public Server(int port, ServerUI gui) {
		this.port = port;
		this.gui = gui;
		clientList = new ArrayList<ClientHandle>();
		// Skapar log för denna session
		setLog();
	}

	/**
	 * Metoden kör igång en ny connection mot server och lagrar klient i vår
	 * arrayList. Metoden hanterar även om man kopplar ifrån sig och tar bort
	 * klienten ur listan.
	 */
	public void start() {
		keepOn = true; // Boolean för att konstant lyssna
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Server up and running smooth!");
			logHandler("New session");
			while (keepOn) {
				Socket socket = serverSocket.accept(); // accept connection

				if (!keepOn)
					break;
				
				// Skapar tråd för klient och slänger in i lista
				ClientHandle t = new ClientHandle(socket);
				clientList.add(t);
				t.start();
			}

			try {
				serverSocket.close();
				for (int i = 0; i < clientList.size(); ++i) {
					ClientHandle tc = clientList.get(i);
					try {
						tc.inputStream.close();
						tc.outputStream.close();
						tc.socket.close();
					} catch (IOException ioE) {
					}
				}
			} catch (Exception e) {
			}
		}

		catch (IOException e) {
		}
	}
	
	/**
	 * Metod hanterar meddelande till client
	 * @param message - Message obj
	 */
	private synchronized void broadcast(Message message) {
		Message messageLf = message;

		for (int i = clientList.size(); --i >= 0;) {
			ClientHandle ct = clientList.get(i);

			try {
				if (!ct.writeMsg(messageLf)) {
					clientList.remove(i);
					System.out.println("Disconnected Client " + ct.username
							+ " removed from list.");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Metoden tar bort klient från listan
	 * @param id
	 */
	synchronized void remove(int id) {
		for (int i = 0; i < clientList.size(); ++i) {
			ClientHandle ct = clientList.get(i);

			if (ct.id == id) {
				clientList.remove(i);
				return;
			}
		}
	}
	
	/**
	 * Klassen hanterar varje klient i en tråd och lyssnar på inkommande meddelanden
	 * @author Mikael, Viktor, Daniel, Fredrik, Emil
	 *
	 */
	class ClientHandle extends Thread {
		Socket socket;
		ObjectInputStream inputStream;
		ObjectOutputStream outputStream;
		int id;
		String username;
		ImageIcon image;
		Message chatMsg;
		LinkedList<String> userList = new LinkedList<String>();
		Set<String> set = new HashSet<String>();
		Set<Message> offMsg = new HashSet<Message>();

		ClientHandle(Socket socket) {
			id = ++uniqueId;
			this.socket = socket;

			try {
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				username = (String) inputStream.readObject();
			}

			catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}

		public void run() {

			boolean keepOn = true;

			while (keepOn) {
				try {
					chatMsg = (Message) inputStream.readObject();
				}

				catch (IOException | ClassNotFoundException e) {
					break;	
				}
				
				// Switch-sats som kollar vilken typ av meddelande som ska hanteras
				switch (chatMsg.getType()) {
				case Message.MESSAGE:
					broadcast(new Message(Message.MESSAGE, username + ": "
							+ chatMsg.getMessage(), chatMsg.getImage(), chatMsg.getRecipient(), chatMsg.getSender()));
					logHandler(username + " has written: " + chatMsg.getMessage());
					if(chatMsg.getImage() != null) {
						logHandler(username + " has sent: " + chatMsg.getImage());
					}	
					break;
				case Message.LOGOUT:
					keepOn = false; // lyssnar ej längre
					broadcast(new Message(Message.USER, username
							+ "updateTheUser", null, "", chatMsg.getSender()));
					logHandler(username + " logged out");
					break;
				case Message.USER:
					logHandler(username + " has logged in");
					for (int i = 0; i < clientList.size(); ++i) {
						ClientHandle ct = clientList.get(i);
						broadcast(new Message(Message.USER, ct.username
								+ "USERNAME100", null, "", chatMsg.getSender()));
						
					}
					break;
				}
			}
			remove(id);
			close();

		}
		
		/**
		 * Metoden stänger alla inputs/outputs och socket
		 */
		private void close() {
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (Exception e) {
			}
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
			}
			;
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
			}
		}
		
		/**
		 * Metoden skriver meddelande till Client
		 * @param msg
		 * @return
		 * @throws IOException
		 */
		private boolean writeMsg(Message msg) throws IOException {
			if (!socket.isConnected()) {
				socket.close();
				return false;
			}
			try {
				outputStream.writeObject(msg);
			} catch (IOException e) {
			}
			return true;
		}
	}
	
	/**
	 * Metoden hantera vår Log på servern
	 */
	private void setLog() {
		try {
			log = Logger.getLogger("New Log");
			fh = new FileHandler("/Users/mikaelhorvath/desktop/logFile.txt");
			format  = new SimpleFormatter();
			log.addHandler(fh);
			fh.setFormatter(format);
		} catch (Exception e) {}
	}
	
	/**
	 * Sätter meddelande på loggen
	 * @param msg meddelande
	 */
	public void logHandler(String msg) {
		log.info(msg + "\n");
	}
}
