package exchangeMonitor;

import java.io.IOException;

import machine.Machine;
import networkConnections.Exchange;

/**
 * Machine ExchangeMonitor : Affiche une copie de chaque message echange sur le
 * reseau, ainsi que l'emmetteur, et le destinataire du message. Affiche le
 * nombre de messages echanges chaque seconde
 * 
 * @author o.boutry
 * 
 */
public class ExchangeMonitor extends Machine {
	/**
	 * temps de reference permettant de compter le nombre de messages echanges
	 * chaque seconde
	 */
	private int tempsRef;
	/**
	 * boolean valant true si la simulation est terminee
	 */
	private boolean endOfSimulation;

	/**
	 * <b>Constructeur d'ExchangeMonitor</b>
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
	public ExchangeMonitor() throws ClassNotFoundException, IOException, InterruptedException {
		super("ExchangeMonitor");
		endOfSimulation = false;
	}

	/**
	 * Methode vide
	 */
	public void initialization() throws IOException, ClassNotFoundException, InterruptedException {
	}

	/**
	 * Boucle de fonctionnement : affichage des exchanges passant par le reseau, le
	 * nombre d exchanges par seconde, et en fin de simulation le nombre moyen
	 * d'echanges par seconde, et le nombre maximum d'echanges par seconde
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
	public void actionLoop() throws InterruptedException, ClassNotFoundException, IOException {
		int averageNbOfExchangesPerSeconds = 0;
		int ExchangeCompteur = 0;
		int secondCompteur = 0;
		int maximumNbOfExchanges = 0;
		tempsRef = (int) System.currentTimeMillis();
		while (!endOfSimulation) {
			Exchange exchange = (Exchange) networkConnections.receiveUDP();
			if (exchange.getReceiver().equals("ExchangeMonitor")) {
				endOfSimulation = true;
			}
			System.out.println(exchange.toString());
			ExchangeCompteur += 1;
			int actualTime = (int) System.currentTimeMillis();
			if (actualTime - tempsRef > 1000) {
				if (ExchangeCompteur > maximumNbOfExchanges) {
					maximumNbOfExchanges = ExchangeCompteur;
				}
				secondCompteur += 1;
				System.out.println("\n *** # " + ExchangeCompteur + " exchanges in 1 sec *** \n");
				tempsRef = (int) System.currentTimeMillis();
				averageNbOfExchangesPerSeconds += ExchangeCompteur;
				ExchangeCompteur = 0;
			}
		}
		averageNbOfExchangesPerSeconds = averageNbOfExchangesPerSeconds / secondCompteur;
		System.out.println("\n *** # " + ExchangeCompteur + " exchanges in less than 1 sec *** \n");
		System.out.println("\n *** # " + averageNbOfExchangesPerSeconds + " exchanges in average every seconds *** \n");
		System.out.println("\n *** # " + maximumNbOfExchanges
				+ " maximum number of exchanges in one second during the simulation *** \n");
	}

}
