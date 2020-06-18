package painterRobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import machine.Machine;
import partToPaint.Part;
import tank.Tank;

/**
 * Machine PainterRobot : recoit la peinture envoyee par le ShadeChanger, peint
 * la piece pixel par pixel en cooperation avec l'autre robot sans entrer en
 * collision avec celui-ci.
 * 
 * 
 * @author o.boutry
 * 
 */
public class PainterRobot extends Machine {
	/**
	 * Identifiant du robot
	 */
	private int idRobot;
	/**
	 * Identifiant de l'autre robot
	 */
	private int idOtherRobot;
	/**
	 * Reservoir de peinture du robot
	 */
	private Tank tank;
	/**
	 * Position actuelle du robot
	 */
	private Position position;
	/**
	 * Prochaine position souhaitee du robot
	 */
	private Position nextDesiredPosition;
	/**
	 * Prochaine position souhaitee de l'autre robot
	 */
	private Position nextDesiredPositionOtherRobot;
	/**
	 * Position actuelle de l'autre robot
	 */
	private Position positionOtherRobot;
	/**
	 * Liste des pixels restant à peindre
	 */
	private PositionsList availablePositions;
	/**
	 * Liste des pixels restant à peindre ordonnes en fonction de leur distance au
	 * robot
	 */
	private ArrayList<Position> availablePositionsOrderedByDistance;
	/**
	 * Horloge du robot
	 */
	private AtomicInteger clock;
	/**
	 * Horloge de l'autre robot
	 */
	private int clockOtherRobot;
	/**
	 * Piece a peindre
	 */
	private Part part;
	/**
	 * Message a envoyer a l'autre robot apres chaque deplacement
	 */
	private Message message;
	/**
	 * Nombre de pieces a peindre
	 */
	private int nbIter;
	/**
	 * Distance entre les deux robots
	 */
	private int distanceBeetwenRobots;
	/**
	 * Booleen valant true s'il n y a plus de pixels disponibles a peindre
	 */
	private volatile boolean close;
	/**
	 * Nombre de message envoyes pour lesquels on attend un accuse de reception
	 */
	private AtomicInteger messageCounter;
	/**
	 * Largeur de la fenetre correspondant aux nombre de messages que peut envoyer
	 * le robot sans attendre d'accuse de reception
	 */
	private int windowWidth;

	/**
	 * <b>Constructeur de PainterRobot</b>
	 * 
	 * @param elementName Le nom du robot : "PainterRobot1" ou "PainterRobot2"
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
	public PainterRobot(String elementName) throws ClassNotFoundException, IOException, InterruptedException {
		super(elementName);
		availablePositions = new PositionsList();
		availablePositionsOrderedByDistance = new ArrayList<Position>();
		if (elementName.equals("PainterRobot1")) {
			idRobot = 1;
			idOtherRobot = 2;
		} else {
			idRobot = 2;
			idOtherRobot = 1;
		}
		tank = new Tank("none", 0);
		clock = new AtomicInteger(0);
		clockOtherRobot = 0;
		message = new Message(0,position, null, clock.get(), 0);
		part = null;
		nextDesiredPosition = null;
		nbIter = (int) scenario.get("nbParts");
		messageCounter = new AtomicInteger(0);
		windowWidth = 5;
		close = false;

	}

	/**
	 * Methode vide
	 */
	public void initialization() throws ClassNotFoundException, IOException, InterruptedException {
	}

	/**
	 * Boucle de fonctionnement de la machine : recoit la piece, la peint, averti le
	 * Conveyor et le DecisionCenter lorsque la piece est totalement peinte
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
		while (nbIter > 0) {
			part = (Part) networkConnections.receiveRequest("Conveyor");

			if (idRobot == 2) {
				position = new Position(part.getNbRows() - 1, 0);
				positionOtherRobot = new Position(0, 0);
			} else {
				position = new Position(0, 0);
				positionOtherRobot = new Position(part.getNbRows() - 1, 0);
			}

			getAvailablePixels();

			System.out.println("----> Part in position : " + part);
			networkConnections.sendRequest("ShadeChanger", "ready");

			receivePaint();

			paint();

			while (!close) {
				Thread.sleep(10);
			}

			Thread.sleep(Math.round(500 * timeFactor));
			System.out.println("----> Part painted successfully : " + part);

			sendPaintToBeReused();

			System.out.println("\n °°° Waiting for next part °°° \n");

			networkConnections.sendRequest("Conveyor", "ready");
			networkConnections.sendRequest("DecisionCenter", "ready");
			nbIter -= 1;
		}
		System.out.println("\n ----> no more part to paint \n");
	}

	/**
	 * Peindre la piece, deux threads. Un thread recevant et analysant les messages,
	 * un thread gerant les deplacements et le peinture
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 * 
	 * @throws IOException          Failed or interrupted I/O operations
	 */
	public void paint() throws InterruptedException, IOException {

		close = false;
		messageCounter.set(0);
		part.setPixel(position, idRobot);
		part.setPixel(positionOtherRobot, idOtherRobot);
		clock.incrementAndGet();
		if (idRobot == 2) {
			networkConnections.sendUDP("PartVisualizer", part, false);
		}

		Thread threadReceiver = new Thread() {

			public void run() {
				while (!close) {
					try {
						Message messageReceived = receiveMessage();
						if (messageReceived.getAck() == 1) {
							messageCounter.decrementAndGet();
						} else {
							analyzeMessage(messageReceived);
							sendAck(messageReceived);
						}
					} catch (InterruptedException | IOException e) {
					} finally {
					}
				}
			}

		};
		threadReceiver.start();

		Thread threadActionPaint = new Thread() {

			public void run() {
				try {
					
					while (availablePositions.getSize() >= 1 && part.getColorLevel()<100) {
						while (messageCounter.get() >= windowWidth && part.getColorLevel()<100) {
							Thread.sleep(1);
						}
						if(availablePositions.getSize() >= 1){
							messageCounter.incrementAndGet();
							displacement();
							setNewWindowWidth();
							System.out.println("Window Width : " + windowWidth);
						}
					}
					
					nextDesiredPosition = null;
					setMessage(0);
					sendMessage();
					while (!isFinishedPart()) {
						paintPixel();
						updateConveyor();
						moveRobot();
						patchPaint();
						System.out.println("clock : " + clock.get());
					}
					nextDesiredPosition = new Position(0, 0);
					setMessage(0);
					sendMessage();
					close = true;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} finally {

				}
			}
		};

		threadActionPaint.start();

	}

	/**
	 * Definir le pas de la fenetre de handshake a partir de la distance entre les
	 * deux robots
	 */
	public void setNewWindowWidth() {
		distanceBeetwenRobots = getDistance(position, positionOtherRobot);
		windowWidth = distanceBeetwenRobots / 2 + 1;
		if (availablePositions.getSize() <= 10) {
			windowWidth = 1;
		}
		if (windowWidth > 20) {
			windowWidth = 20;
		}
	}

	/**
	 * Deplacement du robot : choix du prochain pixel, envoie du message, si
	 * deplacement non autorise la priorite est donnee au robot 1 et le robot 2 sort
	 * de la zone
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 * 
	 * @throws IOException          Failed or interrupted I/O operations
	 */
	public void displacement() throws InterruptedException, IOException {
		chooseNextPixel();
		setMessage(0);
		sendMessage();
		if (!areNextMovesAllowed()) {
			if (idRobot == 2) {
				if (!nextDesiredPosition.equals(nextDesiredPositionOtherRobot)) {
					availablePositions.addElement(nextDesiredPosition);
				}
				nextDesiredPosition = null;
			}
		}
		paintPixel();
		updateConveyor();
		moveRobot();
		System.out.println("clock : " + clock.get());
	}

	/**
	 * Choisir le prochain pixel a peindre : le plus proche parmi les pixels
	 * disponibles
	 */
	public void chooseNextPixel() {
		OrderByDistance();
		nextDesiredPosition = availablePositionsOrderedByDistance.remove(0);
		availablePositions.removeElement(nextDesiredPosition);
	
		System.out.println("Next Desired Position : " + nextDesiredPosition);
		System.out.println("Position : " + position);
	}

	/**
	 * Analyser le message recu : mis a jour de la liste des pixels disponibles, des
	 * positions et positions souhaites de l'autre robot
	 * 
	 * @param messageReceived Le message recu contenant les informations de l'autre
	 *                        robot ou constituant un accuse de reception
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void analyzeMessage(Message messageReceived) throws InterruptedException {
		if (messageReceived.getPartId()!=part.getId()) {
			return;
		}
		if (messageReceived.getClock() < clockOtherRobot) {
			Position oldPosition = messageReceived.getPosition();
			if (oldPosition != null) {
				part.setPixel(oldPosition, 4);
				availablePositions.removePosition(oldPosition);
			}
			part.colorLevelCalculator();
		} else {
			clockOtherRobot = messageReceived.getClock();

			clock.set(Math.max(clock.get(), clockOtherRobot));
			clock.incrementAndGet();

			if (positionOtherRobot != null) {
				part.setPixel(positionOtherRobot, 4);
			}

			positionOtherRobot = messageReceived.getPosition();
			nextDesiredPositionOtherRobot = messageReceived.getDesiredPosition();

			if (positionOtherRobot != null) {
				try {
					part.setPixel(positionOtherRobot, idOtherRobot);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("**** PositionOtherRobot : " + positionOtherRobot);
					System.out.println("size : " + part.getMatrice()[positionOtherRobot.getRow()].length);
				}
				availablePositions.removePosition(positionOtherRobot);
			}

			if (idRobot == 2 && nextDesiredPositionOtherRobot != null) {
				availablePositions.removePosition(nextDesiredPositionOtherRobot);
			}

			if (idRobot == 2 && nextDesiredPositionOtherRobot == null && positionOtherRobot != null) {
				part.setPixel(positionOtherRobot, 4);
			}

			part.colorLevelCalculator();

			if (nextDesiredPositionOtherRobot != null && nextDesiredPositionOtherRobot.getRow() == 0
					&& nextDesiredPositionOtherRobot.getColomn() == 0) {
				part.setColorLevel(100.);
				availablePositions.clear();
			}
		}

	}

	/**
	 * Trier les pixels disponibles en placant les plus proches d'abord
	 */
	public void OrderByDistance() {
			availablePositionsOrderedByDistance.clear();
			if (position == null) {
				availablePositionsOrderedByDistance.add(availablePositions.getElement(0));
			}else {
				ArrayList<Position> positions1 = new ArrayList<Position>();
				ArrayList<Position> positions2 = new ArrayList<Position>();
				ArrayList<Position> positions3 = new ArrayList<Position>();
				ArrayList<Position> positions4 = new ArrayList<Position>();
				ArrayList<Position> positions5Plus = new ArrayList<Position>();
					for (int idPosition = 0; idPosition < availablePositions.getSize(); idPosition++) {
						if (positions1.size() >= 1) {
							break;
						}
						Position positionToOrder;
						try {
							positionToOrder = availablePositions.getElement(idPosition);
						} catch (Exception e) {
							positionToOrder=null;
						}
						int distance = getDistance(position, positionToOrder);
						switch (distance) {
						case 1:
							positions1.add(positionToOrder);
							break;
						case 2:
							positions2.add(positionToOrder);
							break;
						case 3:
							positions3.add(positionToOrder);
							break;
						case 4:
							positions4.add(positionToOrder);
							break;
						default:
							positions5Plus.add(positionToOrder);
							break;
						}
					}

				availablePositionsOrderedByDistance.addAll(positions1);
				availablePositionsOrderedByDistance.addAll(positions2);
				availablePositionsOrderedByDistance.addAll(positions3);
				availablePositionsOrderedByDistance.addAll(positions4);
				availablePositionsOrderedByDistance.addAll(positions5Plus);
			}
	}

	/**
	 * Determiner si la piece est peinte ou non
	 * 
	 * @return true si la piece est totalement peinte, false sinon
	 */
	public boolean isFinishedPart() {
		return part.getColorLevel() == 100;
	}

	/**
	 * Peint le pixel sur lequel se trouve le robot
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 */
	public void paintPixel() throws InterruptedException {
		if (position != null) {
			boolean pixelAlreadyPainted = part.getMatrice()[position.getRow()][position.getColomn()] == 4;
			if (!pixelAlreadyPainted) {
				part.setPixel(position, 4);
				tank.setQuantity(tank.getQuantity() - 1);
			}
		}
		part.colorLevelCalculator();
		System.out.println("Tank : " + tank);
	}

	/**
	 * Pour le robot 1 : envoie le niveau de couleur de la piece entre 0 et 100 au
	 * Conveyor
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void updateConveyor() throws IOException {
		if (idRobot == 1) {
			networkConnections.sendUDP("Conveyor", part.getColorLevel(), true);
		}
	}

	/**
	 * Envoyer le message a l'autre robot
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void sendMessage() throws IOException {
		if (idRobot == 1) {
			networkConnections.sendUDP("PainterRobot2", message, true);
		}
		if (idRobot == 2) {
			networkConnections.sendUDP("PainterRobot1", message, true);
		}
	}

	public void setMessage(int ack) {
		message.setMessage(part.getId(),position, nextDesiredPosition, clock.get(), ack);
	}

	/**
	 * Recevoir le message envoye par l'autre robot
	 * 
	 * @return Le message recu
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public Message receiveMessage() throws IOException {
		Message messageReceived = null;
		messageReceived = (Message) networkConnections.receiveUDP();
		return messageReceived;
	}

	/**
	 * Deplace le robot : la nouvelle position est l'ancienne position souhaitee
	 * 
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, or
	 *                              otherwise occupied, and the thread is
	 *                              interrupted, either before or during the
	 *                              activity
	 * @throws IOException          Failed or interrupted I/O operations
	 */
	public void moveRobot() throws InterruptedException, IOException {
		Thread.sleep(Math.round(100 * timeFactor * getDistance(position, nextDesiredPosition)));
		position = nextDesiredPosition;
		if (position != null) {
			part.setPixel(position, idRobot);
		}
		clock.incrementAndGet();
		if (idRobot == 2) {
			networkConnections.sendUDP("PartVisualizer", part, false);
		}
		System.out.println();
	}

	/**
	 * Determine si les prochains mouvement souhaites des deux robots peuvent
	 * occasionner une collision
	 * 
	 * @return true si les prochains mouvements souhaites sont autorises, false
	 *         sinon
	 */
	public boolean areNextMovesAllowed() {
		if (nextDesiredPosition == null || nextDesiredPositionOtherRobot == null) {
			return true;
		}
		if (getDistance(nextDesiredPosition, nextDesiredPositionOtherRobot) <= 5) {
			return false;
		}
		return true;
	}

	/**
	 * Etablir la liste de tous les pixels restant a peindre
	 */
	public void getAvailablePixels() {
		availablePositions.clear();
		if (idRobot==1) {
			for (int i = 0; i < part.getNbRows(); i++) {
				for (int j = 0; j < part.getMatrice()[i].length; j++) {
					availablePositions.addElement(new Position(i, j));
				}
			}	
		}else {
			for (int i = part.getNbRows()-1; i >=0; i--) {
				for (int j = part.getMatrice()[i].length-1; j>=0; j--) {
					availablePositions.addElement(new Position(i, j));
				}
			}	
		}
		availablePositions.removePosition(new Position(0, 0));
		availablePositions.removePosition(new Position(part.getNbRows() - 1, 0));
	}

	/**
	 * Renvoie l'exces de peinture au ShadeChanger
	 * 
	 * @throws IOException ailed or interrupted I/O operations
	 */
	public void sendPaintToBeReused() throws IOException {
		networkConnections.sendRequest("ShadeChanger", tank.getQuantity());
		System.out.println("-- Paint to reuse quantity sent : " + tank.getQuantity());
		tank.setColor(null);
		tank.setQuantity(0);
	}

	/**
	 * Recevoir la peinture envoyee par le ShadeChanger
	 * 
	 * @throws ClassNotFoundException Thrown when an application tries to load in a
	 *                                class through its string name but no
	 *                                definition for the class with the specified
	 *                                name could be found
	 * @throws IOException            Failed or interrupted I/O operations
	 */
	public void receivePaint() throws ClassNotFoundException, IOException {
		int paintQuantity = (int) networkConnections.receiveRequest("ShadeChanger");
		System.out.println("-- Paint quantity received : " + paintQuantity);
		tank.setQuantity(paintQuantity);
	}

	/**
	 * Calculer la distance entre deux positions sur la matrice
	 * 
	 * @param position1 La premiere position
	 * @param position2 La deuxieme position
	 * 
	 * @return la distance entre les deux positions
	 */
	public int getDistance(Position position1, Position position2) {
		if (position1 == null || position2 == null) {
			return 1;
		}
		int distance = 0;
		distance += 2 * (Math.abs(position1.getRow() - position2.getRow()));
		distance += Math.abs(position1.getColomn() - position2.getColomn());
		return distance;
	}

	/**
	 * S'assure que tous les pixels peints sont bien indiques comme tels
	 */
	public void patchPaint() {
		for (int i = 0; i < part.getNbRows(); i++) {
			for (int j = 0; j < part.getMatrice()[i].length; j++) {
				if (part.getMatrice()[i][j] == 2 || part.getMatrice()[i][j] == 1) {
					part.setPixel(new Position(i, j), 4);
				}
			}
		}
	}

	/**
	 * Envoyer l'accuse de reception qui n'est qu'une copie du message initial avec
	 * l'indicateur ack mis a un
	 * 
	 * @param message le message recu initialement
	 * 
	 * @throws IOException Failed or interrupted I/O operations
	 */
	public void sendAck(Message message) throws IOException {
		Message ackMessage = new Message();
		ackMessage = message;
		ackMessage.setAck(1);
		if (idRobot == 1) {
			networkConnections.sendUDP("PainterRobot2", ackMessage, true);
		}
		if (idRobot == 2) {
			networkConnections.sendUDP("PainterRobot1", ackMessage, true);
		}
	}
}
