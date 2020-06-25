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
    /**
     * Quantum de peinture representé par une barre
     */
    private int unit = 250;


    public Tank(int quantity) {
        this.quantity = quantity;
    }

    public Tank(String color, int quantity) {
        this.color    = color;
        this.quantity = quantity;
    }

    public Tank(String color, int quantity, int unit) {
        this.color    = color;
        this.quantity = quantity;
        this.unit     = unit;
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
        String bar = "";
        for (int i=0; i<(quantity/unit)/8; i++) bar += "█";
        switch ((quantity / unit) % 8) {
            case 0: bar += " "; break; // for in-place animation
            case 1: bar += "▏"; break;
            case 2: bar += "▎"; break;
            case 3: bar += "▍"; break;
            case 4: bar += "▌"; break;
            case 5: bar += "▋"; break;
            case 6: bar += "▊"; break;
            case 7: bar += "▉"; break;
        }
        return bar;
    }

}
