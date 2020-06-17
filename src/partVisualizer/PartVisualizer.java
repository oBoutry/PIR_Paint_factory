package partVisualizer;

import java.io.IOException;

import machine.Machine;
import partToPaint.Part;

public class PartVisualizer extends Machine {

	public PartVisualizer() throws ClassNotFoundException, IOException, InterruptedException {
		super("PartVisualizer");
	}

	public void initialization() throws ClassNotFoundException, IOException, InterruptedException {

	}

	public void actionLoop() throws ClassNotFoundException, IOException, InterruptedException {
		while (true) {
			Part part = (Part) networkConnections.receiveUDP();
			System.out.println(part.getPartInfo());
			part.printMatrice();
			System.out.println();
		}

	}

}
