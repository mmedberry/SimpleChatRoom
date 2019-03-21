package simplechatroom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {
	private String username;
	private Socket socket = null;
	private Scanner scanner = null;
	private DataOutputStream out = null;
	private DataInputStream in = null;
	private boolean disconnect;

	public ChatClient(String address, int port) {
		try {
			// sets up new socket at indicated address and port
			socket = new Socket(address, port);
			// initializes scanner for user input
			scanner = new Scanner(System.in);
			// initializes io for client-server
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());

		}

		// catches exceptions in connecting
		catch (UnknownHostException u) {
			System.out.println("unable to connect to server");
		} catch (IOException i) {
			System.out.println("read/write failed");
		}

		disconnect = false;
		System.out.println("Enter username: ");
		username = scanner.nextLine();
		System.out.println("Enter first message to connect to server: ");

		// creates new threads for io
		Thread receiveThread = new ReceiveThread();
		Thread sendThread = new SendThread();
		receiveThread.start();
		sendThread.start();

	}

	/**
	 * Private class to create a thread to receive data from server
	 * 
	 * @author Max
	 *
	 */
	private class ReceiveThread extends Thread {
		public void run() {
			while (true) {

				try {
					if (disconnect) {
						return;
					}
					if (in.available() > 0) {
						String message = in.readUTF();
						if (message.contains("Server message: username unavailable.")) {
							username = message.substring(message.indexOf(username));
						} 
						System.out.println(message);

					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} 

			}
		}
	}

	/**
	 * Private class for creating a thread to send data to server
	 * 
	 * @author Max
	 *
	 */
	private class SendThread extends Thread {
		public void run() {
			while (true) {
				try {
					// get next message
					String message = scanner.nextLine();
					message = username + ": " + message;
					// sends message plus username to server
					out.writeUTF(message);
					
					if (message.contains(".disconnect")) {
						disconnect = true;
						return;
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				
			}
		}
	}

	public static void main(String[] args) {
		ChatClient cc = new ChatClient("127.0.0.1", 4444);
	}
}
