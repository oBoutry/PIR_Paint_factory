package tank;

/**
 * Constitue l'objet tank ou "reservoir de peinture"
 *
 * Chaque robot peintre en possede un, le ShadeChanger ou "preparateur de
 * peinture" en possede un nombre indique dans le fichier texte "scenario.txt"
 * On trouvera les methodes classiques : constructeurs, setters, getters,
 * toString()
 * 
 * @author o.boutry
 * 
 */
public class Tank {
	/**
	 * Couleur de la peinture presente dans le reservoir
	 */
	private String color;
	/**
	 * Quantite de peinture presente dans le reservoir
	 */
	private int quantity;

	public Tank(int quantity) {
		this.quantity = quantity;
	}

	public Tank(String color, int quantity) {
		this.quantity = quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public String toString() {
		return "" + quantity + "";
	}

}
