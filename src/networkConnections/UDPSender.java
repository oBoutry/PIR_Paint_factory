package networkConnections;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Objet UDP sender permettant d'envoyer des datagrammes UDP
 * 
 * @author o.boutry
 * 
 */
public class UDPSender {
	/**
	 * Socket UDP
	 */
	private DatagramSocket socket;

	/**
	 * <b>Constructeur d'UDPSender</b>
	 * 
	 * Initialise la socket UDP
	 * 
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host
	 *                              could not be determined
	 * 
	 * @throws SocketException      Thrown to indicate that there is an error
	 *                              creating or accessing a Socket
	 */
	public UDPSender() throws UnknownHostException, SocketException {
		super();
		socket = new DatagramSocket();
	}

	/**
	 * Envoyer un tableau de bits via un datagramme UDP
	 * 
	 * @param host   l'adresse du destinataire
	 * @param port   le port UDP du destinataire
	 * @param buffer le tableau de bits a envoyer
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void send(String host, int port, byte[] buffer) throws IOException {
		DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(host), port);
		socket.send(datagram);
	}

	/**
	 * Fermer la socket
	 */
	public void close() {
		socket.close();
	}

}
