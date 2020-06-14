package networkConnections;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPSender {
	private DatagramSocket socket;

	public UDPSender() throws UnknownHostException, SocketException {
		super();
		socket = new DatagramSocket();
	}

	public void send(String host, int port, byte[] buffer) throws IOException {
		DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(host), port);
		socket.send(datagram);
	}

	public void close() {
		socket.close();
	}

}
