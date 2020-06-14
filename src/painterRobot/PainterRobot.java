package painterRobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import machine.Machine;
import partToPaint.Part;
import tank.Tank;

public class PainterRobot extends Machine {
	private int idRobot;
	private int idOtherRobot;
	private Tank tank;
	private Position position;
	private Position nextDesiredPosition;
	private Position nextDesiredPositionOtherRobot;
	private Position positionOtherRobot;
	private PositionsList availablePositions;
	private ArrayList<Position> availablePositionsOrderedByDistance;
	private AtomicInteger clock;
	private int clockOtherRobot;
	private Part part;
	private Message message;
	private int nbIter;
	private int nbUpdates;
	private int compteurForUpdates;
	// private int distanceBeetwenRobots;

	// Ajouter fenetre dynamic + petite fenetre quand il reste que peu de pixels
	// disponibles
	private volatile boolean close;
	private AtomicInteger messageCounter;
	private int windowWidth;

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
		message = new Message(position, null, clock.get(), 0);
		part = null;
		nextDesiredPosition = null;
		nbIter = (int) scenario.get("nbParts");
		messageCounter = new AtomicInteger(0);
		windowWidth = 5;
		close = false;

	}

	public void initialization() throws ClassNotFoundException, IOException, InterruptedException {

	}

	public void actionLoop() throws InterruptedException, ClassNotFoundException, IOException {
		while (nbIter > 0) {
			nbUpdates = 10;
			compteurForUpdates = 1;

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

	public void paint() throws InterruptedException, IOException {

		close = false;

		part.setPixel(position, idRobot);
		part.setPixel(positionOtherRobot, idOtherRobot);
		clock.incrementAndGet();
		//System.out.println(part);
		part.printMatrice();

		Thread threadReceiver = new Thread() {

			public void run() {
				while (!close) {
					try {
						Message messageReceived = receiveMessage();
						if (messageReceived.getAck() == 1) {
							messageCounter.decrementAndGet();
							//System.out.println("messageCounter - : " + messageCounter.get());
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
					while (availablePositions.getSize() >= 1) {
						while (messageCounter.get() == windowWidth) {
							Thread.sleep(10);
						}
						messageCounter.incrementAndGet();
						//System.out.println("messageCounter + : " + messageCounter.get());
						displacement();
					}
					nextDesiredPosition = null;
					setMessage(0);
					sendMessage();
					while (!isFinishedPart()) {
						paintPixel();
						// updateConveyor();
						move();
						//System.out.println("clock : " + clock.get());
					}
					close = true;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} finally {

				}
			}
		};
		threadActionPaint.run();

	}

	public void displacement() throws IOException, InterruptedException {
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
		// updateConveyor();
		move();
		//System.out.println("clock : " + clock.get());
	}

	public void chooseNextPixel() {
		OrderByDistance();
		nextDesiredPosition = availablePositionsOrderedByDistance.remove(0);
		availablePositions.removeElement(nextDesiredPosition);
	}

	public void analyzeMessage(Message messageReceived) throws InterruptedException {

		if (messageReceived.getClock() < clockOtherRobot) {
			Position oldPosition = messageReceived.getPosition();
			if (oldPosition != null) {
				part.setPixel(oldPosition, 4);
				availablePositions.removePosition(oldPosition);
			}
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
				part.setPixel(positionOtherRobot, idOtherRobot);
				availablePositions.removePosition(positionOtherRobot);
			}
			if (idOtherRobot == 1 && nextDesiredPositionOtherRobot != null) {
				availablePositions.removePosition(nextDesiredPositionOtherRobot);
			}
		}
		part.colorLevelCalculator();
	}

	public void OrderByDistance() {
		availablePositionsOrderedByDistance.clear();
		if (position != null) {
			ArrayList<Position> positions1 = new ArrayList<Position>();
			ArrayList<Position> positions2 = new ArrayList<Position>();
			ArrayList<Position> positions3 = new ArrayList<Position>();
			ArrayList<Position> positions4 = new ArrayList<Position>();
			ArrayList<Position> positions5Plus = new ArrayList<Position>();
			for (int idPosition = 0; idPosition < availablePositions.getSize(); idPosition++) {
				Position positionToOrder = availablePositions.getElement(idPosition);
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
		} else {
			availablePositionsOrderedByDistance.add(availablePositions.getElement(0));
		}
	}

	public boolean isFinishedPart() {
		return part.getColorLevel() >= 99.9;
	}

	public void paintPixel() throws InterruptedException {
		if (position != null) {
			boolean pixelAlreadyPainted = part.getMatrice()[position.getRow()][position.getColomn()] == 4;
			if (!pixelAlreadyPainted) {
				part.setPixel(position, 4);
				tank.setQuantity(tank.getQuantity() - 10);
			}
		}
		part.colorLevelCalculator();
		//System.out.println("Tank : " + tank);
	}

	public void updateConveyor() throws IOException {
		if (idRobot == 1) {
			int level = (int) part.getColorLevel();
			if (level / 10 >= compteurForUpdates && nbUpdates >= 0) {
				networkConnections.sendAnswer("Conveyor", compteurForUpdates * 10.);
				nbUpdates -= 1;
				compteurForUpdates += 1;
			}
		}
	}

	public void sendMessage() throws IOException {
		if (idRobot == 1) {
			networkConnections.sendUDP("PainterRobot2", message, true);
		}
		if (idRobot == 2) {
			networkConnections.sendUDP("PainterRobot1", message, true);
		}
	}

	public void setMessage(int ack) {
		message.setMessage(position, nextDesiredPosition, clock.get(), ack);
	}

	public Message receiveMessage() throws IOException {
		Message messageReceived = null;
		messageReceived = (Message) networkConnections.receiveUDP();
		return messageReceived;
	}

	public void move() throws InterruptedException {
		Thread.sleep(Math.round(100 * timeFactor * getDistance(position, nextDesiredPosition)));
		position = nextDesiredPosition;
		if (position != null) {
			part.setPixel(position, idRobot);
		}
		clock.incrementAndGet();
		//System.out.println(part);
		part.printMatrice();
		System.out.println("\n");
	}

	public boolean areNextMovesAllowed() {
		if (nextDesiredPosition == null || nextDesiredPositionOtherRobot == null) {
			return true;
		}
		if (getDistance(nextDesiredPosition, nextDesiredPositionOtherRobot) <= 8) {
			return false;
		}
		return true;
	}

	public void getAvailablePixels() {
		availablePositions.clear();
		for (int i = 0; i < part.getNbRows(); i++) {
			for (int j = 0; j < part.getMatrice()[i].length; j++) {
				availablePositions.addElement(new Position(i, j));
			}
		}
	}

	public void sendPaintToBeReused() throws IOException {
		networkConnections.sendRequest("ShadeChanger", tank.getQuantity());
		System.out.println("-- Paint to reuse quantity sent : " + tank.getQuantity());
		tank.setColor(null);
		tank.setQuantity(0);
	}

	public void receivePaint() throws ClassNotFoundException, IOException {
		int paintQuantity = (int) networkConnections.receiveRequest("ShadeChanger");
		System.out.println("-- Paint quantity received : " + paintQuantity);
		tank.setQuantity(paintQuantity);
	}

	public int getDistance(Position position1, Position position2) {
		if (position1 == null || position2 == null) {
			return 1;
		}
		int distance = 0;
		distance += Math.abs(position1.getRow() - position2.getRow());
		distance += Math.abs(position1.getColomn() - position2.getColomn());
		return distance;
	}

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
