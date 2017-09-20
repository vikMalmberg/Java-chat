package app;

import javax.swing.JFrame;

/**
 * Startar klient
 * @author Mikael, Viktor, Daniel, Fredrik, Emil
 *
 */
public class StartClient {

	public static void main(String[] args) {
		
		ClientUI start = new ClientUI();
		
		JFrame view = new JFrame("AweSome Chat 1.0");
		view.add(start);
		view.setResizable(false);
		view.pack();
		view.setVisible(true);
		
	}

}
