package networkConnections;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Client permettant de recevoir des reponses et d'envoyer des requetes aux
 * servers de toutes les machines de la simulation
 * 
 * @author o.boutry
 * 
 */
public class Client {

	/**
	 * Dictionnaire des sockets associees au nom de chaque machine
	 */
	private Hashtable<String, Socket> sockets;
	/**
	 * Dictionnaire des outputStreams associees au nom de chaque machine
	 */
	private Hashtable<String, ObjectOutputStream> objectOuts;
	/**
	 * Dictionnaire des inputStreams associees au nom de chaque machine
	 */
	private Hashtable<String, ObjectInputStream> objectIns;
	/**
	 * Le nom de la machine sur laquelle s'initialise le client
	 */
	private String clientName;

	/**
	 * <b>Constructeur du client</b>
	 * 
	 * Initialise les sockets et les I/O Streams pour chaque machine de la
	 * simulation
	 * 
	 * @param clientName Le nom de la machine sur laquelle le client s'initialise
	 * 
	 * @param hosts      La liste des adresses de toutes les machines
	 * 
	 * @param ports      La liste des ports TCP de toutes les machines
	 * 
	 * @param nbElements Le nombre de machines de la simulation
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * 
	 * @throws IOException            Failed or interrupted I/O operations
	 * 
	 * @throws InterruptedException   Thrown when a thread is waiting, sleeping, or
	 *                                otherwise occupied, and the thread is
	 *                                interrupted, either before or during the
	 *                                activity
	 * 
	 */
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

	/**
	 * Envoyer un objet en TCP
	 * 
	 * @param serverName Le nom de la machine destinatrice 
	 * @param object L'objet a envoyer 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void sendObject(String serverName, Object object) throws IOException {
		objectOuts.get(serverName).writeObject(object);
		objectOuts.get(serverName).flush();
	}

	/**
	 * Recevoir un objet en TCP
	 * 
	 * @param serverName Le nom de la machine emettrice
	 * 
	 * @return L'objet recu
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public Object receiveObject(String serverName) throws ClassNotFoundException, IOException {
		Object object = objectIns.get(serverName).readObject();
		return object;
	}

	/**
	 * Fermer toutes les sockets
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
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