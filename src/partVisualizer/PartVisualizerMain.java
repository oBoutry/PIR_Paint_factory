package partVisualizer;

import java.io.IOException;

/**
 * Machine PartVisualizer, Main : Initialise la machine, puis permet a la
 * machine d'accomplir ses misssions via la methode actionLoop, avant de fermer
 * toutes ses communications reseaux en fin de simulation
 * 
 * @author o.boutry
 * 
 */
public class PartVisualizerMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		PartVisualizer partVisualizer = new PartVisualizer();

		partVisualizer.initialization();

		partVisualizer.actionLoop();

		partVisualizer.close();

	}

}
