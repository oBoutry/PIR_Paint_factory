package conveyor;

import java.io.IOException;
import java.util.ArrayList;

import machine.Machine;
import partToPaint.Part;

/**
 * Machine Conveyor : Destocke les pieces dans l'ordre indique par le
 * DecisionCenter, et attribue la couleur indique par le DecisionCenter a la
 * piece. Envoie les informations de la prochaine piece a peindre au
 * ShadeChanger pour qu'il prepare la bonne quantite de peinture. Met la
 * prochaine piece a peindre sur le tapis roulant. Active le tapis roulant pour
 * que la piece a peindre arrive en cabine. Retire du tapis roulant et stocke la
 * piece peinte.
 * 
 * @author o.boutry
 * 
 */
public class Conveyor extends Machine {
	/**
	 * Stock de piece a peindre
	 */
	private Stock stockToPaint;
	/**
	 * Stock de piece peintes
	 */
	private Stock stockPainted;
	/**
	 * Tapis roulant
	 */
	private ArrayList<Part> conveyorBelt;
	/**
	 * Identifiant de la prochaine piece a peindre
	 */
	private int nextId;
	/**
	 * Couleur de la prochaine piece a peindre
	 */
	private String nextColor;
	/**
	 * Nombre de pieces a peindre
	 */
	private int nbIter;
	/**
	 * Boolean valant true si la simulation est terminee, permettant d'arreter le
	 * threadReceiver mettant a jour le colorLevel de la piece en cabine de peinture
	 */
	private volatile boolean close;

	/**
	 * <b>Constructeur de Conveyor</b>
	 * 
	 * Initialise les communications reseaux et recupere le scenario grace a la
	 * classe abstraite Machine, puis initialise le Conveyor a partir du scenario
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
	public Conveyor() throws ClassNotFoundException, IOException, InterruptedException {
		super("Conveyor");
		stockToPaint = new Stock((int) scenario.get("nbParts"), (int) scenario.get("nbMinPixels"),
				(int) scenario.get("nbMaxPixels"), (int) scenario.get("nbRows"));
		stockPainted = new Stock();
		conveyorBelt = new ArrayList<Part>();
		conveyorBelt.add(null);
		conveyorBelt.add(null);
		conveyorBelt.add(null);
		nbIter = (int) scenario.get("nbParts");
		close = false;
	}

	/**
	 * Initialisation de la machine avant d'entrer dans la boucle de fonctionnement
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
	public void initialization() throws ClassNotFoundException, IOException, InterruptedException {
		receiveAndSetPart();
		System.out.println(conveyorBeltToString());

		destockToPaint(nextId);
		System.out.println(conveyorBeltToString());

		networkConnections.sendRequest("ShadeChanger", conveyorBelt.get(2));

		moveConveyorBelt();
		System.out.println(conveyorBeltToString());

		sendPartRobots();

		nbIter -= 1;
	}

	/**
	 * Boucle de fonctionnement de la machine
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
		Thread threadReceiver = new Thread() {
			public void run() {
				while (!close) {
					try {
						double colorLevel = (double) networkConnections.receiveUDP();
						if (conveyorBelt.get(1) != null) {
							conveyorBelt.get(1).setColorLevel(colorLevel);
						}
					} catch (IOException e) {
					}

				}
			}
		};

		threadReceiver.start();

		while (nbIter > 0) {
			receiveAndSetPart();

			destockToPaint(nextId);
			System.out.println(conveyorBeltToString());

			networkConnections.sendRequest("ShadeChanger", conveyorBelt.get(2));

			networkConnections.receiveRequest("PainterRobot1");
			networkConnections.receiveRequest("PainterRobot2");
			moveConveyorBelt();
			System.out.println(conveyorBeltToString());

			sendPartRobots();

			nbIter -= 1;
		}
		networkConnections.receiveRequest("PainterRobot1");
		networkConnections.receiveRequest("PainterRobot2");
		close = true;

		conveyorBelt.add(2, null);
		moveConveyorBelt();
		System.out.println(conveyorBeltToString());
		conveyorBelt.add(2, null);
		moveConveyorBelt();
		System.out.println(conveyorBeltToString());
	}

	/**
	 * Recuperer l'identifiant et la couleur de la prochaine piece a peindre
	 * 
	 * @param partRequest Correspondant a la requete envoyee par le DecisionCenter
	 * 
	 */
	public void getIdAndColor(String partRequest) {
		String[] words = partRequest.split(" ", 2);
		nextId = Integer.parseInt(words[0]);
		nextColor = words[1];
	}

	/**
	 * Envoyer la piece a peindre aux robots peintres
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void sendPartRobots() throws IOException {
		Part part = conveyorBelt.get(1);
		networkConnections.sendRequest("PainterRobot1", part);
		networkConnections.sendRequest("PainterRobot2", part);
	}

	/**
	 * Recevoir l'identifiant et la couleur de la piece a peindre et lui attribuer
	 * cette couleur
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * @throws IOException            Failed or interrupted I/O operations
	 */
	public void receiveAndSetPart() throws ClassNotFoundException, IOException {
		String partRequest = (String) networkConnections.receiveRequest("DecisionCenter");
		getIdAndColor(partRequest);
		setColorNextPart();
	}

	/**
	 * Destocker la prochaine piece a peindre et la placer en derniere position du
	 * tapis roulant
	 * 
	 * @param partId Correspondant a l'identifiant de la piece a destocker
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void destockToPaint(int partId) throws InterruptedException {
		int timeToDestock = 500 * partId;
		Thread.sleep(Math.round(timeToDestock * timeFactor));
		conveyorBelt.add(2, stockToPaint.destock(partId));
	}

	/**
	 * Restocker la piece peinte
	 * 
	 * @param part Correspondant a la piece peinte
	 * 
	 */
	public void restockPainted(Part part) {
		stockPainted.stock(part.getId(), part);
	}

	/**
	 * Restocker la piece peinte en derniere position du tapis roulant et faire
	 * avancer le tapis roulant
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void moveConveyorBelt() throws InterruptedException {
		Thread.sleep(Math.round(1000 * timeFactor));
		if (conveyorBelt.get(0) == null) {
			conveyorBelt.remove(0);
		} else {
			restockPainted(conveyorBelt.remove(0));
		}
	}

	/**
	 * Attribuer a la prochaine piece a peindre sa couleur
	 * 
	 */
	public void setColorNextPart() {
		stockToPaint.getParts().get(nextId).setColor(nextColor);
	}

	public Stock getStockToPaint() {
		return stockToPaint;
	}

	public Stock getStockPainted() {
		return stockPainted;
	}

	public ArrayList<Part> getConveyorBelt() {
		return conveyorBelt;
	}

	public String conveyorBeltToString() {
		String firstPlace;
		String secondPlace;
		String thirdPlace;
		if (conveyorBelt.get(0) == null) {
			firstPlace = "      ";
		} else {
			firstPlace = conveyorBelt.get(0).getPartInfo();
		}
		if (conveyorBelt.get(1) == null) {
			secondPlace = "      ";
		} else {
			secondPlace = conveyorBelt.get(1).getPartInfo();
		}
		if (conveyorBelt.get(2) == null) {
			thirdPlace = "      ";
		} else {
			thirdPlace = conveyorBelt.get(2).getPartInfo();
		}
		return "Belt [" + firstPlace + "  /_/  " + secondPlace + "  /_/  " + thirdPlace + "]";
	}
}
