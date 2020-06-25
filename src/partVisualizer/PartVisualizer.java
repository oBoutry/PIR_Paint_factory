package partVisualizer;

import java.io.IOException;

import machine.Machine;
import partToPaint.Part;

/**
 * Machine PartVisualizer : Affiche la matrice correspondant a la piece en
 * cabine de peinture, et les informations de la piece
 * 
 * @author o.boutry
 * 
 */
public class PartVisualizer extends Machine {

	/**
	 * <b>Constructeur de PartVisualizer</b>
	 * 
	 * Initialise les communications reseaux grace a la classe abstraite Machine
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * @throws IOException            Failed or interrupted I/O operations
	 * @throws InterruptedException   Thrown when a thread is waiting, sleeping, or
	 *                                otherwise occupied, and the thread is
	 *                                interrupted, either before or during the
	 *                                activity
	 */
	public PartVisualizer() throws ClassNotFoundException, IOException, InterruptedException {
		super("PartVisualizer");
	}

	/**
	 * Methode vide
	 */
	public void initialization() throws ClassNotFoundException, IOException, InterruptedException {

	}

	/**
	 * Boucle de fonctionnement : Affiche la matrice correspondant a la piece en
	 * cabine de peinture, et les informations de la piece
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * @throws IOException            Failed or interrupted I/O operations
	 * @throws InterruptedException   Thrown when a thread is waiting, sleeping, or
	 *                                otherwise occupied, and the thread is
	 *                                interrupted, either before or during the
	 *                                activity
	 */
	public void actionLoop() throws ClassNotFoundException, IOException, InterruptedException {
		while (true) {
			Part part = (Part) networkConnections.receiveUDP();
			System.out.println(part.getPartInfo());
			part.printMatrice();
			System.out.println();
		}

	}

}
