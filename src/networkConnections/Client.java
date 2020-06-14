package networkConnections;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class Client {

	private Hashtable<String, Socket> sockets;
	private Hashtable<String, ObjectOutputStream> objectOuts;
	private Hashtable<String, ObjectInputStream> objectIns;

	private String clientName;

	public Client(String clientName, ArrayList<String> hosts, ArrayList<Integer> ports, int nbElements)
			throws ClassNotFoundException, IOException, InterruptedException {

		this.clientName = clientName;
		sockets = new Hashtable<String, Socket>();
		objectOuts = new Hashtable<String, ObjectOutputStream>();
		objectIns = new Hashtable<String, ObjectInputStream>();

		System.out.println("---> " + clientName + " client intializing ...");

		// nouvelle socket
		for (int i = 0; i < nbElements; i++) {
			boolean connected = false;
			int maxNbTry = 10;
			int compteur = 0;

			Socket socket = null;
			while (!connected && compteur < maxNbTry) {
				try {
					socket = new Socket(hosts.get(i), ports.get(i));
					connected = true;
				} catch (IOException e) {
					System.out.println("client failed to connect, try again in 3 seconds...");
					Thread.sleep(3000);
				}
				compteur += 1;
			}

			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = socket.getInputStream();

			// communication objet
			ObjectOutputStream objectOut = new ObjectOutputStream(outputStream);
			objectOut.flush();
			ObjectInputStream objectIn = new ObjectInputStream(inputStream);

			// envoie clientName et recoit serverName
			objectOut.writeObject(clientName);
			objectOut.flush();
			String serverName = (String) objectIn.readObject();

			sockets.put(serverName, socket);
			objectOuts.put(serverName, objectOut);
			objectIns.put(serverName, objectIn);

			System.out.println("- " + serverName + " connected successfully to the client");
		}

		System.out.println("---> All servers connected succesfully");
	}

	public String getClientName() {
		return clientName;
	}

	public void sendObject(String serverName, Object object) throws IOException {
		objectOuts.get(serverName).writeObject(object);
		objectOuts.get(serverName).flush();
	}

	public Object receiveObject(String serverName) throws ClassNotFoundException, IOException {
		Object object = objectIns.get(serverName).readObject();
		return object;
	}

	public void close() throws IOException {
		sockets.forEach((serverName, socket) -> {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}