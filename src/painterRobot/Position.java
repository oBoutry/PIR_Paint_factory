package painterRobot;

import java.io.Serializable;

/**
 * La position au sein de la matrice de pixels d'une piece : la donnee de la
 * ligne et de la colonne
 * 
 * @author o.boutry
 * 
 */
public class Position implements Serializable {
	/**
	 * SerialVersionUID permettant d'eviter les problemes de versions differentes
	 * pour les objets serializables
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * La ligne
	 */
	private int row;
	/**
	 * La colonne
	 */
	private int colomn;

	/**
	 * <b>Constructeur de Position</b>
	 * 
	 * @param row    La ligne de la position
	 * @param colomn La colonne de la position
	 */
	public Position(int row, int colomn) {
		this.row = row;
		this.colomn = colomn;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColomn() {
		return colomn;
	}

	public void setColomn(int colomn) {
		this.colomn = colomn;
	}

	public String toString() {
		return "[" + row + "," + colomn + "]";
	}

}
