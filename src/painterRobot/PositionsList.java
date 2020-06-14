package painterRobot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Liste des positions des pixels de la piece restant a peindre
 * 
 * @author o.boutry
 * 
 */
public class PositionsList {
	/**
	 * Liste des positions des pixels de la piece restant a peindre
	 */
	private List<Position> positions;

	/**
	 * <b>Constructeur de PositionsList</b>
	 * 
	 * Utilisation de Collections.synchronizedList pour utiliser des methodes
	 * synchronisees avec ListPosition et eviter les problemes d'acces
	 */
	public PositionsList() {
		positions = Collections.synchronizedList(new ArrayList<Position>());
	}

	public synchronized List<Position> getAvailablePositions() {
		return positions;
	}

	public synchronized void setAvailablePositions(List<Position> positions) {
		this.positions = positions;
	}

	/**
	 * Retirer un pixel peind de la liste des positions des pixels disponibles
	 * 
	 * @param positionToRemove
	 * La position a retirer de la liste des positions disponibles
	 */
	public synchronized void removePosition(Position positionToRemove) {
		positions.removeIf(position -> (position.getRow() == positionToRemove.getRow()
				&& position.getColomn() == positionToRemove.getColomn()));
	}

	public synchronized int getSize() {
		return positions.size();
	}

	public synchronized Position getElement(int idPosition) {
		return positions.get(idPosition);
	}

	public synchronized void addElement(Position position) {
		positions.add(position);
	}

	public synchronized void removeElement(Position position) {
		positions.remove(position);
	}

	public void clear() {
		positions.clear();
	}
}
