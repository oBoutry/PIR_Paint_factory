package shadeChanger;

import java.io.IOException;
import java.util.Hashtable;

import machine.Machine;
import partToPaint.Part;
import tank.Tank;
import terminal.Terminal;

/**
 * Machine ShadeChanger : prepare la bonne quantite de peinture de la bonne
 * couleur a partir des informations indiquees par le Conveyor, envoie la
 * peintures aux robots peintres pour qu'ils peignent la piece, et recoit
 * l'excedent de peinture envoye par les robots une fois que la piece est peinte
 * 
 * @author o.boutry
 * 
 */
public class ShadeChanger extends Machine {
	/**
	 * Les reservoirs de peintures de differentes couleurs
	 */
	private Hashtable<String, Tank> tanks;
	/**
	 * Le reservoir de peinture preparee pour la prochaine piece a peindre
	 */
	private Tank preparedPaintTank;
	/**
	 * Le reservoir receptionnant la peinture en exces avant qu elle ne soit remise
	 * dans le reservoir de la bonne couleur
	 */
	private Tank paintToBeReusedTank;
	/**
	 * Prochaine piece a peindre
	 */
	private Part nextPartToPaint;
	/**
	 * Quantite de peinture a envoyer au robot 1
	 */
	private int paintQuantity1;
	/**
	 * Quantite de peinture a envoyer au robot 2
	 */
	private int paintQuantity2;
	/**
	 * Nombre de pieces a peindre
	 */
	private int nbIter;

	/**
	 * <b>Constructeur de ShadeChanger</b>
	 * 
	 * Initialise les communications reseaux et recupere le scenario grace a la
	 * classe abstraite Machine, puis initialise le ShadeChanger a partir du
	 * scenario
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
	public ShadeChanger() throws ClassNotFoundException, IOException, InterruptedException {
		super("ShadeChanger");
		tanks = new Hashtable<String, Tank>();
		preparedPaintTank = new Tank(null, 0);
		paintToBeReusedTank = new Tank(null, 0);
		nextPartToPaint = null;
		nbIter = (int) scenario.get("nbParts");
	}

	/**
	 * Initialisation de la machine avant d'entrer dans la boucle de fonctionnement,
	 * initialise les reservoirs de peinture
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
		for (int idColor = 0; idColor < (int) scenario.get("nbColors"); idColor++) {
			tanks.put((String) scenario.get("color " + idColor), new Tank(100000));
		}
		tanks.put("preparedPaintTank", preparedPaintTank);
		tanks.put("paintToBeReusedTank", paintToBeReusedTank);
		//System.out.println(tanks.toString());
	}

	/**
	 * Boucle de fonctionnement de la machine : prepare la peinture, l'envoie,
	 * recoit la peinture en exces, et la restocke, tant qu'il reste des pieces a
	 * peindre
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
		while (nbIter > 0) {
			nextPartToPaint = (Part) networkConnections.receiveRequest("Conveyor");
            Terminal.ClearScreen();
            Terminal.Home();
            System.out.println("-- Tanks levels :");
            for (String color:  tanks.keySet())
                System.out.println(color + "\t" + tanks.get(color));
			System.out.println("-- Part to paint :" + nextPartToPaint.getPartFullInfo());
			preparePaint(nextPartToPaint);
			paintToBeReusedTank.setColor(nextPartToPaint.getColor());
			networkConnections.receiveRequest("PainterRobot1");
			networkConnections.receiveRequest("PainterRobot2");
			sendPaint();
			receivePaint();
			restockPaint();
			nbIter -= 1;
		}
	}

	/**
	 * Restocke la peinture recue en exces
	 */
	public void restockPaint() {
		tanks.get(nextPartToPaint.getColor())
				.setQuantity(tanks.get(nextPartToPaint.getColor()).getQuantity() + paintToBeReusedTank.getQuantity());
		paintToBeReusedTank.setQuantity(0);
	}

	/**
	 * Prepare la bonne quantite de peinture pour chaque robot
	 * 
	 * @param part la piece a peindre
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void preparePaint(Part part) throws InterruptedException {
		String color = part.getColor();
		int paintQuantity = 0;
		int tankQuantity = tanks.get(color).getQuantity();
		paintQuantity1 = part.getNbPixels();// (part.getFaces().get(1).getNbPixels()+part.getFaces().get(3).getNbPixels())*10;
		paintQuantity2 = paintQuantity1;
		// paintQuantity2=(part.getFaces().get(2).getNbPixels()+part.getFaces().get(3).getNbPixels())*10;
		paintQuantity = paintQuantity1 + paintQuantity2;
		int timeToPreparePaint = 1 * paintQuantity;
		if (paintQuantity > tankQuantity) {
			System.out.println("Error not enough paint for this piece and this color");
            System.exit(1);
		} else {
			Thread.sleep(Math.round(timeToPreparePaint * timeFactor));
			preparedPaintTank.setQuantity(paintQuantity);
			preparedPaintTank.setColor(part.getColor());
			tanks.get(color).setQuantity(tankQuantity - paintQuantity);
			System.out.println("-- preparedPaintTank : " + preparedPaintTank.toString());
		}
	}

	/**
	 * Envoie la peinture preparee aux robots peintres
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void sendPaint() throws InterruptedException {
		Thread.sleep(Math.round(1000 * timeFactor));
		int quantity = paintQuantity1 + paintQuantity2;
		try {
			networkConnections.sendRequest("PainterRobot1", paintQuantity1);
		} catch (IOException e) {
			System.out.println("Error occured during paint sending");
			e.printStackTrace();
		}
		preparedPaintTank.setQuantity(quantity);
		try {
			networkConnections.sendRequest("PainterRobot2", paintQuantity2);
		} catch (IOException e) {
			System.out.println("Error occured during paint sending");
			e.printStackTrace();
		}
		preparedPaintTank.setQuantity(0);
		System.out.println("-- Paint quantity sent : " + quantity);
	}

	/**
	 * Recoit la peinture en exces renvoyee par les robots peintres
	 * 
	 */
	public void receivePaint() {
		int paintFrom1 = 0;
		int paintFrom2 = 0;
		try {
			paintFrom1 = (int) networkConnections.receiveRequest("PainterRobot1");
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Error occured during paint receiving");
			e.printStackTrace();
		}
		try {
			paintFrom2 = (int) networkConnections.receiveRequest("PainterRobot2");
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Error occured during paint receiving");
			e.printStackTrace();
		}
		paintToBeReusedTank.setQuantity(paintFrom1 + paintFrom2);
		System.out.println("-- Paint quantity received : " + (paintFrom1 + paintFrom2));

	}

	public String tanksToString() {
		return tanks.toString();
	}

}
