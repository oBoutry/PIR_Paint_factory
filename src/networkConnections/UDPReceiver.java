package networkConnections;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Objet UDP receiver permettant de recevoir des datagrammes UDP
 * 
 * @author o.boutry
 * 
 */
public class UDPReceiver {
	/**
	 * Socket UDP
	 */
	private DatagramSocket socket;

	/**
	 * <b>Constructeur d'UDPReceiver</b>
	 * 
	 * Initialise la socket UDP avec le port UDP de la machine
	 * 
	 * @param port Le port UDP de la machine 
	 * 
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket
	 */
	public UDPReceiver(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}

	/**
	 * Recevoir un datagramme UDP contenant un tableau de bits 
	 * 
	 * @return le tableau de bits recu
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public byte[] receive() throws IOException {
		byte[] buffer = new byte[20000];
		DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
		socket.receive(datagram);
		byte[] data = Arrays.copyOf(datagram.getData(), datagram.getLength());
		return data;
	}

	/**
	 * Fermer la socket
	 */
	public void close() {
		socket.close();
	}

}
