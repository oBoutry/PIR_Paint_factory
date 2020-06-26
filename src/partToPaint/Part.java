package partToPaint;

import java.io.Serializable;

import painterRobot.Position;
import terminal.Terminal;

/**
 * Constitue l'objet part ou "piece a peindre"
 *
 * A partir de l'identifiant de la pieces envoye par le DecisionCenter, le
 * Conveyor la destocke et lui attribue la couleur demandee, la piece est
 * ensuite placee sur le tapis roulant puis envoyee dans la cabine de peinture
 * pour etre peinte par les deux robots, ensuite la piece peinte arrive a la fin
 * du tapis roulant et est stockee avec les autres pieces peintes
 * 
 * @author o.boutry
 * 
 */
public class Part implements Serializable {

	/**
	 * SerialVersionUID permettant d'eviter les problemes de versions differentes
	 * pour les objets serializables
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * La matrice de pixels a peindre
	 */
	private int[][] matrice;
	/**
	 * La couleur de la piece
	 */
	private String color;
	/**
	 * L'identifiant de la piece
	 */
	private int id;
	/**
	 * Le pourcentage de la surface de la piece deja peinte
	 */
	private double colorLevel;
	/**
	 * Le nombre de lignes de la piece a peindre
	 */
	private int nbRows;

	/**
	 * <b>Constructeur de Part</b>
	 * 
	 * Permet de construire une piece ayant nbRows lignes, avec pour chaque ligne
	 * entre nbMaxPixels et nbMinPixels pixels
	 * 
	 * @param color       La couleur de la piece
	 * @param id          L'identifiant de la piece
	 * @param nbMinPixels Nombre minimum de pixel dans une ligne de la piece
	 * @param nbMaxPixels Nombre maximum de pixel dans une ligne de la piece
	 * @param nbRows      Nombre de lignes par piece
	 * 
	 */
	public Part(String color, int id, int nbMinPixels, int nbMaxPixels, int nbRows) {
		this.color = color;
		this.id = id;
		this.nbRows = nbRows;
		colorLevel = 0;
		matrice = new int[nbRows][];
		for (int row = 0; row < nbRows; row++) {
			int[] line = new int[(int) Math.floor(Math.random() * (nbMaxPixels - nbMinPixels) + nbMinPixels)];
			matrice[row] = line;
		}
	}

	/**
	 * Afficher la matrice de pixels de la piece
	 * 
	 */
	public void printMatrice() {
		for (int[] row : matrice) {
			System.out.print("[");
			for (int j = 0; j < row.length; j++) {
				System.out.print(row[j]);
			}
		    System.out.print("]");
            Terminal.ClearEndOfLine();
            System.out.println();
		}
	}

	/**
	 * Recuperer la valeur du pixel en position position
	 * 
	 * @param position La position du pixel a recuperer
	 * 
	 * @return La valeur du pixel en position position
	 */
	public int getPixel(Position position) {
		return matrice[position.getRow()][position.getColomn()];
	}

	/**
	 * Calculer le pourcentage de la surface de la piece deja peinte. Methode
	 * synchronisee pour eviter les acces concurrents
	 * 
	 */
	public synchronized void colorLevelCalculator() {
		double totalNbPixels = getNbPixels();
		double totalCount = 0;
		for (int row = 0; row < nbRows; row++) {
			for (int colomn = 0; colomn < matrice[row].length; colomn++) {
				int level = matrice[row][colomn];
				if (level == 4) {
					totalCount += 4;
				}
			}
		}
		colorLevel = totalCount * 100 / (totalNbPixels * 4);
	}

	/**
	 * Recuperer l'identifiant, la couleur, et le pourcentage d'avancement de la
	 * piece
	 * 
	 * @return Les informations de la pieces sauf le nombre de pixels
	 */
	public String getPartInfo() {
		return id + "-" + color + ": " + colorLevel;
	}

	/**
	 * Recuperer l'identifiant, la couleur, le nombre de pixels, et le pourcentage
	 * d'avancement de la piece
	 * 
	 * @return Toutes les informations de la piece
	 */
	public String getPartFullInfo() {
		return id + "-" + color + " " + getNbPixels() + " :" + colorLevel;
	}

	/**
	 * Assigner la valeur value au pixel de la piece en position position
	 * 
	 * @param position La position du pixel a changer
	 * @param value    La valeur a assigner au pixel cible
	 * 
	 */
	public synchronized void setPixel(Position position, int value) {
		matrice[position.getRow()][position.getColomn()] = value;
	}

	/**
	 * Calculer le nombre de pixels dans la matrice de la piece
	 * 
	 * @return Le nombre de pixels de la piece
	 */
	public int getNbPixels() {
		int compteur = 0;
		for (int row = 0; row < nbRows; row++) {
			for (int colomn = 0; colomn < matrice[row].length; colomn++) {
				compteur++;
			}
		}
		return compteur;
	}

	public String toString() {
		return "Part : " + id + " " + color + " " + colorLevel + "\n";
	}

	public void setColorLevel(double colorLevel) {
		this.colorLevel = colorLevel;
	}

	public int getId() {
		return id;
	}

	public int getNbRows() {
		return nbRows;
	}

	public String getColor() {
		return color;
	}

	public double getColorLevel() {
		return colorLevel;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int[][] getMatrice() {
		return matrice;
	}
}
