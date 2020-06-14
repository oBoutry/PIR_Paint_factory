package exchangeMonitor;

import java.io.IOException;

/**
 * Machine ExchangeMonitor, Main : Initialise la machine, puis permet a la
 * machine d'accomplir ses misssions via la methode actionLoop, avant de fermer
 * toutes ses communications reseaux en fin de simulation
 * 
 * @author o.boutry
 * 
 */
public class ExchangeMonitorMain {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

		ExchangeMonitor exchangeMonitor = new ExchangeMonitor();

		exchangeMonitor.initialization();

		exchangeMonitor.actionLoop();

		exchangeMonitor.close();
	}

}
