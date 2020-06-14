package conveyor;

import java.util.Hashtable;

import partToPaint.Part;

/**
 * Stock de pieces
 * 
 * @author o.boutry
 * 
 */
public class Stock {
	/**
	 * Stock de pieces a peindre
	 */
	private Hashtable<Integer, Part> parts;

	public Stock() {
		parts = new Hashtable<Integer, Part>();
	}

	/**
	 * <b>Constructeur de Stock</b>
	 * 
	 * Permet de construire un stock de nbParts parts, ayant nbRows lignes, avec
	 * pour chaque ligne entre nbMaxPixels et nbMinPixels pixels
	 * 
	 * @param nbParts     Nombre de parts a mettre dans le stock
	 * @param nbMinPixels Nombre minimum de pixel dans une ligne de la piece
	 * @param nbMaxPixels Nombre maximum de pixel dans une ligne de la piece
	 * @param nbRows      Nombre de lignes par piece
	 * 
	 */
	public Stock(int nbParts, int nbMinPixels, int nbMaxPixels, int nbRows) {
		parts = new Hashtable<Integer, Part>();
		for (int idPart = 0; idPart < nbParts; idPart++) {
			Part part = new Part("none", idPart, nbMinPixels, nbMaxPixels, nbRows);
			parts.put(idPart, part);
		}
	}

	public Hashtable<Integer, Part> getParts() {
		return parts;
	}

	/**
	 * Destocker une piece
	 * 
	 * @param partId Identifiant de la piece a destocker
	 * 
	 * @return La piece destockee
	 */
	public Part destock(Integer partId) {
		return parts.remove(partId);
	}

	/**
	 * Stocker une piece
	 * 
	 * @param partId Identifiant de la piece a stocker
	 * @param part   Piece a stocker
	 * 
	 * 
	 */
	public void stock(Integer partId, Part part) {
		parts.put(partId, part);
	}

	@Override
	public String toString() {
		return "\n" + parts;
	}

}
