// http://stackoverflow.com/questions/21287087/maintaining-communication-between-arduino-and-java-program
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server_Arduino extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;

	private static final int serverPort = 8892;

	private ServerSocket server;
	private Socket connection;
	private BufferedWriter output;
	private BufferedReader input;

	private String message = "";

	public Server_Arduino() {
		super("Semi-Autonomous User Guided Robot");
		makeMenus();

		// addKeyListener(this);
		// setFocusable(this);
		setFocusTraversalKeysEnabled(false);

		userText = new JTextField();
		userText.setEditable(false);
		/*
		 * userText.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent event) {
		 * sendMessage(event.getActionCommand()); userText.setText(""); } });
		 */
		userText.addKeyListener(new KeyListener() {

			// key pressed accounts for if key is released
			@Override
			public void keyPressed(KeyEvent e) {

				int c = e.getKeyCode();

				if (c == KeyEvent.VK_UP) {
					sendMessage("up");

				} else if (c == KeyEvent.VK_DOWN) {
					sendMessage("down");

				} else if (c == KeyEvent.VK_LEFT) {
					sendMessage("left");

				} else if (c == KeyEvent.VK_RIGHT) {
					sendMessage("right");
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
				sendMessage("stop");

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

		});

		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);

		setSize(1000, 900);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	public void makeMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu info = new JMenu("Info");
		menuBar.add(info);

		// about Robot overall
		JMenuItem about = info.add("About");
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								getContentPane(),
								new JLabel(
										"<html><ul> The Semi-Autonomous User Guided Robot is able to be controlled by a user via WiFi. <p> "
												+ " If the connection is lost the robot will continue to its destination via GPS. <p></html>"),
								"About Semi-Autonomous User Guided Robot",
								JOptionPane.PLAIN_MESSAGE);
			}
		});

		// help
		JMenuItem help = info.add("Help");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								getContentPane(),
								new JLabel(

										"<html><u>Directions for User Control </u><br>"
												+ " Hold down the given arrow key to move the robot. <br>Release the key to stop.<p>"
												+ "<ul> Move Forward : Up arrow  <p> <br>"
												+ "Move Backward : Down arrow <p><br>"
												+ "Turn Right : Right arrow <p><br>"
												+ "Turn Left : Left arrow <p> "
												+ "</html> "), "User Controls",
								JOptionPane.PLAIN_MESSAGE);
			}
		});
		// exit
		JMenuItem exit = info.add("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	public void startRunning() {
		try {

			server = new ServerSocket(serverPort, 100);
			while (true) {
				try {
					waitForConnection();
					setupStreams();
					whileConnected();
				} catch (EOFException eofException) {
					showMessage("Client terminated connection");
				} catch (IOException ioException) {
					showMessage("Could not connect...");
				} finally {
					closeStreams();
				}
			}

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}

	/*
	 * public void keyPressed(KeyEvent e) { int c = e.getKeyCode(); boolean
	 * keyPressed = true;
	 * 
	 * while (keyPressed) { if (c == KeyEvent.VK_LEFT) { message = "left"; }
	 * else if (c == KeyEvent.VK_RIGHT) { message = "right"; } else if (c ==
	 * KeyEvent.VK_UP) { message = "up"; } else if (c == KeyEvent.VK_DOWN) {
	 * message = "down"; } else if (c == KeyEvent.KEY_RELEASED) { keyPressed =
	 * false; } }
	 * 
	 * }
	 * 
	 * public void keyTyped(KeyEvent e) { }
	 * 
	 * public void keyReleased(KeyEvent e) { }
	 */
	private void waitForConnection() throws IOException {

		showMessage("Waiting for someone to connect...");
		connection = server.accept(); // once someone asks to connect, it
										// accepts the connection to the socket
										// this gets repeated fast
		showMessage("Now connected to "
				+ connection.getInetAddress().getHostName()); // shows IP adress

	}

	private void setupStreams() throws IOException {

		showMessage("creating streams...");
		output = new BufferedWriter(new OutputStreamWriter(
				connection.getOutputStream()));
		output.flush();
		input = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		showMessage("Streams are setup!");

	}

	private void whileConnected() throws IOException {

		ableToType(true); // makes the user able to type

		do {

			char x = (char) input.read();
			while (x != '\n') {
				message += x;
				x = (char) input.read();
			}
			showMessage(message);
			message = "";

		} while (!message.equals("END")); // if the user has not disconnected,
											// by sending "END"

	}

	private void closeStreams() {

		ableToType(false);

		showMessage("Closing streams...");
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void sendMessage(String message) {
		try {
			output.write(message + '\n');
			output.flush();
			showMessage("Sent: " + message);
		} catch (IOException ex) {
			chatWindow
					.append("\nSomething messed up whilst sending messages...");
		}

	}

	private void showMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatWindow.append('\n' + message);
			}
		});

	}

	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				userText.setEditable(tof);
			}
		});
	}
}