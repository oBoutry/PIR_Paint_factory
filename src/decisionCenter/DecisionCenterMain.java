package decisionCenter;

import java.io.IOException;

/**
 * Machine DecisionCenter, Main : Initialise la machine, puis permet a la
 * machine d'accomplir ses misssions via la methode actionLoop, avant de fermer
 * toutes ses communications reseaux en fin de simulation
 * 
 * @author o.boutry
 * 
 */
public class DecisionCenterMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

		DecisionCenter decisionCenter = new DecisionCenter();

		decisionCenter.initialization();

		decisionCenter.actionLoop();

		decisionCenter.close();
	}

}
