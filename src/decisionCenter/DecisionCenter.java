package decisionCenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import machine.Machine;

/**
 * Machine DecisionCenter : Choisi aleatoirement les prochaines pieces a
 * peindre, ainsi que leur couleurs et envoie ces informations au Conveyor.
 * 
 * @author o.boutry
 * 
 */
public class DecisionCenter extends Machine {
	/**
	 * Liste des identifiants des pieces restantes a peindre.
	 */
	private ArrayList<Integer> listId;

	/**
	 * <b>Constructeur de DecisionCenter</b>
	 * 
	 * Initialise les communications reseaux et recupere le scenario grace a la
	 * classe abstraite Machine, puis initialise la liste des pieces a peindre.
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
	public DecisionCenter() throws ClassNotFoundException, IOException, InterruptedException {
		super("DecisionCenter");
		listId = new ArrayList<Integer>();

	}

	/**
	 * Initialisation de la machine avant d'entrer dans la boucle de fonctionnement,
	 * ajout des identifiants des pieces a peindre a la liste listId
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void initialization() throws IOException {
		System.out.println("Scenario : " + scenario.toString() + "\n");
		for (int idPart = 0; idPart < (int) scenario.get("nbParts"); idPart++) {
			listId.add(idPart);
		}
		chooseAndSendNextPart();
	}

	/**
	 * Boucle de fonctionnement de la machine : choisi la prochaine piece, sa
	 * couleur, et envoie ces informations au Conveyor, attend que les robots
	 * peintre aient fini avant d'envoyer les informations de la prochaine piece.
	 * Repete ceci tant qu'il reste des pieces a peindre, informe l'ExchangeMonitor
	 * de la fin de la simulation.
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * @throws IOException            Failed or interrupted I/O operations
	 */
	public void actionLoop() throws IOException, ClassNotFoundException {
		while (listId.size() > 0) {
			chooseAndSendNextPart();
			networkConnections.receiveRequest("PainterRobot1");
			networkConnections.receiveRequest("PainterRobot2");
		}
		networkConnections.receiveRequest("PainterRobot1");
		networkConnections.receiveRequest("PainterRobot2");
		networkConnections.sendRequest("ExchangeMonitor", "--- End of the simulation ---");
	}

	/**
	 * Choisi aleatoirement la prochaine piece, et sa couleur, et envoie ces
	 * informations au Conveyor
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void chooseAndSendNextPart() throws IOException {
		Collections.shuffle(listId);
		int idColor = (int) (Math.random() * (int) scenario.get("nbColors"));
		int nextId = listId.remove(0);
		String color = (String) scenario.get("color " + idColor);
		System.out.println("Next part : " + nextId + "-" + color);
		networkConnections.sendRequest("Conveyor", nextId + " " + color);
	}

}
