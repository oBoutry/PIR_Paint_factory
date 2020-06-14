package shadeChanger;

import java.io.IOException;

/**
 * Machine ShadeChanger, Main : Initialise la machine, puis permet a la machine
 * d'accomplir ses misssions via la methode actionLoop, avant de fermer toutes
 * ses communications reseaux en fin de simulation
 * 
 * @author o.boutry
 * 
 */
public class ShadeChangerMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

		ShadeChanger shadeChanger = new ShadeChanger();

		shadeChanger.initialization();

		shadeChanger.actionLoop();

		shadeChanger.close();
	}

}
