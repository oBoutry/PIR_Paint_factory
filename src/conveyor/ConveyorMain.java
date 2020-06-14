package conveyor;

import java.io.IOException;

/**
 * Machine Conveyor, Main : Initialise la machine, puis permet a la machine
 * d'accomplir ses misssions via la methode actionLoop, avant de fermer toutes
 * ses communications reseaux en fin de simulation
 * 
 * @author o.boutry
 * 
 */
public class ConveyorMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

		Conveyor conveyor = new Conveyor();

		conveyor.initialization();

		conveyor.actionLoop();

		conveyor.close();

	}

}
