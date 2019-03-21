package simplechatroom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
	private ArrayList<User> userList;
	int numGen;

	ChatServer() {

		userList = new ArrayList<User>();
		numGen = 0;
		ServerSocket server = null;
		try {
			server = new ServerSocket(4444);
			System.out.println("Server running on localhost");
		} catch (IOException e) {
			System.out.println("unable to utilize port");
		}

		while (true) {
			Socket socket = null;
			try {
				socket = server.accept();
				Thread clientThread = new ClientThread(socket);
				clientThread.start();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		}

	}

	private class ClientThread extends Thread {
		private DataInputStream in;
		private Socket socket;
		private String username;

		ClientThread(Socket socket) {
			try {
				this.socket = socket;
				this.in = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			boolean firstRun = true;
			while (true) {
				try {
					if (in.available() > 0) {
						String message = in.readUTF();
						if (firstRun) {
							username = message.substring(0, message.indexOf(":"));
							boolean contains = false;
							if (userList.size() > 0) {
								for (int i = 0; i < userList.size(); i++) {
									if (userList.get(i).getUsername().equals(username)) {
										contains = true;
									}
								}
							}
							if (contains) {
								System.out.println("User: " + username + " has tried to join the server with an unavailable name. Reassigning with username: " + username + numGen);
								username = username + numGen;
								DataOutputStream out = new DataOutputStream(socket.getOutputStream());
								out.writeUTF(
										"Server message: username unavailable. Your username has been reassigned to: "
												+ username);
								message = message.substring(message.indexOf(":"));
								message = username+message;
								numGen++;
							}
							userList.add(new User(username, socket));
							System.out.println(username + " has joined the server");
							firstRun = false;
						}
						System.out.println(message);
						if (message.contains(".disconnect")) {
							disconnect();
							return;
						}
						for (int i = 0; i < userList.size(); i++) {
							if (!userList.get(i).getUsername().equals(username)) {
								DataOutputStream out = new DataOutputStream(
										userList.get(i).getSocket().getOutputStream());
								out.writeUTF(message);
							}
						}
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
		public void disconnect() {
			
			for (int i = 0; i < userList.size(); i++) {
				if (userList.get(i).getUsername().equals(username)) {
					System.out.println("User: " + username + " has disconnected");
					userList.remove(i);
				}
			}
			try {
				in.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class User {
		private String username;
		private Socket socket;

		User(String username, Socket socket) {
			this.username = username;
			this.socket = socket;
		}

		public Socket getSocket() {
			return socket;
		}

		public String getUsername() {
			return username;
		}

	}

	public static void main(String[] args) {
		ChatServer cs = new ChatServer();
	}
}
