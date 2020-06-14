package painterRobot;

import java.io.IOException;

/**
 * Machine PainterRobot, Main : Initialise la machine, puis permet a la machine
 * d'accomplir ses misssions via la methode actionLoop, avant de fermer toutes
 * ses communications reseaux en fin de simulation
 * 
 * @author o.boutry
 * 
 */
public class PainterRobotMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

		String elementName = args[0];
		PainterRobot painterRobot = new PainterRobot(elementName);

		painterRobot.initialization();

		painterRobot.actionLoop();

		painterRobot.close();

	}

}
