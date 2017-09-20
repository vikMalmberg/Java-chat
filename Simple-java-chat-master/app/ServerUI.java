package app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Simpel UI för att starta igång server
 * @author Mikael, Viktor, Daniel, Fredrik, Emil
 *
 */
public class ServerUI extends JPanel implements ActionListener {
	private JButton btn = new JButton("Starta servern!");
	private Server server;
	private int port;

	/**
	 * Konstruktor som får in port	 
	 * @param port
	 */
	public ServerUI(int port) {
		
		this.port = port;
		server = new Server(port, this);
		
		setPreferredSize(new Dimension(320,160));
		add(btn);
		
		btn.addActionListener(this);
	}
	
	/**
	 * ActionListener på knapp
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btn){
			new ConnectNow().start();
			System.out.println("Tryck på knapp registrerad!");
		}
		
	}
	
	/**
	 * Starta server! 
	 * @author Mikael, Daniel, Viktor, Fredrik, Emil
	 *
	 */
	class ConnectNow extends Thread {
		public void run() {
			server.start(); 
			server = null;
		}
	}

}
