package networkConnections;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

public class Server {

	private ServerSocket serverSocket;
	private Hashtable<String, Socket> clientSockets;
	private Hashtable<String, ObjectOutputStream> objectOuts;
	private Hashtable<String, ObjectInputStream> objectIns;

	public Server(String serverName, int port, int nbElements) throws IOException, ClassNotFoundException {

		// initialisation Socket server
		serverSocket = new ServerSocket(port);

		System.out.println("---> " + serverName + " server initalizing ...");

		clientSockets = new Hashtable<String, Socket>();
		objectOuts = new Hashtable<String, ObjectOutputStream>();
		objectIns = new Hashtable<String, ObjectInputStream>();

		for (int i = 0; i < nbElements; i++) {

			// connection socket client
			Socket clientSocket = serverSocket.accept();

			InputStream inputStream = clientSocket.getInputStream();
			OutputStream outputStream = clientSocket.getOutputStream();

			// communication objet
			ObjectOutputStream objectOut = new ObjectOutputStream(outputStream);
			objectOut.flush();
			ObjectInputStream objectIn = new ObjectInputStream(inputStream);

			// recoit clientName et envoie serverName
			String clientName = (String) objectIn.readObject();
			objectOut.writeObject(serverName);
			objectOut.flush();
			System.out.println("- " + clientName + " connected successfully to the server");

			clientSockets.put(clientName, clientSocket);
			objectOuts.put(clientName, objectOut);
			objectIns.put(clientName, objectIn);

		}
		System.out.println("---> All clients connected successfully");
	}

	public void sendObject(String clientName, Object object) throws IOException {
		objectOuts.get(clientName).writeObject(object);
		objectOuts.get(clientName).flush();
	}

	public Object receiveObject(String clientName) throws ClassNotFoundException, IOException {
		Object object = objectIns.get(clientName).readObject();
		return object;
	}

	public void close() throws IOException {
		serverSocket.close();
	}

}
