package networkConnections;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPReceiver {

	private DatagramSocket socket;

	public UDPReceiver(int port) throws SocketException {

		this.socket = new DatagramSocket(port);
	}

	public byte[] receive() throws IOException {
		byte[] buffer = new byte[20000];
		DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
		socket.receive(datagram);
		byte[] data = Arrays.copyOf(datagram.getData(), datagram.getLength());
		return data;
	}

	public void close() {
		socket.close();
	}

}
