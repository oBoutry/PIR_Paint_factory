package networkConnections;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Server permettant de recevoir des requetes et d'envoyer des reponses a toutes
 * les machines de la simulation
 * 
 * @author o.boutry
 * 
 */
public class Server {

	/**
	 * Sorcket server
	 */
	private ServerSocket serverSocket;
	/**
	 * Dictionnaire des sockets clients associees au nom de chaque machine
	 */
	private Hashtable<String, Socket> clientSockets;
	/**
	 * Dictionnaire des outputStreams associees au nom de chaque machine
	 */
	private Hashtable<String, ObjectOutputStream> objectOuts;
	/**
	 * Dictionnaire des inputStreams associees au nom de chaque machine
	 */
	private Hashtable<String, ObjectInputStream> objectIns;

	/**
	 * <b>Constructeur de server</b>
	 * 
	 * Initialise la socket server puis la socket client et les I/O Streams pour
	 * chaque machine de la simulation
	 * 
	 * @param serverName Le nom de la machine sur laquelle s'initialise le server
	 * 
	 * @param port       Port TCP de la machine sur laquelle s'initialise le server
	 * 
	 * @param nbElements Le nombre de machines de la simulation
	 * 
	 * @throws IOException            Failed or interrupted I/O operations
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 */
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

	/**
	 * Envoyer un objet en TCP
	 * 
	 * @param clientName Le nom du destinataire
	 * @param object     L'objet a envoyer
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void sendObject(String clientName, Object object) throws IOException {
		objectOuts.get(clientName).writeObject(object);
		objectOuts.get(clientName).flush();
	}

	/**
	 * Recevoir un objet en UDP
	 * 
	 * @param clientName nom de l'emetteur
	 * 
	 * @return L'objet recu
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * 
	 * @throws IOException            Failed or interrupted I/O operations
	 */
	public Object receiveObject(String clientName) throws ClassNotFoundException, IOException {
		Object object = objectIns.get(clientName).readObject();
		return object;
	}

	/**
	 * Fermer la socket server
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void close() throws IOException {
		serverSocket.close();
	}

}
