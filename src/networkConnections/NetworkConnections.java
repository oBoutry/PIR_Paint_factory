package networkConnections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import partToPaint.Part;

/**
 * Constitue l'ensemble des methodes de communication UDP et TCP. Chaque machine
 * de la simulation possede un objet networkConnections lui permettant de
 * communiquer avec toutes les autres machines.
 * 
 * 
 * On trouvera donc les methodes permettant d'envoyer et recevoir des objet en
 * UDP et en TCP et deux classes permettant de transformer des objets en bytes
 * et inversement
 * 
 * @author o.boutry
 * 
 */
public class NetworkConnections {

	/**
	 * Nom de la machine (un des noms parmis ceux presents dans le fichier texte
	 * "table_adresses_ports.txt")
	 */
	private String myName;
	/**
	 * Port TCP de la machine
	 */
	private int myTCPPort;
	/**
	 * Port UDP de la machine
	 */
	private int myUDPPort;
	/**
	 * Nombre de machines de la simulation
	 */
	private int nbElements;
	/**
	 * Listes des noms des machines auquelles on doit se connecter
	 */
	private ArrayList<String> elementNames = new ArrayList<String>();
	/**
	 * Listes des adresses des machines auquelles on doit se connecter
	 */
	private ArrayList<String> hosts = new ArrayList<String>();
	/**
	 * Listes des ports TCP des machines auquelles on doit se connecter
	 */
	private ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
	/**
	 * Listes des ports TCP des machines auquelles on doit se connecter
	 */
	private ArrayList<Integer> udpPorts = new ArrayList<Integer>();
	/**
	 * Server de la machine,voue à se connecter aux clients de toutes les autres
	 * machines
	 */
	private Server server;
	/**
	 * Client de la machine, voue à se connecter aux servers de toutes les autres
	 * machines
	 */
	private Client client;
	/**
	 * Sender UDP pour communication en temps reel
	 */
	private UDPSender udpSender;
	/**
	 * Receiver UDP pour communication en temps reel
	 */
	private UDPReceiver udpReceiver;
	/**
	 * Booleen valant true si on doit envoye une copie de la communication echangee
	 * à la machine "ExchangeMonitor"
	 */
	private boolean copyToExchangeMonitorRequired;

	/**
	 * <b>Constructeur de NetworkConnections</b>
	 * 
	 * Permet d'etablir toutes les connections entre machines. Toutes les machines
	 * attendent la machine la plus en retard, au niveau de l'initialisation du
	 * client.
	 * 
	 * @param elementName  Nom de la machine
	 * @param dataFileName Chemin vers le fichier texte contenant l'ensemble des
	 *                     noms, adresses, et ports des machines de la simulation
	 * @throws IOException            Dans le cas d'un probleme de socket
	 * @throws InterruptedException   Dans le cas d'une interruption du thread de
	 *                                lancement du serveur
	 * @throws ClassNotFoundException Dans le cas ou les objets test envoyes sont de
	 *                                type inconnu
	 */
	public NetworkConnections(String elementName, String dataFileName)
			throws IOException, InterruptedException, ClassNotFoundException {

		System.out.println("< Network Connections initializing ... >" + "\n");

		copyToExchangeMonitorRequired = false;

		myName = elementName;

		// get hosts, tcpPorts, myTCPPort, and nbElements from the data file
		getHostsAndPorts(elementName, dataFileName);

		// UDPSender initializing
		udpSender = new UDPSender();

		// UDPReceiver initializing
		udpReceiver = new UDPReceiver(myUDPPort);

		// server initializing
		Thread threadServer = new Thread() {
			public void run() {
				try {
					server = new Server(elementName, myTCPPort, nbElements);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		};
		threadServer.start();

		// Client initializing
		client = new Client(elementName, hosts, tcpPorts, nbElements);

		// Test UDP String
		sendAllUDP(elementName);
		System.out.println("* UDP message sent");
		for (int idElement = 0; idElement < nbElements; idElement++) {
			/// String message = new String(receiveUDP());
			String sender = (String) receiveUDP();
			System.out.println("* UDP message received successfully from " + sender);
		}

		// Test TCP Part
		System.out.println("- part sending TCP test");
		Part part = new Part("none", 0, 5, 20, 5);
		for (int idElement = 0; idElement < nbElements; idElement++) {
			sendRequest(elementNames.get(idElement), part);
		}
		System.out.println("Part sent TCP : " + part.toString());
		part.printMatrice();

		// Reception
		for (int idElement = 0; idElement < nbElements; idElement++) {
			Part part2 = (Part) receiveRequest(elementNames.get(idElement));
			System.out.println("- part received TCP fom " + elementNames.get(idElement) + " : " + part2.toString());
			part2.printMatrice();
		}

		// Test UDP Part
		sendAllUDP(part);
		System.out.println("* Part sent UDP : " + part.toString());
		part.printMatrice();
		// reception
		for (int idElement = 0; idElement < nbElements; idElement++) {
			Part part2 = (Part) receiveUDP();
			System.out.println("Part received UDP :" + part2);
			part2.printMatrice();
		}

		System.out.println("\n" + "< Network Connections initialized with success >" + "\n");
	}

	/**
	 * Fermer toutes les sockets en une seule fois
	 * 
	 * @throws IOException Dans le cas ou une erreur de socket survient
	 */
	public void closeAllConnections() throws IOException {
		server.close();
		client.close();
		udpReceiver.close();
		udpSender.close();
	}

	/**
	 * Recuperer l'ensemble des noms, adresses, et ports, de chaque machine
	 * 
	 * @param elementName  Correspondant au nom de la machine detenant l'objet
	 *                     networkConnections
	 * @param dataFileName Correspondant au chemin vers le fichier texte contenant
	 *                     l'ensemble des noms, adresses, et ports des machines de
	 *                     la simulation
	 * @throws FileNotFoundException Dans le cas ou dans le cas ou le fichier texte
	 *                               est absent ou son nom est errone
	 */
	public void getHostsAndPorts(String elementName, String dataFileName) throws FileNotFoundException {
		String[] mots = null;
		String lineString = new String();
		Scanner scanner = new Scanner(new File(dataFileName));
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			lineString = scanner.nextLine();
			mots = lineString.split(" ", 5);
			elementNames.add(mots[0]);
			hosts.add(mots[1]);
			tcpPorts.add(Integer.parseInt(mots[2]));
			udpPorts.add(Integer.parseInt(mots[3]));
		}
		scanner.close();
		int indexMyInfo = elementNames.indexOf(elementName);
		elementNames.remove(indexMyInfo);
		hosts.remove(indexMyInfo);
		myTCPPort = tcpPorts.remove(indexMyInfo);
		myUDPPort = udpPorts.remove(indexMyInfo);
		nbElements = elementNames.size();
	}

	/**
	 * Envoyer un objet au serveur d'une machine
	 * 
	 * @param elementName Correspondant au nom de la machine destinatrice
	 * @param object      Correspondant à l'objet que l'on souhaite envoyer à la
	 *                    machine elementName
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     TCP
	 */
	public void sendRequest(String elementName, Object object) throws IOException {
		client.sendObject(elementName, object);
		if (copyToExchangeMonitorRequired == true) {
			sendCopyToExchangeMonitor(elementName, object);
		}
	}

	/**
	 * Envoyer un objet au client d'une machine
	 * 
	 * @param elementName Correspondant au nom de la machine destinatrice
	 * @param object      Correspondant à l'objet que l'on souhaite envoyer à la
	 *                    machine elementName
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     TCP
	 */
	public void sendAnswer(String elementName, Object object) throws IOException {
		server.sendObject(elementName, object);
		if (copyToExchangeMonitorRequired == true) {
			sendCopyToExchangeMonitor(elementName, object);
		}
	}

	/**
	 * Recevoir depuis le serveur un objet venant de la machine elementName
	 * 
	 * @param elementName Correspondant au nom de la machine emettrice
	 * @throws IOException            Dans le cas ou une erreur à lieu lors de la
	 *                                communication TCP
	 * @throws ClassNotFoundException Dans le cas ou le type de l'objet envoye est
	 *                                inconnu
	 * @return Retourne l'objet recu
	 */

	public Object receiveRequest(String elementName) throws IOException, ClassNotFoundException {
		return server.receiveObject(elementName);
	}

	/**
	 * Recevoir depuis le client un objet venant de la machine elementName
	 * 
	 * @param elementName Correspondant au nom de la machine emettrice
	 * @throws IOException            Dans le cas ou une erreur à lieu lors de la
	 *                                communication TCP
	 * @throws ClassNotFoundException Dans le cas ou le type de l'objet envoye est
	 *                                inconnu
	 * @return Retourne l'objet recu
	 */
	public Object receiveAnswer(String elementName) throws IOException, ClassNotFoundException {
		return client.receiveObject(elementName);
	}

	/**
	 * Envoyer un objet sur les serveurs de toutes les machines
	 * 
	 * @param object Correspondant à l'objet que l'on souhaite envoyer sur les
	 *               serveurs de toutes les machines
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     TCP
	 */
	public void sendRequestAll(Object object) throws IOException {
		for (int idElement = 0; idElement < nbElements; idElement++) {
			client.sendObject(elementNames.get(idElement), object);
		}
	}

	/**
	 * Envoyer un objet sur les clients de toutes les machines
	 * 
	 * @param object Correspondant à l'objet que l'on souhaite envoyer sur les
	 *               clients de toutes les machines
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     TCP
	 */
	public void sendAnswerAll(Object object) throws IOException {
		for (int idElement = 0; idElement < nbElements; idElement++) {
			server.sendObject(elementNames.get(idElement), object);
		}
	}

	/**
	 * Envoyer un objet en UDP à la machine elementName
	 * 
	 * @param elementName           Correspondant au nom de la machine destinatrice
	 * 
	 * @param object                Correspondant à l'objet à envoyer à la machine
	 *                              elementName
	 * 
	 * @param copyToExchangeMonitor Indiquant si une copie de l'echange UDP doit
	 *                              être envoyee à la machine "ExchangeMonitor"
	 * 
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     UDP
	 */
	public void sendUDP(String elementName, Object object, boolean copyToExchangeMonitor) throws IOException {
		int indexElementInfo = elementNames.indexOf(elementName);
		udpSender.send(hosts.get(indexElementInfo), udpPorts.get(indexElementInfo), objectToByteArray(object));
		if (copyToExchangeMonitor == true && copyToExchangeMonitorRequired == true) {
			sendCopyToExchangeMonitor(elementName, object);
		}
	}

	/**
	 * Recevoir un objet en UDP
	 * 
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     TCP
	 * @return Retourne l'objet recu
	 */

	public Object receiveUDP() throws IOException {
		return byteArrayToObject(udpReceiver.receive());
	}

	/**
	 * Envoyer un objet en UDP à toutes les machines
	 * 
	 * @param object Correspondant à l'objet à envoyer à la machine elementName
	 * 
	 * @throws IOException Dans le cas ou une erreur à lieu lors de la communication
	 *                     UDP
	 */
	public void sendAllUDP(Object object) throws IOException {
		for (int idElement = 0; idElement < nbElements; idElement++) {
			sendUDP(elementNames.get(idElement), object, false);
		}
	}

	/**
	 * Convertir un objet en un tableau d'octets
	 * 
	 * @param object Correspondant à l'objet à convertir en un tableau de byte
	 * 
	 * @throws IOException Dans le cas ou une erreur d'outpustream à lieu
	 * 
	 * @return Retourne le tableau d'octet issu de la conversion de l'objet
	 */
	public byte[] objectToByteArray(Object object) throws IOException {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutput oo = new ObjectOutputStream(bStream);
		oo.writeObject(object);
		oo.close();
		byte[] buffer = bStream.toByteArray();
		return buffer;
	}

	/**
	 * Convertir un tableau d'octets en un objet
	 * 
	 * @param buffer Correspondant au tableau d'octets à convertir en un objet
	 * 
	 * @throws IOException Dans le cas ou une erreur d'inpustream à lieu
	 * 
	 * @return Retourne l'objet issu de la conversion du tableau d'octets
	 */
	public Object byteArrayToObject(byte[] buffer) throws IOException {
		ObjectInputStream iStream;
		Object object;
		try {
			iStream = new ObjectInputStream(new ByteArrayInputStream(buffer));
			object = iStream.readObject();
			iStream.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			object = null;
		}
		return object;
	}

	/**
	 * Envoyer une copie de l'echange à la "machine ExchangeMonitor"
	 * 
	 * @param receiver Correspondant au destinataire de l'echange initial
	 * 
	 * @param object   Correspondant à l'echange initial
	 * 
	 * @throws IOException Dans le cas ou une erreur de communication UDP à lieu
	 */
	public void sendCopyToExchangeMonitor(String receiver, Object object) throws IOException {
		Exchange exchange = new Exchange(myName, receiver, object);
		sendUDP("ExchangeMonitor", exchange, false);
	}

	/**
	 * Choisir de mettre en copie la machine "ExchangeMonitor" pour chaque echange
	 * ou non
	 * 
	 * @param copyToExchangeMonitorRequired Un booleen valant à true si l'on
	 *                                      souhaite mettre en copie
	 *                                      ExchangeMonitor, et false sinon
	 */
	public void setCopyToExchangeMonitorRequired(boolean copyToExchangeMonitorRequired) {
		this.copyToExchangeMonitorRequired = copyToExchangeMonitorRequired;
	}

}