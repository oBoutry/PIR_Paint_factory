package painterRobot;

import java.io.Serializable;

/**
 * Message echange entre les deux robots pour coordonner sans risque la
 * cooperation
 * 
 * @author o.boutry
 * 
 */
public class Message implements Serializable {
	/**
	 * SerialVersionUID permettant d'eviter les problemes de versions differentes
	 * pour les objets serializables
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * La position du robot
	 */
	private Position position;
	/**
	 * La position suivante souhaitee du robot
	 */
	private Position nextDesiredPosition;
	/**
	 * L'horloge du robot
	 */
	private int clock;
	/**
	 * Indicateur d'accuse de reception, si ack est a 0 c'est un message normal, si
	 * il est a 1 c'est un accuse de reception
	 */
	private int ack;

	public Message() {
	}

	/**
	 * <b>Constructeur de Message</b>
	 * 
	 * @param position            La position du robot
	 * @param nextDesiredPosition La position suivante souhaitee du robot
	 * @param clock               L'horloge du robot
	 * @param ack                 Indicateur d'accuse de reception, si ack est a 0
	 *                            c'est un message normal, si il est a 1 c'est un
	 *                            accuse de reception
	 */
	public Message(Position position, Position nextDesiredPosition, int clock, int ack) {
		this.position = position;
		this.nextDesiredPosition = nextDesiredPosition;
		this.clock = clock;
		this.ack = ack;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public void setClock(int clock) {
		this.clock = clock;
	}

	public Position getPosition() {
		return position;
	}

	public Position getDesiredPosition() {
		return nextDesiredPosition;
	}

	public int getClock() {
		return clock;
	}

	public int getAck() {
		return ack;
	}

	public void setAck(int ack) {
		this.ack = ack;
	}

	public void setMessage(Position position, Position nextDesiredPosition, int clock, int ack) {
		this.position = position;
		this.nextDesiredPosition = nextDesiredPosition;
		this.clock = clock;
		this.ack = ack;
	}

	public String toString() {
		return "Message [" + position + "," + nextDesiredPosition + "," + clock + "," + ack + "]";

	}

}
