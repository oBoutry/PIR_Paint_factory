package machine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;

import networkConnections.NetworkConnections;

/**
 * Classe abstraite machine etendue par chacune des cinq machines de la
 * simulation et par l'element ExchangeMonitor Permet d'initialiser les
 * communications reseaux et de recuperer le scenario a partir du fichier texte
 * scenario
 * 
 * @author o.boutry
 * 
 */
public abstract class Machine {
	/**
	 * Scenario contennant des données et des valeurs associees a chaque donnee
	 */
	protected Hashtable<String, Object> scenario;
	/**
	 * Objet networkConnections permettant d'effectuer les communications reseaux en
	 * UDP et en TCP
	 */
	protected NetworkConnections networkConnections;
	/**
	 * Facteur permettant depuis le fichier txt scenario d'augmenter ou de diminuer
	 * la vitesse a laquelle s execute la simulation
	 */
	protected Double timeFactor;

	/**
	 * <b>Constructeur de Machine</b>
	 * 
	 * Initialise les communications reseaux a partir du fichier txt
	 * table_adresses_ports, recupere le scenario a partir du fichier txt scenario,
	 * et indique ensuite a l'objet networkConnections de mettre en copie
	 * ExchangeMonitor pour chaque message envoyé en TCP ou en UDP
	 * 
	 * @param elementName Le nom de la machine qui s'initialise
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
	public Machine(String elementName) throws ClassNotFoundException, IOException, InterruptedException {
		System.out.println("  -- " + elementName + " --  " + "\n");
		scenario = new Hashtable<String, Object>();
		getScenario("data/scenario");
		networkConnections = new NetworkConnections(elementName, "data/table_adresses_ports");
		Thread.sleep(500);
		networkConnections.setCopyToExchangeMonitorRequired(true);
	}

	/**
	 * Recuperer le scenario a partir du fichier txt
	 * 
	 * @param dataFileName Nom du fichier txt contenant le scenario
	 */
	public void getScenario(String dataFileName) {
		String[] words = null;
		Scanner scanner;
		try {
			scanner = new Scanner(new File(dataFileName));
		} catch (FileNotFoundException e) {
			scanner = null;
			System.out.println("error while trying to get scenario");
			e.printStackTrace();
		}
		scanner.nextLine();
		words = scanner.nextLine().split(" ", 7);
		scenario.put("nbParts", Integer.parseInt(words[0]));
		scenario.put("nbMaxPixels", Integer.parseInt(words[1]));
		scenario.put("nbMinPixels", Integer.parseInt(words[2]));
		scenario.put("nbRows", Integer.parseInt(words[3]));
		scenario.put("nbColors", Integer.parseInt(words[4]));
		scenario.put("timeFactor", Double.parseDouble(words[5]));
		timeFactor = (Double) scenario.get("timeFactor");
		scanner.nextLine();
		words = scanner.nextLine().split(" ", (int) scenario.get("nbColors"));
		for (int idColor = 0; idColor < (int) scenario.get("nbColors"); idColor++) {
			scenario.put("color " + idColor, words[idColor]);
		}
		scanner.close();

	}

	/**
	 * Classe abstraite permettant d'initialiser chaque machine avant d'entrer dans
	 * la boucle de fonctionnement
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
	public abstract void initialization() throws ClassNotFoundException, IOException, InterruptedException;

	/**
	 * Boucle de fonctionnement permettant a chaque machine d'accomplir ses missions
	 * tant qu'il reste des pieces a peindre
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
	public abstract void actionLoop() throws ClassNotFoundException, IOException, InterruptedException;

	/**
	 * Permet de fermer toutes les communications reseaux de la machine
	 * 
	 * @throws IOException          Failed or interrupted I/O operations
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void close() throws IOException, InterruptedException {
		Thread.sleep(20000);
		networkConnections.closeAllConnections();
	}

}
